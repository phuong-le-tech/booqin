"use client";

import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Layout from "../../pages/Layout.tsx";
import {BookCollection, BookCollectionShow} from "../../model/BookCollection.ts";
import BookCollectionService from "../../services/BookCollectionService.ts";
import { getBookCount } from "../../utils/BookCollectionUtils";

const PublicCollections: React.FC = () => {
    const [collections, setCollections] = useState<BookCollectionShow[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [currentPage, setCurrentPage] = useState<number>(0);
    const [totalPages, setTotalPages] = useState<number>(0);

    useEffect(() => {
        const fetchCollections = async () => {
            try {
                setLoading(true);
                const publicCollectionsData = await BookCollectionService.getPublicCollections();
                if (publicCollectionsData && Array.isArray(publicCollectionsData)) {
                    console.log("PUBLIC COLLECTIONS FOUND");
                    setCollections(publicCollectionsData);
                } else {
                    console.error("Unexpected data format:", publicCollectionsData);
                    setError("No public collections found");
                }
            } catch (err) {
                console.error("Error fetching collections:", err);
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };
        fetchCollections();
    }, [currentPage]);

    if (loading) {
        return <Layout><div>Loading...</div></Layout>;
    }

    if (error) {
        return <Layout><div>Error: {error}</div></Layout>;
    }

    return (
        <Layout>
            <div className="flex h-screen">
                <div className="flex-1 flex flex-col">
                    <div className="flex-1 bg-gray-100 p-8">
                        <div className="max-w-4xl mx-auto">
                            <h1 className="text-4xl font-bold text-gray-800 mb-8">Public Collections</h1>
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
                                    {collections.map((collection) => (
                                        <tr key={collection.id} className="hover:bg-gray-100">
                                            <td className="px-6 py-4 text-sm text-gray-800">{collection.title}</td>
                                            <td className="px-6 py-4 text-sm text-gray-600">{collection.description}</td>
                                            <td className="px-6 py-4 text-sm text-gray-600">{collection.user.firstName} {collection.user.lastName}</td>
                                            <td className="px-6 py-4 text-sm font-medium">
                                                <Link to={`/collections/${collection.id}?origin=user`}
                                                      className="text-indigo-600 hover:text-indigo-900 px-2 py-1 bg-indigo-100 rounded">View</Link>
                                            </td>
                                            <td className="px-6 py-4 text-sm text-gray-500">
                                                {getBookCount(collection)}
                                            </td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </Layout>
    );
};

export default PublicCollections;
