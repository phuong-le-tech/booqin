import React, { useState, useEffect } from 'react';
import VoteService from '../../services/VoteService';
import { VoteType } from '../../model/Vote';
import { useAuth } from '../../context/AuthContext';

interface CommentVoteButtonsProps {
    entityId: number;
    entityType: string;
    className?: string;
}

const CommentVoteButton: React.FC<CommentVoteButtonsProps> = ({
                                                                   entityId,
                                                                   entityType,
                                                                   className = ''
                                                               }) => {
    const { user } = useAuth();
    const [score, setScore] = useState<number>(0);
    const [userVote, setUserVote] = useState<VoteType | null>(null);
    const [isLoading, setIsLoading] = useState<boolean>(false);

    // Normalize entity type (ensure uppercase for backend compatibility)
    const normalizedEntityType = entityType.toUpperCase();

    useEffect(() => {
        fetchVoteData();
    }, [entityId, normalizedEntityType, user]);

    const fetchVoteData = async () => {
        try {
            const voteCount = await VoteService.getVoteCounts(entityId, normalizedEntityType);
            if (voteCount) {
                setScore(voteCount.score);
            }

            if (user) {
                try {
                    const currentUserVote = await VoteService.getUserVoteType(user.id, entityId, normalizedEntityType);
                    if (currentUserVote === 'UPVOTE' || currentUserVote === 'DOWNVOTE') {
                        setUserVote(currentUserVote);
                    } else {
                        setUserVote(null);
                    }
                } catch (error) {
                    console.log('User has not voted yet');
                    setUserVote(null);
                }
            }
        } catch (error) {
            console.error('Error fetching vote data:', error);
        }
    };

    const handleVote = async (voteType: VoteType) => {
        if (!user) {
            return;
        }

        try {
            setIsLoading(true);

            if (userVote === voteType) {
                await VoteService.deleteVote(user.id, entityId, normalizedEntityType);
                setUserVote(null);
                // Update local score
                const voteCount = await VoteService.getVoteCounts(entityId, normalizedEntityType);
                if (voteCount) {
                    setScore(voteCount.score);
                }
            }
            else if (userVote) {
                await VoteService.updateVote(user.id, entityId, normalizedEntityType, voteType);
                setUserVote(voteType);
                const voteCount = await VoteService.getVoteCounts(entityId, normalizedEntityType);
                if (voteCount) {
                    setScore(voteCount.score);
                }
            }
            else {
                await VoteService.castVote(user.id, entityId, normalizedEntityType, voteType);
                setUserVote(voteType);
                const voteCount = await VoteService.getVoteCounts(entityId, normalizedEntityType);
                if (voteCount) {
                    setScore(voteCount.score);
                }
            }
        } catch (error) {
            console.error('Error processing vote:', error);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className={`flex items-center text-sm ${className}`}>
            <button
                onClick={() => handleVote('UPVOTE')}
                disabled={isLoading}
                className={`p-1 rounded-full focus:outline-none transition-colors ${
                    userVote === 'UPVOTE'
                        ? 'text-green-600'
                        : 'text-gray-400 hover:text-gray-600'
                }`}
                title="Upvote"
            >
                <svg
                    xmlns="http://www.w3.org/2000/svg"
                    className="h-4 w-4"
                    viewBox="0 0 20 20"
                    fill="currentColor"
                >
                    <path fillRule="evenodd" d="M14.707 12.707a1 1 0 01-1.414 0L10 9.414l-3.293 3.293a1 1 0 01-1.414-1.414l4-4a1 1 0 011.414 0l4 4a1 1 0 010 1.414z" clipRule="evenodd" />
                </svg>
            </button>

            <span className={`mx-1 font-medium text-xs ${
                score > 0
                    ? 'text-green-600'
                    : score < 0
                        ? 'text-red-600'
                        : 'text-gray-500'
            }`}>
        {score}
      </span>

            <button
                onClick={() => handleVote('DOWNVOTE')}
                disabled={isLoading}
                className={`p-1 rounded-full focus:outline-none transition-colors ${
                    userVote === 'DOWNVOTE'
                        ? 'text-red-600'
                        : 'text-gray-400 hover:text-gray-600'
                }`}
                title="Downvote"
            >
                <svg
                    xmlns="http://www.w3.org/2000/svg"
                    className="h-4 w-4"
                    viewBox="0 0 20 20"
                    fill="currentColor"
                >
                    <path fillRule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd" />
                </svg>
            </button>
        </div>
    );
};

export default CommentVoteButton;