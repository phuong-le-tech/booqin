// src/pages/BookDetail.tsx
import React, { useState, useEffect } from "react";
import { useParams, Link } from "react-router-dom";
import { Layout } from "../Layout";
import { Book } from "../../model/Book";
import BookService from "../../services/BookService";

export const BookDetail: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const [book, setBook] = useState<Book | null>(null);
    const [isLoading, setIsLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchBookDetails = async () => {
            if (!id) return;

            try {
                setIsLoading(true);
                const bookData = await BookService.getBookById(id);
                setBook(bookData);
                setError(null);
            } catch (error) {
                console.error("Failed to fetch book details:", error);
                setError("Failed to load book details. Please try again later.");
            } finally {
                setIsLoading(false);
            }
        };

        fetchBookDetails();
    }, [id]);

    if (isLoading) {
        return (
            <Layout>
                <div className="container mx-auto px-4 py-8">
                    <div className="animate-pulse">
                        <div className="h-8 bg-gray-200 rounded w-1/3 mb-6"></div>
                        <div className="h-4 bg-gray-200 rounded w-1/4 mb-4"></div>
                        <div className="h-4 bg-gray-200 rounded w-1/5 mb-8"></div>
                        <div className="h-4 bg-gray-200 rounded mb-2"></div>
                        <div className="h-4 bg-gray-200 rounded mb-2"></div>
                        <div className="h-4 bg-gray-200 rounded mb-2"></div>
                        <div className="h-4 bg-gray-200 rounded w-3/4 mb-8"></div>
                    </div>
                </div>
            </Layout>
        );
    }

    if (error || !book) {
        return (
            <Layout>
                <div className="container mx-auto px-4 py-8">
                    <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
                        {error || "Book not found"}
                    </div>
                    <Link
                        to="/books"
                        className="inline-block px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
                    >
                        Back to Books
                    </Link>
                </div>
            </Layout>
        );
    }

    return (
        <Layout>
            <div className="container mx-auto px-4 py-8">
                <div className="flex justify-between items-center mb-6">
                    <Link
                        to="/books"
                        className="text-blue-600 hover:text-blue-800 flex items-center"
                    >
                        <span className="mr-1">←</span> Back to Books
                    </Link>
                </div>

                <div className="bg-white rounded-lg shadow-md overflow-hidden">
                    <div className="p-6">
                        <h1 className="text-3xl font-bold text-gray-800 mb-2">{book.title}</h1>

                        <div className="mb-4">
                            <p className="text-lg text-gray-600">
                                By {book.authors.map(author => author.fullName).join(', ')}
                            </p>
                            {book.publicationDate && (
                                <p className="text-sm text-gray-500">
                                    Published: {book.publicationDate}
                                </p>
                            )}
                        </div>

                        {book.isbns && book.isbns.length > 0 && (
                            <div className="mb-4">
                                <h2 className="text-xl font-semibold text-gray-700 mb-2">ISBN</h2>
                                <ul className="list-disc list-inside">
                                    {book.isbns.map((isbn, index) => (
                                        <li key={index} className="text-gray-600">{isbn}</li>
                                    ))}
                                </ul>
                            </div>
                        )}

                        <div className="mb-6">
                            <h2 className="text-xl font-semibold text-gray-700 mb-2">Description</h2>
                            <p className="text-gray-700 whitespace-pre-line">{book.description}</p>
                        </div>

                        <div className="mt-8 pt-4 border-t border-gray-200 text-sm text-gray-500">
                            <p>Added: {new Date(book.createdAt).toLocaleDateString()}</p>
                            <p>Last updated: {new Date(book.updatedAt).toLocaleDateString()}</p>
                        </div>
                    </div>
                </div>
            </div>
        </Layout>
    );
};