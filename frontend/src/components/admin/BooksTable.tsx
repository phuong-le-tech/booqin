import React, { useState, useEffect } from "react"
import { Edit, Trash2, Search, X } from "lucide-react"
import { BookEditModal } from "../modal/BookEditModal"
import { DeleteConfirmModal } from "../modal/DeleteConfirmModal"
import { PageInfo } from "../../model/Pagination"
import { Book, BookIndex, BookUpdate } from "../../model/Book"
import { Pagination } from "../Pagination"
import BookService from "../../services/BookService"

// Extended interface to add display properties
interface BookForDisplay extends BookIndex {
    collection?: string;  // Using collection instead of category
    displayIsbn?: string; // Single ISBN for display (renamed to avoid confusion)
}

export const BooksTable: React.FC = () => {
    const [books, setBooks] = useState<BookForDisplay[]>([])
    const [loading, setLoading] = useState<boolean>(true)
    const [error, setError] = useState<string | null>(null)
    const [searchTerm, setSearchTerm] = useState<string>("")
    const [currentPage, setCurrentPage] = useState<number>(0)
    const [pageInfo, setPageInfo] = useState<PageInfo>({
        size: 10,
        number: 0,
        totalElements: 0,
        totalPages: 0
    })
    const [editingBook, setEditingBook] = useState<Book | null>(null)
    const [bookToDelete, setBookToDelete] = useState<BookIndex | null>(null)

    const fetchBooks = async (page: number = 0) => {
        setLoading(true)
        try {
            const response = await BookService.getBooks(
                page,
                pageInfo.size,
                'id',
                'asc',
                {}
            )

            if (response) {

                // Map BookIndex to BookForDisplay with additional display info
                setBooks(response.content.map(book => {
                    return {
                        ...book,
                        // Default collection name if there's no data
                        collection: "Uncategorized",
                        // For ISBN display in the table - show first 3 ISBNs
                        displayIsbn: book.isbns && book.isbns.length > 0
                            ? book.isbns.slice(0, 3).join(", ")
                            : "-"
                    }
                }))
                setPageInfo(response.page)
            } else {
                setError("Failed to fetch books")
            }
        } catch (err) {
            setError("Error fetching books. Please try again.")
            console.error(err)
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        fetchBooks(currentPage)
    }, [currentPage])

    const handlePageChange = (page: number) => {
        setCurrentPage(page)
    }

    const handleEdit = async (book: BookIndex) => {
        try {
            setLoading(true)
            // Fetch the full book details for editing
            const bookDetails = await BookService.getBookById(book.id)
            setEditingBook(bookDetails || null)
        } catch (err) {
            console.error("Failed to fetch book details:", err)
            setError("Failed to fetch book details for editing.")
        } finally {
            setLoading(false)
        }
    }

    const handleDelete = (book: BookIndex) => {
        setBookToDelete(book)
    }

    const confirmDelete = async () => {
        if (!bookToDelete) return

        try {
            await BookService.deleteBook(bookToDelete.id)

            // Refresh the list
            fetchBooks(currentPage)
            setBookToDelete(null)
        } catch (err) {
            console.error("Failed to delete book:", err)
            setError("Failed to delete book. Please try again.")
        }
    }

    const handleUpdateBook = async (updatedBook: BookUpdate) => {
        if (!editingBook) return

        try {
            await BookService.patchBook(editingBook.id, updatedBook)

            // Refresh the list
            fetchBooks(currentPage)
            setEditingBook(null)
        } catch (err) {
            console.error("Failed to update book:", err)
            setError("Failed to update book. Please try again.")
        }
    }

    const filteredBooks = books.filter(book =>
        book.title?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        book.authorNames?.join(" ").toLowerCase().includes(searchTerm.toLowerCase()) ||
        (book.isbns && book.isbns.some(isbn => isbn.toLowerCase().includes(searchTerm.toLowerCase()))) ||
        (book.collection && book.collection.toLowerCase().includes(searchTerm.toLowerCase()))
    )

    if (loading && books.length === 0) {
        return <div className="flex justify-center p-8">Loading...</div>
    }

    return (
        <div>
            <div className="mb-4 flex flex-col sm:flex-row items-center justify-between gap-4">
                <h2 className="text-xl font-semibold">Books Management</h2>

                <div className="relative w-full sm:w-64">
                    <input
                        type="text"
                        placeholder="Search books..."
                        className="pl-10 pr-10 py-2 border rounded-md w-full"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                    <Search className="absolute left-3 top-2.5 h-4 w-4 text-gray-400" />
                    {searchTerm && (
                        <button
                            onClick={() => setSearchTerm("")}
                            className="absolute right-3 top-2.5"
                        >
                            <X className="h-4 w-4 text-gray-400" />
                        </button>
                    )}
                </div>
            </div>

            {error && (
                <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4">
                    {error}
                    <button
                        className="absolute top-0 bottom-0 right-0 px-4"
                        onClick={() => setError(null)}
                    >
                        <X className="h-4 w-4" />
                    </button>
                </div>
            )}

            <div className="overflow-x-auto">
                <table className="min-w-full bg-white border rounded-lg">
                    <thead className="bg-gray-50">
                    <tr>
                        <th className="py-2 px-4 border-b text-left">ID</th>
                        <th className="py-2 px-4 border-b text-left">Title</th>
                        <th className="py-2 px-4 border-b text-left">Author</th>
                        <th className="py-2 px-4 border-b text-left">ISBN</th>
                        <th className="py-2 px-4 border-b text-left">Publication Date</th>
                        <th className="py-2 px-4 border-b text-left">Collection</th>
                        <th className="py-2 px-4 border-b text-center">Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    {filteredBooks.length > 0 ? (
                        filteredBooks.map((book) => (
                            <tr key={book.id} className="hover:bg-gray-50">
                                <td className="py-2 px-4 border-b">{book.id}</td>
                                <td className="py-2 px-4 border-b">{book.title}</td>
                                <td className="py-2 px-4 border-b">{book.authorNames?.join(", ")}</td>
                                <td className="py-2 px-4 border-b">{book.displayIsbn}</td>
                                <td className="py-2 px-4 border-b">{book.publicationDate}</td>
                                <td className="py-2 px-4 border-b">
                                    {book.collection && (
                                        <span className="px-2 py-1 bg-green-100 text-green-800 rounded-full text-xs">
                                            {book.collection}
                                        </span>
                                    )}
                                </td>
                                <td className="py-2 px-4 border-b text-center">
                                    <div className="flex justify-center space-x-2">
                                        <button
                                            onClick={() => handleEdit(book)}
                                            className="p-1 text-blue-600 hover:text-blue-800"
                                            title="Edit book"
                                        >
                                            <Edit className="h-4 w-4" />
                                        </button>
                                        <button
                                            onClick={() => handleDelete(book)}
                                            className="p-1 text-red-600 hover:text-red-800"
                                            title="Delete book"
                                        >
                                            <Trash2 className="h-4 w-4" />
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))
                    ) : (
                        <tr>
                            <td colSpan={7} className="py-4 text-center text-gray-500">
                                {searchTerm ? "No books found matching your search" : "No books found"}
                            </td>
                        </tr>
                    )}
                    </tbody>
                </table>
            </div>

            {/* Pagination */}
            {!searchTerm && pageInfo.totalPages > 1 && (
                <div className="mt-4">
                    <div className="text-sm text-gray-700 mb-4">
                        Showing page {pageInfo.number + 1} of {pageInfo.totalPages} ({pageInfo.totalElements} total books)
                    </div>
                    <Pagination pageInfo={pageInfo} onPageChange={handlePageChange} />
                </div>
            )}

            {/* Edit Modal */}
            {editingBook && (
                <BookEditModal
                    book={editingBook}
                    onClose={() => setEditingBook(null)}
                    onSave={handleUpdateBook}
                />
            )}

            {/* Delete Confirmation Modal */}
            {bookToDelete && (
                <DeleteConfirmModal
                    title="Delete Book"
                    message={`Are you sure you want to delete the book "${bookToDelete.title}"? This action cannot be undone.`}
                    onConfirm={confirmDelete}
                    onCancel={() => setBookToDelete(null)}
                />
            )}
        </div>
    )
}