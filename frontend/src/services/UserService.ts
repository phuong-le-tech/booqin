import axiosInstance from "./AxiosInstance";
import { User } from "../model/User";
import { ApiResponse } from "../model/ApiResponse";

const UserService = {
    /**
     * Get a user by their ID
     */
    getUserById: async (userId: number): Promise<User> => {
        try {
            const response = await axiosInstance.get<ApiResponse<User>>(`/v1/users/${userId}`);
            const apiResp = response.data;

            if (!apiResp.success || !apiResp.data) {
                throw new Error(apiResp.message || "User not found");
            }

            return apiResp.data;
        } catch (error: any) {
            if (error.response) {
                throw new Error(error.response.data?.message || "Server error while fetching user");
            } else if (error.request) {
                throw new Error("No response from server. Please try again later.");
            } else {
                throw new Error("Error setting up the request (getUserById).");
            }
        }
    },


    updateUser: async (userId: number, userData: Partial<User>): Promise<User> => {
        try {
            const response = await axiosInstance.patch<ApiResponse<User>>(
                `/v1/users/${userId}`,
                userData
            );
            const apiResp = response.data;

            if (!apiResp.success || !apiResp.data) {
                throw new Error(apiResp.message || "User update failed");
            }

            return apiResp.data;
        } catch (error: any) {
            if (error.response) {
                throw new Error(error.response.data?.message || "Server error while updating user");
            } else if (error.request) {
                throw new Error("No response from server. Please try again later.");
            } else {
                throw new Error("Error setting up the request (updateUser).");
            }
        }
    },

    /**
     * Delete a user by their ID
     */
    deleteUser: async (userId: string): Promise<void> => {
        try {
            const response = await axiosInstance.delete<ApiResponse<null>>(`/v1/users/${userId}`);
            const apiResp = response.data;

            if (!apiResp.success) {
                throw new Error(apiResp.message || "User deletion failed");
            }
        } catch (error: any) {
            if (error.response) {
                throw new Error(error.response.data?.message || "Server error while deleting user");
            } else if (error.request) {
                throw new Error("No response from server. Please try again later.");
            } else {
                throw new Error("Error setting up the request (deleteUser).");
            }
        }
    },

    /**
     * Add a collection to a user's collections
     */
    addCollectionToUser: async (userId: string, collectionId: string): Promise<void> => {
        try {
            const response = await axiosInstance.post<ApiResponse<null>>(
                `/v1/users/${userId}/collections/${collectionId}`
            );
            const apiResp = response.data;

            if (!apiResp.success) {
                throw new Error(apiResp.message || "Failed to add collection to user");
            }
        } catch (error: any) {
            if (error.response) {
                throw new Error(error.response.data?.message || "Server error while adding collection");
            } else if (error.request) {
                throw new Error("No response from server. Please try again later.");
            } else {
                throw new Error("Error setting up the request (addCollectionToUser).");
            }
        }
    },

    /**
     * Remove a collection from a user's collections
     */
    removeCollectionFromUser: async (userId: string, collectionId: string): Promise<void> => {
        try {
            const response = await axiosInstance.delete<ApiResponse<null>>(
                `/v1/users/${userId}/collections/${collectionId}`
            );
            const apiResp = response.data;

            if (!apiResp.success) {
                throw new Error(apiResp.message || "Failed to remove collection from user");
            }
        } catch (error: any) {
            if (error.response) {
                throw new Error(error.response.data?.message || "Server error while removing collection");
            } else if (error.request) {
                throw new Error("No response from server. Please try again later.");
            } else {
                throw new Error("Error setting up the request (removeCollectionFromUser).");
            }
        }
    }
};

export default UserService;