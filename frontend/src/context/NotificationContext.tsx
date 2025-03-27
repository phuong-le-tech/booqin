import React, { createContext, useState, useContext, useEffect, useCallback } from 'react';
import { NotificationService } from '../services/NotificationService';
import { Notification } from '../model/Notification';

interface NotificationContextType {
    notifications: Notification[];
    unreadCount: number;
    loading: boolean;
    error: string | null;
    refreshNotifications: () => Promise<void>;
    markAsRead: (notificationId: number) => Promise<void>;
    markAllAsRead: () => Promise<void>;
    clearAllNotifications: () => Promise<void>;
}

const NotificationContext = createContext<NotificationContextType | undefined>(undefined);

export const NotificationProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [notifications, setNotifications] = useState<Notification[]>([]);
    const [unreadCount, setUnreadCount] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const refreshNotifications = useCallback(async () => {
        try {
            setLoading(true);
            setError(null);

            // Fetch notifications
            const response = await NotificationService.getNotifications();
            setNotifications(response.content);

            // Fetch unread count
            const count = await NotificationService.getUnreadCount();
            setUnreadCount(count);
        } catch (err) {
            setError('Failed to fetch notifications');
            console.error('Error fetching notifications:', err);
        } finally {
            setLoading(false);
        }
    }, []);

    const markAsRead = useCallback(async (notificationId: number) => {
        try {
            const success = await NotificationService.markAsRead(notificationId);
            if (success) {
                // Update local state
                setNotifications(prevNotifications =>
                    prevNotifications.map(notification =>
                        notification.id === notificationId
                            ? { ...notification, read: true }
                            : notification
                    )
                );

                // Update unread count
                setUnreadCount(prevCount => Math.max(0, prevCount - 1));
            }
        } catch (err) {
            console.error('Error marking notification as read:', err);
        }
    }, []);

    const markAllAsRead = useCallback(async () => {
        try {
            const count = await NotificationService.markAllAsRead();
            if (count > 0) {
                // Update local state
                setNotifications(prevNotifications =>
                    prevNotifications.map(notification => ({ ...notification, read: true }))
                );

                // Reset unread count
                setUnreadCount(0);
            }
        } catch (err) {
            console.error('Error marking all notifications as read:', err);
        }
    }, []);

    const clearAllNotifications = useCallback(async () => {
        try {
            const count = await NotificationService.clearAllNotifications();
            if (count > 0) {
                // Clear local state
                setNotifications([]);
                setUnreadCount(0);
            }
        } catch (err) {
            console.error('Error clearing all notifications:', err);
        }
    }, []);

    // Load notifications on component mount
    useEffect(() => {
        refreshNotifications();

        // Optional: set up polling for new notifications
        const intervalId = setInterval(refreshNotifications, 60000); // Poll every minute

        return () => {
            clearInterval(intervalId);
        };
    }, [refreshNotifications]);

    return (
        <NotificationContext.Provider value={{
            notifications,
            unreadCount,
            loading,
            error,
            refreshNotifications,
            markAsRead,
            markAllAsRead,
            clearAllNotifications
        }}>
            {children}
        </NotificationContext.Provider>
    );
};

export const useNotifications = (): NotificationContextType => {
    const context = useContext(NotificationContext);
    if (context === undefined) {
        throw new Error('useNotifications must be used within a NotificationProvider');
    }
    return context;
};