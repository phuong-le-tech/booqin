import type React from "react"
import { Link, useLocation } from "react-router-dom"
import { ShoppingCart } from "lucide-react"
import { useAuth } from "../context/AuthContext"
import { NotificationsMenu } from "../components/notifications/NotificationsMenu"
import { UserMenu } from "../components/UserMenu"

export const Navbar: React.FC = () => {
    const { user, isAuthenticated, logout } = useAuth()
    const location = useLocation()

    const getPageTitle = () => {
        switch (location.pathname) {
            case "/books":
                return "Books"
            case "/dashboard":
                return "Dashboard"
            case "/collections/user":
                return "My Collections"
            case "/collections/public":
                return "Public Collections"
            case "/following":
                return "Following"
            case "/cart":
                return "Shopping Cart"
            case "/profile":
                return "Profile"
            default:
                return ""
        }
    }

    const handleLogout = async () => {
        try {
            await logout()
        } catch (error) {
            console.error("Logout failed:", error)
        }
    }

    return (
        <header className="bg-navbar w-full">
            <div className="w-full h-16 flex justify-between items-center">
                <div className="pl-4 sm:pl-6 md:pl-8">
                    <span className="text-xl sm:text-2xl font-extrabold text-white hover:text-light-blue transition-colors duration-300">
                       {getPageTitle()}
                    </span>
                </div>
                <div className="flex items-center gap-2 sm:gap-4 md:gap-8 pr-4 sm:pr-6 md:pr-8 lg:pr-16">
                    {isAuthenticated && user != null ? (
                        <div className="flex items-center gap-2 sm:gap-4">
                            <UserMenu user={user} onLogout={handleLogout} />
                            <NotificationsMenu />
                        </div>
                    ) : (
                        <>
                            <Link to="/login" className="btn btn-primary text-sm sm:text-base inline-block px-2 py-1 sm:px-4 sm:py-2">
                                Login
                            </Link>
                            <Link to="/register" className="btn btn-primary text-sm sm:text-base inline-block px-2 py-1 sm:px-4 sm:py-2">
                                Register
                            </Link>
                        </>
                    )}
                    <Link to="/cart" className="text-white hover:text-blue-400">
                        <ShoppingCart className="h-5 w-5 sm:h-6 sm:w-6"/>
                    </Link>
                </div>
            </div>
        </header>
    )
}