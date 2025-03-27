import React, { useState, useRef, useEffect } from "react";
import { Bell } from "lucide-react";
import { formatDistanceToNow } from "date-fns";
import { useNotifications } from "../../context/NotificationContext";

export const NotificationsMenu: React.FC = () => {
    const [isOpen, setIsOpen] = useState(false);
    const menuRef = useRef<HTMLDivElement>(null);

    const {
        notifications,
        unreadCount,
        loading,
        markAsRead,
        markAllAsRead,
        clearAllNotifications
    } = useNotifications();

    const handleToggle = () => {
        setIsOpen(!isOpen);
    };

    const handleMarkAsRead = async (id: number) => {
        await markAsRead(id);
    };

    const handleMarkAllAsRead = async () => {
        await markAllAsRead();
    };

    const handleClearAll = async () => {
        if (confirm("Are you sure you want to clear all notifications? This cannot be undone.")) {
            await clearAllNotifications();
        }
    };

    // Close the menu when clicking outside
    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
                setIsOpen(false);
            }
        };

        document.addEventListener("mousedown", handleClickOutside);
        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, []);

    return (
        <div className="relative" ref={menuRef}>
            <button
                className="flex items-center text-white hover:text-blue-400 focus:outline-none"
                onClick={handleToggle}
            >
                <Bell className="h-5 w-5 sm:h-6 sm:w-6" />
                {unreadCount > 0 && (
                    <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full h-4 w-4 flex items-center justify-center">
            {unreadCount}
          </span>
                )}
            </button>

            {isOpen && (
                <div className="absolute right-0 mt-2 w-72 sm:w-80 bg-white rounded-md shadow-lg z-50 max-h-96 overflow-y-auto overflow-x-hidden">
                    <div className="px-4 py-2 border-b">
                        <div className="flex justify-between items-center mb-2">
                            <h3 className="text-lg font-semibold text-gray-700">Notifications</h3>
                            <button
                                onClick={handleClearAll}
                                className="text-sm text-red-600 hover:text-red-800"
                                disabled={notifications.length === 0}
                            >
                                Clear all
                            </button>
                        </div>
                        {unreadCount > 0 && (
                            <div className="flex justify-end">
                                <button
                                    onClick={handleMarkAllAsRead}
                                    className="text-sm text-blue-600 hover:text-blue-800"
                                >
                                    Mark all as read
                                </button>
                            </div>
                        )}
                    </div>

                    {loading && (
                        <div className="p-4 text-center text-gray-500">
                            Loading notifications...
                        </div>
                    )}

                    {!loading && notifications.length === 0 && (
                        <div className="p-4 text-center text-gray-500">
                            No notifications
                        </div>
                    )}

                    {!loading &&
                        notifications.map((notification) => (
                            <div
                                key={notification.id}
                                className={`px-4 py-3 border-b hover:bg-gray-50 transition-colors duration-150 cursor-pointer ${
                                    !notification.read ? "bg-blue-50" : ""
                                }`}
                                onClick={() => handleMarkAsRead(notification.id)}
                            >
                                <div className="flex justify-between items-start">
                                    <p className="text-sm text-gray-800">{notification.message}</p>
                                    {!notification.read && (
                                        <span className="inline-block w-2 h-2 bg-blue-600 rounded-full"></span>
                                    )}
                                </div>
                                <p className="text-xs text-gray-500 mt-1">
                                    {formatDistanceToNow(notification.createdAt, { addSuffix: true })}
                                </p>
                            </div>
                        ))}
                </div>
            )}
        </div>
    );
};