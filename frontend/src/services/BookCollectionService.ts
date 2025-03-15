import axios from 'axios';
import {BookCollection, BookCollectionIndex, BookCollectionShow, BookCollectionUpdate} from '../model/BookCollection';
import { ApiResponse } from '../model/ApiResponse';

const API_URL = '/api/v1/collections';

class BookCollectionService {

    async getUserCollections() {
        try {
            const response = await axios.get<ApiResponse<BookCollection>>(`${API_URL}`);
            return response.data.data.content;
        } catch (error) {
            console.error('Error fetching user collections:', error);
            throw error;
        }
    }

    async getBookCollectionById(id: number) {
        console.log("ID SERV: ", id);
        try {
            const response = await axios.get<ApiResponse<BookCollectionShow>>(`${API_URL}/${id}`);
            return response.data.data;
        } catch (error) {
            console.error(`Error fetching book collection with id ${id}:`, error);
            throw error;
        }
    }

    async createBookCollection(collectionData: any) {
        try {
            const response = await axios.post<ApiResponse<BookCollection>>(API_URL, collectionData);
            return response.data.data;
        } catch (error) {
            console.error('Error creating book collection:', error);
            throw error;
        }
    }

    async updateBookCollection(id: string | number, collectionData: any) {
        try {
            const response = await axios.put<ApiResponse<BookCollectionShow>>(`${API_URL}/${id}`, collectionData);
            return response.data.data;
        } catch (error) {
            console.error(`Error updating book collection with id ${id}:`, error);
            throw error;
        }
    }

    async patchBookCollection(id: string | number, collectionUpdateData: BookCollectionUpdate) {
        try {
            const response = await axios.patch<ApiResponse<BookCollectionShow>>(`${API_URL}/${id}`, collectionUpdateData);
            return response.data.data;
        } catch (error) {
            console.error(`Error patching book collection with id ${id}:`, error);
            throw error;
        }
    }

    async deleteBookCollection(id: string | number) {
        try {
            await axios.delete<ApiResponse<void>>(`${API_URL}/${id}`);
            return true;
        } catch (error) {
            console.error(`Error deleting book collection with id ${id}:`, error);
            throw error;
        }
    }

    async addBookToCollection(collectionId: string | number, bookId: string | number) {
        try {
            const response = await axios.post<ApiResponse<BookCollection>>(`${API_URL}/${collectionId}/books/${bookId}`);
            return response.data.data;
        } catch (error) {
            console.error(`Error adding book ${bookId} to collection ${collectionId}:`, error);
            throw error;
        }
    }

    async removeBookFromCollection(collectionId: string | number, bookId: string | number) {
        try {
            const response = await axios.delete<ApiResponse<BookCollection>>(`${API_URL}/${collectionId}/books/${bookId}`);
            return response.data.data;
        } catch (error) {
            console.error(`Error removing book ${bookId} from collection ${collectionId}:`, error);
            throw error;
        }
    }

    async getPublicCollections() {
        try {
            const response = await axios.get<ApiResponse<BookCollectionShow[]>>(`${API_URL}/public`);
            return response.data.data;
        } catch (error) {
            console.error('Error fetching public collections:', error);
            throw error;
        }
    }
}

export default new BookCollectionService();
