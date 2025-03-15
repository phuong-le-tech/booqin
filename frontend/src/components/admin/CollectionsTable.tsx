import React, { useState, useEffect } from "react"
import { Edit, Trash2, Search, X, Book } from "lucide-react"
import { CollectionEditModal } from "../modal/CollectionEditModal"
import { DeleteConfirmModal } from "../modal/DeleteConfirmModal"
import { PageInfo } from "../../model/Pagination"

interface Collection {
    id: number
    title: string
    description: string
    category: string
    createdBy: string
    bookCount: number
    isPublic: boolean
}

interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: {
        content: T[];
        page: PageInfo;
    };
}

export const CollectionsTable: React.FC = () => {
    const [collections, setCollections] = useState<Collection[]>([])
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
    const [editingCollection, setEditingCollection] = useState<Collection | null>(null)
    const [collectionToDelete, setCollectionToDelete] = useState<Collection | null>(null)

    const fetchCollections = async (page: number = 0) => {
        setLoading(true)
        try {
            const response = await fetch(`/api/v1/collections?page=${page}&size=${pageInfo.size}&sortField=id&sortDirection=asc`)
            const data = await response.json() as ApiResponse<Collection>

            if (data.success) {
                setCollections(data.data.content)
                setPageInfo(data.data.page)
            } else {
                setError(data.message || "Failed to fetch collections")
            }
        } catch (err) {
            setError("Error fetching collections. Please try again.")
            console.error(err)
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        fetchCollections(currentPage)
    }, [currentPage])

    const handleEdit = (collection: Collection) => {
        setEditingCollection(collection)
    }

    const handleDelete = (collection: Collection) => {
        setCollectionToDelete(collection)
    }

    const confirmDelete = async () => {
        if (!collectionToDelete) return

        try {
            // Implement collection deletion API call
            await fetch(`/api/v1/collections/${collectionToDelete.id}`, {
                method: "DELETE",
            })

            // Refresh the list
            fetchCollections(currentPage)
            setCollectionToDelete(null)
        } catch (err) {
            console.error("Failed to delete collection:", err)
            setError("Failed to delete collection. Please try again.")
        }
    }

    const handleUpdateCollection = async (updatedCollection: Partial<Collection>) => {
        if (!editingCollection) return

        try {
            // Implement collection update API call
            await fetch(`/api/v1/collections/${editingCollection.id}`, {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(updatedCollection),
            })

            // Refresh the list
            fetchCollections(currentPage)
            setEditingCollection(null)
        } catch (err) {
            console.error("Failed to update collection:", err)
            setError("Failed to update collection. Please try again.")
        }
    }

    const filteredCollections = collections.filter(collection =>
        collection.title?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        collection.description?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        collection.category?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        collection.createdBy?.toLowerCase().includes(searchTerm.toLowerCase())
    )

    if (loading && collections.length === 0) {
        return <div className="flex justify-center p-8">Loading...</div>
    }

    return (
        <div>
            <div className="mb-4 flex flex-col sm:flex-row items-center justify-between gap-4">
                <h2 className="text-xl font-semibold">Collections Management</h2>

                <div className="relative w-full sm:w-64">
                    <input
                        type="text"
                        placeholder="Search collections..."
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
                        <th className="py-2 px-4 border-b text-left">Name</th>
                        <th className="py-2 px-4 border-b text-left">Description</th>
                        <th className="py-2 px-4 border-b text-left">Category</th>
                        <th className="py-2 px-4 border-b text-left">Created By</th>
                        <th className="py-2 px-4 border-b text-left">Books</th>
                        <th className="py-2 px-4 border-b text-left">Status</th>
                        <th className="py-2 px-4 border-b text-center">Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    {filteredCollections.length > 0 ? (
                        filteredCollections.map((collection) => (
                            <tr key={collection.id} className="hover:bg-gray-50">
                                <td className="py-2 px-4 border-b">{collection.id}</td>
                                <td className="py-2 px-4 border-b">{collection.title}</td>
                                <td className="py-2 px-4 border-b">
                                    {collection.description.length > 50
                                        ? `${collection.description.substring(0, 50)}...`
                                        : collection.description}
                                </td>
                                <td className="py-2 px-4 border-b">
                                        <span className="px-2 py-1 bg-purple-100 text-purple-800 rounded-full text-xs">
                                            {collection.category}
                                        </span>
                                </td>
                                <td className="py-2 px-4 border-b">{collection.createdBy}</td>
                                <td className="py-2 px-4 border-b">
                                    <div className="flex items-center">
                                        <Book className="h-4 w-4 mr-1 text-blue-500" />
                                        <span>{collection.bookCount}</span>
                                    </div>
                                </td>
                                <td className="py-2 px-4 border-b">
                                        <span className={`px-2 py-1 rounded-full text-xs ${
                                            collection.isPublic
                                                ? 'bg-green-100 text-green-800'
                                                : 'bg-gray-100 text-gray-800'
                                        }`}>
                                            {collection.isPublic ? 'Public' : 'Private'}
                                        </span>
                                </td>
                                <td className="py-2 px-4 border-b text-center">
                                    <div className="flex justify-center space-x-2">
                                        <button
                                            onClick={() => handleEdit(collection)}
                                            className="p-1 text-blue-600 hover:text-blue-800"
                                            title="Edit collection"
                                        >
                                            <Edit className="h-4 w-4" />
                                        </button>
                                        <button
                                            onClick={() => handleDelete(collection)}
                                            className="p-1 text-red-600 hover:text-red-800"
                                            title="Delete collection"
                                        >
                                            <Trash2 className="h-4 w-4" />
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))
                    ) : (
                        <tr>
                            <td colSpan={8} className="py-4 text-center text-gray-500">
                                {searchTerm ? "No collections found matching your search" : "No collections found"}
                            </td>
                        </tr>
                    )}
                    </tbody>
                </table>
            </div>

            {/* Pagination */}
            {!searchTerm && pageInfo.totalPages > 1 && (
                <div className="flex justify-between items-center mt-4">
                    <div>
                        Showing page {pageInfo.number + 1} of {pageInfo.totalPages} ({pageInfo.totalElements} total collections)
                    </div>
                    <div className="flex space-x-2">
                        <button
                            onClick={() => setCurrentPage(p => Math.max(0, p - 1))}
                            disabled={pageInfo.number === 0}
                            className={`px-3 py-1 rounded ${
                                pageInfo.number === 0
                                    ? 'bg-gray-200 text-gray-500 cursor-not-allowed'
                                    : 'bg-primary text-white'
                            }`}
                        >
                            Previous
                        </button>
                        <button
                            onClick={() => setCurrentPage(p => Math.min(pageInfo.totalPages - 1, p + 1))}
                            disabled={pageInfo.number === pageInfo.totalPages - 1}
                            className={`px-3 py-1 rounded ${
                                pageInfo.number === pageInfo.totalPages - 1
                                    ? 'bg-gray-200 text-gray-500 cursor-not-allowed'
                                    : 'bg-primary text-white'
                            }`}
                        >
                            Next
                        </button>
                    </div>
                </div>
            )}

            {/* Edit Modal */}
            {editingCollection && (
                <CollectionEditModal
                    collection={editingCollection}
                    onClose={() => setEditingCollection(null)}
                    onSave={handleUpdateCollection}
                />
            )}

            {/* Delete Confirmation Modal */}
            {collectionToDelete && (
                <DeleteConfirmModal
                    title="Delete Collection"
                    message={`Are you sure you want to delete the collection "${collectionToDelete.title}"? This action cannot be undone.`}
                    onConfirm={confirmDelete}
                    onCancel={() => setCollectionToDelete(null)}
                />
            )}
        </div>
    )
}

export default CollectionsTable