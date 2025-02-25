import type React from "react"
import { BookCard } from "./BookCard"
import type { Book } from "../model/Book"

interface BookGridProps {
    books: Book[]
}

export const BookGrid: React.FC<BookGridProps> = ({ books }) => {
    return (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
            {books.map((book) => (
                <BookCard key={book.id} {...book} />
            ))}
        </div>
    )
}

