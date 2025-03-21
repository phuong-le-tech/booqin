import React, { useContext, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {BookCollectionIndex} from "../../model/BookCollection";
import { AuthContext } from "../../context/AuthContext";
import Layout from "../../pages/Layout";
import BookCollectionService from "../../services/BookCollectionService";
import CollectionEditModal from "../modal/CollectionEditModal.tsx";
import DeleteConfirmModal from "../modal/DeleteConfirmModal.tsx";
import FollowButton from "../../components/follow/FollowButton.tsx";
import { Pagination } from "../Pagination.tsx";
import { PageInfo } from "../../model/Pagination";

const Collections: React.FC = () => {
    const { user } = useContext(AuthContext);
    const [collections, setCollections] = useState<BookCollectionIndex[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // State for managing modals
    const [editModalOpen, setEditModalOpen] = useState(false);
    const [deleteModalOpen, setDeleteModalOpen] = useState(false);
    const [selectedCollection, setSelectedCollection] = useState<BookCollectionIndex | null>(null);

    // Active collection type for pagination
    const [activeTab, setActiveTab] = useState<'public' | 'private'>('private');

    // Pagination and sorting state
    const [pageInfo, setPageInfo] = useState<PageInfo>({
        number: 0,
        size: 10,
        totalElements: 0,
        totalPages: 0
    });
    const [sortField, setSortField] = useState<string>("id");
    const [sortDirection, setSortDirection] = useState<string>("asc");

    const fetchCollections = async (page = 0, size = 10, field = sortField, direction = sortDirection) => {
        if (!user) return;

        try {
            setLoading(true);

            // Use the appropriate service method based on active tab
            let response;
            if (activeTab === 'private') {
                response = await BookCollectionService.getUserPrivateCollections(page, size, field, direction);
            } else {
                response = await BookCollectionService.getUserPublicCollections(page, size, field, direction);
            }
            console.log(response);
            if (response && response.content) {
                setCollections(response.content);

                // Update pagination info
                setPageInfo({
                    number: response.page.number,
                    size: response.page.size,
                    totalElements: response.page.totalElements,
                    totalPages: response.page.totalPages
                });

                // Update sort state
                setSortField(field);
                setSortDirection(direction);
            } else {
                console.error("Unexpected data format:", response);
                setError("No collections found");
            }
        } catch (error) {
            console.error("Error fetching collections:", error);
            setError("Error fetching collections");
        } finally {
            setLoading(false);
        }
    };
  console.log(pageInfo);
    useEffect(() => {
        if (user) {
            fetchCollections(0, pageInfo.size, sortField, sortDirection);
        } else {
            setLoading(false);
        }
    }, [user, activeTab]);

    // Handle page change from pagination component
    const handlePageChange = (page: number) => {
        fetchCollections(page, pageInfo.size, sortField, sortDirection);
    };

    // Handle changing the sort field/direction
    const handleSortChange = (field: string) => {
        // Toggle direction if clicking the same field, otherwise use ascending
        const direction = field === sortField && sortDirection === "asc" ? "desc" : "asc";
        fetchCollections(0, pageInfo.size, field, direction);
    };

    // Handle opening the edit modal
    const handleEditClick = (collection: BookCollectionIndex) => {
        setSelectedCollection(collection);
        setEditModalOpen(true);
    };

    // Handle opening the delete modal
    const handleDeleteClick = (collection: BookCollectionIndex) => {
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

            await BookCollectionService.patchBookCollection(selectedCollection.id, updateData);
            setEditModalOpen(false);

            // Refresh with current pagination and sorting
            fetchCollections(pageInfo.number, pageInfo.size, sortField, sortDirection);
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

            // Refresh with current pagination and sorting
            fetchCollections(pageInfo.number, pageInfo.size, sortField, sortDirection);
        } catch (error) {
            console.error("Error deleting collection:", error);
            setError("Failed to delete collection");
        }
    };

    // Toggle between public and private collections tab
    // We no longer call fetchCollections here since the useEffect will handle it
    const handleTabChange = (tab: 'public' | 'private') => {
        setActiveTab(tab);
        // No need to call fetchCollections here, the useEffect will do it
    };

    // Loading and error states
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

    // Helper function to render a collection row
    const renderCollectionRow = (collection: BookCollectionIndex, isOwner: boolean) => (
        <tr key={collection.id} className="hover:bg-gray-100">
            <td className="px-6 py-4 text-sm text-gray-800">{collection.title}</td>
            <td className="px-6 py-4 text-sm text-gray-600">{collection.description}</td>
            <td className="px-6 py-4 text-sm">
                <Link to={`/users/${collection.userId}`} className="flex items-center group hover:text-blue-600 transition-colors">
                    <span className="text-gray-600 group-hover:text-blue-600 transition-colors">
                         {collection.userFullName}
                    </span>
                </Link>
            </td>
            <td className="px-6 py-4 text-sm font-medium">
                <div className="flex space-x-2">
                    <Link to={`/collections/${collection.id}`}
                          className="text-indigo-600 hover:text-indigo-900 px-2 py-1 bg-indigo-100 rounded">
                        View
                    </Link>
                    {isOwner && (
                        <>
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
                        </>
                    )}
                    {!isOwner && (
                        <div className="ml-2">
                            <FollowButton
                                entityId={collection.id}
                                entityType="COLLECTION"
                                size="sm"
                            />
                        </div>
                    )}
                </div>
            </td>
            <td className="px-6 py-4 text-sm text-gray-500">
                {collection.bookCount}
            </td>
        </tr>
    );

    return (
        <Layout>
            <div className="flex h-screen">
                <div className="flex-1 flex flex-col">
                    <div className="flex-1 bg-gray-100 p-8">
                        <div className="max-w-4xl mx-auto">
                            <div className="flex justify-between items-center mb-6">
                                <h1 className="text-4xl font-bold text-gray-800">My Collections</h1>
                            </div>

                            {user ? (
                                <div>
                                    {/* Tabs for navigating between private and public collections */}
                                    <div className="mb-6 border-b border-gray-200">
                                        <ul className="flex flex-wrap items-center -mb-px">
                                            <li className="mr-2">
                                                <button
                                                    className={`inline-block p-4 rounded-t-lg ${
                                                        activeTab === 'private'
                                                            ? 'text-black font-semibold border-b-2 border-primary'
                                                            : 'text-gray-500 hover:text-gray-600'
                                                    }`}
                                                    onClick={() => handleTabChange('private')}
                                                >
                                                    Private Collections
                                                </button>
                                            </li>
                                            <li className="mr-2">
                                                <button
                                                    className={`inline-block p-4 rounded-t-lg ${
                                                        activeTab === 'public'
                                                            ? 'text-black font-semibold border-b-2 border-primary'
                                                            : 'text-gray-500 hover:text-gray-600'
                                                    }`}
                                                    onClick={() => handleTabChange('public')}
                                                >
                                                    Public Collections
                                                </button>
                                            </li>
                                            <li className="ml-auto">
                                                <Link to="/collections/new" className="inline-block px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700">
                                                    Add New Collection
                                                </Link>
                                            </li>
                                        </ul>
                                    </div>

                                    {collections.length === 0 ? (
                                        <div className="bg-white p-8 rounded-lg shadow text-center">
                                            <p className="text-lg text-gray-600">
                                                No {activeTab === 'private' ? 'private' : 'public'} collections found.
                                            </p>
                                            <Link to="/collections/new" className="mt-4 inline-block px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700">
                                                Create a New Collection
                                            </Link>
                                        </div>
                                    ) : (
                                        <div className="bg-white shadow rounded-lg overflow-hidden mb-8">
                                            <table className="min-w-full">
                                                <thead className="bg-gray-100">
                                                <tr>
                                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">
                                                        <button
                                                            className="flex items-center hover:text-gray-900"
                                                            onClick={() => handleSortChange('title')}
                                                        >
                                                            Name
                                                            {sortField === 'title' && (
                                                                <span className="ml-1">
                                                                    {sortDirection === 'asc' ? '↑' : '↓'}
                                                                </span>
                                                            )}
                                                        </button>
                                                    </th>
                                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">Description</th>
                                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">Owner</th>
                                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">Actions</th>
                                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                        Number of Books
                                                    </th>
                                                </tr>
                                                </thead>
                                                <tbody className="divide-y divide-gray-200">
                                                {collections.map((collection) =>
                                                    renderCollectionRow(collection, collection.userId === user?.id)
                                                )}
                                                </tbody>
                                            </table>

                                            {/* Pagination component */}
                                            {pageInfo.totalPages > 1 && (
                                                <div className="px-6 py-4">
                                                    <Pagination
                                                        pageInfo={pageInfo}
                                                        onPageChange={handlePageChange}
                                                    />
                                                </div>
                                            )}
                                        </div>
                                    )}
                                </div>
                            ) : (
                                <div>
                                    <h1 className="text-center text-gray-500">Please login to view your collections!</h1>
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
                        category: "Undefined",
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