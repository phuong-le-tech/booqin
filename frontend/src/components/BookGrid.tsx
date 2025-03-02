// src/components/BookGrid.tsx
import React from 'react';
import { BookCard } from './BookCard';
import { BookIndex } from '../model/Book';

interface BookGridProps {
    books: BookIndex[];
    isLoading: boolean;
}

export const BookGrid: React.FC<BookGridProps> = ({ books, isLoading }) => {
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
                        </div>
                    </div>
                ))}
            </div>
        );
    }

    return (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6">
            {books.map(book => (
                <BookCard key={book.id} book={book} />
            ))}
        </div>
    );
};
