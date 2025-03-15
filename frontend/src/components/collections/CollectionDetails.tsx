// Dans CollectionDetails.tsx, ajoutez ces imports et états
import React, {useContext, useEffect, useState} from "react";
import {useParams, Link, useNavigate} from "react-router-dom";
import {BookCollection, BookCollectionShow} from "../../model/BookCollection";
import Layout from "../../pages/Layout";
import {AuthContext} from "../../context/AuthContext.tsx";
import BookCollectionService from "../../services/BookCollectionService.ts";
import BookService from "../../services/BookService.ts"; // Assurez-vous de créer ce service

const CollectionDetails: React.FC = () => {
    const navigate = useNavigate();
    const { user } = useContext(AuthContext);
    const { id } = useParams<{ id: string }>();
    const [collection, setCollection] = useState<BookCollectionShow | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const urlSearchParams = new URLSearchParams(location.search);
    const origin = urlSearchParams.get('origin');

    // États pour le modal
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [availableBooks, setAvailableBooks] = useState<any[]>([]);
    const [loadingBooks, setLoadingBooks] = useState(false);

    // Fetch collection data
    useEffect(() => {
        fetchCollection();
    }, [id]);

    const fetchCollection = async () => {
        try {
            setLoading(true);
            // Parse id as number only if it's not "new"
            const numericId = parseInt(id as string, 10);

            // Check if id is a valid number
            if (isNaN(numericId)) {
                setError("Invalid collection ID");
                setLoading(false);
                return;
            }
            const collectionsData = await BookCollectionService.getBookCollectionById(numericId);
            if (collectionsData) {
                setCollection(collectionsData);
            } else {
                console.error("Unexpected data format:", collectionsData);
                setError("No public collections found");
            }
        } catch (error) {
            console.error("Error fetching collection details:", error);
        } finally {
            setLoading(false);
        }
    };
    const handleRemoveBook = async (bookId: number) => {
        if (!window.confirm("Are you sure you want to remove this book from the collection?")) {
            return;
        }

        try {
            setLoading(true);
            const numericCollectionId = parseInt(id as string, 10);
            await BookCollectionService.removeBookFromCollection(numericCollectionId, bookId);
            fetchCollection();
        } catch (error) {
            console.error("Error removing book from collection:", error);
            setError("Failed to remove book from collection.");
        } finally {
            setLoading(false);
        }
    };

    // Nouvelle fonction pour ouvrir le modal et charger les livres
    const openAddBookModal = async () => {
        try {
            setLoadingBooks(true);
            setIsModalOpen(true);

            // Charger la liste des livres
            const response = await BookService.getBooks();
            if (response && Array.isArray(response.content) && response.page) {
                const books = response.content;
                console.log(books);// Filtrer les livres déjà dans la collection
                const collectionBookIds = collection?.books.map(book => book.id) || [];
                const filteredBooks = books.filter(book => !collectionBookIds.includes(book.id));

                const limitedBooks = filteredBooks.slice(0, 10);    // pour l'instant, on affiche que les 10 premiers
                console.log("10 BOOKS\n", books.slice(0, 10));
                setAvailableBooks(limitedBooks);
            } else {
                setError("Invalid response format from server");
            }
        } catch (error) {
            console.error("Error loading books:", error);
            setError("Failed to load available books.");
        } finally {
            setLoadingBooks(false);
        }
    };

    // Fonction pour ajouter un livre
    const handleAddBook = async (bookId: number) => {
        try {
            setLoadingBooks(true);
            const numericCollectionId = parseInt(id as string, 10);
            await BookCollectionService.addBookToCollection(numericCollectionId, bookId);

            // Fermer le modal et rafraîchir les données
            setIsModalOpen(false);
            fetchCollection();
        } catch (error) {
            console.error("Error adding book to collection:", error);
            setError("Failed to add book to collection.");
        } finally {
            setLoadingBooks(false);
        }
    };

    if (loading) return <div className="text-center py-8">Loading...</div>;

    if (!collection) return <div className="text-center py-8">Collection not found.</div>;

    if (user) {
        if (collection.user.id !== user.id && !collection.isPublic) {
            return <div className="text-center py-8">You don't have permission to view this collection.</div>;
        }
    }
    const parentPath = origin === 'public' ? "/collections/public" : "/collections";
    const backButtonName = origin === 'public' ? "Back to Public Collections" : "Back to My Collections";
    const isOwner = user && collection.user.id === user.id;


    return (
        <Layout>
            <div className="flex h-screen">
                <div className="flex-1 flex flex-col">
                    <div className="flex-1 bg-white p-8">
                        <div className="max-w-4xl mx-auto bg-gray-50 shadow rounded-lg p-6">
                            <h1 className="text-4xl font-bold text-gray-800 mb-4">{collection?.title}</h1>
                            <p className="text-gray-700 mb-6">{collection?.description}</p>

                            <h2 className="text-2xl font-semibold mb-4">Books in this Collection:</h2>
                            {collection.books.length > 0 ? (
                                <ul className="space-y-4">
                                    {collection.books.map((book) => (
                                        <li key={book.id} className="p-4 border rounded-lg bg-white">
                                            <div className="flex justify-between items-start">
                                                <div>
                                                    <h3 className="text-lg font-bold">{book.title}</h3>
                                                    <p className="text-sm text-gray-600">{book.description}</p>
                                                    <p className="text-sm text-gray-500">Published: {book.publicationDate}</p>
                                                    {book.authorNames.length > 0 && (
                                                        <p className="text-sm text-gray-500">
                                                            Authors: {book.authorNames.map((author) => `${author}`).join(", ")}
                                                        </p>
                                                    )}
                                                </div>
                                                {isOwner && (
                                                    <button
                                                        onClick={() => handleRemoveBook(book.id)}
                                                        className="px-3 py-1 bg-red-600 text-white text-sm rounded-md hover:bg-red-700 transition-colors"
                                                    >
                                                        Remove
                                                    </button>
                                                )}
                                            </div>
                                        </li>
                                    ))}
                                </ul>
                            ) : (
                                <p className="text-gray-500">No books available in this collection.</p>
                            )}

                            {isOwner && (
                                <div className="mt-6">
                                    <button
                                        onClick={openAddBookModal}
                                        className="inline-block px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition duration-300"
                                    >
                                        Add Book
                                    </button>
                                </div>
                            )}

                            <div className="mt-6 flex space-x-4">
                                <Link
                                    to="/home"
                                    className="inline-block px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700"
                                >
                                    Back to Home
                                </Link>
                                <Link
                                    to={parentPath}
                                    className="inline-block px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700"
                                >
                                    {backButtonName}
                                </Link>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Modal pour ajouter des livres */}
            {isModalOpen && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
                    <div className="bg-white rounded-lg shadow-xl w-full max-w-4xl max-h-[80vh] overflow-hidden">
                        <div className="p-6 border-b">
                            <div className="flex justify-between items-center">
                                <h3 className="text-2xl font-semibold text-gray-800">Add Book to Collection</h3>
                                <button
                                    onClick={() => setIsModalOpen(false)}
                                    className="text-gray-500 hover:text-gray-700"
                                >
                                    <span className="text-2xl">&times;</span>
                                </button>
                            </div>
                        </div>

                        <div className="p-6 overflow-y-auto max-h-[calc(80vh-130px)]">
                            {loadingBooks ? (
                                <div className="text-center py-8">Loading books...</div>
                            ) : availableBooks.length > 0 ? (
                                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                                    {availableBooks.map(book => (
                                        <div key={book.id} className="border p-4 rounded-lg">
                                            <h4 className="font-semibold">{book.title}</h4>
                                            <p className="text-sm text-gray-600 mb-2">{book.publicationDate}</p>
                                            {book.authorNames && (
                                                <p className="text-sm text-gray-600 mb-3">
                                                    Authors: {book.authorNames.join(", ")}
                                                </p>
                                            )}
                                            <button
                                                onClick={() => handleAddBook(book.id)}
                                                className="w-full px-3 py-1 bg-green-500 text-white text-sm rounded-md hover:bg-green-600 transition-colors"
                                            >
                                                Add to Collection
                                            </button>
                                        </div>
                                    ))}
                                </div>
                            ) : (
                                <p className="text-center py-4">No books available to add to this collection.</p>
                            )}
                        </div>

                        <div className="p-4 border-t flex justify-end">
                            <button
                                onClick={() => setIsModalOpen(false)}
                                className="px-4 py-2 bg-gray-300 text-gray-800 rounded hover:bg-gray-400 transition-colors"
                            >
                                Close
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </Layout>
    );
};

export default CollectionDetails;