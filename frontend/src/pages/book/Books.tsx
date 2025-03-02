// src/pages/Books.tsx
import React, { useState, useEffect } from "react";
import { BookGrid } from "../../components/BookGrid";
import { Pagination } from "../../components/Pagination";
import { Layout } from "../Layout";
import { BookIndex } from "../../model/Book";
import { PageInfo } from "../../model/Pagination";
import BookService from "../../services/BookService";

export const Books: React.FC = () => {
    const [books, setBooks] = useState<BookIndex[]>([]);
    const [pageInfo, setPageInfo] = useState<PageInfo>({
        size: 10,
        number: 0,
        totalElements: 0,
        totalPages: 1
    });
    const [isLoading, setIsLoading] = useState<boolean>(true);
    const [searchTerm, setSearchTerm] = useState<string>("");
    const [searchType, setSearchType] = useState<string>("title");
    const [error, setError] = useState<string | null>(null);

    // Conserver les filtres actifs
    const [activeFilters, setActiveFilters] = useState<{
        title?: string;
        author?: string;
        isbn?: string;
    }>({});

    useEffect(() => {
        fetchBooks(pageInfo.number);
    }, [pageInfo.number]); // Refetch quand la page change

    const fetchBooks = async (page: number) => {
        try {
            setIsLoading(true);

            // Utilise les filtres actifs pour la requête
            const response = await BookService.getBooks(
                page,
                pageInfo.size,
                'id',
                'asc',
                activeFilters
            );
            if (response) {
                setBooks(response.content);
                setPageInfo(response.page);
            }
            setError(null);
        } catch (error) {
            console.error("Failed to fetch books:", error);
            setError("Failed to load books. Please try again later.");
        } finally {
            setIsLoading(false);
        }
    };

    const handleSearch = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!searchTerm.trim()) {
            // Réinitialise les filtres si la recherche est vide
            setActiveFilters({});
            fetchBooks(0);
            return;
        }

        // Crée un nouvel objet de filtres basé sur le type de recherche
        const newFilters: any = {};
        if (searchType === "title") {
            newFilters.title = searchTerm.trim();
        } else if (searchType === "author") {
            newFilters.author = searchTerm.trim();
        } else if (searchType === "isbn") {
            newFilters.isbn = searchTerm.trim();
        }

        // Met à jour les filtres actifs
        setActiveFilters(newFilters);

        // Revient à la première page pour une nouvelle recherche
        setPageInfo(prev => ({ ...prev, number: 0 }));

        try {
            setIsLoading(true);
            const response = await BookService.getBooks(0, pageInfo.size, 'id', 'asc', newFilters);
            if (response) {
                setBooks(response.content);
                setPageInfo(response.page);
            }
            setError(null);
        } catch (error) {
            console.error("Search failed:", error);
            setError("Search failed. Please try again.");
        } finally {
            setIsLoading(false);
        }
    };

    // Gestionnaire de changement de page
    const handlePageChange = (page: number) => {
        setPageInfo(prev => ({ ...prev, number: page }));
        // Le useEffect se déclenchera et rechargera les livres avec la nouvelle page
    };

    // Réinitialiser la recherche
    const handleResetSearch = () => {
        setSearchTerm("");
        setActiveFilters({});
        setPageInfo(prev => ({ ...prev, number: 0 }));
        fetchBooks(0);
    };

    // Détermine si des filtres sont actifs
    const hasActiveFilters = Object.values(activeFilters).some(value => value !== undefined && value !== "");

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
                                <option value="isbn">By ISBN</option>
                            </select>
                        </div>
                        <div className="flex gap-2">
                            <button
                                type="submit"
                                className="w-full md:w-auto px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
                            >
                                Search
                            </button>
                            {hasActiveFilters && (
                                <button
                                    type="button"
                                    onClick={handleResetSearch}
                                    className="w-full md:w-auto px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 focus:outline-none focus:ring-2 focus:ring-gray-500"
                                >
                                    Clear
                                </button>
                            )}
                        </div>
                    </div>
                </form>

                {/* Active filter indicator */}
                {hasActiveFilters && (
                    <div className="mb-4 px-4 py-2 bg-blue-50 border border-blue-200 rounded-lg">
                        <p className="text-blue-700">
                            {activeFilters.title && (
                                <span>Filtering by title: <strong>{activeFilters.title}</strong></span>
                            )}
                            {activeFilters.author && (
                                <span>Filtering by author: <strong>{activeFilters.author}</strong></span>
                            )}
                            {activeFilters.isbn && (
                                <span>Filtering by ISBN: <strong>{activeFilters.isbn}</strong></span>
                            )}
                            {pageInfo.totalElements > 0 && (
                                <span> • {pageInfo.totalElements} results found</span>
                            )}
                        </p>
                    </div>
                )}

                {/* Error message */}
                {error && (
                    <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
                        {error}
                    </div>
                )}

                {/* Books grid */}
                <BookGrid books={books} isLoading={isLoading} />

                {/* Empty state */}
                {!isLoading && books.length === 0 && (
                    <div className="text-center py-12 bg-gray-50 rounded-lg">
                        <p className="text-gray-600">
                            {hasActiveFilters
                                ? "No books found matching your search criteria."
                                : "No books available."}
                        </p>
                    </div>
                )}

                {/* Pagination - on l'affiche si on a plus d'une page */}
                {pageInfo && pageInfo.totalPages > 1 && (
                    <Pagination
                        pageInfo={pageInfo}
                        onPageChange={handlePageChange}
                    />
                )}
            </div>
        </Layout>
    );
};
