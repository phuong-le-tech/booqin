import {Link, useLocation} from "react-router-dom"
import {LayoutDashboard, Book, LogOut, LibraryBigIcon, Library} from "lucide-react"
import {useContext} from "react"
import {AuthContext} from "../context/AuthContext.tsx"
import AuthService from "../services/AuthService.ts"

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
    {icon: Library, label: "My Collections", path: "/collections", color: "text-yellow-300"},
]

export function SidebarNav({ onCloseSidebar = () => {} }: SidebarNavProps) {
    const {user, setUser} = useContext(AuthContext)
    const location = useLocation()

    const handleLogout = async () => {
        try {
            await AuthService.logout()
            setUser(null)
            if (onCloseSidebar) {
                onCloseSidebar()
            }
        } catch (error) {
            console.error("Logout failed:", error)
        }
    }

    const handleNavClick = () => {
        // Close sidebar on navigation (for mobile)
        if (onCloseSidebar) {
            onCloseSidebar()
        }
    }

    return (
        <div className="bg-navbar w-64 p-4 border-r flex flex-col h-full">
            <nav className="space-y-2 flex-1">
                <div className="mt-auto">
                    {user ? (
                        authenticatedNavItems.map((item, index) => (
                            <Link
                                key={index}
                                to={item.path}
                                className={`sidebar-nav-item ${item.color} ${location.pathname === item.path ? 'bg-opacity-20 bg-white' : ''}`}
                                onClick={handleNavClick}
                            >
                                <item.icon className="mr-2 h-4 w-4"/>
                                {item.label}
                            </Link>
                        ))
                    ) : (
                        unauthenticatedNavItems.map((item, index) => (
                            <Link
                                key={index}
                                to={item.path}
                                className={`sidebar-nav-item ${item.color} ${location.pathname === item.path ? 'bg-opacity-20 bg-white' : ''}`}
                                onClick={handleNavClick}
                            >
                                <item.icon className="mr-2 h-4 w-4"/>
                                {item.label}
                            </Link>
                        ))
                    )}
                    {user && (
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