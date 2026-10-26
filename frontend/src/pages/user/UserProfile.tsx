import { useState, useEffect } from "react";
import { useParams, Link } from "react-router-dom";
import { toast } from "react-toastify";
import { User } from "lucide-react";
import { Layout } from "../Layout";
import UserService from "../../services/UserService";
import FollowButton from "../../components/follow/FollowButton";
import type { User as UserType } from "../../model/User";

const UserProfile = () => {
    const { id } = useParams<{ id: string }>();
    const [user, setUser] = useState<UserType | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchUserData = async () => {
            try {
                setLoading(true);
                if (!id) {
                    throw new Error("User ID is missing");
                }

                const userData = await UserService.getUserById(parseInt(id));
                setUser(userData);

            } catch (error: any) {
                console.error("Error fetching user data:", error);
                setError(error.message || "Failed to load user profile");
                toast.error(error.message || "Failed to load user profile");
            } finally {
                setLoading(false);
            }
        };

        fetchUserData();
    }, [id]);

    if (loading) {
        return (
            <Layout>
                <div className="flex items-center justify-center min-h-screen">
                    <div className="text-center">Loading user profile...</div>
                </div>
            </Layout>
        );
    }

    if (error || !user) {
        return (
            <Layout>
                <div className="flex items-center justify-center min-h-screen">
                    <div className="text-center text-red-500">{error || "User not found"}</div>
                </div>
            </Layout>
        );
    }

    return (
        <Layout>
            <div className="flex items-center justify-center min-h-screen bg-white px-4 py-6 sm:py-12">
                <div className="w-full max-w-6xl">
                    {/* Profile Header */}
                    <div className="bg-secondary py-6 px-4 sm:py-8 sm:px-6 md:px-10 shadow rounded-lg mb-6">
                        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-6">
                            <div className="flex items-center">
                                <div className="flex-shrink-0 h-20 w-20 bg-primary rounded-full flex items-center justify-center text-3xl font-bold text-secondary mr-4">
                                    {user.firstName.charAt(0)}{user.lastName.charAt(0)}
                                </div>
                                <div>
                                    <h1 className="text-3xl font-bold text-primary">{user.firstName} {user.lastName}</h1>
                                    <p className="text-primary">Member</p>
                                </div>
                            </div>

                            <FollowButton
                                entityId={parseInt(id!)}
                                entityType="USER"
                                showFollowersCount={true}
                                size="lg"
                            />
                        </div>

                        <div className="border-t border-gray-200 pt-6">
                            <h2 className="text-xl font-semibold text-primary mb-4">About</h2>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <div className="bg-white p-4 rounded-lg shadow">
                                    <div className="flex items-center mb-2">
                                        <User className="h-5 w-5 text-blue-500 mr-2" />
                                        <h3 className="font-medium text-gray-800">Contact Information</h3>
                                    </div>
                                    <p className="text-sm text-gray-600">Email: {user.email}</p>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Back Button */}
                    <div className="flex justify-start">
                        <Link
                            to="/"
                            className="inline-block px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700"
                        >
                            Back to Home
                        </Link>
                    </div>
                </div>
            </div>
        </Layout>
    );
};

export default UserProfile;