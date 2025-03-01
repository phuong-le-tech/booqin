import React from "react";
import { BookCard } from "./BookCard";
import { Book } from "../model/Book";

interface BookGridProps {
    books: Book[];
    isLoading?: boolean;
}

export const BookGrid: React.FC<BookGridProps> = ({ books, isLoading = false }) => {
    if (isLoading) {
        return (
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6">
                {[...Array(10)].map((_, index) => (
                    <div key={index} className="animate-pulse bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden h-96">
                        {/* Image placeholder */}
                        <div className="w-full h-48 bg-blue-200"></div>
                        <div className="p-5">
                            {/* Title placeholder */}
                            <div className="h-6 bg-blue-100 rounded w-3/4 mb-3"></div>
                            {/* Author placeholder */}
                            <div className="h-4 bg-blue-50 rounded w-1/2 mb-3"></div>
                            {/* Date placeholder */}
                            <div className="h-3 bg-gray-100 rounded w-1/3 mb-4"></div>
                            {/* Description placeholders */}
                            <div className="h-4 bg-gray-100 rounded mb-2"></div>
                            <div className="h-4 bg-gray-100 rounded w-5/6 mb-2"></div>
                            <div className="h-4 bg-gray-100 rounded w-4/6 mb-4"></div>
                            {/* Button placeholder */}
                            <div className="mt-4 pt-3 border-t border-gray-100 flex justify-center">
                                <div className="h-10 bg-blue-100 rounded-lg w-32"></div>
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        );
    }

    return (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6">
            {books.length > 0 ? (
                books.map((book) => (
                    <BookCard key={book.id} {...book} />
                ))
            ) : (
                <div className="col-span-full text-center py-12 text-gray-500">
                    <svg className="w-16 h-16 mx-auto text-blue-200 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
                    </svg>
                    <p className="text-xl font-medium">No books available</p>
                    <p className="mt-2">Try adjusting your search or check back later</p>
                </div>
            )}
        </div>
    );
};