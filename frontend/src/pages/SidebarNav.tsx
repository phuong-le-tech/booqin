import { Link, useLocation } from "react-router-dom"
import { LayoutDashboard, Book, LogOut, LibraryBigIcon, Library, Settings, Users } from "lucide-react"
import { useAuth } from "../context/AuthContext"

interface SidebarNavProps {
    onCloseSidebar?: () => void
}

const unauthenticatedNavItems = [
    {icon: Book, label: "Books", path: "/books", color: "text-green-300"},
    {icon: LibraryBigIcon, label: "Collections", path: "/collections/public", color: "text-purple-300"},
]

const authenticatedNavItems = [
    {icon: LayoutDashboard, label: "Dashboard", path: "/dashboard", color: "text-blue-300"},
    {icon: Book, label: "Books", path: "/books", color: "text-green-300"},
    {icon: LibraryBigIcon, label: "Collections", path: "/collections/public", color: "text-purple-300"},
    {icon: Library, label: "My Collections", path: "/collections/user", color: "text-yellow-300"},
    {icon: Users, label: "Following", path: "/following", color: "text-pink-300"},
]

export function SidebarNav({ onCloseSidebar = () => {} }: SidebarNavProps) {
    const { user, isAuthenticated, logout } = useAuth();
    const location = useLocation();

    const handleLogout = async () => {
        try {
            await logout();
            if (onCloseSidebar) {
                onCloseSidebar();
            }
        } catch (error) {
            console.error("Logout failed:", error);
        }
    }

    const handleNavClick = () => {
        if (onCloseSidebar) {
            onCloseSidebar();
        }
    }

    // Determine which navigation items to show
    const navItems = isAuthenticated ? authenticatedNavItems : unauthenticatedNavItems;
    const isAdmin = user?.role === "ADMIN";

    return (
        <div className="bg-navbar w-64 p-4 border-r flex flex-col h-full">
            <nav className="space-y-2 flex-1">
                <div className="mt-auto">
                    {navItems.map((item, index) => (
                        <Link
                            key={index}
                            to={item.path}
                            className={`sidebar-nav-item ${item.color} ${location.pathname === item.path ? 'bg-opacity-20 bg-white' : ''}`}
                            onClick={handleNavClick}
                        >
                            <item.icon className="mr-2 h-4 w-4"/>
                            {item.label}
                        </Link>
                    ))}

                    {/* Admin section */}
                    {isAdmin && (
                        <Link
                            to="/admin"
                            className={`sidebar-nav-item text-red-300 ${location.pathname === '/admin' ? 'bg-opacity-20 bg-white' : ''}`}
                            onClick={handleNavClick}
                        >
                            <Settings className="mr-2 h-4 w-4"/>
                            Admin Panel
                        </Link>
                    )}

                    {/* Logout button only for authenticated users */}
                    {isAuthenticated && (
                        <button
                            onClick={handleLogout}
                            className="sidebar-nav-item text-red-400"
                        >
                            <LogOut className="mr-2 h-4 w-4"/>
                            Logout
                        </button>
                    )}
                </div>
            </nav>
        </div>
    )
}