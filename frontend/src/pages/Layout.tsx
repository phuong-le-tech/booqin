import type React from "react"
import { Link } from "react-router-dom"
import type { ReactNode } from "react"
import { SidebarNav } from "./SidebarNav.tsx"
import { Navbar } from "./Navbar.tsx"
import { BookOpen, Menu, X } from "lucide-react"
import { useState, useEffect } from "react"

interface LayoutProps {
    children: ReactNode
}

export const Layout: React.FC<LayoutProps> = ({ children }) => {
    const [sidebarOpen, setSidebarOpen] = useState(false)
    const [isMobile, setIsMobile] = useState(window.innerWidth < 768)

    useEffect(() => {
        const handleResize = () => {
            setIsMobile(window.innerWidth < 768)
            if (window.innerWidth >= 768) {
                setSidebarOpen(false)
            }
        }

        window.addEventListener('resize', handleResize)
        return () => window.removeEventListener('resize', handleResize)
    }, [])

    return (
        <div className="flex flex-col h-screen">
            <div className="flex">
                <div className="bg-navbar w-full md:w-64 h-16 flex items-center justify-between md:justify-center border-b md:border-r">
                    <Link
                        to="/"
                        className="text-3xl md:text-4xl font-extrabold text-white hover:text-light-blue transition-colors duration-300 ml-4 md:ml-0"
                    >
                        <BookOpen className="inline-block h-6 w-6 md:h-8 md:w-8 mr-2"/>
                        BooqIn
                    </Link>
                    <button
                        className="md:hidden mr-4 text-white focus:outline-none"
                        onClick={() => setSidebarOpen(!sidebarOpen)}
                    >
                        {sidebarOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
                    </button>
                </div>
                <div className="hidden md:block md:flex-1">
                    <Navbar />
                </div>
            </div>
            <div className="md:hidden">
                <Navbar />
            </div>
            <div className="flex flex-1 overflow-hidden">
                {/* Mobile sidebar (overlay) */}
                {isMobile && sidebarOpen && (
                    <div
                        className="fixed inset-0 bg-black bg-opacity-50 z-40"
                        onClick={() => setSidebarOpen(false)}
                    />
                )}
                <div
                    className={`fixed md:relative z-50 md:z-auto h-screen w-64 transform transition-transform duration-300 ease-in-out md:transform-none ${
                        sidebarOpen ? 'translate-x-0' : '-translate-x-full md:translate-x-0'
                    }`}
                >
                    <SidebarNav onCloseSidebar={() => setSidebarOpen(false)} />
                </div>
                <main className="flex-1 overflow-x-hidden overflow-y-auto bg-gray-100">{children}</main>
            </div>
        </div>
    )
}

export default Layout