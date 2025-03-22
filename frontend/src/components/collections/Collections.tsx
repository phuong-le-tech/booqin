import React, { useContext, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {BookCollectionIndex, BookCollectionShow} from "../../model/BookCollection";
import { AuthContext } from "../../context/AuthContext";
import Layout from "../../pages/Layout";
import BookCollectionService from "../../services/BookCollectionService";
import CollectionEditModal from "../modal/CollectionEditModal.tsx";
import DeleteConfirmModal from "../modal/DeleteConfirmModal.tsx";
import FollowButton from "../../components/follow/FollowButton.tsx";
import { Pagination } from "../Pagination.tsx";
import { PageInfo } from "../../model/Pagination";

// Type pour les onglets disponibles
type TabType = "ALL" | "PRIVATE" | "PUBLIC" | "SMART" | "GLOBAL";

const Collections: React.FC = () => {
    const { user } = useContext(AuthContext);
    const [activeTab, setActiveTab] = useState<TabType>("ALL");
    const [collections, setCollections] = useState<BookCollectionShow[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // State for managing modals
    const [editModalOpen, setEditModalOpen] = useState(false);
    const [deleteModalOpen, setDeleteModalOpen] = useState(false);
    const [selectedCollection, setSelectedCollection] = useState<BookCollectionIndex | null>(null);

    // Active collection type for pagination


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
            const userCollectionsData = await BookCollectionService.getUserCollections();
            console.log("USER COLLECTIONS\n", userCollectionsData);
            if (userCollectionsData && Array.isArray(userCollectionsData)) {
                setCollections(userCollectionsData);

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
                console.error("Unexpected data format:", userCollectionsData);
                setError("No collections found");
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

    // Déterminer le type de collection basé sur ses propriétés
    const getCollectionType = (collection: BookCollectionShow): string => {
        // System-generated collections (user.id === 1 means system user)
        if (collection.user.id === 1) {
            return "GENERATED";
        }

        // All other collections are considered user-created
        return "USER_CREATED";
    };

    // Déterminer si une collection est "smart" (pour le filtrage)
    const isSmartCollection = (collection: BookCollectionShow): boolean => {
        return collection.title.includes("Smart");
    };

    const renderCollectionBadge = (collection: BookCollectionShow) => {
        const type = getCollectionType(collection);

        if (type === "GENERATED") {
            return (
                <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                    <svg className="mr-1.5 h-2 w-2 text-green-800" fill="currentColor" viewBox="0 0 8 8">
                        <circle cx="4" cy="4" r="3" />
                    </svg>
                    Created Automatically
                </span>
            );
        } else {
            return (
                <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                    <svg className="mr-1.5 h-2 w-2 text-blue-800" fill="currentColor" viewBox="0 0 8 8">
                        <circle cx="4" cy="4" r="3" />
                    </svg>
                    Created by User
                </span>
            );
        }
    };

    // Filtrer les collections selon l'onglet actif
    const getFilteredCollections = () => {
        if (!user) return [];

        switch (activeTab) {
            case "ALL":
                return collections;
            case "PRIVATE":
                return collections.filter(c => !c.isPublic && c.user.id === user.id);
            case "PUBLIC":
                return collections.filter(c => c.isPublic);
            case "SMART":
                return collections.filter(c => isSmartCollection(c));
            case "GLOBAL":
                // If you have a specific definition for global collections
                return collections.filter(c => c.isPublic && c.user.id === 1); // Assuming global means public + system created
            default:
                return collections;
        }
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

    // Vérifier si un utilisateur peut éditer une collection
    const canEditCollection = (collection: BookCollectionShow): boolean => {
        if (!user) return false;
        // Only allow editing of user's own collections
        return collection.user.id === user.id;
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

    const filteredCollections = getFilteredCollections();

    return (
        <Layout>
            <div className="flex h-screen">
                <div className="flex-1 flex flex-col">
                    <div className="flex-1 bg-gray-100 p-8">
                        <div className="max-w-6xl mx-auto">
                            <h1 className="text-4xl font-bold text-gray-800 mb-6">Collections</h1>

                            {user ? (
                                <div>
                                    {/* Onglets de navigation */}
                                    <div className="flex border-b border-gray-200 mb-6">
                                        <button
                                            className={`py-2 px-4 font-medium ${activeTab === "ALL" ? "text-blue-600 border-b-2 border-blue-600" : "text-gray-500 hover:text-gray-700"}`}
                                            onClick={() => setActiveTab("ALL")}
                                        >
                                            All Collections
                                        </button>
                                        <button
                                            className={`py-2 px-4 font-medium ${activeTab === "PRIVATE" ? "text-blue-600 border-b-2 border-blue-600" : "text-gray-500 hover:text-gray-700"}`}
                                            onClick={() => setActiveTab("PRIVATE")}
                                        >
                                            My Private Collections
                                        </button>
                                        <button
                                            className={`py-2 px-4 font-medium ${activeTab === "PUBLIC" ? "text-blue-600 border-b-2 border-blue-600" : "text-gray-500 hover:text-gray-700"}`}
                                            onClick={() => setActiveTab("PUBLIC")}
                                        >
                                            Public Collections
                                        </button>
                                        <button
                                            className={`py-2 px-4 font-medium ${activeTab === "SMART" ? "text-blue-600 border-b-2 border-blue-600" : "text-gray-500 hover:text-gray-700"}`}
                                            onClick={() => setActiveTab("SMART")}
                                        >
                                            Smart Collections
                                        </button>
                                        <button
                                            className={`py-2 px-4 font-medium ${activeTab === "GLOBAL" ? "text-blue-600 border-b-2 border-blue-600" : "text-gray-500 hover:text-gray-700"}`}
                                            onClick={() => setActiveTab("GLOBAL")}
                                        >
                                            Global Collections
                                        </button>
                                    </div>

                                    {/* Tableau des collections */}
                                    {filteredCollections.length > 0 ? (
                                        <div className="bg-white shadow rounded-lg overflow-hidden">
                                            <table className="min-w-full">
                                                <thead className="bg-gray-100">
                                                <tr>
                                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">Type</th>
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
                                                {filteredCollections.map((collection) => (
                                                    <tr key={collection.id} className="hover:bg-gray-50">
                                                        <td className="px-6 py-4 text-sm text-gray-500">
                                                            {renderCollectionBadge(collection)}
                                                        </td>
                                                        <td className="px-6 py-4 text-sm font-medium text-gray-800">{collection.title}</td>
                                                        <td className="px-6 py-4 text-sm text-gray-600 max-w-xs truncate">{collection.description}</td>
                                                        <td className="px-6 py-4 text-sm text-gray-600">{collection.user.firstName} {collection.user.lastName}</td>
                                                        <td className="px-6 py-4 text-sm font-medium">
                                                            <div className="flex space-x-2">
                                                                <Link
                                                                    to={`/collections/${collection.id}?origin=${activeTab.toLowerCase()}`}
                                                                    className="text-indigo-600 hover:text-indigo-900 px-2 py-1 bg-indigo-100 rounded"
                                                                >
                                                                    View
                                                                </Link>

                                                                {canEditCollection(collection) && (
                                                                    <>
                                                                        <button
                                                                            onClick={() => handleEditClick(collection)}
                                                                            className="text-green-600 hover:text-green-900 px-2 py-1 bg-green-100 rounded"
                                                                        >
                                                                            Edit
                                                                        </button>
                                                                        <button
                                                                            onClick={() => handleDeleteClick(collection)}
                                                                            className="text-red-600 hover:text-red-900 px-2 py-1 bg-red-100 rounded"
                                                                        >
                                                                            Delete
                                                                        </button>
                                                                    </>
                                                                )}
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
                                    ) : (
                                        <div className="bg-white shadow rounded-lg p-6 text-center text-gray-500">
                                            No collections found for this category.
                                        </div>
                                    )}

                                    {/* Boutons d'action */}
                                    <div className="mt-8 flex justify-center space-x-4">
                                        <Link to="/collections/new" className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
                                            Create New Collection
                                        </Link>
                                        <Link to="/collections/smart/new" className="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors">
                                            Create Smart Collection
                                        </Link>
                                    </div>
                                </div>
                            ) : (
                                <div className="bg-white shadow rounded-lg p-8 text-center">
                                    <h2 className="text-2xl text-gray-700 mb-4">Please login to view your collections</h2>
                                    <Link to="/" className="inline-block px-4 py-2 mt-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700">
                                        Go back to home page
                                    </Link>
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