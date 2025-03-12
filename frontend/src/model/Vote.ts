export type VoteType = 'UPVOTE' | 'DOWNVOTE';

// Vote response from API
export interface VoteResponse {
    id: number;
    userId: number;
    username: string;
    entityId: number;
    entityType: string;
    voteType: VoteType;
}

// Vote count response from API
export interface VoteCount {
    entityId: number;
    entityType: string;
    upvoteCount: number;
    downvoteCount: number;
    score: number;
}