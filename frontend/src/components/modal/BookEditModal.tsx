import React, { useState } from "react"
import { X } from "lucide-react"
import { Book, BookUpdate } from "../../model/Book"

interface BookEditModalProps {
    book: Book
    onClose: () => void
    onSave: (updatedBook: BookUpdate) => void
}

export const BookEditModal: React.FC<BookEditModalProps> = ({
                                                                book,
                                                                onClose,
                                                                onSave
                                                            }) => {
    const [formData, setFormData] = useState<BookUpdate>({
        title: book.title || "",
        description: book.description || "",
        publicationDate: book.publicationDate || "",
        isbns: book.isbns || []
    })

    const [errors, setErrors] = useState<{
        title?: string
        description?: string
        publicationDate?: string
        isbns?: string
    }>({})

    const validateForm = () => {
        const newErrors: {
            title?: string
            description?: string
            publicationDate?: string
            isbns?: string
        } = {}

        if (!formData.title?.trim()) {
            newErrors.title = "Title is required"
        }

        if (!formData.publicationDate?.trim()) {
            newErrors.publicationDate = "Published date is required"
        }

        if (!formData.isbns?.length) {
            newErrors.isbns = "At least one ISBN is required"
        }

        setErrors(newErrors)
        return Object.keys(newErrors).length === 0
    }

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value } = e.target
        setFormData(prev => ({
            ...prev,
            [name]: value
        }))
    }

    const handleIsbnChange = (e: React.ChangeEvent<HTMLInputElement>, index: number) => {
        const { value } = e.target
        const updatedIsbns = [...(formData.isbns || [])]
        updatedIsbns[index] = value

        setFormData(prev => ({
            ...prev,
            isbns: updatedIsbns
        }))
    }

    const addIsbnField = () => {
        setFormData(prev => ({
            ...prev,
            isbns: [...(prev.isbns || []), ""]
        }))
    }

    const removeIsbnField = (index: number) => {
        const updatedIsbns = [...(formData.isbns || [])]
        updatedIsbns.splice(index, 1)

        setFormData(prev => ({
            ...prev,
            isbns: updatedIsbns
        }))
    }

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault()

        if (validateForm()) {
            onSave(formData)
        }
    }

    return (
        <>
            {/* Modal Backdrop with reduced opacity and blur effect */}
            <div
                className="fixed inset-0 bg-black/50 backdrop-blur-[1px] z-50 flex items-center justify-center"
                onClick={onClose}
            >
                {/* Modal Content */}
                <div
                    className="bg-white rounded-lg shadow-lg max-w-md w-full p-6 max-h-[90vh] overflow-y-auto"
                    onClick={(e) => e.stopPropagation()}
                >
                    <div className="flex justify-between items-center mb-4">
                        <h3 className="text-xl font-semibold text-primary">Edit Book</h3>
                        <button
                            onClick={onClose}
                            className="text-gray-400 hover:text-gray-600"
                            aria-label="Close"
                        >
                            <X className="h-5 w-5" />
                        </button>
                    </div>

                    <form onSubmit={handleSubmit}>
                        <div className="space-y-4">
                            <div>
                                <label htmlFor="title" className="block text-sm font-medium text-gray-700 mb-1">
                                    Title
                                </label>
                                <input
                                    type="text"
                                    id="title"
                                    name="title"
                                    value={formData.title}
                                    onChange={handleChange}
                                    className={`w-full px-3 py-2 border rounded-md ${
                                        errors.title ? 'border-red-500' : 'border-gray-300'
                                    }`}
                                />
                                {errors.title && (
                                    <p className="mt-1 text-sm text-red-500">{errors.title}</p>
                                )}
                            </div>

                            <div>
                                <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-1">
                                    Description
                                </label>
                                <textarea
                                    id="description"
                                    name="description"
                                    rows={3}
                                    value={formData.description}
                                    onChange={handleChange}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md"
                                />
                            </div>

                            <div>
                                <label htmlFor="publicationDate" className="block text-sm font-medium text-gray-700 mb-1">
                                    Publication Date
                                </label>
                                <input
                                    type="date"
                                    id="publicationDate"
                                    name="publicationDate"
                                    value={formData.publicationDate}
                                    onChange={handleChange}
                                    className={`w-full px-3 py-2 border rounded-md ${
                                        errors.publicationDate ? 'border-red-500' : 'border-gray-300'
                                    }`}
                                />
                                {errors.publicationDate && (
                                    <p className="mt-1 text-sm text-red-500">{errors.publicationDate}</p>
                                )}
                            </div>

                            <div>
                                <div className="flex justify-between items-center mb-1">
                                    <label className="block text-sm font-medium text-gray-700">
                                        ISBNs
                                    </label>
                                    <button
                                        type="button"
                                        onClick={addIsbnField}
                                        className="text-sm text-blue-600 hover:text-blue-800"
                                    >
                                        + Add ISBN
                                    </button>
                                </div>

                                {formData.isbns && formData.isbns.map((isbn, index) => (
                                    <div key={index} className="flex items-center mb-2">
                                        <input
                                            type="text"
                                            value={isbn}
                                            onChange={(e) => handleIsbnChange(e, index)}
                                            placeholder="ISBN"
                                            className={`w-full px-3 py-2 border rounded-md ${
                                                errors.isbns ? 'border-red-500' : 'border-gray-300'
                                            }`}
                                        />
                                        {formData.isbns && formData.isbns.length > 1 && (
                                            <button
                                                type="button"
                                                onClick={() => removeIsbnField(index)}
                                                className="ml-2 text-red-600 hover:text-red-800"
                                            >
                                                <X className="h-4 w-4" />
                                            </button>
                                        )}
                                    </div>
                                ))}

                                {errors.isbns && (
                                    <p className="mt-1 text-sm text-red-500">{errors.isbns}</p>
                                )}
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Authors
                                </label>
                                <div className="px-3 py-2 border border-gray-300 rounded-md bg-gray-50">
                                    {book.authors && book.authors.length > 0 ? (
                                        book.authors.map((author, index) => (
                                            <div key={index} className="py-1">
                                                {author.firstName} {author.lastName}
                                            </div>
                                        ))
                                    ) : (
                                        <div className="text-gray-500">No authors assigned</div>
                                    )}
                                </div>
                                <p className="mt-1 text-xs text-gray-500">
                                    Author information cannot be modified here.
                                </p>
                            </div>

                            {book.categories?.length && (
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">
                                        Category
                                    </label>
                                    <div className="px-3 py-2 border border-gray-300 rounded-md bg-gray-50">
                                        <span className="inline-block px-2 py-1 bg-green-100 text-green-800 rounded-full text-xs">
                                            {book.categories?.join(", ")}
                                        </span>
                                    </div>
                                    <p className="mt-1 text-xs text-gray-500">
                                        Category information cannot be modified here.
                                    </p>
                                </div>
                            )}
                        </div>

                        <div className="flex justify-end mt-6 space-x-3">
                            <button
                                type="button"
                                onClick={onClose}
                                className="px-4 py-2 rounded bg-gray-200 text-gray-800 hover:bg-gray-300"
                            >
                                Cancel
                            </button>
                            <button
                                type="submit"
                                className="px-4 py-2 rounded bg-gray-200 text-gray-800 hover:bg-blue-400"
                            >
                                Save Changes
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </>
    )
}

export default BookEditModal