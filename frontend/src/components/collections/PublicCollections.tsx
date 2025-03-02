"use client";

import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Layout from "../../pages/Layout.tsx";
import {BookCollection} from "../../model/BookCollection.ts";
import {List} from "lucide-react";
import {collections} from "../../assets/testData.ts";

const PublicCollections: React.FC = () => {
    /*const [collections, setCollections] = useState<BookCollection[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        // Fonction pour récupérer les collections publiques depuis le backend
        const fetchPublicCollections = async () => {
            try {
                const response = await fetch('/api/v1/collections/public'); // Assurez-vous que l'URL est correcte
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                const data = await response.json();
                setCollections(data);
                setLoading(false);
            } catch (error) {
                setError(error.message);
                setLoading(false);
            }
        };
        fetchPublicCollections();
    }, []);
    if (loading) {
        return <div>Loading...</div>;
    }
    if (error) {
        return <div>Error: {error}</div>;
    }*/

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
                                    </tr>
                                    </thead>
                                    <tbody className="divide-y divide-gray-200">
                                    {collections.map((collection) => (
                                        <tr key={collection.id} className="hover:bg-gray-100">
                                            <td className="px-6 py-4 text-sm text-gray-800">{collection.title}</td>
                                            <td className="px-6 py-4 text-sm text-gray-600">{collection.description}</td>
                                            <td className="px-6 py-4 text-sm text-gray-600">{collection.owner.firstName} {collection.owner.lastName}</td>
                                            <td className="px-6 py-4 text-sm font-medium">
                                                <Link to={`/collections/${collection.id}?origin=public`}
                                                      className="text-indigo-600 hover:text-indigo-900">View</Link>
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
