import React, { useEffect, useState } from "react";
import { Layout } from "../Layout";
import { useAuth } from "../../context/AuthContext";
import FollowService from "../../services/FollowService";
import UserService from "../../services/UserService";
import BookService from "../../services/BookService";
import { Link } from "react-router-dom";
import { BookOpen, Users, Library, User as UserIcon, Loader } from "lucide-react";
import BookCollectionService from "../../services/BookCollectionService.ts";
import { User } from "../../model/User";

type Book = {
    id: number;
    title: string;
    authors: { firstName: string; lastName: string }[];
};

type Collection = {
    id: number;
    title: string;
    user: {
        id: number;
        firstName: string;
        lastName: string;
    };
};

export const Following: React.FC = () => {
    const { user } = useAuth();
    const [activeTab, setActiveTab] = useState<"users" | "books" | "collections">("users");
    const [followedUsers, setFollowedUsers] = useState<User[]>([]);
    const [followedBooks, setFollowedBooks] = useState<Book[]>([]);
    const [followedCollections, setFollowedCollections] = useState<Collection[]>([]);
    const [isLoading, setIsLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (user) {
            fetchFollowedEntities(activeTab);
        }
    }, [user, activeTab]);

    const fetchFollowedEntities = async (tab: string) => {
        if (!user) return;

        setIsLoading(true);
        setError(null);

        try {
            const entityType = tab === "users" ? "USER" : tab === "books" ? "BOOK" : "COLLECTION";
            const followedIds = await FollowService.getFollowedEntities(entityType);

            if (followedIds.length === 0) {
                setIsLoading(false);
                return;
            }

            if (tab === "users") {
                const usersData = await Promise.all(
                    followedIds.map((id: number) => UserService.getUserById(id))
                );
                setFollowedUsers(usersData.filter(Boolean));
            } else if (tab === "books") {
                const booksData = await Promise.all(
                    followedIds.map((id: number) => BookService.getBookById(id.toString()))
                );
                setFollowedBooks(booksData.filter(Boolean));
            } else if (tab === "collections") {
                const collectionsData = await Promise.all(
                    followedIds.map((id: number) => BookCollectionService.getBookCollectionById(id))
                );
                setFollowedCollections(collectionsData.filter(Boolean));
            }
        } catch (err: any) {
            setError(err.message || "Failed to fetch followed entities");
            console.error("Error fetching followed entities:", err);
        } finally {
            setIsLoading(false);
        }
    };

    const getAuthorNames = (book: Book) => {
        if (!book.authors || book.authors.length === 0) return "Unknown Author";
        return book.authors.map(author => `${author.firstName} ${author.lastName}`).join(", ");
    };

    return (
        <Layout>
            <div className="container mx-auto px-4 py-8">
                <h1 className="text-3xl font-bold mb-6 text-black">Following</h1>

                {/* Tabs */}
                <div className="mb-8">
                    <div className="flex border-b border-gray-200">
                        <button
                            onClick={() => setActiveTab("users")}
                            className={`py-3 px-6 font-medium flex items-center ${
                                activeTab === "users"
                                    ? "border-b-2 border-blue-600 text-blue-600"
                                    : "text-gray-500 hover:text-gray-700"
                            }`}
                        >
                            <UserIcon className="h-5 w-5 mr-2" />
                            Users
                        </button>
                        <button
                            onClick={() => setActiveTab("books")}
                            className={`py-3 px-6 font-medium flex items-center ${
                                activeTab === "books"
                                    ? "border-b-2 border-blue-600 text-blue-600"
                                    : "text-gray-500 hover:text-gray-700"
                            }`}
                        >
                            <BookOpen className="h-5 w-5 mr-2" />
                            Books
                        </button>
                        <button
                            onClick={() => setActiveTab("collections")}
                            className={`py-3 px-6 font-medium flex items-center ${
                                activeTab === "collections"
                                    ? "border-b-2 border-blue-600 text-blue-600"
                                    : "text-gray-500 hover:text-gray-700"
                            }`}
                        >
                            <Library className="h-5 w-5 mr-2" />
                            Collections
                        </button>
                    </div>
                </div>

                {isLoading ? (
                    <div className="flex justify-center items-center py-12">
                        <Loader className="h-8 w-8 animate-spin text-blue-600" />
                    </div>
                ) : error ? (
                    <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
                        {error}
                    </div>
                ) : (
                    <div>
                        {activeTab === "users" && (
                            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                                {followedUsers.length > 0 ? (
                                    followedUsers.map(user => (
                                        <div
                                            key={user.id}
                                            className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow"
                                        >
                                            <div className="p-5">
                                                <div className="flex items-start">
                                                    <div className="bg-blue-100 rounded-full p-3">
                                                        <UserIcon className="h-6 w-6 text-blue-600" />
                                                    </div>
                                                    <div className="ml-4">
                                                        <h3 className="text-lg font-semibold text-gray-800">
                                                            {user.firstName} {user.lastName}
                                                        </h3>
                                                        <p className="text-gray-600 text-sm">{user.email}</p>
                                                    </div>
                                                </div>
                                                <div className="mt-4 flex justify-end">
                                                    <Link
                                                        to={`/users/${user.id}`}
                                                        className="text-blue-600 hover:text-blue-800 font-medium text-sm"
                                                    >
                                                        View Profile
                                                    </Link>
                                                </div>
                                            </div>
                                        </div>
                                    ))
                                ) : (
                                    <div className="col-span-full text-center py-10 bg-gray-50 rounded-lg">
                                        <Users className="mx-auto h-12 w-12 text-gray-400 mb-3" />
                                        <h3 className="text-lg font-medium text-gray-900">No users followed yet</h3>
                                        <p className="mt-2 text-gray-500">
                                            You are not following any users yet. Follow users to see their updates.
                                        </p>
                                    </div>
                                )}
                            </div>
                        )}

                        {activeTab === "books" && (
                            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                                {followedBooks.length > 0 ? (
                                    followedBooks.map(book => (
                                        <div
                                            key={book.id}
                                            className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow"
                                        >
                                            <div className="p-5">
                                                <div className="flex items-start">
                                                    <div className="bg-green-100 rounded-full p-3">
                                                        <BookOpen className="h-6 w-6 text-green-600" />
                                                    </div>
                                                    <div className="ml-4">
                                                        <h3 className="text-lg font-semibold text-gray-800">{book.title}</h3>
                                                        <p className="text-gray-600 text-sm">{getAuthorNames(book)}</p>
                                                    </div>
                                                </div>
                                                <div className="mt-4 flex justify-end">
                                                    <Link
                                                        to={`/books/${book.id}`}
                                                        className="text-blue-600 hover:text-blue-800 font-medium text-sm"
                                                    >
                                                        View Details
                                                    </Link>
                                                </div>
                                            </div>
                                        </div>
                                    ))
                                ) : (
                                    <div className="col-span-full text-center py-10 bg-gray-50 rounded-lg">
                                        <BookOpen className="mx-auto h-12 w-12 text-gray-400 mb-3" />
                                        <h3 className="text-lg font-medium text-gray-900">No books followed yet</h3>
                                        <p className="mt-2 text-gray-500">
                                            You are not following any books yet. Follow books to receive updates about them.
                                        </p>
                                    </div>
                                )}
                            </div>
                        )}

                        {activeTab === "collections" && (
                            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                                {followedCollections.length > 0 ? (
                                    followedCollections.map(collection => (
                                        <div
                                            key={collection.id}
                                            className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow"
                                        >
                                            <div className="p-5">
                                                <div className="flex items-start">
                                                    <div className="bg-purple-100 rounded-full p-3">
                                                        <Library className="h-6 w-6 text-purple-600" />
                                                    </div>
                                                    <div className="ml-4">
                                                        <h3 className="text-lg font-semibold text-gray-800">{collection.title}</h3>
                                                        <p className="text-gray-600 text-sm">
                                                            by {collection.user.firstName} {collection.user.lastName}
                                                        </p>
                                                    </div>
                                                </div>
                                                <div className="mt-4 flex justify-end">
                                                    <Link
                                                        to={`/collections/${collection.id}`}
                                                        className="text-blue-600 hover:text-blue-800 font-medium text-sm"
                                                    >
                                                        View Collection
                                                    </Link>
                                                </div>
                                            </div>
                                        </div>
                                    ))
                                ) : (
                                    <div className="col-span-full text-center py-10 bg-gray-50 rounded-lg">
                                        <Library className="mx-auto h-12 w-12 text-gray-400 mb-3" />
                                        <h3 className="text-lg font-medium text-gray-900">No collections followed yet</h3>
                                        <p className="mt-2 text-gray-500">
                                            You are not following any collections yet. Follow collections to stay updated.
                                        </p>
                                    </div>
                                )}
                            </div>
                        )}
                    </div>
                )}
            </div>
        </Layout>
    );
};

export default Following;