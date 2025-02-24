import type React from "react"
import {useState, useContext} from "react"
import {Link, useLocation} from "react-router-dom"
import {ShoppingCart} from "lucide-react"
import {AuthContext} from "../context/AuthContext.tsx"
import type {Notification} from "../model/Notification.ts"
import {NotificationsMenu} from "../components/NotificationsMenu.tsx"
import AuthService from "../services/AuthService.ts";
import {UserMenu} from "../components/UserMenu.tsx"

export const Navbar: React.FC = () => {
    const {user, setUser} = useContext(AuthContext)
    const [notifications, setNotifications] = useState<Notification[]>([
        {
            id: 1,
            message: "Nouveau livre ajouté à la collection",
            read: false,
            date: new Date(),
        },
        {
            id: 2,
            message: "Votre commande a été expédiée",
            read: false,
            date: new Date(),
        },
    ])

    const location = useLocation()

    const getPageTitle = () => {
        switch (location.pathname) {
            case "/books":
                return "Books"
            case "/dashboard":
                return "Dashboard"
            default:
                return ""
        }
    }
    const markAsRead = (id: number) => {
        setNotifications(notifications.map((n) => (n.id === id ? {...n, read: true} : n)))
    }

    const handleLogout = async () => {
        try {
            await AuthService.logout()
            setUser(null)
        } catch (error) {
            console.error("Logout failed:", error)
        }
    }

    return (
        <header className="bg-navbar w-full">
            <div className="w-full h-16 flex justify-between items-center">
                <div className="pl-8">

            <span className="text-2xl font-extrabold text-white hover:text-light-blue transition-colors duration-300">
               {getPageTitle()}
            </span>
                </div>
                <div className="flex items-center gap-8 pr-8 sm:pr-12 lg:pr-16">
                    {user ? (
                        <div className="flex items-center gap-4">
                            <UserMenu user={user} onLogout={handleLogout}/>
                            <NotificationsMenu notifications={notifications} markAsRead={markAsRead}/>
                        </div>
                    ) : (
                        <>
                            <Link to="/login" className="btn btn-primary inline-block px-4 py-2 text-lg">
                                Login
                            </Link>
                            <Link to="/register" className="btn btn-primary inline-block px-4 py-2 text-lg">
                                Register
                            </Link>
                        </>
                    )}
                    <Link to="/cart" className="text-white hover:text-blue-400">
                        <ShoppingCart className="h-6 w-6"/>
                    </Link>
                </div>
            </div>
        </header>
    )
}


