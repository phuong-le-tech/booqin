// src/services/BookService.ts
import axios from 'axios';
import { BookPagedResponse, BookShow } from '../model/Book';
import { ApiResponse } from '../model/ApiResponse';

const API_URL = '/api/v1/books';

class BookService {
    /**
     * Récupère une liste paginée de livres avec filtres optionnels
     */
    async getBooks(
        page: number = 0,
        size: number = 10,
        sortField: string = 'id',
        sortDirection: string = 'asc',
        filters: {
            title?: string,
            author?: string,
            isbn?: string
        } = {}
    ) {
        try {
            // Construit les paramètres en incluant uniquement les valeurs non vides
            const params: any = {
                page,
                size,
                sortField,
                sortDirection
            };

            // Ajoute seulement les filtres qui ont une valeur
            if (filters.title) params.title = filters.title;
            if (filters.author) params.author = filters.author;
            if (filters.isbn) params.isbn = filters.isbn;

            const response = await axios.get<ApiResponse<BookPagedResponse>>(`${API_URL}`, { params });
            return response.data.data;
        } catch (error) {
            console.error('Error fetching books:', error);
            throw error;
        }
    }

    /**
     * Récupère un livre par son ID
     */
    async getBookById(id: string | number) {
        try {
            const response = await axios.get<ApiResponse<BookShow>>(`${API_URL}/${id}`);
            return response.data.data;
        } catch (error) {
            console.error(`Error fetching book with id ${id}:`, error);
            throw error;
        }
    }

    /**
     * Crée un nouveau livre (réservé aux administrateurs)
     */
    async createBook(bookData: any) {
        try {
            const response = await axios.post<ApiResponse<BookShow>>(`${API_URL}`, bookData);
            return response.data.data;
        } catch (error) {
            console.error('Error creating book:', error);
            throw error;
        }
    }

    /**
     * Met à jour un livre existant (réservé aux administrateurs)
     */
    async updateBook(id: string | number, bookData: any) {
        try {
            const response = await axios.put<ApiResponse<BookShow>>(`${API_URL}/${id}`, bookData);
            return response.data.data;
        } catch (error) {
            console.error(`Error updating book with id ${id}:`, error);
            throw error;
        }
    }

    /**
     * Mise à jour partielle d'un livre (réservé aux administrateurs)
     */
    async patchBook(id: string | number, bookUpdateData: any) {
        try {
            const response = await axios.patch<ApiResponse<BookShow>>(`${API_URL}/${id}`, bookUpdateData);
            return response.data.data;
        } catch (error) {
            console.error(`Error patching book with id ${id}:`, error);
            throw error;
        }
    }

    /**
     * Supprime un livre (réservé aux administrateurs)
     */
    async deleteBook(id: string | number) {
        try {
            await axios.delete<ApiResponse<void>>(`${API_URL}/${id}`);
            return true;
        } catch (error) {
            console.error(`Error deleting book with id ${id}:`, error);
            throw error;
        }
    }
}

export default new BookService();