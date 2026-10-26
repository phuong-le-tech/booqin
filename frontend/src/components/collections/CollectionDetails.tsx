// In CollectionDetails.tsx
import React, { useEffect, useState} from "react";
import {useParams, Link} from "react-router-dom";
import { BookCollectionShow} from "../../model/BookCollection";
import Layout from "../../pages/Layout";
import {useAuth} from "../../context/AuthContext.tsx";
import BookCollectionService from "../../services/BookCollectionService.ts";
import BookService from "../../services/BookService.ts";
import axiosInstance from "../../services/AxiosInstance.ts";
import FollowButton from "../../components/follow/FollowButton.tsx";
import {LendStatus} from "../../model/LendStatus.ts";
import { VoteSection } from "../../components/vote/VoteSection";
import { CommentSection } from "../../components/comment/CommentSection";
import {PageInfo} from "../../model/Pagination.ts";
import {Pagination} from "../Pagination.tsx";

const CollectionDetails: React.FC = () => {
    const { user } = useAuth();
    const { id } = useParams<{ id: string }>();
    const [collection, setCollection] = useState<BookCollectionShow | null>(null);
    const [loading, setLoading] = useState(true);
    const [, setError] = useState<string | null>(null);
    const urlSearchParams = new URLSearchParams(location.search);
    const origin = urlSearchParams.get('origin');

    // États pour le modal
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [availableBooks, setAvailableBooks] = useState<any[]>([]);
    const [loadingBooks, setLoadingBooks] = useState(false);

    // États pour les livres prêtés/empruntés
    const [lentBooks, setLentBooks] = useState<number[]>([]);
    const [borrowedBooks, setBorrowedBooks] = useState<number[]>([]);
    const [lendingLoading, setLendingLoading] = useState<number | null>(null);

    // Pagination
    const [displayedBooks, setDisplayedBooks] = useState<any[]>([]);
    const [pageInfo, setPageInfo] = useState<PageInfo>({
        number: 0,
        size: 10,
        totalElements: 0,
        totalPages: 0
    });

    // Fetch collection data
    useEffect(() => {
        fetchCollection();
    }, [id]);

    // useEffect pour charger les informations de prêt
    useEffect(() => {
        if (user) {
            fetchUserBooks();
        }
    }, [user]);

    useEffect(() => {
        if (collection && collection.books) {
            updateBookPagination();
        }
    }, [collection, pageInfo.number, pageInfo.size]);

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
                setPageInfo(prev => ({
                    ...prev,
                    totalElements: collectionsData.books.length,
                    totalPages: Math.max(1, Math.ceil(collectionsData.books.length / prev.size))
                }));
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

    const updateBookPagination = () => {
        if (!collection || !collection.books) return;

        const startIndex = pageInfo.number * pageInfo.size;
        const endIndex = Math.min(startIndex + pageInfo.size, collection.books.length);
        const paginatedBooks = collection.books.slice(startIndex, endIndex);

        setDisplayedBooks(paginatedBooks);
    };

    const handlePageChange = (page: number) => {
        setPageInfo(prev => ({
            ...prev,
            number: page
        }));
    };

    // Fonction pour charger les informations de prêt
    const fetchUserBooks = async () => {
        if (!user?.id) {
            console.warn("userId is undefined, skipping lending fetch");
            return;
        }

        try {
            const response = await axiosInstance.get(`/v1/loans/user/${user.id}?type=${LendStatus.ALL}`);

            if (response.data?.success && response.data?.data) {
                // Extraire les IDs des livres prêtés
                const lentBookIds = response.data.data.lentBooks.map((book: any) => book.book.id);
                setLentBooks(lentBookIds);

                // Extraire les IDs des livres empruntés
                const borrowedBookIds = response.data.data.borrowedBooks.map((book: any) => book.book.id);
                setBorrowedBooks(borrowedBookIds);
            } else {
                console.warn("Unexpected response format from /v1/loans/user", response);
            }
        } catch (err: any) {
            console.error("Error fetching user books:", err);
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

    // Nouvelle fonction pour ajouter un livre à la liste de prêt
    const addToLoanable = async (bookId: number) => {
        if (!user?.id) return;

        setLendingLoading(bookId);
        try {
            const params = new URLSearchParams();
            params.append('bookId', bookId.toString());
            params.append('userId', user.id.toString());

            const response = await axiosInstance.post("/v1/loans/make-loanable", params);

            if (response.data?.success) {
                alert("Book added to your lending list");
                fetchUserBooks(); // Rafraîchir la liste des livres prêtés
            } else {
                alert("Failed to add book to lending list");
            }
        } catch (error: any) {
            console.error("Error adding book to lending list:", error);
            console.error("Response data:", error.response?.data);
            alert(error.response?.data?.message || "Failed to add book to lending list");
        } finally {
            setLendingLoading(null);
        }
    };

    // Nouvelle fonction pour retirer un livre de la liste de prêt
    const removeFromLoanable = async (bookId: number) => {
        if (!user?.id) return;

        setLendingLoading(bookId);
        try {
            const response = await axiosInstance.delete("/v1/loans/remove-loanable", {
                params: {
                    bookId: bookId,
                    userId: user.id
                }
            });

            if (response.data?.success) {
                alert("Book removed from your lending list");
                fetchUserBooks(); // Rafraîchir la liste des livres prêtés
            } else {
                alert("Failed to remove book from lending list");
            }
        } catch (error: any) {
            console.error("Error removing book from lending list:", error);
            console.error("Response data:", error.response?.data);
            alert(error.response?.data?.message || "Failed to remove book from lending list");
        } finally {
            setLendingLoading(null);
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
                const collectionBookIds = collection?.books.map(book => book.id) || [];
                const filteredBooks = books.filter(book => !collectionBookIds.includes(book.id));

                const limitedBooks = filteredBooks.slice(0, 10);
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

    const handlePageSizeChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        const newSize = parseInt(e.target.value, 10);
        setPageInfo(prev => ({
            ...prev,
            size: newSize,
            number: 0, // After changing page size, return to first page
            totalPages: Math.max(1, Math.ceil((collection?.books.length || 0) / newSize))
        }));
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
                            {/* Collection Header - Added owner info card and follow button */}
                            <div className="mb-6 border-b pb-4">
                                <h1 className="text-4xl font-bold text-gray-800 mb-2">{collection?.title}</h1>

                                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-4">
                                    {/* Owner information with card style - now clickable */}
                                    <Link to={`/users/${collection.user.id}`} className="flex items-center bg-white p-3 rounded-lg shadow-sm hover:bg-gray-50 transition-colors group">
                                        <div>
                                            <p className="text-sm text-gray-500">Created by</p>
                                            <p className="font-medium text-gray-800 group-hover:text-blue-600 transition-colors">
                                                {collection.user.firstName} {collection.user.lastName}
                                            </p>
                                        </div>
                                    </Link>

                                    {/* Voting and Following buttons section */}
                                    <div className="flex flex-col sm:flex-row items-start sm:items-center gap-3">
                                        {/* Vote Section */}
                                        <VoteSection entityId={parseInt(id as string, 10)} entityType="COLLECTION" />

                                        {/* Follow collection button - only show if not the owner */}
                                        {!isOwner && (
                                            <FollowButton
                                                entityId={collection.id}
                                                entityType="COLLECTION"
                                                showFollowersCount={true}
                                                size="md"
                                            />
                                        )}
                                    </div>
                                </div>

                                <p className="text-gray-700">{collection?.description}</p>
                            </div>

                            <div className="flex justify-between items-center mb-4">
                                <h2 className="text-2xl font-semibold">Books in this Collection:</h2>

                                {/* Options de pagination */}
                                <div className="flex items-center space-x-2">
                                    <span className="text-sm text-gray-600">Books per page:</span>
                                    <select
                                        value={pageInfo.size}
                                        onChange={handlePageSizeChange}
                                        className="border rounded p-1 text-sm"
                                    >
                                        <option value="5">5</option>
                                        <option value="10">10</option>
                                        <option value="20">20</option>
                                        <option value="50">50</option>
                                    </select>
                                </div>
                            </div>

                            {collection.books.length > 0 ? (
                                <div>
                                    <ul className="space-y-4">
                                        {/*{collection.books.map((book) => (*/}
                                        {displayedBooks.map((book) => (
                                            <li key={book.id} className="p-4 border rounded-lg bg-white relative">
                                                {/* Badge pour indiquer un livre emprunté */}
                                                {borrowedBooks.includes(book.id) && (
                                                    <div
                                                        className="absolute top-2 right-2 bg-red-500 text-white text-xs px-2 py-1 rounded-full z-10">
                                                        Prêté
                                                    </div>
                                                )}
                                                {/* Structure du livre avec les informations en haut et les boutons en bas */}
                                                <div>
                                                    <div>
                                                        <h3 className="text-lg font-bold">{book.title}</h3>
                                                        <p className="text-sm text-gray-600">{book.description}</p>
                                                        <p className="text-sm text-gray-500">Published: {book.publicationDate}</p>
                                                        {book.authorNames.length > 0 && (
                                                            <p className="text-sm text-gray-500">
                                                                Authors: {book.authorNames.map((author: string) => `${author}`).join(", ")}
                                                            </p>
                                                        )}
                                                    </div>

                                                    {/* Boutons placés en bas */}
                                                    <div className="flex space-x-2 mt-4">
                                                        {/* View book details button */}
                                                        <Link
                                                            to={`/books/${book.id}`}
                                                            className="px-3 py-1 bg-blue-500 text-white text-sm rounded-md hover:bg-blue-600 transition-colors"
                                                        >
                                                            View Details
                                                        </Link>

                                                        {user && !borrowedBooks.includes(book.id) && (
                                                            lentBooks.includes(book.id) ? (
                                                                <button
                                                                    onClick={() => removeFromLoanable(book.id)}
                                                                    className="px-3 py-1 text-white text-sm rounded-md transition-colors bg-yellow-500 hover:bg-yellow-600"
                                                                    disabled={lendingLoading === book.id}
                                                                >
                                                                    {lendingLoading === book.id ? "Processing..." : "Remove from lending"}
                                                                </button>
                                                            ) : (
                                                                <button
                                                                    onClick={() => addToLoanable(book.id)}
                                                                    className="px-3 py-1 text-white text-sm rounded-md transition-colors bg-green-500 hover:bg-green-600"
                                                                    disabled={lendingLoading === book.id}
                                                                >
                                                                    {lendingLoading === book.id ? "Processing..." : "Add to lending"}
                                                                </button>
                                                            )
                                                        )}

                                                        {/* Remove book button - only for collection owner */}
                                                        {isOwner && (
                                                            <button
                                                                onClick={() => handleRemoveBook(book.id)}
                                                                className="px-3 py-1 bg-red-500 text-white text-sm rounded-md hover:bg-red-600 transition-colors"
                                                            >
                                                                Remove
                                                            </button>
                                                        )}
                                                    </div>
                                                </div>
                                            </li>
                                        ))}
                                    </ul>

                                    {/* Pagination component - only show if there's more than one page */}
                                    {pageInfo.totalPages > 1 && (
                                        <div className="mt-6 flex justify-center">
                                            <Pagination
                                                pageInfo={pageInfo}
                                                onPageChange={handlePageChange}
                                            />
                                        </div>
                                    )}
                                </div>
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

                        {/* Comment Section */}
                        <div className="max-w-4xl mx-auto mt-8 bg-white rounded-xl shadow-lg overflow-hidden border border-gray-100 p-6">
                            <CommentSection entityId={parseInt(id as string, 10)} entityType="COLLECTION" />
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