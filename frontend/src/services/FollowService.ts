import axiosInstance from './AxiosInstance';

class FollowService {
    /**
     * Follow an entity (user, book, collection)
     */
    async followEntity(entityId: number, entityType: string) {
        try {
            const response = await axiosInstance.post('/v1/follows', {
                entityId,
                entityType
            });
            return response.data;
        } catch (error) {
            console.error('Error following entity:', error);
            throw error;
        }
    }

    /**
     * Unfollow an entity
     */
    async unfollowEntity(entityId: number, entityType: string) {
        try {
            const response = await axiosInstance.delete('/v1/follows', {
                params: {
                    entityId,
                    entityType
                }
            });
            return response.data;
        } catch (error) {
            console.error('Error unfollowing entity:', error);
            throw error;
        }
    }

    /**
     * Check if current user follows an entity
     */
    async isFollowing(entityId: number, entityType: string) {
        try {
            const response = await axiosInstance.get('/v1/follows/check', {
                params: {
                    entityId,
                    entityType
                }
            });
            return response.data?.data || false;
        } catch (error) {
            console.error('Error checking follow status:', error);
            return false;
        }
    }

    /**
     * Get followers of an entity
     */
    async getFollowers(entityId: number, entityType: string) {
        try {
            const response = await axiosInstance.get('/v1/follows/followers', {
                params: {
                    entityId,
                    entityType
                }
            });
            return response.data?.data || [];
        } catch (error) {
            console.error('Error getting followers:', error);
            return [];
        }
    }

    /**
     * Get count of followers for an entity
     */
    async getFollowersCount(entityId: number, entityType: string) {
        try {
            const response = await axiosInstance.get('/v1/follows/count', {
                params: {
                    entityId,
                    entityType
                }
            });
            return response.data?.data || 0;
        } catch (error) {
            console.error('Error getting followers count:', error);
            return 0;
        }
    }

    /**
     * Get all entities of a specific type that the current user follows
     */
    async getFollowedEntities(entityType: string) {
        try {
            const response = await axiosInstance.get(`/v1/follows/following/${entityType}`);
            return response.data?.data || [];
        } catch (error) {
            console.error('Error getting followed entities:', error);
            return [];
        }
    }
}

export default new FollowService();