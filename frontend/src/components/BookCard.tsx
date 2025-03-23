import React, { useState, useEffect} from 'react';
import { Link } from 'react-router-dom';
import { BookShow, BookIndex } from '../model/Book';
import axiosInstance from "../services/AxiosInstance";
import { useAuth } from "../context/AuthContext";

interface BookCardProps {
    book: BookShow | BookIndex;
    borrowedBooks?: number[];
    lentBooks?: number[];
    onLendingChange?: () => void;
}

export const BookCard: React.FC<BookCardProps> = ({ book, borrowedBooks = [], lentBooks = [], onLendingChange }) => {
    const { id } = book;
    const { user } = useAuth();
    const userId = user?.id;
    const [isLoanable, setIsLoanable] = useState<boolean>(false);
    const [loading, setLoading] = useState<boolean>(false);
    const [isBorrowed, setIsBorrowed] = useState<boolean>(false);
    const [imageError, setImageError] = useState<boolean>(false);
    const [bookDetails, setBookDetails] = useState<BookShow | null>(null);
    const [loadingDetails, setLoadingDetails] = useState<boolean>(false);

    const isBookShow = (obj: any): obj is BookShow => {
        return obj && 'isbns' in obj && 'authors' in obj && Array.isArray(obj.authors);
    };

    const currentBook = isBookShow(book) ? book : bookDetails;

    const getValidImageUrl = (url: string | undefined, isExternal: boolean | undefined): string | undefined => {
        if (!url) return undefined;

        if (isExternal || url.includes('books.google.com')) {
            return `${window.location.origin}/api/v1/proxy/image?url=${encodeURIComponent(url)}`;
        }

        if (!url.startsWith('http://') && !url.startsWith('https://')) {
            const baseUrl = process.env.REACT_APP_API_BASE_URL || window.location.origin;

            if (url.startsWith('/')) {
                return `${baseUrl}${url}`;
            } else {
                return `${baseUrl}/${url}`;
            }
        }

        return url;
    };

    useEffect(() => {
        if (!isBookShow(book) && !bookDetails && !loadingDetails) {
            setLoadingDetails(true);
            axiosInstance.get(`/v1/books/${book.id}`)
                .then(response => {
                    if (response.data?.success && response.data?.data) {
                        setBookDetails(response.data.data);
                    }
                })
                .catch(error => {
                    console.error(`Error fetching details for book ${book.id}:`, error);
                })
                .finally(() => {
                    setLoadingDetails(false);
                });
        }
    }, [book, bookDetails, loadingDetails]);

    const validCoverUrl = currentBook && 'coverImageUrl' in currentBook ?
        getValidImageUrl(currentBook.coverImageUrl, currentBook.isExternalImage) :
        undefined;

    useEffect(() => {
        setIsBorrowed(borrowedBooks.includes(id));
        setIsLoanable(lentBooks.includes(id));
    }, [borrowedBooks, lentBooks, id]);

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
                alert("Book added to your lending list");
                setIsLoanable(true);
                if (onLendingChange) {
                    onLendingChange();
                }
            } else {
                alert("Failed to add book to lending list");
            }
        } catch (error: any) {
            console.error("Error adding book to lending list:", error);
            console.error("Response data:", error.response?.data);
            alert(error.response?.data?.message || "Failed to add book to lending list");
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
                alert("Book removed from your lending list");
                setIsLoanable(false);
                if (onLendingChange) {
                    onLendingChange();
                }
            } else {
                alert("Failed to remove book from lending list");
            }
        } catch (error: any) {
            console.error("Error removing book from lending list:", error);
            console.error("Response data:", error.response?.data);
            alert(error.response?.data?.message || "Failed to remove book from lending list");
        } finally {
            setLoading(false);
        }
    };

    const addToCart = async () => {
        try {
            const response = await axiosInstance.post("/v1/carts/add-book", {
                userId,
                bookId: id,
            });

            if (response.status === 200) {
                alert("The book has been added to cart");
            }
        } catch (error: any) {
            console.error("Error adding book to cart", error);
            console.log(error.response?.data);
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

    const handleImageError = () => {
        console.group(`Image Error for Book ${book.id} - ${book.title}`);
        console.error(`Failed to load image with URL: ${validCoverUrl}`);
        if (currentBook && isBookShow(currentBook)) {
            console.error(`Original URL: ${currentBook.coverImageUrl}`);
            console.error(`Is External: ${currentBook.isExternalImage}`);
        }
        console.groupEnd();
        setImageError(true);
    };

    const authorNames = isBookShow(book) ?
        book.authors.map(author => `${author.firstName} ${author.lastName}`).join(', ') :
        ('authorNames' in book && Array.isArray(book.authorNames)) ?
            book.authorNames.join(', ') :
            'Unknown author';

    if (!isBookShow(book) && loadingDetails) {
        return (
            <div className="bg-white rounded-lg overflow-hidden shadow-md animate-pulse">
                <div className="w-full h-48 bg-gray-200"></div>
                <div className="p-4">
                    <div className="h-5 bg-gray-200 rounded w-3/4 mb-2"></div>
                    <div className="h-4 bg-gray-200 rounded w-1/2 mb-2"></div>
                    <div className="h-3 bg-gray-200 rounded w-1/4"></div>
                </div>
            </div>
        );
    }

    return (
        <div className="bg-white rounded-lg overflow-hidden shadow-md hover:shadow-lg transition-shadow duration-300 flex flex-col h-full relative">
            {isBorrowed && (
                <div className="absolute top-2 right-2 bg-red-500 text-white text-xs px-2 py-1 rounded-full z-10">
                    Prêté
                </div>
            )}

            <Link to={`/books/${book.id}`} className="block flex-grow">
                {/* Conteneur d'image avec ratio fixe */}
                <div className="relative pb-[150%] overflow-hidden">
                    {validCoverUrl && !imageError ? (
                        <img
                            src={validCoverUrl}
                            alt={`Cover of ${book.title}`}
                            className="absolute inset-0 w-full h-full object-contain bg-gray-100"
                            style={{ opacity: isBorrowed ? 0.6 : 1 }}
                            onError={handleImageError}
                            crossOrigin="anonymous"
                        />
                    ) : (
                        <div
                            className="absolute inset-0 w-full h-full flex items-center justify-center"
                            style={{
                                backgroundColor: getBookCoverColor(book.id),
                                opacity: isBorrowed ? 0.6 : 1
                            }}
                        >
                            <span className="text-white text-5xl font-bold">
                                {book.title.charAt(0).toUpperCase()}
                            </span>
                        </div>
                    )}
                </div>

                <div className="p-4 flex flex-col flex-grow">
                    <h3 className="text-lg font-semibold text-gray-800 line-clamp-2 mb-2 h-14">
                        {book.title}
                    </h3>

                    <p className="text-sm text-gray-600 line-clamp-1 mb-2 h-5">
                        {authorNames}
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

            <div className="p-3 space-y-2">
                <button
                    onClick={addToCart}
                    className="w-full bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600 transition"
                    disabled={isBorrowed}
                >
                    Add to cart
                </button>

                {!isBorrowed && user && (
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