import type React from "react"
import { User, Settings, LogOut } from "lucide-react"
import { Link } from "react-router-dom"
import { Dropdown } from "./Dropdown"
import { User as UserApp } from "../model/User"

interface UserMenuProps {
    user: UserApp,
    onLogout: () => void
}

export const UserMenu: React.FC<UserMenuProps> = ({ user, onLogout }) => {
    const trigger = (
        <button className="flex items-center gap-1 sm:gap-2 text-white hover:text-blue-400 focus:outline-none">
            <User className="h-5 w-5 sm:h-6 sm:w-6" />
            <span className="hidden sm:inline">{user.firstName}</span>
        </button>
    )

    return (
        <Dropdown trigger={trigger}>
            <div className="py-1">
                <div className="px-4 py-2 text-sm text-secondary border-b">
                    <div className="font-medium truncate">{`${user.firstName} ${user.lastName}`}</div>
                    <div className="text-xs text-secondary truncate">{user.email}</div>
                </div>
                <Link to="/profile" className="block px-4 py-2 text-sm text-secondary hover:bg-gray-500">
                    <User className="inline-block w-4 h-4 mr-2" />
                    Profile
                </Link>
                <Link to="/settings" className="block px-4 py-2 text-sm text-secondary hover:bg-gray-500">
                    <Settings className="inline-block w-4 h-4 mr-2" />
                    Settings
                </Link>
                <button onClick={onLogout} className="block w-full text-left px-4 py-2 text-sm text-secondary hover:bg-gray-500">
                    <LogOut className="inline-block w-4 h-4 mr-2" />
                    Logout
                </button>
            </div>
        </Dropdown>
    )
}