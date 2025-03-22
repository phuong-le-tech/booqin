import React, { useState, useEffect } from 'react';
import { VoteButton } from './VoteButton';
import VoteService from '../../services/VoteService';
import { VoteCount, VoteType } from '../../model/Vote';
import { useAuth } from '../../context/AuthContext';

interface VoteSectionProps {
    entityId: number;
    entityType: string;
}

export const VoteSection: React.FC<VoteSectionProps> = ({ entityId, entityType }) => {
    const [voteCount, setVoteCount] = useState<VoteCount | null>(null);
    const [userVote, setUserVote] = useState<VoteType | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const { user } = useAuth();

    // Normalize entity type (ensure uppercase for backend compatibility)
    const normalizedEntityType = entityType.toUpperCase();

    // Fetch initial vote data
    useEffect(() => {
        const fetchVoteData = async () => {
            try {
                setIsLoading(true);
                console.log(`Fetching vote data for entity: ${entityId}, type: ${normalizedEntityType}`);

                // Get vote counts for this entity
                const counts = await VoteService.getVoteCounts(entityId, normalizedEntityType);
                setVoteCount(counts as VoteCount);

                // If user is logged in, get their vote
                if (user) {
                    try {
                        const voteType = await VoteService.getUserVoteType(user.id, entityId, normalizedEntityType);
                        setUserVote(voteType as VoteType);
                    } catch (userVoteErr) {
                        console.warn('Could not get user vote type:', userVoteErr);
                        // Continue anyway as this is non-critical
                    }
                }

                setError(null);
            } catch (err) {
                console.error('Failed to fetch vote data:', err);
                setError('Failed to load vote information');
            } finally {
                setIsLoading(false);
            }
        };

        fetchVoteData();
    }, [entityId, normalizedEntityType, user]);

    // Handle vote action (cast, update, or remove vote)
    const handleVote = async (voteType: VoteType) => {
        if (!user) {
            // Redirect to login or show login modal
            alert('Please log in to vote');
            return;
        }

        try {
            // If user already voted with the same type, remove the vote
            if (userVote === voteType) {
                await VoteService.deleteVote(user.id, entityId, entityType);
                setUserVote(null);

                // Update local vote count
                if (voteCount) {
                    const updatedCount = { ...voteCount };
                    if (voteType === 'UPVOTE') {
                        updatedCount.upvoteCount = Math.max(0, updatedCount.upvoteCount - 1);
                    } else {
                        updatedCount.downvoteCount = Math.max(0, updatedCount.downvoteCount - 1);
                    }
                    updatedCount.score = updatedCount.upvoteCount - updatedCount.downvoteCount;
                    setVoteCount(updatedCount);
                }
            }
            // If user already voted but with a different type, update the vote
            else if (userVote !== null) {
                await VoteService.updateVote(user.id, entityId, entityType, voteType);
                setUserVote(voteType);

                // Update local vote count
                if (voteCount) {
                    const updatedCount = { ...voteCount };
                    if (voteType === 'UPVOTE') {
                        updatedCount.upvoteCount += 1;
                        updatedCount.downvoteCount = Math.max(0, updatedCount.downvoteCount - 1);
                    } else {
                        updatedCount.downvoteCount += 1;
                        updatedCount.upvoteCount = Math.max(0, updatedCount.upvoteCount - 1);
                    }
                    updatedCount.score = updatedCount.upvoteCount - updatedCount.downvoteCount;
                    setVoteCount(updatedCount);
                }
            }
            // If user hasn't voted yet, cast a new vote
            else {
                await VoteService.castVote(user.id, entityId, entityType, voteType);
                setUserVote(voteType);

                // Update local vote count
                if (voteCount) {
                    const updatedCount = { ...voteCount };
                    if (voteType === 'UPVOTE') {
                        updatedCount.upvoteCount += 1;
                    } else {
                        updatedCount.downvoteCount += 1;
                    }
                    updatedCount.score = updatedCount.upvoteCount - updatedCount.downvoteCount;
                    setVoteCount(updatedCount);
                }
            }
        } catch (err) {
            console.error('Failed to process vote:', err);
            setError('Failed to process your vote. Please try again.');
        }
    };

    if (isLoading) {
        return (
            <div className="flex items-center space-x-2 animate-pulse">
                <div className="h-10 w-16 bg-gray-200 rounded"></div>
                <div className="h-10 w-16 bg-gray-200 rounded"></div>
            </div>
        );
    }

    if (error || !voteCount) {
        return (
            <div className="text-sm text-gray-500">
                Unable to load voting information
            </div>
        );
    }

    return (
        <div className="flex flex-col space-y-2">
            <div className="flex items-center space-x-2">
                <VoteButton
                    type="UPVOTE"
                    count={voteCount.upvoteCount}
                    isActive={userVote === 'UPVOTE'}
                    onClick={() => handleVote('UPVOTE')}
                />
                <VoteButton
                    type="DOWNVOTE"
                    count={voteCount.downvoteCount}
                    isActive={userVote === 'DOWNVOTE'}
                    onClick={() => handleVote('DOWNVOTE')}
                />
                <div className="text-sm text-gray-600 ml-2">
                    Score: <span className="font-medium">{voteCount.score}</span>
                </div>
            </div>
            {!user && (
                <p className="text-xs text-gray-500">Sign in to vote</p>
            )}
        </div>
    );
};