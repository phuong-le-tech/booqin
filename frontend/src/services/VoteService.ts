import axios from 'axios';
import { ApiResponse } from '../model/ApiResponse';
import { VoteResponse, VoteCount } from '../model/Vote';

const API_URL = '/api/v1/votes';

const normalizeEntityType = (entityType: string): string => {
    return entityType.toUpperCase();
};

class VoteService {
    /**
     * Cast a vote (upvote or downvote)
     */
    async castVote(userId: number, entityId: number, entityType: string, voteType: 'UPVOTE' | 'DOWNVOTE') {
        try {
            const normalizedEntityType = normalizeEntityType(entityType);
            const response = await axios.post<ApiResponse<VoteResponse>>(`${API_URL}`, {
                userId,
                entityId,
                entityType: normalizedEntityType,
                voteType
            });
            return response.data.data;
        } catch (error) {
            console.error('Error casting vote:', error);
            throw error;
        }
    }

    /**
     * Update an existing vote
     */
    async updateVote(userId: number, entityId: number, entityType: string, voteType: 'UPVOTE' | 'DOWNVOTE') {
        try {
            const normalizedEntityType = normalizeEntityType(entityType);
            const response = await axios.put<ApiResponse<VoteResponse>>(
                `${API_URL}/user/${userId}/entity/${entityId}/type/${normalizedEntityType}`,
                { voteType }
            );
            return response.data.data;
        } catch (error) {
            console.error('Error updating vote:', error);
            throw error;
        }
    }

    /**
     * Delete a vote
     */
    async deleteVote(userId: number, entityId: number, entityType: string) {
        try {
            const normalizedEntityType = normalizeEntityType(entityType);
            await axios.delete<ApiResponse<void>>(
                `${API_URL}/user/${userId}/entity/${entityId}/type/${normalizedEntityType}`
            );
            return true;
        } catch (error) {
            console.error('Error deleting vote:', error);
            throw error;
        }
    }

    /**
     * Get vote counts for an entity
     */
    async getVoteCounts(entityId: number, entityType: string) {
        try {
            const normalizedEntityType = normalizeEntityType(entityType);
            const response = await axios.get<ApiResponse<VoteCount>>(
                `${API_URL}/count/entity/${entityId}/type/${normalizedEntityType}`
            );
            return response.data.data;
        } catch (error: any) {
            console.error('Error getting vote counts:', error);

            // Return default count object instead of throwing
            return {
                entityId: entityId,
                entityType: entityType,
                upvoteCount: 0,
                downvoteCount: 0,
                score: 0
            };
        }
    }

    /**
     * Check if user has voted for an entity
     */
    async hasVoted(userId: number, entityId: number, entityType: string) {
        try {
            const normalizedEntityType = normalizeEntityType(entityType);
            const response = await axios.get<ApiResponse<boolean>>(
                `${API_URL}/user/${userId}/entity/${entityId}/type/${normalizedEntityType}/has-voted`
            );
            return response.data.data;
        } catch (error) {
            console.error('Error checking if user has voted:', error);
            throw error;
        }
    }

    /**
     * Get user's vote type for an entity
     */
    async getUserVoteType(userId: number, entityId: number, entityType: string) {
        try {
            const normalizedEntityType = normalizeEntityType(entityType);
            const response = await axios.get<ApiResponse<'UPVOTE' | 'DOWNVOTE' | null>>(
                `${API_URL}/user/${userId}/entity/${entityId}/type/${normalizedEntityType}/vote-type`
            );
            return response.data.data;
        } catch (error) {
            console.error('Error getting user vote type:', error);
            throw error;
        }
    }
}

export default new VoteService();