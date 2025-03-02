// src/pages/BookDetail.tsx
import React, { useState, useEffect } from "react";
import { useParams, Link } from "react-router-dom";
import { Layout } from "../Layout";
import { Book } from "../../model/Book";
import { Author } from "../../model/Author";
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
                if (!bookData) {
                    setError("Book not found");
                    setIsLoading(false);
                    return;
                }
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

    // Fonction pour générer une couleur à partir de l'id du livre
    const getBookCoverColor = (bookId: number) => {
        const colors = [
            '#1e3a8a', // bleu foncé
            '#1e40af',
            '#1d4ed8',
            '#2563eb', // bleu moyen
            '#3b82f6',
            '#60a5fa', // bleu clair
            '#93c5fd'
        ];
        const colorIndex = bookId % colors.length;
        return colors[colorIndex];
    };

    if (isLoading) {
        return (
            <Layout>
                <div className="container mx-auto px-4 py-8">
                    <div className="animate-pulse">
                        <div className="flex flex-col md:flex-row gap-8">
                            {/* Cover placeholder */}
                            <div className="md:w-1/3 lg:w-1/4 h-64 bg-blue-200 rounded-lg"></div>

                            {/* Details placeholder */}
                            <div className="md:w-2/3 lg:w-3/4">
                                <div className="h-8 bg-gray-200 rounded w-1/2 mb-6"></div>
                                <div className="h-4 bg-gray-200 rounded w-1/3 mb-4"></div>
                                <div className="h-4 bg-gray-200 rounded w-1/4 mb-8"></div>

                                <div className="h-5 bg-gray-200 rounded w-1/5 mb-4"></div>
                                <div className="h-4 bg-gray-200 rounded w-1/3 mb-6"></div>

                                <div className="h-5 bg-gray-200 rounded w-1/4 mb-4"></div>
                                <div className="h-4 bg-gray-200 rounded mb-2"></div>
                                <div className="h-4 bg-gray-200 rounded mb-2"></div>
                                <div className="h-4 bg-gray-200 rounded mb-2"></div>
                                <div className="h-4 bg-gray-200 rounded w-3/4 mb-8"></div>
                            </div>
                        </div>
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

    // Fonction pour formater la date
    const formatDate = (dateString: string) => {
        try {
            const date = new Date(dateString);
            return date.toLocaleDateString(undefined, {
                year: 'numeric',
                month: 'long',
                day: 'numeric'
            });
        } catch (e) {
            return dateString;
        }
    };

    // Fonction pour créer le nom complet des auteurs
    const getAuthorFullName = (author: Author) => {
        return `${author.firstName} ${author.lastName}`.trim();
    };

    return (
        <Layout>
            <div className="container mx-auto px-4 py-8">
                <div className="flex justify-between items-center mb-6">
                    <Link
                        to="/books"
                        className="text-blue-600 hover:text-blue-800 flex items-center font-medium"
                    >
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-1" viewBox="0 0 20 20" fill="currentColor">
                            <path fillRule="evenodd" d="M9.707 16.707a1 1 0 01-1.414 0l-6-6a1 1 0 010-1.414l6-6a1 1 0 011.414 1.414L5.414 9H17a1 1 0 110 2H5.414l4.293 4.293a1 1 0 010 1.414z" clipRule="evenodd" />
                        </svg>
                        Back to Books
                    </Link>
                </div>

                <div className="bg-white rounded-xl shadow-lg overflow-hidden border border-gray-100">
                    <div className="flex flex-col md:flex-row">
                        {/* Book Cover */}
                        <div className="md:w-1/3 lg:w-1/4">
                            <div
                                className="w-full h-64 md:h-full flex items-center justify-center text-white font-bold text-6xl"
                                style={{ backgroundColor: getBookCoverColor(book.id) }}
                            >
                                {book.title.substring(0, 1).toUpperCase()}
                            </div>
                        </div>

                        {/* Book Details */}
                        <div className="p-6 md:p-8 md:w-2/3 lg:w-3/4">
                            <h1 className="text-3xl font-bold text-gray-800 mb-2">{book.title}</h1>

                            <div className="mb-6">
                                <p className="text-lg text-blue-700 font-medium">
                                    By {book.authors.map(author => getAuthorFullName(author)).join(', ')}
                                </p>
                                {book.publicationDate && (
                                    <p className="text-sm text-gray-500 mt-1">
                                        Published: {formatDate(book.publicationDate)}
                                    </p>
                                )}
                            </div>

                            {book.isbns && book.isbns.length > 0 && (
                                <div className="mb-6 p-4 bg-gray-50 rounded-lg">
                                    <h2 className="text-lg font-semibold text-gray-700 mb-2">ISBN</h2>
                                    <ul className="space-y-1">
                                        {book.isbns.map((isbn, index) => (
                                            <li key={index} className="text-gray-600 flex items-center">
                                                <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-2 text-blue-500" viewBox="0 0 20 20" fill="currentColor">
                                                    <path fillRule="evenodd" d="M3 4a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zm0 4a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zm0 4a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zm0 4a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1z" clipRule="evenodd" />
                                                </svg>
                                                {isbn}
                                            </li>
                                        ))}
                                    </ul>
                                </div>
                            )}

                            <div className="mb-8">
                                <h2 className="text-xl font-semibold text-gray-700 mb-3">Description</h2>
                                <div className="prose prose-blue max-w-none text-gray-700">
                                    <p className="whitespace-pre-line">{book.description}</p>
                                </div>
                            </div>

                            {/* Actions */}
                            <div className="flex flex-wrap gap-3 mb-8">
                                <a
                                    href={`https://www.goodreads.com/search?q=${encodeURIComponent(book.title)}`}
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    className="inline-flex items-center px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors"
                                >
                                    <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                                        <path d="M11 3a1 1 0 100 2h2.586l-6.293 6.293a1 1 0 101.414 1.414L15 6.414V9a1 1 0 102 0V4a1 1 0 00-1-1h-5z" />
                                        <path d="M5 5a2 2 0 00-2 2v8a2 2 0 002 2h8a2 2 0 002-2v-3a1 1 0 10-2 0v3H5V7h3a1 1 0 000-2H5z" />
                                    </svg>
                                    Find on Goodreads
                                </a>

                                {book.isbns && book.isbns.length > 0 && (
                                    <a
                                        href={`https://www.amazon.com/s?k=${encodeURIComponent(book.isbns[0])}`}
                                        target="_blank"
                                        rel="noopener noreferrer"
                                        className="inline-flex items-center px-4 py-2 bg-gray-800 text-white text-sm font-medium rounded-lg hover:bg-gray-900 transition-colors"
                                    >
                                        <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                                            <path fillRule="evenodd" d="M10 2a4 4 0 00-4 4v1H5a1 1 0 00-.994.89l-1 9A1 1 0 004 18h12a1 1 0 00.994-1.11l-1-9A1 1 0 0015 7h-1V6a4 4 0 00-4-4zm2 5V6a2 2 0 10-4 0v1h4zm-6 3a1 1 0 112 0 1 1 0 01-2 0zm7-1a1 1 0 100 2 1 1 0 000-2z" clipRule="evenodd" />
                                        </svg>
                                        Find on Amazon
                                    </a>
                                )}
                            </div>

                            <div className="mt-6 pt-4 border-t border-gray-200 text-sm text-gray-500 flex flex-col sm:flex-row sm:justify-between">
                                <p>Added on {formatDate(book.createdAt)}</p>
                                <p>Last updated on {formatDate(book.updatedAt)}</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </Layout>
    );
};