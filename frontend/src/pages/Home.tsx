import type React from "react"
import { useEffect, useState } from "react"
import { Link } from "react-router-dom"
import { ArrowRight } from "lucide-react"
import { useAuth } from "../context/AuthContext"
import { Layout } from "./Layout.tsx"
import { BookCover } from "../components/book/BookCover"
import BookService from "../services/BookService"
import type { BookIndex } from "../model/Book"

const SHELF_SIZE = 6

export const Home: React.FC = () => {
    const { user } = useAuth()
    const [shelf, setShelf] = useState<BookIndex[]>([])
    const [shelfLoading, setShelfLoading] = useState(true)

    useEffect(() => {
        // Newest books first
        BookService.getBooks(0, SHELF_SIZE, "id", "desc")
            .then(response => setShelf(Array.isArray(response?.content) ? response.content : []))
            .catch(error => console.error("Failed to load the shelf:", error))
            .finally(() => setShelfLoading(false))
    }, [])

    return (
        <Layout>
            <div className="mx-auto w-full max-w-[1200px] px-4 sm:px-8">
                <section className="max-w-[720px] pb-16 pt-14 sm:pb-20 sm:pt-24">
                    <h1 className="font-serif text-[36px] font-semibold leading-[1.08] tracking-[-0.02em] text-ink sm:text-[56px]">
                        {user ? `Welcome back, ${user.firstName}.` : "Books are better when they travel."}
                    </h1>
                    <p className="mt-5 max-w-[540px] text-lg leading-7 text-ink-2">
                        {user
                            ? "Pick up where you left off, or find your next read on someone else's shelf."
                            : "BooqIn lets you borrow books from other readers, and lend your own. Find a title, see who has it, and have it delivered."}
                    </p>
                    <div className="mt-8 flex flex-wrap items-center gap-3">
                        <Link to="/books" className="btn-accent">Browse books</Link>
                        {user ? (
                            <Link to="/dashboard" className="btn-outline h-11">My lendings</Link>
                        ) : (
                            <Link to="/register" className="btn-outline h-11">Create an account</Link>
                        )}
                    </div>
                </section>

                <section className="pb-20" aria-labelledby="shelf-heading">
                    <div className="mb-6 flex items-baseline justify-between">
                        <h2 id="shelf-heading" className="text-xl font-semibold tracking-[-0.01em] text-ink">New on the shelves</h2>
                        <Link to="/books" className="inline-flex items-center gap-1 text-sm font-medium text-ink-2 hover:text-ink">
                            See all <ArrowRight className="h-4 w-4" aria-hidden="true" />
                        </Link>
                    </div>

                    <ul className="grid grid-cols-2 gap-x-6 gap-y-8 sm:grid-cols-3 lg:grid-cols-6">
                        {shelfLoading
                            ? Array.from({ length: SHELF_SIZE }, (_, i) => (
                                <li key={i} aria-hidden="true">
                                    <div className="skeleton aspect-[2/3] w-full rounded-cover"></div>
                                    <div className="skeleton mt-3 h-4 w-4/5 rounded-full"></div>
                                </li>
                            ))
                            : shelf.map(book => (
                                <li key={book.id} className="book-card">
                                    <Link to={`/books/${book.id}`} className="block rounded-cover" aria-label={book.title}>
                                        <div className="cover-lift rounded-cover shadow-1">
                                            <BookCover
                                                title={book.title}
                                                authors={book.authorNames}
                                                src={book.thumbnailPath ? `/api/v1/books/images/${book.thumbnailPath}` : null}
                                            />
                                        </div>
                                    </Link>
                                    <Link to={`/books/${book.id}`} className="mt-3 font-serif text-[15px] font-semibold leading-5 text-ink line-clamp-2 hover:text-accent">
                                        {book.title}
                                    </Link>
                                    <p className="mt-0.5 text-[13px] text-ink-2 line-clamp-1">{book.authorNames?.join(", ")}</p>
                                </li>
                            ))}
                    </ul>

                    {!shelfLoading && shelf.length === 0 && (
                        <p className="py-10 text-ink-2">No books on the shelves yet.</p>
                    )}
                </section>
            </div>
        </Layout>
    )
}
