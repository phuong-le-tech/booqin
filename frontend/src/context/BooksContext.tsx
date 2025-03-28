import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { BookIndex } from '../model/Book';
import { PageInfo } from '../model/Pagination';
import BookService from '../services/BookService';

interface BooksContextType {
    books: BookIndex[];
    pageInfo: PageInfo;
    isLoading: boolean;
    error: string | null;
    searchTerm: string;
    searchType: string;
    activeFilters: {
        title?: string;
        author?: string;
        isbn?: string;
    };
    setSearchTerm: (term: string) => void;
    setSearchType: (type: string) => void;
    handleSearch: (e: React.FormEvent) => Promise<void>;
    handlePageChange: (page: number) => void;
    handleResetSearch: () => void;
    refreshBooks: () => Promise<void>; // Ajout de la fonction refreshBooks
}

const defaultPageInfo: PageInfo = {
    size: 10,
    number: 0,
    totalElements: 0,
    totalPages: 1
};

const BooksContext = createContext<BooksContextType | undefined>(undefined);

export const BooksProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
    // Load initial state from sessionStorage
    const getInitialState = () => {
        try {
            const savedState = sessionStorage.getItem('booksState');
            if (savedState) {
                const parsed = JSON.parse(savedState);
                return {
                    books: Array.isArray(parsed.books) ? parsed.books : [],
                    pageInfo: parsed.pageInfo && typeof parsed.pageInfo === 'object'
                        ? parsed.pageInfo
                        : { ...defaultPageInfo },
                    searchTerm: typeof parsed.searchTerm === 'string' ? parsed.searchTerm : '',
                    searchType: typeof parsed.searchType === 'string' ? parsed.searchType : 'title',
                    activeFilters: parsed.activeFilters && typeof parsed.activeFilters === 'object'
                        ? parsed.activeFilters
                        : {}
                };
            }
        } catch (error) {
            console.error('Error loading state from sessionStorage:', error);
        }

        return {
            books: [],
            pageInfo: { ...defaultPageInfo },
            searchTerm: '',
            searchType: 'title',
            activeFilters: {}
        };
    };

    const initialState = getInitialState();

    const [books, setBooks] = useState<BookIndex[]>(initialState.books);
    const [pageInfo, setPageInfo] = useState<PageInfo>(initialState.pageInfo);
    const [isLoading, setIsLoading] = useState<boolean>(true);
    const [searchTerm, setSearchTerm] = useState<string>(initialState.searchTerm);
    const [searchType, setSearchType] = useState<string>(initialState.searchType);
    const [activeFilters, setActiveFilters] = useState<{
        title?: string;
        author?: string;
        isbn?: string;
    }>(initialState.activeFilters);
    const [error, setError] = useState<string | null>(null);

    // Save state to sessionStorage whenever it changes
    useEffect(() => {
        try {
            const stateToSave = {
                books,
                pageInfo,
                searchTerm,
                searchType,
                activeFilters
            };

            sessionStorage.setItem('booksState', JSON.stringify(stateToSave));
        } catch (error) {
            console.error('Error saving state to sessionStorage:', error);
        }
    }, [books, pageInfo, searchTerm, searchType, activeFilters]);

    // Fetch books on initial load or when page changes
    useEffect(() => {
        fetchBooks(pageInfo.number);
    }, [pageInfo.number]);

    const fetchBooks = async (page: number): Promise<void> => {
        try {
            setIsLoading(true);

            const response = await BookService.getBooks(
                page,
                pageInfo.size,
                'id',
                'asc',
                activeFilters
            );

            // Vérifier si la réponse existe et a les propriétés attendues
            if (response && Array.isArray(response.content) && response.page) {
                setBooks(response.content);
                setPageInfo(response.page);
                setError(null);
            } else {
                // Gérer le cas où la réponse n'a pas la structure attendue
                setBooks([]);
                setPageInfo({ ...defaultPageInfo });
                setError("Invalid response format from server");
            }
        } catch (error) {
            console.error("Failed to fetch books:", error);
            setBooks([]);
            setError("Failed to load books. Please try again later.");
        } finally {
            setIsLoading(false);
        }
    };

    // Fonction pour rafraîchir la liste des livres (après ajout d'un livre)
    const refreshBooks = async (): Promise<void> => {
        // Utiliser la page courante pour conserver la position de l'utilisateur
        await fetchBooks(pageInfo.number);
    };

    const handleSearch = async (e: React.FormEvent): Promise<void> => {
        e.preventDefault();

        if (!searchTerm || !searchTerm.trim()) {
            // Reset filters if search is empty
            setActiveFilters({});
            setPageInfo(prev => ({ ...prev, number: 0 }));
            await fetchBooks(0);
            return;
        }

        // Create a new filter object based on search type
        const newFilters: {
            title?: string;
            author?: string;
            isbn?: string;
        } = {};

        if (searchType === "title") {
            newFilters.title = searchTerm.trim();
        } else if (searchType === "author") {
            newFilters.author = searchTerm.trim();
        } else if (searchType === "isbn") {
            newFilters.isbn = searchTerm.trim();
        }

        // Update active filters
        setActiveFilters(newFilters);

        // Return to first page for a new search
        setPageInfo(prev => ({ ...prev, number: 0 }));

        try {
            setIsLoading(true);
            const response = await BookService.getBooks(0, pageInfo.size, 'id', 'asc', newFilters);

            // Vérifier si la réponse existe et a les propriétés attendues
            if (response && Array.isArray(response.content) && response.page) {
                setBooks(response.content);
                setPageInfo(response.page);
                setError(null);
            } else {
                // Gérer le cas où la réponse n'a pas la structure attendue
                setBooks([]);
                setPageInfo({ ...defaultPageInfo });
                setError("Invalid response format from server");
            }
        } catch (error) {
            console.error("Search failed:", error);
            setBooks([]);
            setError("Search failed. Please try again.");
        } finally {
            setIsLoading(false);
        }
    };

    const handlePageChange = (page: number): void => {
        setPageInfo(prev => ({ ...prev, number: page }));
        // The useEffect will trigger and reload books with the new page
    };

    const handleResetSearch = (): void => {
        setSearchTerm("");
        setActiveFilters({});
        setPageInfo(prev => ({ ...prev, number: 0 }));
        fetchBooks(0);
    };

    const value: BooksContextType = {
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
    };

    return (
        <BooksContext.Provider value={value}>
            {children}
        </BooksContext.Provider>
    );
};

export const useBooks = (): BooksContextType => {
    const context = useContext(BooksContext);
    if (context === undefined) {
        throw new Error('useBooks must be used within a BooksProvider');
    }
    return context;
};