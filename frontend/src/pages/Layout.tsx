import type React from "react"
import { Link } from "react-router-dom"
import type { ReactNode } from "react"
import { SidebarNav } from "./SidebarNav.tsx"
import { Navbar } from "./Navbar.tsx"
import {BookOpen} from "lucide-react";

interface LayoutProps {
    children: ReactNode
}

export const Layout: React.FC<LayoutProps> = ({ children }) => {
    return (
        <div className="flex flex-col h-screen">
            <div className="flex">
                <div className="bg-navbar w-64 h-16 flex items-center justify-center border-r border-b">
                    <Link
                        to="/"
                        className="text-4xl font-extrabold text-white hover:text-light-blue transition-colors duration-300"
                    >
                        <BookOpen className="inline-block h-8 w-8 mr-2"/>
                        BooqIn
                    </Link>
                </div>
                <div className="flex-1">
                    <Navbar />
                </div>
            </div>
            <div className="flex flex-1 overflow-hidden">
                <SidebarNav />
                <main className="flex-1 overflow-x-hidden overflow-y-auto bg-gray-100">{children}</main>
            </div>
        </div>
    )
}

export default Layout

