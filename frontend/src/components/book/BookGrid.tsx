// src/components/BookGrid.tsx
import React, {useState, useEffect} from 'react';
import {BookCard} from './BookCard.tsx';
import {BookIndex} from '../../model/Book.ts';
import axiosInstance from "../../services/AxiosInstance.ts";
import {useAuth} from "../../context/AuthContext.tsx";
import {LendStatus} from "../../model/LendStatus";

interface LoanInfo {
    bookId: number;
    state: string;
}

interface Collection {
    id: number;
    title: string;
    type: string;
}

interface BookGridProps {
    books: BookIndex[];
    isLoading: boolean;
    userId?: number;
}

export const BookGrid: React.FC<BookGridProps> = ({books, isLoading}) => {
    const [lentBooks, setLentBooks] = useState<number[]>([]);
    const [borrowedBooks, setBorrowedBooks] = useState<number[]>([]);
    const [loanStates, setLoanStates] = useState<LoanInfo[]>([]);
    const [userCollections, setUserCollections] = useState<Collection[]>([]);
    const {user} = useAuth();
    const userIds = user?.id;

    useEffect(() => {
        if (userIds) {
            fetchUserBooks();
            fetchUserCollections();
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
                            return {bookId, state};
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

            setUserCollections(allCollections.filter(collection => collection.type !== 'LIKED'
                && collection.type !== 'FOLLOWED' && collection.type !== 'AUTHOR'
                && collection.type !== 'RECENT' && collection.type !== 'SMART'
                && collection.type !== 'GLOBAL'));

        } catch (err: any) {
            console.error("Error fetching user collections:", err);
        }
    };

    const handleLendingChange = () => {
        fetchUserBooks();
    };

    if (isLoading) {
        return (
            <div className="grid grid-cols-2 gap-x-6 gap-y-10 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6" aria-busy="true" aria-label="Loading books">
                {Array(12).fill(null).map((_, index) => (
                    <div key={index}>
                        <div className="skeleton aspect-[2/3] w-full rounded-cover"></div>
                        <div className="skeleton mt-3 h-4 w-4/5 rounded-full"></div>
                        <div className="skeleton mt-2 h-3.5 w-1/2 rounded-full"></div>
                    </div>
                ))}
            </div>
        );
    }

    return (
        <div className="grid grid-cols-2 gap-x-6 gap-y-10 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6">
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