import React, { useState, useEffect} from 'react';
import { Link } from 'react-router-dom';
import { BookIndex } from '../../model/Book.ts';
import axiosInstance from "../../services/AxiosInstance.ts";
import { useAuth } from "../../context/AuthContext.tsx";

interface BookCardProps {
    book: BookIndex;
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
                // Notifier le parent pour rafraîchir la liste des livres prêtés
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
                // Notifier le parent pour rafraîchir la liste des livres prêtés
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
    const getThumbnailUrl = () => {
        if (!book.thumbnailPath || imageError) return null;
        return `/api/v1/books/images/${book.thumbnailPath}`;
    };

    const handleImageError = () => {
        console.error(`Failed to load image for book: ${book.id}, path: ${book.thumbnailPath}`);
        setImageError(true);
    };


    return (
        <div className="bg-white rounded-lg overflow-hidden shadow-md hover:shadow-lg transition-shadow duration-300 flex flex-col h-full relative">
            {/* Nouveau badge pour indiquer un livre prêté */}
            {isBorrowed && (
                <div className="absolute top-2 right-2 bg-red-500 text-white text-xs px-2 py-1 rounded-full z-10">
                    Prêté
                </div>
            )}

            <Link to={`/books/${book.id}`} className="block flex-grow">
                <div
                    className="w-full h-48 flex items-center justify-center flex-shrink-0"
                    style={{
                        backgroundColor: getThumbnailUrl() ? 'transparent' : getBookCoverColor(book.id),
                        opacity: isBorrowed ? 0.6 : 1 // Réduit l'opacité si le livre est prêté
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
                {/* Add to Cart button - désactivé si le livre est prêté */}
                <button
                    onClick={addToCart}
                    className="w-full bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600 transition"
                    disabled={isBorrowed}
                >
                    Add to cart
                </button>

                {/* Bouton de prêt - caché si le livre est prêté */}
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