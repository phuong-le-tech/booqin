import type React from "react"
import { LayoutDashboard, Library, LogOut, Settings, User } from "lucide-react"
import { Link } from "react-router-dom"
import { Dropdown } from "./Dropdown"
import { User as UserApp } from "../model/User"

interface UserMenuProps {
    user: UserApp,
    onLogout: () => void
}

const itemClass =
    "flex w-full items-center gap-3 rounded-control px-3 py-2.5 text-left text-sm text-ink hover:bg-sunk"

export const UserMenu: React.FC<UserMenuProps> = ({ user, onLogout }) => {
    const initials = `${user.firstName?.[0] ?? ""}${user.lastName?.[0] ?? ""}`.toUpperCase()

    const trigger = (
        <button
            className="flex h-10 w-10 items-center justify-center rounded-full bg-ink text-sm font-semibold text-paper hover:bg-ink-2"
            aria-label="Open account menu"
            aria-haspopup="menu"
        >
            {initials || <User className="h-5 w-5" />}
        </button>
    )

    return (
        <Dropdown trigger={trigger}>
            {(close) => (
                <div role="menu">
                    <div className="px-3 pb-3 pt-2">
                        <div className="truncate font-medium text-ink">{`${user.firstName} ${user.lastName}`}</div>
                        <div className="truncate text-[13px] text-ink-2">{user.email}</div>
                    </div>
                    <div className="border-t border-line pt-2">
                        <Link to="/dashboard" onClick={close} className={itemClass} role="menuitem">
                            <LayoutDashboard className="h-4 w-4 text-ink-2" /> My lendings
                        </Link>
                        <Link to="/collections/user" onClick={close} className={itemClass} role="menuitem">
                            <Library className="h-4 w-4 text-ink-2" /> My collections
                        </Link>
                        <Link to="/profile" onClick={close} className={itemClass} role="menuitem">
                            <User className="h-4 w-4 text-ink-2" /> Profile
                        </Link>
                        {user.role === "ADMIN" && (
                            <Link to="/admin" onClick={close} className={itemClass} role="menuitem">
                                <Settings className="h-4 w-4 text-ink-2" /> Admin
                            </Link>
                        )}
                    </div>
                    <div className="mt-2 border-t border-line pt-2">
                        <button onClick={() => { close(); onLogout() }} className={itemClass} role="menuitem">
                            <LogOut className="h-4 w-4 text-ink-2" /> Log out
                        </button>
                    </div>
                </div>
            )}
        </Dropdown>
    )
}
