import React, { useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import { BookIndex } from '../../model/Book.ts';
import axiosInstance from "../../services/AxiosInstance.ts";
import { LendStatus } from "../../model/LendStatus";
import { toast } from "react-toastify";
import { useAuth } from "../../context/AuthContext";

// Type for loan information
interface LoanInfo {
    bookId: number;
    state: string;
}

// Type for collection information
interface Collection {
    id: number;
    title: string;
}

// Type for book notification information
interface BookNotification {
    id: number;
    bookId: number;
    position: number;
    status: string;
}

interface BookCardProps {
    book: BookIndex;
    borrowedBooks?: number[];
    lentBooks?: number[];
    loanStates?: LoanInfo[];
    userCollections?: Collection[];
    onLendingChange?: () => void;
}

export const BookCard: React.FC<BookCardProps> = ({
                                                      book,
                                                      borrowedBooks = [],
                                                      lentBooks = [],
                                                      loanStates = [],
                                                      userCollections = [],
                                                      onLendingChange
                                                  }) => {
    const { id } = book;
    const { user } = useAuth();
    const userId = user?.id;
    const [isLoanable, setIsLoanable] = useState<boolean>(false);
    const [loading, setLoading] = useState<boolean>(false);
    const [isBorrowed, setIsBorrowed] = useState<boolean>(false);
    const [imageError, setImageError] = useState<boolean>(false);
    const [isBookAvailable, setIsBookAvailable] = useState<boolean>(true);
    const [showCollectionDropdown, setShowCollectionDropdown] = useState<boolean>(false);
    const [showCollectionModal, setShowCollectionModal] = useState<boolean>(false);
    const [queuePosition, setQueuePosition] = useState<number>(-1);
    const [notificationId, setNotificationId] = useState<number | null>(null);
    const [userNotifications, setUserNotifications] = useState<BookNotification[]>([]);
    const cardRef = useRef<HTMLDivElement>(null);

    const isBookBorrowed = loanStates.some(loan => {
        const bookIdMatches = loan.bookId === id;
        const stateIsBorrowed = loan.state === LendStatus.BORROWED;
        return bookIdMatches && stateIsBorrowed;
    });

    const isInQueue = queuePosition > 0;

    useEffect(() => {
        setIsBorrowed(borrowedBooks.includes(id));
        setIsLoanable(lentBooks.includes(id));

        // Check book availability when component loads
        checkBookAvailability();

        // Check if user is in queue for this book
        if (userId) {
            checkQueuePosition();
            fetchUserNotifications();
        }

        // Add event listener for closing the dropdown/modal when clicking outside
        const handleClickOutside = (event: MouseEvent) => {
            if (cardRef.current && !cardRef.current.contains(event.target as Node)) {
                setShowCollectionDropdown(false);
                setShowCollectionModal(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [borrowedBooks, lentBooks, id, userId]);

    // Check if user is in queue for this book
    const checkQueuePosition = async () => {
        if (!userId) return;

        try {
            const response = await axiosInstance.get(`/v1/book-queue/check`, {
                params: {
                    bookId: id,
                    userId: userId
                }
            });

            if (response.data?.success) {
                setQueuePosition(response.data.data);
            }
        } catch (error) {
            console.error("Error checking queue position:", error);
        }
    };

    // Fetch user's active notifications
    const fetchUserNotifications = async () => {
        if (!userId) return;

        try {
            const response = await axiosInstance.get(`/v1/book-queue/active`, {
                params: {
                    userId: userId
                }
            });

            if (response.data?.success) {
                setUserNotifications(response.data.data);

                // Find notification for this book
                const bookNotification = response.data.data.find(
                    (notification: BookNotification) => notification.bookId === id
                );

                if (bookNotification) {
                    setNotificationId(bookNotification.id);
                }
            }
        } catch (error) {
            console.error("Error fetching notifications:", error);
        }
    };

    // Check book availability using the existing endpoint
    const checkBookAvailability = async () => {
        try {
            const response = await axiosInstance.get(`/v1/carts/check-book-availability/${id}`);
            if (response.data?.data !== undefined) {
                setIsBookAvailable(response.data.data);
            }
        } catch (error) {
            console.error("Error checking book availability:", error);
            setIsBookAvailable(false);
        }
    };

    const getBookCoverColor = (bookId: number) => {
        const colors = [
            '#1e3a8a', // dark blue
            '#1e40af',
            '#1d4ed8',
            '#2563eb', // medium blue
            '#3b82f6',
            '#60a5fa', // light blue
            '#93c5fd'
        ];
        const colorIndex = bookId % colors.length;
        return colors[colorIndex];
    };

    const addToLoanable = async () => {
        if (!userId) return;

        setLoading(true);
        try {
            const params = new URLSearchParams();
            params.append('bookId', id.toString());
            params.append('userId', userId.toString());

            const response = await axiosInstance.post("/v1/loans/make-loanable", params);

            if (response.data?.success) {
                toast.success("Book added to your lending list");
                setIsLoanable(true);
                if (onLendingChange) {
                    onLendingChange();
                }
            } else {
                toast.error("Unable to add book to lending list");
            }
        } catch (error: any) {
            toast.error(error.response?.data?.message || "Failed to add book to lending list");
        } finally {
            setLoading(false);
        }
    };

    const removeFromLoanable = async () => {
        if (!userId) return;

        setLoading(true);
        try {
            const response = await axiosInstance.delete("/v1/loans/remove-loanable", {
                params: {
                    bookId: id,
                    userId: userId
                }
            });

            if (response.data?.success) {
                toast.success("Book removed from your lending list");
                setIsLoanable(false);
                if (onLendingChange) {
                    onLendingChange();
                }
            } else {
                toast.error("Unable to remove book from lending list");
            }
        } catch (error: any) {
            toast.error(error.response?.data?.message || "Failed to remove book from lending list");
        } finally {
            setLoading(false);
        }
    };

    const addToCart = async () => {
        if (!userId) return;

        setLoading(true);
        try {
            const response = await axiosInstance.post("/v1/carts/add-book", {
                userId,
                bookId: id
            });

            if (response.status === 200 && response.data?.success) {
                toast.success("Book has been added to cart");
                // Update availability after adding to cart
                checkBookAvailability();
            } else {
                toast.error(response.data?.message || "Failed to add to cart");
            }
        } catch (error: any) {
            toast.error(error.response?.data?.message || "Error adding book to cart");
        } finally {
            setLoading(false);
        }
    };

    const notifyBookAvailability = async () => {
        if (!userId) return;

        setLoading(true);
        try {
            const params = new URLSearchParams();
            params.append('bookId', id.toString());
            params.append('userId', userId.toString());

            const response = await axiosInstance.post(
                "/v1/book-queue/join",
                params
            );

            if (response.status === 200 && response.data?.success) {
                toast.success("You will be notified when the book is available");
                // Update queue position and notifications after joining
                checkQueuePosition();
                fetchUserNotifications();
            } else {
                toast.error(response.data?.message || "You are already subscribed to notifications");
            }
        } catch (error) {
            toast.error(error.response?.data?.message || "Error subscribing to notifications");
        } finally {
            setLoading(false);
        }
    };

    const cancelNotification = async () => {
        if (!userId || !notificationId) return;

        setLoading(true);
        try {
            const response = await axiosInstance.post(
                "/v1/book-queue/cancel",
                null,
                {
                    params: {
                        notificationId: notificationId,
                        userId: userId
                    }
                }
            );

            if (response.status === 200 && response.data?.success) {
                toast.success("Notification cancelled successfully");
                setQueuePosition(-1);
                setNotificationId(null);
                // Refresh notifications
                fetchUserNotifications();
            } else {
                toast.error(response.data?.message || "Failed to cancel notification");
            }
        } catch (error: any) {
            toast.error(error.response?.data?.message || "Error cancelling notification");
        } finally {
            setLoading(false);
        }
    };

    const formatDate = (dateString: string) => {
        try {
            const date = new Date(dateString);
            return date.toLocaleDateString('en-US', {
                year: 'numeric',
                month: 'long'
            });
        } catch (e) {
            return dateString;
        }
    };

    const getThumbnailUrl = () => {
        if (!book.thumbnailPath || imageError) return null;
        return `/api/v1/books/images/${book.thumbnailPath}`;
    };

    const handleImageError = () => {
        console.error(`Failed to load image for book: ${book.id}, path: ${book.thumbnailPath}`);
        setImageError(true);
    };

    const handleContextMenu = (e: React.MouseEvent) => {
        e.preventDefault();
        setShowCollectionDropdown(true);
    };

    const handleOpenCollectionModal = (e: React.MouseEvent) => {
        e.preventDefault();
        setShowCollectionDropdown(false);
        setShowCollectionModal(true);
    };

    const addToCollection = async (collectionId: number) => {
        if (!userId) return;

        setLoading(true);
        try {
            const response = await axiosInstance.post(
                `/v1/collections/${collectionId}/books/${id}`
            );

            if (response.status === 200 && response.data?.success) {
                toast.success("Book successfully added to collection");
                setShowCollectionModal(false);
            } else {
                toast.error(response.data?.message || "Failed to add book to collection");
            }
        } catch (error: any) {
            console.error("Error adding to collection:", error);
            const errorMessage = error.response?.data?.message ||
                error.response?.data?.data?.message ||
                "Error adding book to collection";
            toast.error(errorMessage);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div
            ref={cardRef}
            className="bg-white rounded-lg overflow-hidden shadow-md hover:shadow-lg transition-shadow duration-300 flex flex-col h-full relative"
            onContextMenu={handleContextMenu}
        >
            {isBookBorrowed && (
                <div className="absolute top-2 right-2 bg-red-500 text-white text-xs px-2 py-1 rounded-full z-10">
                    Lent
                </div>
            )}

            {isInQueue && (
                <div className="absolute top-2 left-2 bg-purple-500 text-white text-xs px-2 py-1 rounded-full z-10">
                    Queue: {queuePosition}
                </div>
            )}

            {/* Context Menu Dropdown */}
            {showCollectionDropdown && (
                <div className="absolute right-0 mt-8 bg-white shadow-lg rounded-md p-2 z-50">
                    <a
                        href="#"
                        className="block px-4 py-2 text-gray-800 hover:bg-gray-200 rounded-md"
                        onClick={handleOpenCollectionModal}
                    >
                        Ajouter à une collection
                    </a>
                </div>
            )}

            {/* Collection Modal */}
            {showCollectionModal && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
                    <div className="bg-white rounded-lg shadow-xl w-full max-w-md mx-4 overflow-hidden transform transition-all">
                        <div className="bg-blue-600 px-4 py-3 flex justify-between items-center">
                            <h3 className="text-lg font-medium text-white">Add to Collection</h3>
                            <button
                                onClick={() => setShowCollectionModal(false)}
                                className="text-white hover:text-gray-200 focus:outline-none"
                            >
                                <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
                                </svg>
                            </button>
                        </div>
                        <div className="p-4 max-h-96 overflow-y-auto">
                            {/* Debug information */}
                            <div className="text-xs text-gray-500 mb-2">
                                {userCollections ? `${userCollections.length} collections disponibles` : 'Aucune collection chargée'}
                            </div>

                            {userCollections && userCollections.length > 0 ? (
                                <div className="space-y-2">
                                    {userCollections.map(collection => (
                                        <div
                                            key={collection.id}
                                            className="hover:bg-gray-100 p-2 rounded-md cursor-pointer flex items-center"
                                            onClick={() => addToCollection(collection.id)}
                                        >
                                            <div className="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center text-white mr-3">
                                                {collection.title.charAt(0).toUpperCase()}
                                            </div>
                                            <div className="font-medium">{collection.title}</div>
                                        </div>
                                    ))}
                                </div>
                            ) : (
                                <div className="text-center py-8">
                                    <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 13h6m-3-3v6m-9 1V7a2 2 0 012-2h6l2 2h6a2 2 0 012 2v8a2 2 0 01-2 2H5a2 2 0 01-2-2z" />
                                    </svg>
                                    <p className="mt-2 text-gray-500">No collections available</p>
                                    <p className="text-sm text-gray-400 mt-1">Create a collection first to add books</p>
                                </div>
                            )}
                        </div>
                        <div className="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
                            <button
                                onClick={() => setShowCollectionModal(false)}
                                className="w-full sm:w-auto px-4 py-2 bg-gray-300 text-gray-800 rounded-md hover:bg-gray-400 focus:outline-none"
                            >
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}

            <Link to={`/books/${book.id}`} className="block flex-grow">
                <div
                    className="w-full h-48 flex items-center justify-center flex-shrink-0"
                    style={{
                        backgroundColor: getThumbnailUrl() ? 'transparent' : getBookCoverColor(book.id),
                        opacity: isBorrowed ? 0.6 : 1
                    }}
                >
                    {getThumbnailUrl() ? (
                        <img
                            src={getThumbnailUrl()!}
                            alt={`Cover for ${book.title}`}
                            className="h-full w-full object-contain"
                            onError={handleImageError}
                        />
                    ) : (
                        <span className="text-white text-5xl font-bold">
                            {book.title.charAt(0).toUpperCase()}
                        </span>
                    )}
                </div>

                <div className="p-4 flex flex-col flex-grow">
                    <h3 className="text-lg font-semibold text-gray-800 line-clamp-2 mb-2 h-14">
                        {book.title}
                    </h3>

                    <p className="text-sm text-gray-600 line-clamp-1 mb-2 h-5">
                        {book.authorNames && book.authorNames.length > 0
                            ? book.authorNames.join(', ')
                            : 'Unknown author'}
                    </p>

                    <div className="mt-auto">
                        {book.publicationDate && (
                            <p className="text-xs text-gray-500">
                                Published: {formatDate(book.publicationDate.toString())}
                            </p>
                        )}
                    </div>
                </div>
            </Link>

            {/* Action buttons */}
            <div className="p-3 space-y-2">
                {/* Conditionally display Borrow, Notify, or Cancel Notification button */}
                {isBookAvailable ? (
                    <button
                        onClick={addToCart}
                        className="w-full bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600 transition"
                        disabled={loading || isBookBorrowed}
                    >
                        {loading ? "Processing..." : "Borrow"}
                    </button>
                ) : isInQueue ? (
                    <button
                        onClick={cancelNotification}
                        className="w-full bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700 transition"
                        disabled={loading}
                    >
                        {loading ? "Processing..." : "Cancel Notification"}
                    </button>
                ) : (
                    <button
                        onClick={notifyBookAvailability}
                        className="w-full bg-purple-600 text-white px-4 py-2 rounded-lg hover:bg-purple-700 transition"
                        disabled={loading}
                    >
                        {loading ? "Processing..." : "Notify"}
                    </button>
                )}

                {/* Lending button - hidden if book has BORROWED status */}
                {!isBookBorrowed && user && (
                    <button
                        onClick={isLoanable ? removeFromLoanable : addToLoanable}
                        className={`w-full px-4 py-2 rounded-lg transition ${
                            isLoanable
                                ? "bg-yellow-500 hover:bg-yellow-600 text-white"
                                : "bg-green-500 hover:bg-green-600 text-white"
                        }`}
                        disabled={loading}
                    >
                        {loading ? "Processing..." : (isLoanable ? "Remove from lending" : "Add to lending")}
                    </button>
                )}
            </div>
        </div>
    );
};