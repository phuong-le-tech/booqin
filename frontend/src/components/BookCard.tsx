import type React from "react"
import { Link } from "react-router-dom"
import type { Book } from "../model/Book"

interface BookCardProps extends Book {}

export const BookCard: React.FC<BookCardProps> = ({ id, title, author, rating }) => {
    const generateBackgroundColor = (title: string) => {
        let hash = 0
        for (let i = 0; i < title.length; i++) {
            hash = title.charCodeAt(i) + ((hash << 5) - hash)
        }
        const color = Math.floor(Math.abs((Math.sin(hash) * 16777215) % 1) * 16777215).toString(16)
        return "#" + "0".repeat(6 - color.length) + color
    }

    const bgColor = generateBackgroundColor(title)

    return (
        <div className="bg-secondary rounded-lg shadow-md overflow-hidden transition-transform duration-300 hover:scale-105">
            <Link to={`/books/${id}`}>
                <div
                    className="w-full h-48 flex items-center justify-center text-white text-2xl font-bold"
                    style={{ backgroundColor: bgColor }}
                >
                    {title.substring(0, 2).toUpperCase()}
                </div>
                <div className="p-4">
                    <h3 className="text-lg font-semibold text-primary mb-1">{title}</h3>
                    <p className="text-sm text-primary mb-2">{author}</p>
                    <div className="flex items-center">
                        <span className="text-yellow-400 mr-1">★</span>
                        <span className="text-sm text-primary">{rating.toFixed(1)}</span>
                    </div>
                </div>
            </Link>
        </div>
    )
}

