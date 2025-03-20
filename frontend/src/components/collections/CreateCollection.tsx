import React, { useContext, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Layout from "../../pages/Layout";
import { AuthContext } from "../../context/AuthContext";
import BookCollectionService from "../../services/BookCollectionService";

const CreateCollection: React.FC = () => {
    const { user } = useContext(AuthContext);
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const [formData, setFormData] = useState({
        title: "",
        description: "",
        isPublic: false
    });

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value, type } = e.target as HTMLInputElement;
        setFormData(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? (e.target as HTMLInputElement).checked : value
        }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!formData.title.trim()) {
            setError("Title is required");
            return;
        }

        try {
            setLoading(true);
            setError(null);

            if (!user) {
                setError("You must be logged in to create a collection");
                return;
            }

            // Ne pas envoyer l'objet user complet, le serveur l'obtient du token d'authentification
            const collectionData = {
                title: formData.title,
                description: formData.description,
                isPublic: formData.isPublic
                // Ne pas inclure owner ici, il sera défini côté serveur
            };

            console.log("Sending collection data:", collectionData);
            const createdCollection = await BookCollectionService.createBookCollection(collectionData);
            console.log("Collection created successfully:", createdCollection);

            // Rediriger vers la liste des collections après succès
            navigate(`/collections`);
        } catch (err: any) {
            console.error("Error creating collection:", err);
            if (err.response) {
                console.error("Response data:", err.response.data);
                console.error("Response status:", err.response.status);
                setError(err.response.data?.message || "Failed to create collection: server error");
            } else {
                setError(err.message || "Failed to create collection");
            }
        } finally {
            setLoading(false);
        }
    };

    // Redirect to login if not authenticated
    if (!user) {
        return (
            <Layout>
                <div className="text-center py-8">
                    <h1 className="text-2xl font-bold mb-4">Please login to create a collection</h1>
                    <Link to="/login" className="text-indigo-600 hover:text-indigo-700">
                        Go to login page
                    </Link>
                </div>
            </Layout>
        );
    }

    return (
        <Layout>
            <div className="bg-gray-50 text-gray-900 p-8">
                <div className="max-w-6xl mx-auto">
                    <h1 className="text-4xl font-bold text-gray-800 mb-8 text-center">Create a New Collection</h1>

                    <div className="bg-white shadow-lg rounded-lg overflow-hidden mb-8">
                        <div className="p-6">
                            <form onSubmit={handleSubmit} className="space-y-6">
                                <div className="grid grid-cols-1 gap-6">
                                    <div>
                                        <label htmlFor="title" className="block text-sm font-medium text-gray-700 mb-1">Title:</label>
                                        <input
                                            type="text"
                                            id="title"
                                            name="title"
                                            value={formData.title}
                                            onChange={handleChange}
                                            required
                                            className="w-full px-4 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500"
                                        />
                                    </div>

                                    <div>
                                        <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-1">Description:</label>
                                        <textarea
                                            id="description"
                                            name="description"
                                            value={formData.description}
                                            onChange={handleChange}
                                            rows={4}
                                            className="w-full px-4 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500"
                                        ></textarea>
                                    </div>

                                    <div className="flex items-center">
                                        <input
                                            type="checkbox"
                                            id="isPublic"
                                            name="isPublic"
                                            checked={formData.isPublic}
                                            onChange={handleChange}
                                            className="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                                        />
                                        <label htmlFor="isPublic" className="ml-2 block text-sm text-gray-700">Make this collection public</label>
                                    </div>
                                </div>

                                <div className="flex justify-center space-x-4">
                                    <button
                                        type="submit"
                                        disabled={loading}
                                        className="inline-block px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:bg-indigo-400"
                                    >
                                        {loading ? "Creating..." : "Create Collection"}
                                    </button>
                                    <Link
                                        to="/collections/user"
                                        className="inline-block px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300"
                                    >
                                        Cancel
                                    </Link>
                                </div>
                            </form>

                            {/* Display error message if any */}
                            {error && (
                                <div className="mt-4 p-4 bg-red-100 border border-red-400 text-red-700 rounded">
                                    <p>{error}</p>
                                </div>
                            )}
                        </div>
                    </div>

                    <div className="mt-6 text-center">
                        <Link to="/collections/public" className="inline-block px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700">
                            Back to My Collections
                        </Link>
                    </div>
                </div>
            </div>
        </Layout>
    );
};

export default CreateCollection;