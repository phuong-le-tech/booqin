// src/components/BookGrid.tsx
import React, { useState, useEffect } from 'react';
import { BookCard } from './BookCard.tsx';
import { BookIndex } from '../../model/Book.ts';
import axiosInstance from "../../services/AxiosInstance.ts";
import { useAuth } from "../../context/AuthContext.tsx";
import { LendStatus } from "../../model/LendStatus";
import { toast } from "react-toastify";

interface LoanInfo {
    bookId: number;
    state: string;
}

interface Collection {
    id: number;
    title: string;
}

interface BookGridProps {
    books: BookIndex[];
    isLoading: boolean;
    userId?: number;
}

export const BookGrid: React.FC<BookGridProps> = ({ books, isLoading }) => {
    const [lentBooks, setLentBooks] = useState<number[]>([]);
    const [borrowedBooks, setBorrowedBooks] = useState<number[]>([]);
    const [loanStates, setLoanStates] = useState<LoanInfo[]>([]);
    const [userCollections, setUserCollections] = useState<Collection[]>([]);
    const { user } = useAuth();
    const userIds = user?.id;

    useEffect(() => {
        if (userIds) {
            fetchUserBooks();
            fetchUserCollections();
            console.log("Utilisateur connecté, ID:", userIds);
        } else {
            console.log("Aucun utilisateur connecté");
        }
    }, [userIds]);

    const fetchUserBooks = async () => {
        if (!userIds) return;

        try {
            const response = await axiosInstance.get(`/v1/loans/user/${userIds}?type=${LendStatus.ALL}`);
            if (response.data?.success && response.data?.data) {
                if (Array.isArray(response.data.data.lentBooks)) {
                    const lentBookIds = response.data.data.lentBooks.map((loan: any) => {
                        if (loan.book && typeof loan.book.id === 'number') {
                            return loan.book.id;
                        } else if (loan.bookId) {
                            return loan.bookId;
                        } else if (typeof loan.id === 'number') {
                            return loan.id;
                        }
                        return null;
                    }).filter((id: any) => id !== null) as number[];
                    setLentBooks(lentBookIds);

                    const loanStatesData = response.data.data.lentBooks.map((loan: any) => {
                        let bookId: number | null = null;
                        let state: string | null = null;

                        if (loan.book && typeof loan.book.id === 'number') {
                            bookId = loan.book.id;
                        } else if (loan.bookId) {
                            bookId = loan.bookId;
                        } else if (typeof loan.id === 'number') {
                            bookId = loan.id;
                        }

                        if (typeof loan.state === 'string') {
                            state = loan.state;
                        } else if (loan.status) {
                            state = loan.status;
                        } else if (loan.loanState) {
                            state = loan.loanState;
                        }

                        if (bookId !== null && state !== null) {
                            return { bookId, state };
                        }
                        return null;
                    }).filter((info: any) => info !== null) as LoanInfo[];
                    setLoanStates(loanStatesData);
                }

                if (Array.isArray(response.data.data.borrowedBooks)) {
                    const borrowedBookIds = response.data.data.borrowedBooks.map((loan: any) => {
                        if (loan.book && typeof loan.book.id === 'number') {
                            return loan.book.id;
                        } else if (loan.bookId) {
                            return loan.bookId;
                        } else if (typeof loan.id === 'number') {
                            return loan.id;
                        }
                        return null;
                    }).filter((id: any) => id !== null) as number[];
                    setBorrowedBooks(borrowedBookIds);
                }
            }
        } catch (err: any) {
            console.error("Error fetching user books:", err);
        }
    };

    const fetchUserCollections = async () => {
        if (!userIds) return;

        try {
            console.log("Fetching collections for user ID:", userIds);

            const publicResponse = await axiosInstance.get(`/v1/collections/user/public`, {
                params: {
                    page: 0,
                    size: 100
                }
            });

            const privateResponse = await axiosInstance.get(`/v1/collections/user/private`, {
                params: {
                    page: 0,
                    size: 100
                }
            });

            console.log("Public collections response:", publicResponse);
            console.log("Private collections response:", privateResponse);

            // Extraire et combiner les collections
            let publicCollections: Collection[] = [];
            let privateCollections: Collection[] = [];

            if (publicResponse.data?.success && publicResponse.data?.data?.content) {
                publicCollections = publicResponse.data.data.content;
            }

            if (privateResponse.data?.success && privateResponse.data?.data?.content) {
                privateCollections = privateResponse.data.data.content;
            }

            // Combiner les deux ensembles de collections
            const allCollections = [...publicCollections, ...privateCollections];

            console.log(`${allCollections.length} collections trouvées:`, allCollections);
            setUserCollections(allCollections);

            if (allCollections.length === 0) {
                // Collections de test si aucune n'est trouvée
                const testCollections = [
                    { id: 1, title: 'Favoris (test)' },
                    { id: 2, title: 'À lire plus tard (test)' },
                    { id: 3, title: 'Science-fiction (test)' }
                ];
                setUserCollections(testCollections);
                console.log("Aucune collection trouvée - Utilisation de collections de test");
            }
        } catch (err: any) {
            console.error("Error fetching user collections:", err);

            // En cas d'erreur, utiliser des collections de test
            const testCollections = [
                { id: 1, title: 'Favoris (test)' },
                { id: 2, title: 'À lire plus tard (test)' }
            ];
            setUserCollections(testCollections);
            console.log("Erreur API - Utilisation de collections de test");
        }
    };

    const handleLendingChange = () => {
        fetchUserBooks();
    };

    if (isLoading) {
        // Skeleton loading state
        return (
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6">
                {Array(10).fill(null).map((_, index) => (
                    <div key={index} className="animate-pulse">
                        <div className="bg-gray-200 h-48 rounded-t-lg"></div>
                        <div className="bg-white rounded-b-lg p-4 space-y-3">
                            <div className="h-4 bg-gray-200 rounded w-3/4"></div>
                            <div className="h-3 bg-gray-200 rounded w-1/2"></div>
                            <div className="h-3 bg-gray-200 rounded w-1/4"></div>
                            <div className="space-y-2 mt-4">
                                <div className="h-8 bg-gray-200 rounded"></div>
                                <div className="h-8 bg-gray-200 rounded"></div>
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        );
    }

    // Afficher les collections avant de rendre le composant
    console.log("Rendering BookGrid with collections:", userCollections);

    return (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6">
            {books.map(book => (
                <BookCard
                    key={book.id}
                    book={book}
                    lentBooks={lentBooks}
                    borrowedBooks={borrowedBooks}
                    loanStates={loanStates}
                    userCollections={userCollections}
                    onLendingChange={handleLendingChange}
                />
            ))}
        </div>
    );
};