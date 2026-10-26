import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {BookCollectionIndex} from "../../model/BookCollection";
import { useAuth } from "../../context/AuthContext";
import Layout from "../../pages/Layout";
import BookCollectionService from "../../services/BookCollectionService";
import CollectionEditModal from "../modal/CollectionEditModal.tsx";
import DeleteConfirmModal from "../modal/DeleteConfirmModal.tsx";
import FollowButton from "../../components/follow/FollowButton.tsx";
import { Pagination } from "../Pagination.tsx";
import { PageInfo } from "../../model/Pagination";

// Type for available tabs
type TabType = "ALL" | "PRIVATE" | "PUBLIC" | "SMART" | "GLOBAL";

const Collections: React.FC = () => {
    const { user } = useAuth();
    const [allCollections, setAllCollections] = useState<BookCollectionIndex[]>([]);
    const [privateCollections, setPrivateCollections] = useState<BookCollectionIndex[]>([]);
    const [publicCollections, setPublicCollections] = useState<BookCollectionIndex[]>([]);
    const [displayedCollections, setDisplayedCollections] = useState<BookCollectionIndex[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // State for managing modals
    const [editModalOpen, setEditModalOpen] = useState(false);
    const [deleteModalOpen, setDeleteModalOpen] = useState(false);
    const [selectedCollection, setSelectedCollection] = useState<BookCollectionIndex | null>(null);

    // Active collection type for pagination and filtering
    const [activeTab, setActiveTab] = useState<TabType>("ALL");

    // Pagination and sorting state
    const [pageInfo, setPageInfo] = useState<PageInfo>({
        number: 0,
        size: 10,
        totalElements: 0,
        totalPages: 0
    });
    const [sortField, setSortField] = useState<string>("title");
    const [sortDirection, setSortDirection] = useState<string>("asc");

    // Function to determine collection type
    const getCollectionType = (collection: BookCollectionIndex): "SMART" | "GLOBAL" | "NORMAL" => {
        if (collection.type === "SMART" || collection.type === "LIKED" || collection.type === "FOLLOWED") {
            return "SMART";
        }
        if (collection.type === "GLOBAL" || collection.type === "RECENT" || collection.type === "AUTHOR") {
            return "GLOBAL";
        }
        if (collection.type === "NORMAL") {
            return "NORMAL";
        }

        return "NORMAL";
    };

    // Fetch all collections once
    const fetchAllCollections = async () => {
        if (!user) return;

        try {
            setLoading(true);

            // Fetch with a large size to get potentially all collections
            const privateResp = await BookCollectionService.getUserPrivateCollections(0, 1000, sortField, sortDirection);
            const publicResp = await BookCollectionService.getUserPublicCollections(0, 1000, sortField, sortDirection);

            // Process private collections
            let privateData: BookCollectionIndex[] = [];
            if (privateResp && privateResp.content) {
                privateData = privateResp.content;
                setPrivateCollections(privateData);
            }

            // Process public collections
            let publicData: BookCollectionIndex[] = [];
            if (publicResp && publicResp.content) {
                publicData = publicResp.content;
                setPublicCollections(publicData);
            }

            // Combine all collections
            const allCollectionsData = [...privateData, ...publicData];

            // Remove duplicates
            const uniqueCollections = Array.from(
                new Map(allCollectionsData.map(item => [item.id, item])).values()
            );


            // Sort collections
            const sortedCollections = sortCollections(uniqueCollections, sortField, sortDirection);

            // Store all collections
            setAllCollections(sortedCollections);

            // Count by type for debugging
            const typeCounts = {
                SMART: 0,
                GLOBAL: 0,
                NORMAL: 0
            };

            sortedCollections.forEach(collection => {
                const type = getCollectionType(collection);
                typeCounts[type]++;
            });


        } catch (error) {
            console.error("Error fetching collections:", error);
            setError("Error fetching collections");
        } finally {
            setLoading(false);
        }
    };

    // Sort collections helper
    const sortCollections = (collections: BookCollectionIndex[], field: string, direction: string) => {
        return [...collections].sort((a, b) => {
            if (a[field as keyof BookCollectionIndex] < b[field as keyof BookCollectionIndex]) {
                return direction === 'asc' ? -1 : 1;
            }
            if (a[field as keyof BookCollectionIndex] > b[field as keyof BookCollectionIndex]) {
                return direction === 'asc' ? 1 : -1;
            }
            return 0;
        });
    };

    // Filter and paginate collections based on active tab
    const filterAndPaginateCollections = () => {
        if (!allCollections.length) return;


        // Filter based on tab
        let filtered: BookCollectionIndex[] = [];

        switch (activeTab) {
            case "PRIVATE":
                // Private tab: show normal private collections and specialized collections that are private
                filtered = allCollections.filter(collection => {
                    // Include private collections of any type (NORMAL, SMART, RECENT, AUTHOR)
                    return !collection.isPublic &&
                        // But exclude GLOBAL type as they are system collections
                        collection.type !== "GLOBAL";
                });
                break;

            case "PUBLIC":
                // Public tab: show public collections of all types
                filtered = allCollections.filter(collection => {
                    // Include public collections of any type (NORMAL, SMART, RECENT, AUTHOR)
                    return collection.isPublic &&
                        // But exclude GLOBAL type as they are system collections
                        collection.type !== "GLOBAL";
                });
                break;

            case "SMART":
                // Smart tab: show all smart collections (both public and private)
                filtered = allCollections.filter(collection => {
                    return getCollectionType(collection) === "SMART";
                });
                break;

            case "GLOBAL":
                // Global tab: show all global collections (including RECENT and AUTHOR)
                filtered = allCollections.filter(collection => {
                    return getCollectionType(collection) === "GLOBAL" ||
                        collection.type === "RECENT" ||
                        collection.type === "AUTHOR";
                });
                break;

            case "ALL":
            default:
                // All tab: show all collections
                filtered = [...allCollections];
                break;
        }

        // Sort if needed
        const sorted = sortCollections(filtered, sortField, sortDirection);

        // Calculate pagination
        const totalElements = sorted.length;
        const totalPages = Math.max(1, Math.ceil(totalElements / pageInfo.size));

        // Ensure current page is valid
        const currentPage = Math.min(pageInfo.number, Math.max(0, totalPages - 1));

        // Get current page of collections
        const startIndex = currentPage * pageInfo.size;
        const paginatedCollections = sorted.slice(startIndex, startIndex + pageInfo.size);

        // Update state
        setDisplayedCollections(paginatedCollections);
        setPageInfo({
            ...pageInfo,
            number: currentPage,
            totalElements,
            totalPages
        });

    };

    // Load all collections on component mount
    useEffect(() => {
        if (user) {
            fetchAllCollections();
        }
    }, [user]);

    // Filter, sort and paginate when collections, tab, sort, or page changes
    useEffect(() => {
        filterAndPaginateCollections();
    }, [allCollections, privateCollections, publicCollections, activeTab, sortField, sortDirection, pageInfo.number, pageInfo.size]);

    // Handle page change
    const handlePageChange = (page: number) => {
        setPageInfo(prev => ({
            ...prev,
            number: page
        }));
    };

    // Handle changing the sort field/direction
    const handleSortChange = (field: string) => {
        const direction = field === sortField && sortDirection === "asc" ? "desc" : "asc";

        setSortField(field);
        setSortDirection(direction);

        // Reset to first page when sort changes
        setPageInfo(prev => ({
            ...prev,
            number: 0
        }));
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

            // Refresh collections
            fetchAllCollections();
        } catch (error) {
            console.error("[DEBUG] Error updating collection:", error);
            setError("Failed to update collection");
        }
    };

    // Handle confirming collection deletion
    const handleConfirmDelete = async () => {
        if (!selectedCollection) return;

        try {
            await BookCollectionService.deleteBookCollection(selectedCollection.id);
            setDeleteModalOpen(false);

            // Refresh collections
            fetchAllCollections();
        } catch (error) {
            console.error("[DEBUG] Error deleting collection:", error);
            setError("Failed to delete collection");
        }
    };

    // Toggle between tabs
    const handleTabChange = (tab: TabType) => {
        setActiveTab(tab);
        // Reset to first page when tab changes
        setPageInfo(prev => ({
            ...prev,
            number: 0
        }));
    };

    // Determine collection type badge
    const renderCollectionBadge = (collection: BookCollectionIndex) => {
        // Special case checks for RECENT and AUTHOR collections that are part of the GLOBAL type
        if (collection.type === "RECENT") {
            return (
                <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                    <svg className="mr-1.5 h-2 w-2 text-blue-800" fill="currentColor" viewBox="0 0 8 8">
                        <circle cx="4" cy="4" r="3" />
                    </svg>
                    Recent Books
                </span>
            );
        }

        if (collection.type === "AUTHOR") {
            return (
                <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-indigo-100 text-indigo-800">
                    <svg className="mr-1.5 h-2 w-2 text-indigo-800" fill="currentColor" viewBox="0 0 8 8">
                        <circle cx="4" cy="4" r="3" />
                    </svg>
                    Author Collection
                </span>
            );
        }

        // Standard type checks
        const type = getCollectionType(collection);

        switch (type) {
            case "SMART":
                return (
                    <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-purple-100 text-purple-800">
                        <svg className="mr-1.5 h-2 w-2 text-purple-800" fill="currentColor" viewBox="0 0 8 8">
                            <circle cx="4" cy="4" r="3" />
                        </svg>
                        Smart Collection
                    </span>
                );
            case "GLOBAL":
                return (
                    <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                        <svg className="mr-1.5 h-2 w-2 text-green-800" fill="currentColor" viewBox="0 0 8 8">
                            <circle cx="4" cy="4" r="3" />
                        </svg>
                        Global Collection
                    </span>
                );
            default:
                return (
                    <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
                        <svg className="mr-1.5 h-2 w-2 text-gray-800" fill="currentColor" viewBox="0 0 8 8">
                            <circle cx="4" cy="4" r="3" />
                        </svg>
                        User Collection
                    </span>
                );
        }
    };

    // Check if user can edit a collection
    const canEditCollection = (collection: BookCollectionIndex): boolean => {
        // Users cannot edit RECENT, AUTHOR, or GLOBAL collections
        if (collection.type === "RECENT" || collection.type === "AUTHOR" || collection.type === "GLOBAL") {
            return false;
        }

        const type = getCollectionType(collection);
        if (type === "GLOBAL") {
            return false;
        }

        if (!user) return false;
        return collection.userId === user.id;
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

    return (
        <Layout>
            <div className="flex h-screen">
                <div className="flex-1 flex flex-col">
                    <div className="flex-1 bg-gray-100 p-8">
                        <div className="max-w-6xl mx-auto">
                            <div className="flex justify-between items-center mb-6">
                                <h1 className="text-4xl font-bold text-gray-800">Collections</h1>
                            </div>

                            {user ? (
                                <div>
                                    {/* Tabs for navigating between different collection types */}
                                    <div className="flex border-b border-gray-200 mb-6">
                                        <button
                                            className={`py-2 px-4 font-medium ${activeTab === "ALL" ? "text-blue-600 border-b-2 border-blue-600" : "text-gray-500 hover:text-gray-700"}`}
                                            onClick={() => handleTabChange("ALL")}
                                        >
                                            All Collections
                                        </button>
                                        <button
                                            className={`py-2 px-4 font-medium ${activeTab === "PRIVATE" ? "text-blue-600 border-b-2 border-blue-600" : "text-gray-500 hover:text-gray-700"}`}
                                            onClick={() => handleTabChange("PRIVATE")}
                                        >
                                            My Private Collections
                                        </button>
                                        <button
                                            className={`py-2 px-4 font-medium ${activeTab === "PUBLIC" ? "text-blue-600 border-b-2 border-blue-600" : "text-gray-500 hover:text-gray-700"}`}
                                            onClick={() => handleTabChange("PUBLIC")}
                                        >
                                            Public Collections
                                        </button>
                                        <button
                                            className={`py-2 px-4 font-medium ${activeTab === "SMART" ? "text-blue-600 border-b-2 border-blue-600" : "text-gray-500 hover:text-gray-700"}`}
                                            onClick={() => handleTabChange("SMART")}
                                        >
                                            Smart Collections
                                        </button>
                                        <button
                                            className={`py-2 px-4 font-medium ${activeTab === "GLOBAL" ? "text-blue-600 border-b-2 border-blue-600" : "text-gray-500 hover:text-gray-700"}`}
                                            onClick={() => handleTabChange("GLOBAL")}
                                        >
                                            Global Collections
                                        </button>
                                    </div>

                                    {displayedCollections.length === 0 ? (
                                        <div className="bg-white p-8 rounded-lg shadow text-center">
                                            <p className="text-lg text-gray-600">
                                                No collections found for this category.
                                            </p>
                                        </div>
                                    ) : (
                                        <div className="bg-white shadow rounded-lg overflow-hidden mb-8">
                                            <table className="min-w-full">
                                                <thead className="bg-gray-100">
                                                <tr>
                                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">Type</th>
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
                                                {displayedCollections.map((collection) => (
                                                    <tr key={collection.id} className="hover:bg-gray-50">
                                                        <td className="px-6 py-4 text-sm text-gray-500">
                                                            {renderCollectionBadge(collection)}
                                                        </td>
                                                        <td className="px-6 py-4 text-sm font-medium text-gray-800">{collection.title}</td>
                                                        <td className="px-6 py-4 text-sm text-gray-600 max-w-xs truncate">{collection.description}</td>
                                                        <td className="px-6 py-4 text-sm">
                                                            <Link to={`/users/${collection.userId}`} className="flex items-center group hover:text-blue-600 transition-colors">
                                                                <span className="text-gray-600 group-hover:text-blue-600 transition-colors">
                                                                    {collection.userFullName}
                                                                </span>
                                                            </Link>
                                                        </td>
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
                                                                {!canEditCollection(collection) && (
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
                                                ))}
                                                </tbody>
                                            </table>

                                            {/* Pagination component - only show if there's more than one page */}
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

                                    {/* Action buttons */}
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
                        category: "Uncategorized", // You may need to add this field to your model
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