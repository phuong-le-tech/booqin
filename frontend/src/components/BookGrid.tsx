// src/components/BookGrid.tsx
import React, {useState, useEffect } from 'react';
import { BookCard } from './BookCard';
import { BookIndex, BookShow } from '../model/Book';
import axiosInstance from "../services/AxiosInstance.ts";
import {useAuth} from "../context/AuthContext.tsx";

interface BookGridProps {
    books: BookIndex[];
    isLoading: boolean;
    userId?: number;
}

export const BookGrid: React.FC<BookGridProps> = ({ books, isLoading}) => {
    const [lentBooks, setLentBooks] = useState<number[]>([]);
    const [borrowedBooks, setBorrowedBooks] = useState<number[]>([]);
    const [detailedBooks, setDetailedBooks] = useState<BookShow[]>([]);
    const [loadingDetails, setLoadingDetails] = useState<boolean>(true);
    const { user } = useAuth();
    const userIds = user?.id;

    const fetchUserBooks = async () => {
        if (!userIds) {
            console.warn("userId is undefined, skipping lending fetch");
            return;
        }

        try {
            console.log(`Fetching user book data for user ${userIds}`);
            // Utiliser l'endpoint que nous avons créé précédemment
            const response = await axiosInstance.get(`/v1/loans/user/${userIds}?type=all`);
            console.log("User books response:", response);

            if (response.data?.success && response.data?.data) {
                // Extraire les IDs des livres prêtés
                const lentBookIds = response.data.data.lentBooks.map((book: any) => book.book.id);
                setLentBooks(lentBookIds);

                // Extraire les IDs des livres empruntés
                const borrowedBookIds = response.data.data.borrowedBooks.map((book: any) => book.book.id);
                setBorrowedBooks(borrowedBookIds);
            } else {
                console.warn("Unexpected response format from /v1/loans/user", response);
            }
        } catch (err: any) {
            console.error("Error fetching user books:", err);
        }
    };

    const fetchDetailedBooks = async () => {
        if (books.length === 0) return;

        setLoadingDetails(true);
        try {
            const detailedBooksPromises = books.map(book =>
                axiosInstance.get(`/v1/books/${book.id}`)
                    .then(response => {
                        if (response.data?.success && response.data?.data) {
                            return response.data.data;
                        }
                        throw new Error(`Failed to get details for book ${book.id}`);
                    })
                    .catch(error => {
                        console.error(`Error fetching details for book ${book.id}:`, error);
                        return {
                            id: book.id,
                            title: book.title,
                            publicationDate: book.publicationDate,
                            authors: book.authorNames.map(name => {
                                const nameParts = name.split(' ');
                                return {
                                    id: 0,
                                    firstName: nameParts[0] || '',
                                    lastName: nameParts.slice(1).join(' ') || ''
                                };
                            }),
                            isbns: [],
                            description: '',
                            createdAt: '',
                            updatedAt: '',
                            categories: [],
                            // pas de coverImageUrl ni isExternalImage
                        };
                    })
            );

            const results = await Promise.all(detailedBooksPromises);
            setDetailedBooks(results);
        } catch (error) {
            console.error("Error fetching book details:", error);
        } finally {
            setLoadingDetails(false);
        }
    };

    useEffect(() => {
        fetchUserBooks();
    }, [userIds]);

    useEffect(() => {
        fetchDetailedBooks();
    }, [books]);

    const handleLendingChange = () => {
        fetchUserBooks();
    };

    if (isLoading || loadingDetails) {
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
                        </div>
                    </div>
                ))}
            </div>
        );
    }

    return (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6">
            {detailedBooks.map(book => (
                <BookCard
                    key={book.id}
                    book={book}
                    lentBooks={lentBooks}
                    borrowedBooks={borrowedBooks}
                    onLendingChange={handleLendingChange}
                />
            ))}
        </div>
    );
};