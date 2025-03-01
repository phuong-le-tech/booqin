import { Book } from '../model/Book';

const API_URL = '/api/v1/books';

interface PaginatedBooks {
    books: Book[];
    totalPages: number;
    currentPage: number;
}

const BookService = {
    async getAllBooks(): Promise<Book[]> {
        try {
            const response = await fetch(API_URL);
            const data = await response.json();

            if (!response.ok || !data.success) {
                throw new Error(data.message || 'Failed to fetch books');
            }

            return data.data;
        } catch (error) {
            console.error('Error fetching books:', error);
            throw error;
        }
    },

    async getPaginatedBooks(page: number, size: number): Promise<PaginatedBooks> {
        try {
            const response = await fetch(`${API_URL}?page=${page}&size=${size}`);
            const data = await response.json();

            if (!response.ok || !data.success) {
                throw new Error(data.message || 'Failed to fetch paginated books');
            }

            return {
                books: data.data,
                totalPages: data.meta?.totalPages || 1,
                currentPage: data.meta?.currentPage || 0
            };
        } catch (error) {
            console.error('Error fetching paginated books:', error);
            throw error;
        }
    },

    async getBookById(id: string): Promise<Book> {
        try {
            const response = await fetch(`${API_URL}/${id}`);
            const data = await response.json();

            if (!response.ok || !data.success) {
                throw new Error(data.message || `Failed to fetch book with ID: ${id}`);
            }

            return data.data;
        } catch (error) {
            console.error(`Error fetching book with ID ${id}:`, error);
            throw error;
        }
    },

    async searchBooksByTitle(title: string): Promise<Book[]> {
        try {
            const response = await fetch(`${API_URL}/searchByTitle?title=${encodeURIComponent(title)}`);
            const data = await response.json();

            if (!response.ok || !data.success) {
                throw new Error(data.message || 'Failed to search books by title');
            }

            return data.data;
        } catch (error) {
            console.error('Error searching books by title:', error);
            throw error;
        }
    },

    async searchBooksByAuthor(author: string): Promise<Book[]> {
        try {
            const response = await fetch(`${API_URL}/searchByAuthor?author=${encodeURIComponent(author)}`);
            const data = await response.json();

            if (!response.ok || !data.success) {
                throw new Error(data.message || 'Failed to search books by author');
            }

            return data.data;
        } catch (error) {
            console.error('Error searching books by author:', error);
            throw error;
        }
    }
};

export default BookService;