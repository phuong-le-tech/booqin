import React, { useState } from "react"
import { X } from "lucide-react"

interface Collection {
    id: number
    name: string
    description: string
    category: string
    createdBy: string
    bookCount: number
    isPublic: boolean
}

interface CollectionEditModalProps {
    collection: Collection
    onClose: () => void
    onSave: (updatedCollection: Partial<Collection>) => void
}

export const CollectionEditModal: React.FC<CollectionEditModalProps> = ({
                                                                            collection,
                                                                            onClose,
                                                                            onSave
                                                                        }) => {
    const [formData, setFormData] = useState({
        name: collection.name || "",
        description: collection.description || "",
        category: collection.category || "",
        isPublic: collection.isPublic
    })

    const [errors, setErrors] = useState<{
        name?: string
        description?: string
        category?: string
    }>({})

    const validateForm = () => {
        const newErrors: {
            name?: string
            description?: string
            category?: string
        } = {}

        if (!formData.name.trim()) {
            newErrors.name = "Collection name is required"
        }

        if (!formData.description.trim()) {
            newErrors.description = "Description is required"
        }

        if (!formData.category.trim()) {
            newErrors.category = "Category is required"
        }

        setErrors(newErrors)
        return Object.keys(newErrors).length === 0
    }

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
        const { name, value, type } = e.target as HTMLInputElement
        setFormData(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? (e.target as HTMLInputElement).checked : value
        }))
    }

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault()

        if (validateForm()) {
            onSave(formData)
        }
    }

    // Common categories for collections
    const categories = [
        "Fiction",
        "Non-Fiction",
        "Academic",
        "Educational",
        "Mystery",
        "Fantasy",
        "Sci-Fi",
        "Romance",
        "Historical",
        "Biography",
        "Self-Help",
        "Business",
        "Technology",
        "Art",
        "Philosophy",
        "Children's"
    ]

    return (
        <>
            {/* Modal Backdrop */}
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
                        <h3 className="text-xl font-semibold text-primary">Edit Collection</h3>
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
                                <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-1">
                                    Collection Name
                                </label>
                                <input
                                    type="text"
                                    id="name"
                                    name="name"
                                    value={formData.name}
                                    onChange={handleChange}
                                    className={`w-full px-3 py-2 border rounded-md ${
                                        errors.name ? 'border-red-500' : 'border-gray-300'
                                    }`}
                                />
                                {errors.name && (
                                    <p className="mt-1 text-sm text-red-500">{errors.name}</p>
                                )}
                            </div>

                            <div>
                                <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-1">
                                    Description
                                </label>
                                <textarea
                                    id="description"
                                    name="description"
                                    rows={4}
                                    value={formData.description}
                                    onChange={handleChange}
                                    className={`w-full px-3 py-2 border rounded-md ${
                                        errors.description ? 'border-red-500' : 'border-gray-300'
                                    }`}
                                />
                                {errors.description && (
                                    <p className="mt-1 text-sm text-red-500">{errors.description}</p>
                                )}
                            </div>

                            <div>
                                <label htmlFor="category" className="block text-sm font-medium text-gray-700 mb-1">
                                    Category
                                </label>
                                <select
                                    id="category"
                                    name="category"
                                    value={formData.category}
                                    onChange={handleChange}
                                    className={`w-full px-3 py-2 border rounded-md ${
                                        errors.category ? 'border-red-500' : 'border-gray-300'
                                    }`}
                                >
                                    <option value="" disabled>Select a category</option>
                                    {categories.map((cat) => (
                                        <option key={cat} value={cat}>
                                            {cat}
                                        </option>
                                    ))}
                                </select>
                                {errors.category && (
                                    <p className="mt-1 text-sm text-red-500">{errors.category}</p>
                                )}
                            </div>

                            <div className="flex items-center">
                                <input
                                    type="checkbox"
                                    id="isPublic"
                                    name="isPublic"
                                    checked={formData.isPublic}
                                    onChange={handleChange}
                                    className="h-4 w-4 text-primary focus:ring-primary border-gray-300 rounded"
                                />
                                <label htmlFor="isPublic" className="ml-2 block text-sm text-gray-700">
                                    Make this collection public
                                </label>
                            </div>
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

export default CollectionEditModal