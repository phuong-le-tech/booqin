import React from "react";
import { CommentItem } from "./CommentItem";

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

interface CommentTreeProps {
    comments: CommentDTO[];
    onReplySubmit: (content: string, parentId: number) => void;
    onDeleteComment: (commentId: number) => void;
    onEditComment: (commentId: number, content: string) => void;
    isAuthenticated: boolean;
    currentUserId?: number;
}

export const CommentTree: React.FC<CommentTreeProps> = ({
                                                            comments,
                                                            onReplySubmit,
                                                            onDeleteComment,
                                                            onEditComment,
                                                            isAuthenticated,
                                                            currentUserId
                                                        }) => {
    return (
        <div className="mt-6 space-y-6">
            {comments.map((comment) => (
                <CommentItem
                    key={comment.id}
                    comment={comment}
                    onReplySubmit={onReplySubmit}
                    onDeleteComment={onDeleteComment}
                    onEditComment={onEditComment}
                    isAuthenticated={isAuthenticated}
                    currentUserId={currentUserId}
                    depth={0}
                />
            ))}
        </div>
    );
};