import React from "react";
import { Bell } from "lucide-react";
import { formatDistanceToNow } from "date-fns";
import { useNotifications } from "../../context/NotificationContext";
import { Dropdown } from "../Dropdown";

export const NotificationsMenu: React.FC = () => {
    const {
        notifications,
        unreadCount,
        loading,
        markAsRead,
        markAllAsRead,
        clearAllNotifications
    } = useNotifications();

    const handleClearAll = async () => {
        if (confirm("Clear all notifications? This cannot be undone.")) {
            await clearAllNotifications();
        }
    };

    const trigger = (
        <button
            className="relative flex h-10 w-10 items-center justify-center rounded-full text-ink hover:bg-sunk"
            aria-label={unreadCount > 0 ? `Notifications, ${unreadCount} unread` : "Notifications"}
        >
            <Bell className="h-5 w-5" />
            {unreadCount > 0 && (
                <span className="absolute right-1 top-1 h-2.5 w-2.5 rounded-full bg-accent ring-2 ring-paper" />
            )}
        </button>
    );

    return (
        <Dropdown trigger={trigger} panelClassName="w-80">
            <div className="flex items-center justify-between px-3 pb-2 pt-1">
                <h3 className="font-serif text-lg font-semibold text-ink">Notifications</h3>
                <div className="flex gap-3 text-[13px]">
                    {unreadCount > 0 && (
                        <button onClick={markAllAsRead} className="text-accent underline underline-offset-2 hover:text-accent-hover">
                            Mark all read
                        </button>
                    )}
                    <button
                        onClick={handleClearAll}
                        disabled={notifications.length === 0}
                        className="text-ink-2 underline underline-offset-2 hover:text-ink disabled:no-underline disabled:opacity-50"
                    >
                        Clear all
                    </button>
                </div>
            </div>

            <div className="border-t border-line">
                {loading && <p className="px-3 py-6 text-center text-sm text-ink-2">Loading...</p>}

                {!loading && notifications.length === 0 && (
                    <p className="px-3 py-8 text-center text-sm text-ink-2">Nothing new. We'll tell you when a book you wait for is free.</p>
                )}

                {!loading && notifications.map((notification) => (
                    <button
                        key={notification.id}
                        onClick={() => markAsRead(notification.id)}
                        className="flex w-full items-start gap-3 rounded-control px-3 py-3 text-left hover:bg-sunk"
                    >
                        <span
                            className={`mt-1.5 h-2 w-2 shrink-0 rounded-full ${notification.read ? "bg-transparent" : "bg-accent"}`}
                            aria-hidden="true"
                        />
                        <span className="min-w-0">
                            <span className={`block text-sm ${notification.read ? "text-ink-2" : "text-ink"}`}>
                                {notification.message}
                            </span>
                            <span className="mt-0.5 block text-[13px] text-ink-3">
                                {formatDistanceToNow(notification.createdAt, { addSuffix: true })}
                            </span>
                        </span>
                    </button>
                ))}
            </div>
        </Dropdown>
    );
};
