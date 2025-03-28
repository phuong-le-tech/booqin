// src/services/CommentService.ts
import axios from 'axios';
import { ApiResponse } from '../model/ApiResponse';

const API_URL = '/api/v1/comments';

/**
 * Interface pour le DTO de création de commentaire
 */
interface CommentCreateDTO {
    userId: number;
    entityId: number;
    entityType: string;
    content: string;
    parentId?: number | null;
}

/**
 * Interface pour le DTO de mise à jour de commentaire
 */
interface CommentUpdateDTO {
    content: string;
}

/**
 * Interface pour les commentaires retournés par l'API
 */
interface CommentDTO {
    id: number;
    content: string;
    userId: number;
    username: string;
    entityId: number;
    entityType: string;
    createdAt: string;
    updatedAt: string;
    replies: CommentDTO[];
}

class CommentService {
    /**
     * Récupère les commentaires avec pagination et filtres optionnels
     */
    async getComments(
        page: number = 0,
        size: number = 10,
        sortField: string = 'createdAt',
        sortDirection: string = 'desc',
        filters: {
            content?: string,
            userId?: number,
            entityType?: string,
            entityId?: number
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
            if (filters.content) params.content = filters.content;
            if (filters.userId) params.userId = filters.userId;
            if (filters.entityType) params.entityType = filters.entityType;
            if (filters.entityId) params.entityId = filters.entityId;

            const response = await axios.get<ApiResponse<any>>(`${API_URL}`, { params });
            return response.data.data;
        } catch (error) {
            console.error('Error fetching comments:', error);
            throw error;
        }
    }

    /**
     * Récupère un commentaire par son ID
     */
    async getById(id: number) {
        try {
            const response = await axios.get<ApiResponse<CommentDTO>>(`${API_URL}/${id}`);
            return response.data.data;
        } catch (error) {
            console.error(`Error fetching comment with id ${id}:`, error);
            throw error;
        }
    }

    /**
     * Récupère les commentaires pour une entité spécifique
     */
    async getByEntity(entityId: number, entityType: string) {
        try {
            const response = await axios.get<ApiResponse<CommentDTO[]>>(
                `${API_URL}/entity/${entityType}/${entityId}`
            );
            return response.data.data;
        } catch (error) {
            console.error(`Error fetching comments for entity ${entityType}/${entityId}:`, error);
            throw error;
        }
    }

    /**
     * Récupère l'arbre des commentaires pour une entité spécifique
     * IMPORTANT: Cette méthode est utilisée dans CommentSection.tsx
     */
    async getCommentTreeForEntity(entityType: string, entityId: number) {
        try {
            const response = await axios.get<ApiResponse<CommentDTO[]>>(
                `${API_URL}/entity/${entityType}/${entityId}/tree`
            );
            return response.data.data;
        } catch (error) {
            console.error(`Error fetching comment tree for entity ${entityType}/${entityId}:`, error);
            throw error;
        }
    }

    /**
     * Compte les commentaires pour une entité spécifique
     * IMPORTANT: Cette méthode est utilisée dans CommentSection.tsx
     */
    async countCommentsByEntity(entityType: string, entityId: number) {
        try {
            const response = await axios.get<ApiResponse<number>>(
                `${API_URL}/entity/${entityType}/${entityId}/count`
            );
            return response.data.data;
        } catch (error) {
            console.error(`Error counting comments for entity ${entityType}/${entityId}:`, error);
            throw error;
        }
    }

    /**
     * Récupère les commentaires d'un utilisateur
     */
    async getByUser(userId: number) {
        try {
            const response = await axios.get<ApiResponse<CommentDTO[]>>(
                `${API_URL}/user/${userId}`
            );
            return response.data.data;
        } catch (error) {
            console.error(`Error fetching comments for user ${userId}:`, error);
            throw error;
        }
    }

    /**
     * Récupère les réponses à un commentaire
     */
    async getRepliesByCommentId(commentId: number) {
        try {
            const response = await axios.get<ApiResponse<CommentDTO[]>>(
                `${API_URL}/${commentId}/replies`
            );
            return response.data.data;
        } catch (error) {
            console.error(`Error fetching replies for comment ${commentId}:`, error);
            throw error;
        }
    }

    /**
     * Crée un nouveau commentaire
     * IMPORTANT: Cette méthode est utilisée dans CommentSection.tsx
     */
    async createComment(commentData: CommentCreateDTO) {
        try {
            // S'assurer que parentId est correctement défini (null si non défini)
            const payload = {
                ...commentData,
                parentId: commentData.parentId || null
            };


            const response = await axios.post<ApiResponse<CommentDTO>>(
                `${API_URL}`,
                payload
            );
            return response.data.data;
        } catch (error) {
            console.error('Error creating comment:', error);
            throw error;
        }
    }

    /**
     * Met à jour un commentaire existant
     */
    async update(commentId: number, userId: number, updateData: CommentUpdateDTO) {
        try {
            const response = await axios.put<ApiResponse<CommentDTO>>(
                `${API_URL}/${commentId}`,
                updateData,
                {
                    headers: {
                        "User-Id": userId.toString()
                    }
                }
            );
            return response.data.data;
        } catch (error) {
            console.error(`Error updating comment ${commentId}:`, error);
            throw error;
        }
    }

    /**
     * Supprime un commentaire
     */
    async delete(commentId: number, userId: number) {
        try {
            await axios.delete<ApiResponse<void>>(
                `${API_URL}/${commentId}`,
                {
                    headers: {
                        "User-Id": userId.toString()
                    }
                }
            );
            return true;
        } catch (error) {
            console.error(`Error deleting comment ${commentId}:`, error);
            throw error;
        }
    }
}

export default new CommentService();