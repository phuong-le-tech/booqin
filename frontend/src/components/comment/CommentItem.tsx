import React, { useState } from "react";
import { CommentForm } from "./CommentForm";
import { formatDate } from "../../utils/dateUtils";

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

interface CommentItemProps {
    comment: CommentDTO;
    onReplySubmit: (content: string, parentId: number) => void;
    onDeleteComment: (commentId: number) => void;
    onEditComment: (commentId: number, content: string) => void;
    isAuthenticated: boolean;
    depth: number;
    currentUserId?: number;
}

export const CommentItem: React.FC<CommentItemProps> = ({
                                                            comment,
                                                            onReplySubmit,
                                                            onDeleteComment,
                                                            onEditComment,
                                                            isAuthenticated,
                                                            depth,
                                                            currentUserId
                                                        }) => {
    const [isReplying, setIsReplying] = useState(false);
    const [isEditing, setIsEditing] = useState(false);
    const [showReplies, setShowReplies] = useState(true);
    const [isDeleting, setIsDeleting] = useState(false);

    const hasReplies = comment.replies && comment.replies.length > 0;
    const maxDepth = 5; // Maximum depth for indentation
    const currentDepth = Math.min(depth, maxDepth);
    const isOwner = currentUserId === comment.userId;

    // Generate a different color for each user
    const getUserColor = (userId: number) => {
        const colors = [
            'bg-blue-600', 'bg-green-600', 'bg-purple-600',
            'bg-pink-600', 'bg-yellow-600', 'bg-red-600',
            'bg-indigo-600', 'bg-amber-600', 'bg-teal-600'
        ];
        return colors[userId % colors.length];
    };

    const handleReply = (content: string) => {
        onReplySubmit(content, comment.id);
        setIsReplying(false);
    };

    const handleEdit = (content: string) => {
        // Call the onEditComment function passed from parent
        onEditComment(comment.id, content);
        setIsEditing(false);
    };

    const toggleReplies = () => {
        setShowReplies(!showReplies);
    };

    const handleDelete = () => {
        onDeleteComment(comment.id);
        setIsDeleting(false);
    };

    const getBgColor = () => {
        if (currentDepth % 2 === 0) return 'bg-white';
        return 'bg-blue-50';
    };

    return (
        <div className={`comment-thread ${currentDepth > 0 ? 'ml-3 pl-3 sm:ml-6 sm:pl-6' : ''}`}>
            <div className={`p-4 border rounded-lg ${getBgColor()} shadow-sm hover:shadow-md transition-shadow duration-200`}>
                <div className="flex justify-between items-start mb-2">
                    <div className="flex items-center">
                        <div className={`h-9 w-9 rounded-full ${getUserColor(comment.userId)} text-white flex items-center justify-center text-sm font-medium mr-2`}>
                            {comment.username.charAt(0).toUpperCase()}
                        </div>
                        <div>
                            <div className="font-medium text-blue-600">{comment.username}</div>
                            <div className="text-xs text-gray-500">{formatDate(comment.createdAt)}</div>
                        </div>
                    </div>

                    {isAuthenticated && isOwner && (
                        <div className="flex space-x-2">
                            <button onClick={() => setIsEditing(!isEditing)}
                                    className="text-gray-500 hover:text-blue-600">
                                <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
                                    <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z" />
                                </svg>
                            </button>
                            <button onClick={() => setIsDeleting(!isDeleting)}
                                    className="text-gray-500 hover:text-red-600">
                                <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
                                    <path fillRule="evenodd" d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z" clipRule="evenodd" />
                                </svg>
                            </button>
                        </div>
                    )}
                </div>

                {isEditing ? (
                    <div className="mt-2 pl-10">
                        <CommentForm
                            onSubmit={handleEdit}
                            initialValue={comment.content}
                            placeholder="Edit your comment..."
                            buttonText="Save Changes"
                            isReply={true}
                            onCancel={() => setIsEditing(false)}
                        />
                    </div>
                ) : (
                    <div className="mt-2 text-gray-700 whitespace-pre-line pl-10">
                        {comment.content}
                    </div>
                )}

                {isDeleting && (
                    <div className="mt-3 pl-10 bg-red-50 p-3 rounded-md border border-red-200">
                        <p className="text-sm text-red-700 mb-2">Are you sure you want to delete this comment?</p>
                        <div className="flex space-x-2">
                            <button
                                onClick={handleDelete}
                                className="px-3 py-1 bg-red-600 text-white text-sm rounded-md hover:bg-red-700">
                                Delete
                            </button>
                            <button
                                onClick={() => setIsDeleting(false)}
                                className="px-3 py-1 bg-gray-200 text-gray-800 text-sm rounded-md hover:bg-gray-300">
                                Cancel
                            </button>
                        </div>
                    </div>
                )}

                <div className="mt-3 pl-10 flex items-center space-x-4">
                    {isAuthenticated && !isEditing && !isDeleting && (
                        <button
                            onClick={() => setIsReplying(!isReplying)}
                            className="text-sm text-blue-600 hover:text-blue-800 flex items-center hover:underline transition-colors"
                        >
                            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1" viewBox="0 0 20 20" fill="currentColor">
                                <path fillRule="evenodd" d="M7.707 3.293a1 1 0 010 1.414L5.414 7H11a7 7 0 017 7v2a1 1 0 11-2 0v-2a5 5 0 00-5-5H5.414l2.293 2.293a1 1 0 11-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clipRule="evenodd" />
                            </svg>
                            Reply
                        </button>
                    )}

                    {comment.updatedAt && comment.updatedAt !== comment.createdAt && (
                        <span className="text-xs text-gray-400 italic">(edited)</span>
                    )}
                </div>

                {isReplying && (
                    <div className="mt-3 pl-10 border-l-2 border-blue-100 ml-2 py-2">
                        <CommentForm
                            onSubmit={handleReply}
                            placeholder="Write a reply..."
                            buttonText="Post Reply"
                            isReply={true}
                            onCancel={() => setIsReplying(false)}
                        />
                    </div>
                )}
            </div>

            {hasReplies && (
                <div className="mt-2 mb-3 ml-4">
                    <button
                        onClick={toggleReplies}
                        className="text-sm text-blue-600 hover:text-blue-800 flex items-center font-medium hover:underline transition-colors"
                    >
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1" viewBox="0 0 20 20" fill="currentColor">
                            {showReplies ? (
                                <path fillRule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd" />
                            ) : (
                                <path fillRule="evenodd" d="M14.707 12.707a1 1 0 01-1.414 0L10 9.414l-3.293 3.293a1 1 0 01-1.414-1.414l4-4a1 1 0 011.414 0l4 4a1 1 0 010 1.414z" clipRule="evenodd" />
                            )}
                        </svg>
                        {showReplies ? "Hide replies" : `Show ${comment.replies.length} ${comment.replies.length === 1 ? 'reply' : 'replies'}`}
                    </button>
                </div>
            )}

            {showReplies && hasReplies && (
                <div className={`mt-2 border-l-2 border-blue-300 pl-3 ml-2`}>
                    {comment.replies.map((reply) => (
                        <CommentItem
                            key={reply.id}
                            comment={reply}
                            onReplySubmit={onReplySubmit}
                            onDeleteComment={onDeleteComment}
                            onEditComment={onEditComment}
                            isAuthenticated={isAuthenticated}
                            currentUserId={currentUserId}
                            depth={depth + 1}
                        />
                    ))}
                </div>
            )}
        </div>
    );
};