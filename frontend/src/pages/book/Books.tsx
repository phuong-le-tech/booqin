import React, { useState } from "react";
import { Plus, Search } from "lucide-react";
import { BookGrid } from "../../components/book/BookGrid.tsx";
import { Pagination } from "../../components/Pagination";
import { Layout } from "../Layout";
import { useBooks } from "../../context/BooksContext";
import BookAddModal from "../../components/modal/BookAddModal";
import BookService from "../../services/BookService"; // Import du service

export const Books: React.FC = () => {
    const {
        books,
        pageInfo,
        isLoading,
        error,
        searchTerm,
        searchType,
        activeFilters,
        setSearchTerm,
        setSearchType,
        handleSearch,
        handlePageChange,
        handleResetSearch,
        refreshBooks
    } = useBooks();

    const [addBookError, setAddBookError] = useState<string | null>(null);
    const [showAddBookModal, setShowAddBookModal] = useState<boolean>(false);
    const [, setIsAddingBook] = useState<boolean>(false);

    const toggleAddBookModal = () => {
        setShowAddBookModal(!showAddBookModal);
        if (showAddBookModal) {
            setAddBookError(null);
        }
    };

    const handleAddBook = async (bookData: any) => {
        try {
            setIsAddingBook(true);
            setAddBookError(null);

            const formattedData = {
                title: bookData.title,
                description: bookData.description || "",
                publicationDate: bookData.publicationDate || null,
                isbn: bookData.isbn.filter((isbn: string) => isbn.trim() !== ""),
                authors: bookData.authors.filter((author: string) => author.trim() !== "")
            };

            await BookService.createBook(formattedData);
            toggleAddBookModal();
            await refreshBooks();

        } catch (error: any) {
            console.error("Erreur lors de l'ajout du livre:", error);

            if (error.response?.data?.message && error.response.data.message.includes("author")) {
                setAddBookError("Problème avec les auteurs: " + error.response.data.message);
            } else {
                setAddBookError(
                    error.response?.data?.message ||
                    "Une erreur est survenue lors de l'ajout du livre."
                );
            }
        } finally {
            setIsAddingBook(false);
        }
    };

    const hasActiveFilters = Object.values(activeFilters).some(value => value !== undefined && value !== "");
    const activeTerm = activeFilters.title ?? activeFilters.author ?? activeFilters.isbn;

    const searchTypes: { value: string; label: string }[] = [
        { value: "title", label: "Title" },
        { value: "author", label: "Author" },
        { value: "isbn", label: "ISBN" }
    ];

    return (
        <Layout>
            <div className="mx-auto w-full max-w-[1200px] px-4 py-8 sm:px-8 sm:py-10">
                <div className="mb-6 flex flex-wrap items-baseline justify-between gap-4">
                    <div className="flex items-baseline gap-3">
                        <h1 className="page-title">Books</h1>
                        {pageInfo.totalElements > 0 && (
                            <span className="text-[15px] text-ink-3">
                                {pageInfo.totalElements.toLocaleString()} {hasActiveFilters ? "results" : "books"}
                            </span>
                        )}
                    </div>
                    <button onClick={toggleAddBookModal} className="btn-outline">
                        <Plus className="h-4 w-4" aria-hidden="true" />
                        Add a book
                    </button>
                </div>

                <form onSubmit={handleSearch} role="search" className="mb-8 space-y-4">
                    <div className="relative">
                        <Search className="pointer-events-none absolute left-5 top-1/2 h-5 w-5 -translate-y-1/2 text-ink-2" aria-hidden="true" />
                        <input
                            type="search"
                            placeholder={`Search by ${searchType}`}
                            aria-label="Search the catalogue"
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="h-12 w-full rounded-full bg-card pr-28 text-[15px] text-ink shadow-1 outline-none ring-1 ring-line placeholder:text-ink-3 hover:ring-ink-3 focus:ring-2 focus:ring-accent"
                            style={{ paddingLeft: 52 }}
                        />
                        <button
                            type="submit"
                            className="absolute right-1.5 top-1/2 h-9 -translate-y-1/2 rounded-full bg-accent px-5 text-sm font-medium text-white hover:bg-accent-hover"
                        >
                            Search
                        </button>
                    </div>

                    <div className="flex flex-wrap items-center gap-2" role="group" aria-label="Search by">
                        {searchTypes.map(type => (
                            <button
                                key={type.value}
                                type="button"
                                onClick={() => setSearchType(type.value)}
                                aria-pressed={searchType === type.value}
                                className="chip"
                            >
                                {type.label}
                            </button>
                        ))}
                        {hasActiveFilters && (
                            <button type="button" onClick={handleResetSearch} className="ml-2 text-sm text-ink-2 underline underline-offset-2 hover:text-ink">
                                Clear search
                            </button>
                        )}
                    </div>
                </form>

                {error && (
                    <div role="alert" className="mb-6 flex flex-wrap items-center justify-between gap-3 rounded-control bg-bad-tint px-4 py-3 text-sm text-bad">
                        <span>{error}</span>
                        <button onClick={refreshBooks} className="font-medium underline underline-offset-2">Try again</button>
                    </div>
                )}

                <BookGrid books={books} isLoading={isLoading} />

                {!isLoading && !error && books.length === 0 && (
                    <div className="py-20 text-center">
                        <p className="font-serif text-xl font-semibold text-ink">
                            {hasActiveFilters ? `No books match "${activeTerm}"` : "No books yet"}
                        </p>
                        <p className="mt-2 text-ink-2">
                            {hasActiveFilters ? "Check the spelling, or search by another field." : "Add the first book to the catalogue."}
                        </p>
                        {hasActiveFilters && (
                            <button onClick={handleResetSearch} className="btn-outline mt-6">Clear search</button>
                        )}
                    </div>
                )}

                {pageInfo && pageInfo.totalPages > 1 && (
                    <Pagination
                        pageInfo={pageInfo}
                        onPageChange={handlePageChange}
                    />
                )}
            </div>

            <BookAddModal
                isOpen={showAddBookModal}
                onClose={toggleAddBookModal}
                onSubmit={handleAddBook}
                error={addBookError}
            />
        </Layout>
    );
};
