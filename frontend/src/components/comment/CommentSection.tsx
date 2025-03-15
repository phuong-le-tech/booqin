import React, { useState, useEffect } from "react";
import { CommentForm } from "./CommentForm";
import { CommentTree } from "./CommentTree";
import CommentService from "../../services/CommentService";
import AuthService from "../../services/AuthService";
import { User } from "../../model/User";

// Constants for entity types
export const ENTITY_TYPES = {
    BOOK: 'BOOK',
    AUTHOR: 'AUTHOR',
    COMMENT: 'COMMENT',
};

interface CommentSectionProps {
    entityId: number;
    entityType: string;
}

interface CommentDTO {
    id: number;
    content: string;
    userId: number;
    username: string;
    entityId: number;
    entityType: string;
    createdAt: string;
    updatedAt: string;
    replies: CommentDTO[];
}

export const CommentSection: React.FC<CommentSectionProps> = ({ entityId, entityType }) => {
    const [comments, setComments] = useState<CommentDTO[]>([]);
    const [isLoading, setIsLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [commentCount, setCommentCount] = useState<number>(0);
    const [currentUser, setCurrentUser] = useState<User | null>(null);
    const [refreshKey, setRefreshKey] = useState<number>(0);
    const [sortOrder, setSortOrder] = useState<'newest' | 'oldest'>('newest');
    const [formActive, setFormActive] = useState<boolean>(false);

    useEffect(() => {
        const fetchCurrentUser = async () => {
            try {
                const user = await AuthService.fetchCurrentUser();
                console.log("Current user fetched:", user);
                setCurrentUser(user);
            } catch (err) {
                console.error("Failed to get current user:", err);
            }
        };

        fetchCurrentUser();
    }, []);

    useEffect(() => {
        console.log(`CommentSection: Fetching comments due to refreshKey=${refreshKey}`);
        fetchComments();
        fetchCommentCount();
    }, [entityId, entityType, refreshKey, sortOrder]);

    const fetchComments = async () => {
        try {
            setIsLoading(true);
            console.log(`Fetching comments for ${entityType} with ID ${entityId}`);

            const commentTree = await CommentService.getCommentTreeForEntity(entityType, entityId);
            console.log("CommentSection: Received comments:", commentTree);

            let sortedComments = [...(commentTree || [])];
            if (sortOrder === 'newest') {
                sortedComments = sortedComments.sort((a, b) =>
                    new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
                );
            } else {
                sortedComments = sortedComments.sort((a, b) =>
                    new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
                );
            }

            setComments(sortedComments);
            setError(null);
        } catch (err) {
            console.error("CommentSection: Failed to fetch comments:", err);
            setError("Failed to load comments. Please try again later.");
        } finally {
            setIsLoading(false);
        }
    };

    const fetchCommentCount = async () => {
        try {
            if (!entityType) {
                console.error("entityType is undefined");
                setCommentCount(0);
                return;
            }
            const count = await CommentService.countCommentsByEntity(entityType, entityId);
            setCommentCount(typeof count === 'number' ? count : 0);
        } catch (err) {
            console.error("Failed to fetch comment count:", err);
            setCommentCount(0);
        }
    };

    const handleCommentSubmit = async (content: string, parentId: number | null = null) => {
        try {
            if (!currentUser) {
                setError("You must be logged in to comment");
                return;
            }

            setIsLoading(true);

            console.log("CommentSection: Submitting comment with data:", {
                userId: currentUser.id,
                entityId,
                entityType,
                content,
                parentId
            });

            await CommentService.createComment({
                userId: currentUser.id,
                entityId,
                entityType,
                content,
                parentId
            });

            console.log("CommentSection: Comment submitted successfully, refreshing comments...");
            setRefreshKey(prev => prev + 1);
            setError(null);
            setFormActive(false);
        } catch (err) {
            console.error("CommentSection: Failed to submit comment:", err);
            setError("Failed to submit comment. Please try again later.");
            setIsLoading(false);
        }
    };

    const handleDeleteComment = async (commentId: number) => {
        try {
            if (!currentUser) {
                setError("You must be logged in to delete a comment");
                return;
            }

            setIsLoading(true);

            console.log("CommentSection: Deleting comment with ID:", commentId);
            await CommentService.delete(commentId, currentUser.id);

            console.log("CommentSection: Comment deleted successfully, refreshing comments...");
            setRefreshKey(prev => prev + 1);
            setError(null);
        } catch (err) {
            console.error("CommentSection: Failed to delete comment:", err);
            setError("Failed to delete comment. Please try again later.");
            setIsLoading(false);
        }
    };

    const handleEditComment = async (commentId: number, content: string) => {
        try {
            if (!currentUser) {
                setError("You must be logged in to edit a comment");
                return;
            }

            setIsLoading(true);

            console.log("CommentSection: Editing comment with ID:", commentId, "and content:", content);
            await CommentService.update(commentId, currentUser.id, { content });

            console.log("CommentSection: Comment edited successfully, refreshing comments...");
            setRefreshKey(prev => prev + 1);
            setError(null);
        } catch (err) {
            console.error("CommentSection: Failed to edit comment:", err);
            setError("Failed to edit comment. Please try again later.");
            setIsLoading(false);
        }
    };

    const toggleSortOrder = () => {
        setSortOrder(sortOrder === 'newest' ? 'oldest' : 'newest');
    };

    const isAuthenticated = !!currentUser;

    return (
        <div className="mt-8 pt-6 border-t border-gray-200">
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-6">
                <h2 className="text-2xl font-semibold text-gray-800 flex items-center">
                    Discussion
                    {commentCount > 0 && (
                        <span className="ml-3 px-2 py-1 bg-blue-100 text-blue-800 text-sm font-medium rounded-full">
                            {commentCount}
                        </span>
                    )}
                </h2>

                <div className="mt-3 sm:mt-0 flex flex-wrap gap-2">
                    {commentCount > 1 && (
                        <button
                            onClick={toggleSortOrder}
                            className="flex items-center text-sm text-blue-600 hover:text-blue-800 font-medium bg-blue-50 px-3 py-1 rounded-md transition-colors duration-200"
                        >
                            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1" viewBox="0 0 20 20" fill="currentColor">
                                <path d="M5 12a1 1 0 102 0V6.414l1.293 1.293a1 1 0 001.414-1.414l-3-3a1 1 0 00-1.414 0l-3 3a1 1 0 001.414 1.414L5 6.414V12zM15 8a1 1 0 10-2 0v5.586l-1.293-1.293a1 1 0 00-1.414 1.414l3 3a1 1 0 001.414 0l3-3a1 1 0 00-1.414-1.414L15 13.586V8z" />
                            </svg>
                            {sortOrder === 'newest' ? 'Newest first' : 'Oldest first'}
                        </button>
                    )}

                    {isAuthenticated && (
                        <button
                            onClick={() => setFormActive(!formActive)}
                            className="flex items-center text-sm bg-blue-600 text-white px-3 py-1 rounded-md hover:bg-blue-700 transition-colors duration-200"
                        >
                            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1" viewBox="0 0 20 20" fill="currentColor">
                                <path fillRule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clipRule="evenodd" />
                            </svg>
                            {formActive ? 'Cancel' : 'Add Comment'}
                        </button>
                    )}
                </div>
            </div>

            {error && (
                <div className="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4 rounded">
                    <div className="flex">
                        <div className="flex-shrink-0">
                            <svg className="h-5 w-5 text-red-500" viewBox="0 0 20 20" fill="currentColor">
                                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                            </svg>
                        </div>
                        <div className="ml-3">
                            <p className="text-sm">{error}</p>
                        </div>
                    </div>
                </div>
            )}

            {!isAuthenticated ? (
                <div className="bg-blue-50 border-l-4 border-blue-500 text-blue-700 p-4 mb-6 rounded">
                    <div className="flex">
                        <div className="flex-shrink-0">
                            <svg className="h-5 w-5 text-blue-500" viewBox="0 0 20 20" fill="currentColor">
                                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
                            </svg>
                        </div>
                        <div className="ml-3">
                            <p className="text-sm">
                                Please <a href="/login" className="font-medium text-blue-600 underline hover:text-blue-800">log in</a> to join the conversation.
                            </p>
                        </div>
                    </div>
                </div>
            ) : formActive && (
                <div className="mb-8">
                    <CommentForm
                        onSubmit={(content) => handleCommentSubmit(content)}
                        onCancel={() => setFormActive(false)}
                    />
                </div>
            )}

            {isLoading && comments.length === 0 ? (
                <div className="mt-4 space-y-3">
                    <div className="animate-pulse p-4 border rounded-lg">
                        <div className="flex items-center">
                            <div className="rounded-full bg-gray-200 h-10 w-10"></div>
                            <div className="ml-3">
                                <div className="h-4 bg-gray-200 rounded w-24 mb-2"></div>
                                <div className="h-3 bg-gray-200 rounded w-16"></div>
                            </div>
                        </div>
                        <div className="mt-3 pl-12">
                            <div className="h-4 bg-gray-200 rounded w-full mb-2"></div>
                            <div className="h-4 bg-gray-200 rounded w-3/4"></div>
                        </div>
                    </div>
                    <div className="animate-pulse p-4 border rounded-lg">
                        <div className="flex items-center">
                            <div className="rounded-full bg-gray-200 h-10 w-10"></div>
                            <div className="ml-3">
                                <div className="h-4 bg-gray-200 rounded w-32 mb-2"></div>
                                <div className="h-3 bg-gray-200 rounded w-16"></div>
                            </div>
                        </div>
                        <div className="mt-3 pl-12">
                            <div className="h-4 bg-gray-200 rounded w-full mb-2"></div>
                            <div className="h-4 bg-gray-200 rounded w-2/3"></div>
                        </div>
                    </div>
                </div>
            ) : comments.length > 0 ? (
                <div className="space-y-6">
                    <CommentTree
                        comments={comments}
                        onReplySubmit={handleCommentSubmit}
                        onDeleteComment={handleDeleteComment}
                        onEditComment={handleEditComment}
                        isAuthenticated={isAuthenticated}
                        currentUserId={currentUser?.id}
                    />
                </div>
            ) : (
                <div className="mt-6 py-8 bg-gray-50 rounded-lg text-center">
                    <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                    </svg>
                    <p className="mt-4 text-gray-500 text-lg">
                        No comments yet
                    </p>
                    <p className="mt-2 text-gray-400">
                        {isAuthenticated ? (
                            <button
                                onClick={() => setFormActive(true)}
                                className="text-blue-600 hover:text-blue-800 hover:underline focus:outline-none"
                            >
                                Be the first to start the discussion!
                            </button>
                        ) : 'Log in to start the discussion!'}
                    </p>
                </div>
            )}

            {isLoading && comments.length > 0 && (
                <div className="mt-4 py-3 bg-blue-50 text-blue-700 rounded-lg text-center text-sm">
                    <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-blue-700 inline-block" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Loading new comments...
                </div>
            )}
        </div>
    );
};