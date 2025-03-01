// src/pages/Books.tsx
import React, { useState, useEffect } from "react";
import { BookGrid } from "../../components/BookGrid";
import { Layout } from "../Layout";
import { Book } from "../../model/Book";
import BookService from "../../services/BookService";

export const Books: React.FC = () => {
    const [books, setBooks] = useState<Book[]>([]);
    const [isLoading, setIsLoading] = useState<boolean>(true);
    const [searchTerm, setSearchTerm] = useState<string>("");
    const [searchType, setSearchType] = useState<string>("title");
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchBooks = async () => {
            try {
                setIsLoading(true);
                const fetchedBooks = await BookService.getAllBooks();
                setBooks(fetchedBooks);
                setError(null);
            } catch (error) {
                console.error("Failed to fetch books:", error);
                setError("Failed to load books. Please try again later.");
            } finally {
                setIsLoading(false);
            }
        };

        fetchBooks();
    }, []);

    const handleSearch = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!searchTerm.trim()) {
            // Si la recherche est vide, on charge tous les livres
            try {
                setIsLoading(true);
                const fetchedBooks = await BookService.getAllBooks();
                setBooks(fetchedBooks);
                setError(null);
            } catch (error) {
                setError("Failed to load books. Please try again later.");
            } finally {
                setIsLoading(false);
            }
            return;
        }

        try {
            setIsLoading(true);
            let searchResults: Book[];

            if (searchType === "title") {
                searchResults = await BookService.searchBooksByTitle(searchTerm);
            } else {
                searchResults = await BookService.searchBooksByAuthor(searchTerm);
            }

            setBooks(searchResults);
            setError(null);
        } catch (error) {
            console.error("Search failed:", error);
            setError("Search failed. Please try again.");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <Layout>
            <div className="container mx-auto px-4 py-8">
                <h1 className="text-3xl font-bold mb-6">Books</h1>

                {/* Search Form */}
                <form onSubmit={handleSearch} className="mb-8">
                    <div className="flex flex-col md:flex-row gap-3">
                        <div className="flex-grow">
                            <input
                                type="text"
                                placeholder="Search for books..."
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                            />
                        </div>
                        <div className="w-full md:w-48">
                            <select
                                value={searchType}
                                onChange={(e) => setSearchType(e.target.value)}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                            >
                                <option value="title">By Title</option>
                                <option value="author">By Author</option>
                            </select>
                        </div>
                        <div>
                            <button
                                type="submit"
                                className="w-full md:w-auto px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
                            >
                                Search
                            </button>
                        </div>
                    </div>
                </form>

                {/* Error message */}
                {error && (
                    <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
                        {error}
                    </div>
                )}

                <BookGrid books={books} isLoading={isLoading} />
            </div>
        </Layout>
    );
};