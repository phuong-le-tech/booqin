// src/services/BookCollectionService.ts
import axiosInstance from './AxiosInstance';
import {BookCollectionIndex, BookCollectionShow} from '../model/BookCollection';
import { Page } from '../model/Pagination';
import {ApiResponse} from "../model/ApiResponse.ts";

const API_URL = '/v1/collections';

class BookCollectionService {
    /**
     * Get all collections with pagination support
     */
    async getCollections(
        page = 0,
        size = 10,
        sortField = 'id',
        sortDirection = 'asc',
        title?: string,
        description?: string,
        isPublic?: boolean
    ): Promise<Page<BookCollectionShow>> {
        try {
            const response = await axiosInstance.get<ApiResponse<Page<BookCollectionShow>>>(`${API_URL}`, {
                params: {
                    page,
                    size,
                    sortField,
                    sortDirection,
                    title,
                    description,
                    isPublic
                }
            });

            if (response.data?.success) {
                return response.data.data;
            }

            throw new Error(response.data?.message || 'Failed to fetch collections');
        } catch (error) {
            console.error('Error fetching collections:', error);
            throw error;
        }
    }

    // Add this method to BookCollectionService.ts
    async getPublicCollections(
        page = 0,
        size = 10,
        sortField = 'id',
        sortDirection = 'asc'
    ): Promise<Page<BookCollectionIndex>> {
        try {
            const response = await axiosInstance.get<ApiResponse<Page<BookCollectionIndex>>>(`${API_URL}?isPublic=true`, {
                params: {
                    page,
                    size,
                    sortField,
                    sortDirection
                }
            });

            if (response.data?.success) {
                return response.data.data;
            }

            throw new Error(response.data?.message || 'Failed to fetch public collections');
        } catch (error) {
            console.error('Error fetching public collections:', error);
            throw error;
        }
    }

    /**
     * Get collection by ID
     */
    async getBookCollectionById(id: number): Promise<BookCollectionShow> {
        try {
            const response = await axiosInstance.get<ApiResponse<BookCollectionShow>>(`${API_URL}/${id}`);

            if (response.data?.success) {
                return response.data.data;
            }

            throw new Error(response.data?.message || 'Failed to fetch collection');
        } catch (error) {
            console.error(`Error fetching collection with ID ${id}:`, error);
            throw error;
        }
    }

    /**
     * Create a new collection
     */
    async createBookCollection(collectionData: any): Promise<BookCollectionShow> {
        try {
            const response = await axiosInstance.post<ApiResponse<BookCollectionShow>>(`${API_URL}`, collectionData);

            if (response.data?.success) {
                return response.data.data;
            }

            throw new Error(response.data?.message || 'Failed to create collection');
        } catch (error) {
            console.error('Error creating collection:', error);
            throw error;
        }
    }

    /**
     * Update a collection
     */
    async patchBookCollection(id: number, updateData: any): Promise<BookCollectionShow> {
        try {
            const response = await axiosInstance.patch<ApiResponse<BookCollectionShow>>(`${API_URL}/${id}`, updateData);

            if (response.data?.success) {
                return response.data.data;
            }

            throw new Error(response.data?.message || 'Failed to update collection');
        } catch (error) {
            console.error(`Error updating collection with ID ${id}:`, error);
            throw error;
        }
    }

    /**
     * Delete a collection
     */
    async deleteBookCollection(id: number): Promise<void> {
        try {
            const response = await axiosInstance.delete<ApiResponse<void>>(`${API_URL}/${id}`);

            if (!response.data?.success) {
                throw new Error(response.data?.message || 'Failed to delete collection');
            }
        } catch (error) {
            console.error(`Error deleting collection with ID ${id}:`, error);
            throw error;
        }
    }

    /**
     * Add a book to a collection
     */
    async addBookToCollection(collectionId: number, bookId: number): Promise<void> {
        try {
            const response = await axiosInstance.post<ApiResponse<void>>(`${API_URL}/${collectionId}/books/${bookId}`);

            if (!response.data?.success) {
                throw new Error(response.data?.message || 'Failed to add book to collection');
            }
        } catch (error) {
            console.error(`Error adding book ${bookId} to collection ${collectionId}:`, error);
            throw error;
        }
    }

    /**
     * Remove a book from a collection
     */
    async removeBookFromCollection(collectionId: number, bookId: number): Promise<void> {
        try {
            const response = await axiosInstance.delete<ApiResponse<void>>(`${API_URL}/${collectionId}/books/${bookId}`);

            if (!response.data?.success) {
                throw new Error(response.data?.message || 'Failed to remove book from collection');
            }
        } catch (error) {
            console.error(`Error removing book ${bookId} from collection ${collectionId}:`, error);
            throw error;
        }
    }

    async getUserPublicCollections(
        page = 0,
        size = 10,
        sortField = 'id',
        sortDirection = 'asc'
    ) {
        try {
            const response = await axiosInstance.get<ApiResponse<Page<BookCollectionIndex>>>(`${API_URL}/user/public`, {
                params: {
                    page,
                    size,
                    sortField,
                    sortDirection
                }
            });
            return response.data.data;
        } catch (error) {
            console.error('Error fetching user public collections:', error);
            throw error;
        }
    }

    async getUserPrivateCollections(
        page = 0,
        size = 10,
        sortField = 'id',
        sortDirection = 'asc'
    ) {
        try {
            const response = await axiosInstance.get<ApiResponse<Page<BookCollectionIndex>>>(`${API_URL}/user/private`, {
                params: {
                    page,
                    size,
                    sortField,
                    sortDirection
                }
            });
            return response.data.data;
        } catch (error) {
            console.error('Error fetching user private collections:', error);
            throw error;
        }
    }
}

export default new BookCollectionService();