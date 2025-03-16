import React, { useContext, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { BookCollection, BookCollectionShow } from "../../model/BookCollection";
import { AuthContext } from "../../context/AuthContext";
import Layout from "../../pages/Layout";
import BookCollectionService from "../../services/BookCollectionService";
import CollectionEditModal from "../modal/CollectionEditModal.tsx";
import DeleteConfirmModal from "../modal/DeleteConfirmModal.tsx";
import { getBookCount } from "../../utils/BookCollectionUtils";

const Collections: React.FC = () => {
    const { user } = useContext(AuthContext);
    const [publicCollections, setPublicCollections] = useState<BookCollectionShow[]>([]);
    const [privateCollections, setPrivateCollections] = useState<BookCollectionShow[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // State for managing modals
    const [editModalOpen, setEditModalOpen] = useState(false);
    const [deleteModalOpen, setDeleteModalOpen] = useState(false);
    const [selectedCollection, setSelectedCollection] = useState<BookCollectionShow | null>(null);

    const fetchCollections = async () => {
        try {
            setLoading(true);
            const userCollectionsData = await BookCollectionService.getUserCollections();
            console.log("USER COLLECTIONS\n", userCollectionsData);
            if (userCollectionsData && Array.isArray(userCollectionsData)) {
                const userCollections = userCollectionsData.filter((collection) => collection.user.id === user?.id);
                console.log("USER COLLECTION CORRECT ID:\n", userCollections);
                const publicUserCollections = userCollectionsData.filter((collection) => collection.isPublic);
                const privateUserCollections = userCollectionsData.filter((collection) => !collection.isPublic);
                setPublicCollections(publicUserCollections);
                setPrivateCollections(privateUserCollections);
            } else {
                console.error("Unexpected data format:", userCollectionsData);
                setError("No public collections found");
            }
        } catch (error) {
            console.error("Error fetching collection details:", error);
            setError("Error fetching collections");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        user ? fetchCollections() : setLoading(false);
    }, [user]);

    // Handle opening the edit modal
    const handleEditClick = (collection: BookCollectionShow) => {
        setSelectedCollection(collection);
        setEditModalOpen(true);
    };

    // Handle opening the delete modal
    const handleDeleteClick = (collection: BookCollectionShow) => {
        setSelectedCollection(collection);
        setDeleteModalOpen(true);
    };

    // Handle saving collection edits
    const handleSaveEdit = async (updatedData: any) => {
        if (!selectedCollection) return;

        try {
            const updateData = {
                title: updatedData.name,
                description: updatedData.description,
                isPublic: updatedData.isPublic
            };
            console.log("UPDATED Data:\n", updateData);
            try {
                await BookCollectionService.patchBookCollection(selectedCollection.id, updateData);
                setEditModalOpen(false);
                fetchCollections(); // Actualiser la liste après modification
            } catch (apiError: any) {
                console.error("API Error:", apiError);
                if (apiError.response) {
                    console.error("Response status:", apiError.response.status);
                    console.error("Response data:", apiError.response.data);
                }
            }
        } catch (error) {
            console.error("Error updating collection:", error);
            setError("Failed to update collection");
        }
    };

    // Handle confirming collection deletion
    const handleConfirmDelete = async () => {
        if (!selectedCollection) return;

        try {
            await BookCollectionService.deleteBookCollection(selectedCollection.id);
            setDeleteModalOpen(false);
            fetchCollections(); // Refresh the collections list
        } catch (error) {
            console.error("Error deleting collection:", error);
            setError("Failed to delete collection");
        }
    };

    if (loading) {
        return (
            <Layout>
                <div className="text-center py-8">Loading...</div>
            </Layout>
        );
    }

    if (error) {
        return (
            <Layout>
                <div className="text-center py-8 text-red-500">{error}</div>
            </Layout>
        );
    }

    return (
        <Layout>
            <div className="flex h-screen">
                <div className="flex-1 flex flex-col">
                    <div className="flex-1 bg-gray-100 p-8">
                        <div className="max-w-4xl mx-auto">
                            <h1 className="text-4xl font-bold text-gray-800 mb-8">My Collections</h1>
                            {user ? (
                                <div>
                                    <h2 className="text-2xl font-bold text-gray-800 mb-4 mt-8">Private Collections</h2>
                                    <div className="bg-white shadow rounded-lg overflow-hidden mb-8">
                                        <table className="min-w-full">
                                            <thead className="bg-gray-100">
                                            <tr>
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">Name</th>
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">Description</th>
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">Owner</th>
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">Actions</th>
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                    Number of Books
                                                </th>
                                            </tr>
                                            </thead>
                                            <tbody className="divide-y divide-gray-200">
                                            {privateCollections.map((collection) => (
                                                <tr key={collection.id} className="hover:bg-gray-100">
                                                    <td className="px-6 py-4 text-sm text-gray-800">{collection.title}</td>
                                                    <td className="px-6 py-4 text-sm text-gray-600">{collection.description}</td>
                                                    <td className="px-6 py-4 text-sm text-gray-600">{collection.user.firstName} {collection.user.lastName}</td>
                                                    <td className="px-6 py-4 text-sm font-medium">
                                                        <div className="flex space-x-2">
                                                            <Link to={`/collections/${collection.id}`}
                                                                  className="text-indigo-600 hover:text-indigo-900 px-2 py-1 bg-indigo-100 rounded">View</Link>
                                                            <button
                                                                onClick={() => handleEditClick(collection)}
                                                                className="text-green-600 hover:text-green-900 px-2 py-1 bg-green-100 rounded">
                                                                Edit
                                                            </button>
                                                            <button
                                                                onClick={() => handleDeleteClick(collection)}
                                                                className="text-red-600 hover:text-red-900 px-2 py-1 bg-red-100 rounded">
                                                                Delete
                                                            </button>
                                                        </div>
                                                    </td>
                                                    <td className="px-6 py-4 text-sm text-gray-500">
                                                        {getBookCount(collection)}
                                                    </td>
                                                </tr>
                                            ))}
                                            </tbody>
                                        </table>
                                    </div>

                                    <h2 className="text-2xl font-bold text-gray-800 mb-4 mt-8">Public Collections</h2>
                                    <div className="bg-white shadow rounded-lg overflow-hidden">
                                        <table className="min-w-full">
                                            <thead className="bg-gray-100">
                                            <tr>
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">Name</th>
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">Description</th>
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">Owner</th>
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">Actions</th>
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                    Number of Books
                                                </th>
                                            </tr>
                                            </thead>
                                            <tbody className="divide-y divide-gray-200">
                                            {publicCollections.map((collection) => (
                                                <tr key={collection.id} className="hover:bg-gray-100">
                                                    <td className="px-6 py-4 text-sm text-gray-800">{collection.title}</td>
                                                    <td className="px-6 py-4 text-sm text-gray-600">{collection.description}</td>
                                                    <td className="px-6 py-4 text-sm text-gray-600">{collection.user.firstName} {collection.user.lastName}</td>
                                                    <td className="px-6 py-4 text-sm font-medium">
                                                        <div className="flex space-x-2">
                                                            <Link to={`/collections/${collection.id}?origin=user`}
                                                                  className="text-indigo-600 hover:text-indigo-900 px-2 py-1 bg-indigo-100 rounded">View</Link>
                                                            <button
                                                                onClick={() => handleEditClick(collection)}
                                                                className="text-green-600 hover:text-green-900 px-2 py-1 bg-green-100 rounded">
                                                                Edit
                                                            </button>
                                                            <button
                                                                onClick={() => handleDeleteClick(collection)}
                                                                className="text-red-600 hover:text-red-900 px-2 py-1 bg-red-100 rounded">
                                                                Delete
                                                            </button>
                                                        </div>
                                                    </td>
                                                    <td className="px-6 py-4 text-sm text-gray-500">
                                                        {getBookCount(collection)}
                                                    </td>
                                                </tr>
                                            ))}
                                            </tbody>
                                        </table>
                                    </div>

                                    <div className="mt-6 text-center">
                                        <Link to="/collections/new" className="inline-block px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700">
                                            Add New Collection
                                        </Link>
                                    </div>
                                </div>
                            ) : (
                                <div>
                                    <h1 className="text-center text-gray-500">Please login to view your collections !</h1>
                                    <span className="block text-center text-gray-500">
                                        <Link to="/" className="btn btn-secondary text-blue-900 border-blue-700 inline-block px-4 py-2 mt-4">Go back to home page</Link>
                                    </span>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </div>

            {/* Edit Modal */}
            {editModalOpen && selectedCollection && (
                <CollectionEditModal
                    collection={{
                        id: selectedCollection.id,
                        title: selectedCollection.title,
                        description: selectedCollection.description,
                        category: "Uncategorized", // You may need to add this field to your data model
                        createdBy: `${selectedCollection.user.firstName} ${selectedCollection.user.lastName}`,
                        bookCount: getBookCount(selectedCollection),
                        isPublic: selectedCollection.isPublic
                    }}
                    onClose={() => setEditModalOpen(false)}
                    onSave={handleSaveEdit}
                />
            )}

            {/* Delete Confirmation Modal */}
            {deleteModalOpen && selectedCollection && (
                <DeleteConfirmModal
                    title="Delete Collection"
                    message={`Are you sure you want to delete the collection "${selectedCollection.title}"? This action cannot be undone.`}
                    onConfirm={handleConfirmDelete}
                    onCancel={() => setDeleteModalOpen(false)}
                />
            )}
        </Layout>
    );
};

export default Collections;