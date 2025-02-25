import {Link} from "react-router-dom"
import {LayoutDashboard, Book, LogOut, LibraryBigIcon, Library} from "lucide-react"
import {useContext} from "react"
import {AuthContext} from "../context/AuthContext.tsx"
import AuthService from "../services/AuthService.ts"

const unauthenticatedNavItems = [
    {icon: Book, label: "Books", path: "/books", color: "text-green-300"},
    {icon: LibraryBigIcon, label: "Collections", path: "/collection", color: "text-purple-300"},
]

const authenticatedNavItems = [
    {icon: LayoutDashboard, label: "Dashboard", path: "/dashboard", color: "text-blue-300"},
    {icon: Book, label: "Books", path: "/books", color: "text-green-300"},
    {icon: LibraryBigIcon, label: "Collections", path: "/collection", color: "text-purple-300"},
    {icon: Library, label: "My Collections", path: "/my-collection", color: "text-yellow-300"},
]

export function SidebarNav() {
    const {user, setUser} = useContext(AuthContext)

    const handleLogout = async () => {
        try {
            await AuthService.logout()
            setUser(null)
        } catch (error) {
            console.error("Logout failed:", error)
        }
    }

    return (
        <div className="bg-navbar w-64 p-4 border-r flex flex-col">
            <nav className="space-y-2 flex-1">
                <div className="mt-auto">
                    {user ? (
                        authenticatedNavItems.map((item, index) => (
                            <Link
                                key={index}
                                to={item.path}
                                className={`sidebar-nav-item ${item.color}`}
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
                                className={`sidebar-nav-item ${item.color}`}
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

