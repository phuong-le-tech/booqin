import type React from "react"
import { Bell } from "lucide-react"
import { Dropdown } from "./Dropdown"
import type { Notification } from "../model/Notification"

interface NotificationsMenuProps {
    notifications: Notification[]
    markAsRead: (id: number) => void
}

export const NotificationsMenu: React.FC<NotificationsMenuProps> = ({ notifications, markAsRead }) => {
    const unreadCount = notifications.filter((n) => !n.read).length

    const trigger = (
        <button className="relative p-2 text-white hover:text-blue-400 focus:outline-none">
            <Bell className="h-6 w-6" />
            {unreadCount > 0 && (
                <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center">
          {unreadCount}
        </span>
            )}
        </button>
    )

    return (
        <Dropdown trigger={trigger}>
            <div className="py-1">
                {notifications.length === 0 ? (
                    <div className="px-4 py-2 text-sm text-gray-700">Pas de notifications</div>
                ) : (
                    notifications.map((notification) => (
                        <div
                            key={notification.id}
                            onClick={() => markAsRead(notification.id)}
                            className={`px-4 py-2 text-sm text-gray-700 hover:bg-gray-500 cursor-pointer ${
                                notification.read ? "opacity-50" : ""
                            }`}
                        >
                            <div className="font-medium">{notification.message}</div>
                            <div className="text-xs text-gray-500">{notification.date.toLocaleString()}</div>
                        </div>
                    ))
                )}
            </div>
        </Dropdown>
    )
}

