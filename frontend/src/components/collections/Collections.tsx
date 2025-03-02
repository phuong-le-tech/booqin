import React, { useContext, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { BookCollection } from "../../model/BookCollection";
import { AuthContext } from "../../context/AuthContext";
import Layout from "../../pages/Layout";
import { collections as testCollections } from "../../assets/testData";

const Collections: React.FC = () => {
    const { user } = useContext(AuthContext);
    const [publicCollections, setPublicCollections] = useState<BookCollection[]>([]);
    const [privateCollections, setPrivateCollections] = useState<BookCollection[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // Remplacer par un appel API pour récupérer les données réelles
        const fetchUserCollections = async () => {
            const userCollections = testCollections.filter((collection) => collection.owner.id === user?.id);
            const publicUserCollections = userCollections.filter((collection) => collection.isPublic);
            const privateUserCollections = userCollections.filter((collection) => !collection.isPublic);
            setPublicCollections(publicUserCollections);
            setPrivateCollections(privateUserCollections);
            setLoading(false);
        };
        fetchUserCollections();
    }, [user]);

    if (loading) return <div className="text-center py-8">Loading...</div>;

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
                                            </tr>
                                            </thead>
                                            <tbody className="divide-y divide-gray-200">
                                            {privateCollections.map((collection) => (
                                                <tr key={collection.id} className="hover:bg-gray-100">
                                                    <td className="px-6 py-4 text-sm text-gray-800">{collection.title}</td>
                                                    <td className="px-6 py-4 text-sm text-gray-600">{collection.description}</td>
                                                    <td className="px-6 py-4 text-sm text-gray-600">{collection.owner.firstName} {collection.owner.lastName}</td>
                                                    <td className="px-6 py-4 text-sm font-medium">
                                                        <Link to={`/collections/${collection.id}`}
                                                              className="text-indigo-600 hover:text-indigo-900">View</Link>
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
                                            </tr>
                                            </thead>
                                            <tbody className="divide-y divide-gray-200">
                                            {publicCollections.map((collection) => (
                                                <tr key={collection.id} className="hover:bg-gray-100">
                                                    <td className="px-6 py-4 text-sm text-gray-800">{collection.title}</td>
                                                    <td className="px-6 py-4 text-sm text-gray-600">{collection.description}</td>
                                                    <td className="px-6 py-4 text-sm text-gray-600">{collection.owner.firstName} {collection.owner.lastName}</td>
                                                    <td className="px-6 py-4 text-sm font-medium">
                                                        <Link to={`/collections/${collection.id}?origin=user`}
                                                              className="text-indigo-600 hover:text-indigo-900">View</Link>
                                                    </td>
                                                </tr>
                                            ))}
                                            </tbody>
                                        </table>
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
        </Layout>
    );
};

export default Collections;
