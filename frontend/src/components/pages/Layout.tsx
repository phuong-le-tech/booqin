"use client"

import type React from "react"

import { Link } from "react-router-dom"
import { useContext } from "react"
import { AuthContext } from "../../context/AuthContext"
import type { ReactNode } from "react"
import AuthService from "../../services/AuthService"

interface LayoutProps {
    children: ReactNode
}

export const Layout: React.FC<LayoutProps> = ({ children }) => {
    const { user, setUser } = useContext(AuthContext)

    const handleLogout = async () => {
        try {
            await AuthService.logout()
            setUser(null)
        } catch (error) {
            console.error("Logout failed:", error)
        }
    }

    return (
        <div className="flex flex-col min-h-screen w-full" style={{ backgroundColor: "var(--background)" }}>
            <header className="w-full bg-white border-b" style={{ borderColor: "var(--border)" }}>
                <nav className="w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between h-16">
                        <div className="flex items-center">
                            <Link to="/" className="flex-shrink-0 flex items-center">
                <span className="text-2xl font-bold" style={{ color: "var(--primary)" }}>
                  BooqIn
                </span>
                            </Link>
                        </div>
                        <div className="flex items-center gap-4">
                            {user ? (
                                <>
                                    <span style={{ color: "var(--text)" }}>Hi, {user.firstName}!</span>
                                    <button onClick={handleLogout} className="btn btn-primary">
                                        Logout
                                    </button>
                                </>
                            ) : (
                                <>
                                    <Link
                                        to="/login"
                                        className="px-3 py-2 rounded-md text-sm font-medium transition-colors duration-300"
                                        style={{ color: "var(--text)" }}
                                    >
                                        Login
                                    </Link>
                                    <Link to="/register" className="btn btn-primary">
                                        Register
                                    </Link>
                                </>
                            )}
                        </div>
                    </div>
                </nav>
            </header>

            <main className="flex-grow w-full">{children}</main>

            <footer
                className="w-full py-8 text-center"
                style={{ backgroundColor: "var(--text)", color: "var(--background)" }}
            >
                <div className="w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <p className="text-sm opacity-80">&copy; {new Date().getFullYear()} BooqIn. All rights reserved.</p>
                    <div className="mt-4 space-x-6">
                        <Link to="/about" className="text-sm opacity-80 hover:opacity-100 transition-opacity duration-300">
                            About
                        </Link>
                        <Link to="/contact" className="text-sm opacity-80 hover:opacity-100 transition-opacity duration-300">
                            Contact
                        </Link>
                        <Link to="/privacy" className="text-sm opacity-80 hover:opacity-100 transition-opacity duration-300">
                            Privacy Policy
                        </Link>
                    </div>
                </div>
            </footer>
        </div>
    )
}

export default Layout;