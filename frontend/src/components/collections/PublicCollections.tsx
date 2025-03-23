"use client";

import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Layout from "../../pages/Layout.tsx";
import {BookCollectionIndex} from "../../model/BookCollection.ts";
import BookCollectionService from "../../services/BookCollectionService.ts";
import FollowButton from "../../components/follow/FollowButton.tsx";
import { PageInfo } from "../../model/Pagination.ts"
import {Pagination} from "../Pagination.tsx";

const PublicCollections: React.FC = () => {
    const [collections, setCollections] = useState<BookCollectionIndex[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    // Pagination state
    const [pageInfo, setPageInfo] = useState<PageInfo>({
        number: 0,
        size: 8,
        totalElements: 0,
        totalPages: 0
    });
    const [sortField, setSortField] = useState<string>("id");
    const [sortDirection, setSortDirection] = useState<string>("asc");

    const fetchCollections = async (page = 0, size = 8, field = sortField, direction = sortDirection) => {
        try {
            setLoading(true);
            // Use the dedicated public collections endpoint instead of filtering
            const response = await BookCollectionService.getPublicCollections(
                page,
                size,
                field,
                direction
            );
            console.log(response);
            // Update collections and pagination info
            setCollections(response.content);
            setPageInfo({
                number: response.page.number,
                size: response.page.size,
                totalElements: response.page.totalElements,
                totalPages: response.page.totalPages
            });
            setSortField(field);
            setSortDirection(direction);
        } catch (err) {
            console.error("Error fetching public collections:", err);
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchCollections();
    }, []);

    const handlePageChange = (page: number, size?: number) => {
        fetchCollections(page, size || pageInfo.size, sortField, sortDirection);
    };

    const handleSortChange = (field: string) => {
        // Toggle direction if clicking the same field, otherwise use ascending
        const direction = field === sortField && sortDirection === "asc" ? "desc" : "asc";
        fetchCollections(pageInfo.number, pageInfo.size, field, direction);
    };

    if (loading) {
        return <Layout><div className="p-8 text-center">Loading collections...</div></Layout>;
    }

    if (error) {
        return <Layout><div className="p-8 text-center text-red-500">Error: {error}</div></Layout>;
    }

    return (
        <Layout>
            <div className="flex h-screen">
                <div className="flex-1 flex flex-col">
                    <div className="flex-1 bg-gray-100 p-8">
                        <div className="max-w-6xl mx-auto">
                            <div className="flex justify-between items-center mb-8">
                                <h1 className="text-4xl font-bold text-gray-800">Public Collections</h1>

                                {/* Sort controls */}
                                <div className="flex items-center space-x-2">
                                    <span className="text-sm text-gray-600">Sort by:</span>
                                    <select
                                        value={`${sortField}-${sortDirection}`}
                                        onChange={(e) => {
                                            const [field, direction] = e.target.value.split('-');
                                            fetchCollections(pageInfo.number, pageInfo.size, field, direction);
                                        }}
                                        className="border rounded p-1 text-sm"
                                    >
                                        <option value="id-asc">ID (Asc)</option>
                                        <option value="id-desc">ID (Desc)</option>
                                        <option value="title-asc">Title (A-Z)</option>
                                        <option value="title-desc">Title (Z-A)</option>
                                        <option value="createdAt-desc">Newest First</option>
                                        <option value="createdAt-asc">Oldest First</option>
                                    </select>
                                </div>
                            </div>

                            {collections.length === 0 ? (
                                <div className="bg-white p-8 rounded-lg shadow text-center">
                                    <p className="text-lg text-gray-600">No public collections available.</p>
                                </div>
                            ) : (
                                <>
                                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                                        {collections.map((collection) => (
                                            <div key={collection.id} className="bg-white rounded-lg shadow overflow-hidden hover:shadow-lg transition-shadow h-64 flex flex-col">
                                                <div className="p-6 flex flex-col flex-grow">
                                                    <h2 className="text-xl font-bold text-gray-800 mb-2 line-clamp-1">{collection.title}</h2>

                                                    {/* Owner information */}
                                                    <Link to={`/users/${collection.userId}`} className="flex items-center mb-3 group hover:text-blue-600 transition-colors">
                                                        <div className="h-8 w-8 bg-blue-500 rounded-full flex items-center justify-center text-white font-bold text-sm mr-2 group-hover:bg-blue-600 transition-colors">
                                                            {collection.userFullName.split(" ").map(name => name.charAt(0)).join("").substring(0, 2)}
                                                        </div>
                                                        <span className="text-sm text-gray-600 group-hover:text-blue-600 line-clamp-1">
                                                            Created by <span className="font-medium">{collection.userFullName}</span>
                                                        </span>
                                                    </Link>

                                                    <p className="text-gray-600 text-sm mb-4 line-clamp-2 flex-grow">
                                                        {collection.description}
                                                    </p>

                                                    <div className="flex items-center justify-between mb-4 mt-auto">
                                                        <span className="text-sm text-gray-500">
                                                            {collection.bookCount} book{collection.bookCount !== 1 ? 's' : ''}
                                                        </span>

                                                        {/* Follow button */}
                                                        <FollowButton
                                                            entityId={collection.id}
                                                            entityType="COLLECTION"
                                                            size="sm"
                                                        />
                                                    </div>

                                                    <Link
                                                        to={`/collections/${collection.id}?origin=public`}
                                                        className="w-full block text-center px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition-colors"
                                                    >
                                                        View Collection
                                                    </Link>
                                                </div>
                                            </div>
                                        ))}
                                    </div>

                                    {/* Pagination - display if more than one page */}
                                    {pageInfo && pageInfo.totalPages > 1 && (
                                        <div className="mt-8">
                                            <Pagination
                                                pageInfo={pageInfo}
                                                onPageChange={handlePageChange}
                                            />
                                        </div>
                                    )}
                                </>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </Layout>
    );
};

export default PublicCollections;