import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { UserPlus, UserMinus, Users, ExternalLink } from "lucide-react"; // Added ExternalLink
import { toast } from "react-toastify";
import FollowService from "../../services/FollowService.ts"
import { useAuth } from "../../context/AuthContext";

interface FollowButtonProps {
    entityId: number;
    entityType: "USER" | "BOOK" | "COLLECTION";
    showFollowersCount?: boolean;
    showUserProfile?: boolean; // New prop to show user profile link
    size?: "sm" | "md" | "lg";
    className?: string;
}

export const FollowButton: React.FC<FollowButtonProps> = ({
                                                              entityId,
                                                              entityType,
                                                              showFollowersCount = false,
                                                              showUserProfile = false, // Default to false for backward compatibility
                                                              size = "md",
                                                              className = ""
                                                          }) => {
    const [isFollowing, setIsFollowing] = useState<boolean>(false);
    const [isLoading, setIsLoading] = useState<boolean>(false);
    const [followersCount, setFollowersCount] = useState<number>(0);
    const { user } = useAuth();

    // Size mappings for button classes
    const sizeClasses = {
        sm: "px-2 py-1 text-xs",
        md: "px-3 py-2 text-sm",
        lg: "px-4 py-2 text-base"
    };

    const iconSize = {
        sm: 14,
        md: 16,
        lg: 18
    };

    useEffect(() => {
        if (user && entityId) {
            // Check if the current user is following this entity
            checkFollowStatus();

            if (showFollowersCount) {
                fetchFollowersCount();
            }
        }
    }, [user, entityId, entityType]);

    const checkFollowStatus = async () => {
        try {
            const isUserFollowing = await FollowService.isFollowing(entityId, entityType);
            setIsFollowing(isUserFollowing);
        } catch (error) {
            console.error("Error checking follow status:", error);
        }
    };

    const fetchFollowersCount = async () => {
        try {
            const count = await FollowService.getFollowersCount(entityId, entityType);
            setFollowersCount(count);
        } catch (error) {
            console.error("Error fetching followers count:", error);
        }
    };

    const handleToggleFollow = async () => {
        if (!user) {
            toast.info("Please login to follow");
            return;
        }

        setIsLoading(true);
        try {
            if (isFollowing) {
                await FollowService.unfollowEntity(entityId, entityType);
                toast.success(`Unfollowed successfully`);
                setIsFollowing(false);
                if (showFollowersCount) {
                    setFollowersCount(prevCount => Math.max(0, prevCount - 1));
                }
            } else {
                await FollowService.followEntity(entityId, entityType);
                toast.success(`Followed successfully`);
                setIsFollowing(true);
                if (showFollowersCount) {
                    setFollowersCount(prevCount => prevCount + 1);
                }
            }
        } catch (error: any) {
            toast.error(error.response?.data?.message || "Error toggling follow status");
        } finally {
            setIsLoading(false);
        }
    };

    // Don't show follow button for the current user
    if (entityType === "USER" && user?.id === entityId) {
        return showFollowersCount ? (
            <div className={`flex items-center ${className}`}>
                <span className="flex items-center text-gray-600">
                    <Users size={iconSize[size]} className="mr-1" />
                    {followersCount} {followersCount === 1 ? "Follower" : "Followers"}
                </span>
            </div>
        ) : null;
    }

    return (
        <div className={`flex items-center ${className}`}>
            <button
                onClick={handleToggleFollow}
                disabled={isLoading}
                className={`
                    ${isFollowing
                    ? "bg-red-100 text-red-700 hover:bg-red-200 focus:ring-red-500"
                    : "bg-blue-600 text-white hover:bg-blue-700 focus:ring-blue-500"}
                    ${sizeClasses[size]}
                    flex items-center justify-center rounded-md
                    font-medium transition-colors
                    focus:outline-none focus:ring-2 focus:ring-offset-2
                    disabled:opacity-50
                `}
            >
                {isFollowing ? (
                    <>
                        <UserMinus size={iconSize[size]} className="mr-1" />
                        Unfollow
                    </>
                ) : (
                    <>
                        <UserPlus size={iconSize[size]} className="mr-1" />
                        Follow
                    </>
                )}
            </button>

            {/* Show profile link if requested and it's a user entity */}
            {showUserProfile && entityType === "USER" && (
                <Link
                    to={`/users/${entityId}`}
                    className={`ml-2 ${sizeClasses[size]} bg-gray-100 text-gray-700 hover:bg-gray-200 rounded-md flex items-center justify-center transition-colors`}
                >
                    <ExternalLink size={iconSize[size]} className="mr-1" />
                    Profile
                </Link>
            )}

            {showFollowersCount && (
                <span className="ml-3 text-gray-600 flex items-center">
                    <Users size={iconSize[size]} className="mr-1" />
                    {followersCount} {followersCount === 1 ? "Follower" : "Followers"}
                </span>
            )}
        </div>
    );
};

export default FollowButton;