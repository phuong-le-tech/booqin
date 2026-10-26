import type React from "react"
import type { ReactNode } from "react"
import { Link, NavLink, useLocation } from "react-router-dom"
import { BookOpen, Library, ShoppingBag, User, Users } from "lucide-react"
import { useAuth } from "../context/AuthContext"
import { useCart } from "../hooks/useCart"
import { NotificationsMenu } from "../components/notifications/NotificationsMenu"
import { UserMenu } from "../components/UserMenu"
import { DemoBanner } from "../components/demo/DemoBanner"

interface LayoutProps {
    children: ReactNode
}

const navLinkClass = (active: boolean) =>
    `relative flex h-[72px] items-center px-1 text-[15px] font-medium transition-colors ${
        active ? "text-ink" : "text-ink-2 hover:text-ink"
    }`

const TopBar: React.FC = () => {
    const { user, isAuthenticated, logout } = useAuth()
    const location = useLocation()
    const { count: cartCount } = useCart()

    const onBooks = location.pathname.startsWith("/books")
    const onCollections = location.pathname.startsWith("/collections")
    const onFollowing = location.pathname.startsWith("/following")

    const handleLogout = async () => {
        try {
            await logout()
        } catch (error) {
            console.error("Logout failed:", error)
        }
    }

    const underline = (active: boolean) =>
        active ? <span className="absolute inset-x-0 bottom-0 h-0.5 rounded-full bg-accent" /> : null

    return (
        <header className="sticky top-0 z-40 border-b border-line bg-paper/95 backdrop-blur-sm">
            <div className="mx-auto flex h-[72px] w-full max-w-[1200px] items-center gap-8 px-4 sm:px-8">
                <Link to="/" className="font-serif text-[22px] font-semibold tracking-tight text-ink">
                    BooqIn
                </Link>

                <nav className="hidden items-center gap-6 md:flex" aria-label="Main">
                    <NavLink to="/books" className={navLinkClass(onBooks)}>
                        Books{underline(onBooks)}
                    </NavLink>
                    <NavLink to="/collections/public" className={navLinkClass(onCollections)}>
                        Collections{underline(onCollections)}
                    </NavLink>
                    {isAuthenticated && (
                        <NavLink to="/following" className={navLinkClass(onFollowing)}>
                            Following{underline(onFollowing)}
                        </NavLink>
                    )}
                </nav>

                <div className="ml-auto flex items-center gap-1 sm:gap-2">
                    <Link
                        to="/cart"
                        data-cart-target
                        className="relative flex h-10 w-10 items-center justify-center rounded-full text-ink hover:bg-sunk"
                        aria-label={cartCount > 0 ? `Cart, ${cartCount} books` : "Cart"}
                    >
                        <ShoppingBag className="h-5 w-5" />
                        {cartCount > 0 && (
                            <span
                                key={cartCount}
                                className="badge-pop absolute -right-0.5 -top-0.5 flex h-5 min-w-5 items-center justify-center rounded-full bg-accent px-1 text-[13px] font-semibold leading-none text-white"
                            >
                                {cartCount}
                            </span>
                        )}
                    </Link>

                    {isAuthenticated && user != null ? (
                        <>
                            <NotificationsMenu />
                            <UserMenu user={user} onLogout={handleLogout} />
                        </>
                    ) : (
                        <>
                            <Link to="/login" className="hidden h-10 items-center rounded-control px-3 text-sm font-medium text-ink hover:bg-sunk sm:flex">
                                Log in
                            </Link>
                            <Link to="/register" className="btn-outline">
                                Sign up
                            </Link>
                        </>
                    )}
                </div>
            </div>
        </header>
    )
}

const MobileTabBar: React.FC = () => {
    const { isAuthenticated } = useAuth()

    const tab = ({ isActive }: { isActive: boolean }) =>
        `flex flex-1 flex-col items-center justify-center gap-0.5 text-[13px] font-medium ${
            isActive ? "text-accent" : "text-ink-2"
        }`

    return (
        <nav className="fixed inset-x-0 bottom-0 z-40 flex h-16 border-t border-line bg-paper md:hidden" aria-label="Main">
            <NavLink to="/books" className={tab}>
                <BookOpen className="h-5 w-5" /> Browse
            </NavLink>
            <NavLink to="/collections/public" className={tab}>
                <Library className="h-5 w-5" /> Collections
            </NavLink>
            {isAuthenticated && (
                <NavLink to="/following" className={tab}>
                    <Users className="h-5 w-5" /> Following
                </NavLink>
            )}
            <NavLink to={isAuthenticated ? "/profile" : "/login"} className={tab}>
                <User className="h-5 w-5" /> {isAuthenticated ? "Me" : "Log in"}
            </NavLink>
        </nav>
    )
}

export const Layout: React.FC<LayoutProps> = ({ children }) => {
    return (
        <div className="flex min-h-screen flex-col bg-paper">
            <DemoBanner />
            <TopBar />
            <main className="w-full flex-1 pb-20 md:pb-0">{children}</main>
            <MobileTabBar />
        </div>
    )
}

export default Layout
