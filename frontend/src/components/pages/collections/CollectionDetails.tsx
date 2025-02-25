"use client";

import React, { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import { BookCollection } from "../../../model/BookCollection";
import { Book } from "../../../model/Book";
import { collections } from "../../../assets/testData";

const CollectionDetails: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const [collection, setCollection] = useState<BookCollection | null>(null);
    const [loading, setLoading] = useState(true);

    // USE REAL DATA IN BDD (use requests)
    /*useEffect(() => {
        const fetchCollection = async () => {
            try {
                const response = await fetch(`/api/v1/collections/${id}`);
                if (!response.ok) throw new Error("Failed to fetch collection");
                const data: BookCollection = await response.json();
                setCollection(data);
            } catch (error) {
                console.error("Error fetching collection details:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchCollection();
    }, [id]);*/
    // USE TEST DATA
    useEffect(() => {
        // Chercher la collection dans les données de test
        const foundCollection = collections.find((col) => col.id === id);
        setCollection(foundCollection ?? null);
        setLoading(false);
    }, [id]);

    if (loading) return <div className="text-center py-8">Loading...</div>;

    if (!collection) return <div className="text-center py-8">Collection not found.</div>;

    return (
        <div className="bg-gray-100 p-8 min-h-screen">
            <div className="max-w-4xl mx-auto bg-white shadow-md rounded-lg p-6">
                <h1 className="text-3xl font-bold text-gray-900 mb-4">
                    {collection.title}
                </h1>
                <p className="text-gray-700 mb-6">{collection.description}</p>

                <h2 className="text-xl font-semibold mb-4">Books in this Collection:</h2>
                {collection.books.length > 0 ? (
                    <ul className="space-y-4">
                        {collection.books.map((book: Book) => (
                            <li key={book.id} className="p-4 border rounded-lg bg-gray-50">
                                <h3 className="text-lg font-bold">{book.title}</h3>
                                <p className="text-sm text-gray-600">{book.description}</p>
                                <p className="text-sm text-gray-500">Published: {book.publicationDate}</p>
                                {book.authors.length > 0 && (
                                    <p className="text-sm text-gray-500">
                                        Authors: {book.authors.map((author) => `${author.firstName} ${author.lastName}`).join(", ")}
                                    </p>
                                )}
                            </li>
                        ))}
                    </ul>
                ) : (
                    <p className="text-gray-500">No books available in this collection.</p>
                )}

                <div className="mt-6">
                    {/*<Link
                        to="/collections"
                        className="btn btn-secondary inline-block px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700"
                    >
                        Back to Collections
                    </Link>*/}
                    <Link
                        to="/home"
                        className="btn btn-secondary inline-block px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700">
                        Back to Home
                    </Link>
                </div>
            </div>
        </div>
    );
};

export default CollectionDetails;
