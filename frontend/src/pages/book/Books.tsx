import type React from "react"
import { useState, useEffect } from "react"
import { BookGrid } from "../../components/BookGrid"
import { Layout } from "../Layout"
import type { Book } from "../../model/Book"

const fetchBooks = async (): Promise<Book[]> => {
    // Ici, vous feriez normalement un appel API
    return [
        { id: "1", title: "The Great Gatsby", author: "F. Scott Fitzgerald", rating: 4.5 },
        { id: "2", title: "To Kill a Mockingbird", author: "Harper Lee", rating: 4.8 },
        { id: "3", title: "1984", author: "George Orwell", rating: 4.6 },
    ]
}

export const Books: React.FC = () => {
    const [books, setBooks] = useState<Book[]>([])

    useEffect(() => {
        const getBooks = async () => {
            const fetchedBooks = await fetchBooks()
            setBooks(fetchedBooks)
        }
        getBooks()
    }, [])

    return (
        <Layout>
            <div className="container mx-auto px-4 py-8">
                <BookGrid books={books} />
            </div>
        </Layout>
    )
}

