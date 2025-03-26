// src/components/BookGrid.tsx
import React, {useState, useEffect } from 'react';
import { BookCard } from './BookCard.tsx';
import { BookIndex } from '../../model/Book.ts';
import axiosInstance from "../../services/AxiosInstance.ts";
import {useAuth} from "../../context/AuthContext.tsx";
import { LendStatus } from "../../model/LendStatus";
import { toast } from "react-toastify";

interface LoanInfo {
    bookId: number;
    state: string;
}

interface BookGridProps {
    books: BookIndex[];
    isLoading: boolean;
    userId?: number;
}

export const BookGrid: React.FC<BookGridProps> = ({ books, isLoading}) => {
    const [lentBooks, setLentBooks] = useState<number[]>([]);
    const [borrowedBooks, setBorrowedBooks] = useState<number[]>([]);
    const [loanStates, setLoanStates] = useState<LoanInfo[]>([]);
    const { user } = useAuth();
    const userIds = user?.id;

    const fetchUserBooks = async () => {
        if (!userIds) {
            return;
        }

        try {
            const response = await axiosInstance.get(`/v1/loans/user/${userIds}?type=${LendStatus.ALL}`);
            if (response.data?.success && response.data?.data) {

                if (Array.isArray(response.data.data.lentBooks)) {

                    const lentBookIds = response.data.data.lentBooks.map((loan: any) => {
                        if (loan.book && typeof loan.book.id === 'number') {
                            return loan.book.id;
                        } else if (loan.bookId) {
                            // Structure alternative possible
                            return loan.bookId;
                        } else if (typeof loan.id === 'number') {
                            // Autre structure alternative
                            return loan.id;
                        }
                        return null;
                    }).filter(id => id !== null) as number[];

                    setLentBooks(lentBookIds);

                    // Adapter l'extraction des états de prêt
                    const loanStatesData = response.data.data.lentBooks.map((loan: any) => {
                        let bookId: number | null = null;
                        let state: string | null = null;

                        // Déterminer l'ID du livre
                        if (loan.book && typeof loan.book.id === 'number') {
                            bookId = loan.book.id;
                        } else if (loan.bookId) {
                            bookId = loan.bookId;
                        } else if (typeof loan.id === 'number') {
                            bookId = loan.id;
                        }

                        // Déterminer l'état du prêt
                        if (typeof loan.state === 'string') {
                            state = loan.state;
                        } else if (loan.status) {
                            // Structure alternative possible
                            state = loan.status;
                        } else if (loan.loanState) {
                            // Autre structure alternative
                            state = loan.loanState;
                        }

                        if (bookId !== null && state !== null) {
                            return { bookId, state };
                        }
                        return null;
                    }).filter(info => info !== null) as LoanInfo[];

                    setLoanStates(loanStatesData);
                } else {
                    toast.warning("Format de données inattendu pour les livres prêtés");
                }

                // Faire de même pour les livres empruntés
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
                    }).filter(id => id !== null) as number[];

                    setBorrowedBooks(borrowedBookIds);
                }
            } else {
                console.warn("Unexpected response format from /v1/loans/user", response);
            }
        } catch (err: any) {
            console.error("Error fetching user books:", err);
        }
    };

    useEffect(() => {
        fetchUserBooks();
    }, [userIds]);

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

    return (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6">
            {books.map(book => (
                <BookCard
                    key={book.id}
                    book={book}
                    lentBooks={lentBooks}
                    borrowedBooks={borrowedBooks}
                    loanStates={loanStates}
                    onLendingChange={handleLendingChange}
                />
            ))}
        </div>
    );
};