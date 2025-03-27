import axiosInstance from "./AxiosInstance";
import { AxiosResponse } from "axios";
import { Notification } from "../model/Notification";

export interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;
    timestamp: string;
}

export interface NotificationResponse {
    content: Notification[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}

export interface NotificationCountsResponse {
    unread: number;
}

export const NotificationService = {
    /**
     * Get user notifications with pagination
     */
    getNotifications: async (page = 0, size = 10): Promise<NotificationResponse> => {
        const response: AxiosResponse<ApiResponse<NotificationResponse>> = await axiosInstance.get(
            `/v1/notifications?page=${page}&size=${size}`
        );

        // Convert ISO date strings to Date objects
        const content = response.data.data.content.map(notification => ({
            ...notification,
            date: new Date(notification.createdAt)
        }));

        return {
            ...response.data.data,
            content
        };
    },

    /**
     * Get unread notification count
     */
    getUnreadCount: async (): Promise<number> => {
        const response: AxiosResponse<ApiResponse<{unread: number}>> = await axiosInstance.get(
            "/v1/notifications/counts"
        );
        return response.data.data.unread;
    },

    /**
     * Mark a notification as read
     */
    markAsRead: async (notificationId: number): Promise<boolean> => {
        const response: AxiosResponse<ApiResponse<boolean>> = await axiosInstance.put(
            `/v1/notifications/${notificationId}/read`
        );
        return response.data.data;
    },

    /**
     * Mark all notifications as read
     */
    markAllAsRead: async (): Promise<number> => {
        const response: AxiosResponse<ApiResponse<number>> = await axiosInstance.put(
            "/v1/notifications/read-all"
        );
        return response.data.data;
    },

    /**
     * Delete a notification
     */
    deleteNotification: async (notificationId: number): Promise<boolean> => {
        const response: AxiosResponse<ApiResponse<boolean>> = await axiosInstance.delete(
            `/v1/notifications/${notificationId}`
        );
        return response.data.data;
    },

    /**
     * Clear all notifications for the user
     */
    clearAllNotifications: async (): Promise<number> => {
        const response: AxiosResponse<ApiResponse<number>> = await axiosInstance.delete(
            "/v1/notifications/clear-all"
        );
        return response.data.data;
    }
}