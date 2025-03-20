import React, {useContext, useState, useEffect} from 'react';
import { Link } from 'react-router-dom';
import { BookIndex } from '../model/Book';
import axiosInstance from "../services/AxiosInstance";
import { AuthContext } from "../context/AuthContext";
import { toast } from "react-toastify";

// Type pour les informations de prêt
interface LoanInfo {
    bookId: number;
    state: string;
}

interface BookCardProps {
    book: BookIndex;
    borrowedBooks?: number[];
    lentBooks?: number[];
    loanStates?: LoanInfo[]; // État de prêt
    onLendingChange?: () => void;
}

export const BookCard: React.FC<BookCardProps> = ({
                                                      book,
                                                      borrowedBooks = [],
                                                      lentBooks = [],
                                                      loanStates = [],
                                                      onLendingChange
                                                  }) => {
    const { id } = book;
    const { user } = useContext(AuthContext);
    const userId = user?.id;
    const [isLoanable, setIsLoanable] = useState<boolean>(false);
    const [loading, setLoading] = useState<boolean>(false);
    const [isBorrowed, setIsBorrowed] = useState<boolean>(false);

    // Vérifier si le livre a le statut "BORROWED" ou une variante similaire
    const isBookBorrowed = loanStates.some(loan => {
        // Normaliser l'ID et l'état pour la comparaison
        const bookIdMatches = loan.bookId === id;
        // Vérifier plusieurs variantes possibles de l'état "BORROWED"
        const stateIsBorrowed =
            loan.state === "BORROWED" ||
            loan.state === "borrowed" ||
            loan.state === "BORROWING" ||
            loan.state === "1" || // Parfois les états sont stockés comme des nombres/strings
            loan.state === 1;     // ou des nombres

        return bookIdMatches && stateIsBorrowed;
    });

    useEffect(() => {
        // Vérifier si le livre est dans la liste des empruntés
        setIsBorrowed(borrowedBooks.includes(id));

        // Vérifier si le livre est dans la liste des prêtables
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
                toast.success("Livre ajouté à votre liste de prêt");
                setIsLoanable(true);
                // Notifier le parent pour rafraîchir la liste des livres prêtés
                if (onLendingChange) {
                    onLendingChange();
                }
            } else {
                toast.error("Impossible d'ajouter le livre à la liste de prêt");
            }
        } catch (error: any) {
            toast.error(error.response?.data?.message || "Échec lors de l'ajout du livre à la liste de prêt");
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
                toast.success("Livre retiré de votre liste de prêt");
                setIsLoanable(false);
                // Notifier le parent pour rafraîchir la liste des livres prêtés
                if (onLendingChange) {
                    onLendingChange();
                }
            } else {
                toast.error("Impossible de retirer le livre de la liste de prêt");
            }
        } catch (error: any) {
            toast.error(error.response?.data?.message || "Échec lors du retrait du livre de la liste de prêt");
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
                toast.success("Le livre a été ajouté au panier");
            }
        } catch (error: any) {
            toast.error("Erreur lors de l'ajout du livre au panier");
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

    return (
        <div className="bg-white rounded-lg overflow-hidden shadow-md hover:shadow-lg transition-shadow duration-300 flex flex-col h-full relative">
            {/* Badge "Prêté" uniquement si le livre a le statut BORROWED */}
            {isBookBorrowed && (
                <div className="absolute top-2 right-2 bg-red-500 text-white text-xs px-2 py-1 rounded-full z-10">
                    Prêté
                </div>
            )}

            <Link to={`/books/${book.id}`} className="block flex-grow">
                <div
                    className="w-full h-48 flex items-center justify-center flex-shrink-0"
                    style={{
                        backgroundColor: getBookCoverColor(book.id),
                        opacity: isBookBorrowed ? 0.6 : 1 // Réduit l'opacité si le livre a le statut BORROWED
                    }}
                >
                    <span className="text-white text-5xl font-bold">
                        {book.title.charAt(0).toUpperCase()}
                    </span>
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
                                Published: {formatDate(book.publicationDate)}
                            </p>
                        )}
                    </div>
                </div>
            </Link>

            {/* Action buttons */}
            <div className="p-3 space-y-2">
                {/* Add to Cart button - désactivé si le livre a le statut BORROWED */}
                <button
                    onClick={addToCart}
                    className="w-full bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600 transition"
                    disabled={isBookBorrowed}
                >
                    Add to cart
                </button>

                {/* Bouton de prêt - caché si le livre a le statut BORROWED */}
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