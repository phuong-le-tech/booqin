import React, { useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import { BookIndex } from '../../model/Book.ts';
import axiosInstance from "../../services/AxiosInstance.ts";
import { LendStatus } from "../../model/LendStatus";
import { toast } from "react-toastify";
import { useAuth } from "../../context/AuthContext";
import { BookCover } from "./BookCover";
import { useBookBorrowing } from "../../hooks/useBookBorrowing";

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
    const [showCollectionDropdown, setShowCollectionDropdown] = useState<boolean>(false);
    const [showCollectionModal, setShowCollectionModal] = useState<boolean>(false);
    const cardRef = useRef<HTMLElement>(null);
    const coverRef = useRef<HTMLDivElement>(null);
    const borrowing = useBookBorrowing(id);
    const { isAvailable: isBookAvailable, queuePosition, isInQueue } = borrowing;

    const isBookBorrowed = loanStates.some(loan => {
        const bookIdMatches = loan.bookId === id;
        const stateIsBorrowed = loan.state === LendStatus.BORROWED;
        return bookIdMatches && stateIsBorrowed;
    });

    useEffect(() => {
        setIsBorrowed(borrowedBooks.includes(id));
        setIsLoanable(lentBooks.includes(id));

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

    const authors = book.authorNames ?? [];
    const thumbnail = book.thumbnailPath ? `/api/v1/books/images/${book.thumbnailPath}` : null;

    let status = { label: "Unavailable", tone: "status-off" };
    if (isBorrowed) {
        status = { label: "With you", tone: "status-off" };
    } else if (isBookBorrowed) {
        status = { label: "On loan", tone: "status-off" };
    } else if (isBookAvailable) {
        status = { label: "Available", tone: "status-ok" };
    } else if (isInQueue) {
        status = { label: `Waiting list #${queuePosition}`, tone: "status-wait" };
    }

    let action: { label: string; onClick: () => void } | null = null;
    if (isBorrowed) {
        action = null;
    } else if (isBookAvailable && !isBookBorrowed) {
        action = { label: "Borrow", onClick: () => borrowing.addToCart(coverRef.current) };
    } else if (!isBookAvailable && isInQueue) {
        action = { label: "Leave waiting list", onClick: borrowing.leaveQueue };
    } else if (!isBookAvailable) {
        action = { label: "Notify me", onClick: borrowing.joinQueue };
    }

    return (
        <article
            ref={cardRef}
            className="book-card relative flex flex-col"
            onContextMenu={handleContextMenu}
        >
            <div className="relative">
                <Link to={`/books/${book.id}`} className="block rounded-cover" aria-label={book.title}>
                    <div ref={coverRef} className="cover-lift rounded-cover shadow-1">
                        <BookCover title={book.title} authors={authors} src={thumbnail} />
                    </div>
                </Link>

                {action && (
                    <button
                        onClick={action.onClick}
                        disabled={loading || borrowing.loading}
                        className="cover-action absolute inset-x-2 bottom-2 z-10 h-10 rounded-full bg-ink text-sm font-medium text-paper hover:bg-ink-2 disabled:opacity-60"
                    >
                        {loading || borrowing.loading ? "Working..." : action.label}
                    </button>
                )}
            </div>

            <div className="mt-3 flex flex-1 flex-col">
                <Link
                    to={`/books/${book.id}`}
                    className="font-serif text-[15px] font-semibold leading-5 text-ink line-clamp-2 hover:text-accent"
                >
                    {book.title}
                </Link>
                <p className="mt-0.5 text-[13px] leading-[18px] text-ink-2 line-clamp-1">
                    {authors.length > 0 ? authors.join(", ") : "Unknown author"}
                </p>

                <div className="mt-2 flex flex-wrap items-center gap-2">
                    <span className={`status ${status.tone}`}>{status.label}</span>
                </div>

                {!isBookBorrowed && user && (
                    <button
                        onClick={isLoanable ? removeFromLoanable : addToLoanable}
                        disabled={loading}
                        className="mt-2 self-start text-[13px] text-ink-2 underline underline-offset-2 hover:text-ink disabled:opacity-60"
                    >
                        {isLoanable ? "Stop lending this book" : "Lend this book"}
                    </button>
                )}
            </div>

            {showCollectionDropdown && (
                <div className="absolute right-0 top-8 z-30 w-56 rounded-surface bg-card p-2 shadow-2">
                    <button
                        className="block w-full rounded-control px-3 py-2 text-left text-sm text-ink hover:bg-sunk"
                        onClick={handleOpenCollectionModal}
                    >
                        Add to a collection
                    </button>
                </div>
            )}

            {showCollectionModal && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-ink/40 p-4">
                    <div role="dialog" aria-modal="true" aria-label="Add to collection"
                         className="w-full max-w-md overflow-hidden rounded-surface bg-card shadow-2">
                        <div className="flex items-center justify-between px-6 pb-2 pt-6">
                            <h3 className="font-serif text-xl font-semibold text-ink">Add to a collection</h3>
                            <button
                                onClick={() => setShowCollectionModal(false)}
                                className="rounded-full p-2 text-ink-2 hover:bg-sunk hover:text-ink"
                                aria-label="Close"
                            >
                                <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
                                </svg>
                            </button>
                        </div>
                        <div className="max-h-96 overflow-y-auto px-4 pb-2">
                            {userCollections.length > 0 ? (
                                <ul className="space-y-1">
                                    {userCollections.map(collection => (
                                        <li key={collection.id}>
                                            <button
                                                className="flex w-full items-center gap-3 rounded-control p-2 text-left hover:bg-sunk"
                                                onClick={() => addToCollection(collection.id)}
                                            >
                                                <span className="flex h-9 w-9 items-center justify-center rounded-full bg-sunk font-serif font-semibold text-ink">
                                                    {collection.title.charAt(0).toUpperCase()}
                                                </span>
                                                <span className="font-medium text-ink">{collection.title}</span>
                                            </button>
                                        </li>
                                    ))}
                                </ul>
                            ) : (
                                <div className="px-4 py-8 text-center">
                                    <p className="text-ink">No collections yet</p>
                                    <p className="mt-1 text-[13px] text-ink-2">Create a collection first, then add books to it.</p>
                                </div>
                            )}
                        </div>
                        <div className="flex justify-end px-6 py-4">
                            <button onClick={() => setShowCollectionModal(false)} className="btn-quiet">
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </article>
    );
};
