import React from 'react';

interface VoteButtonProps {
    type: 'UPVOTE' | 'DOWNVOTE';
    count: number;
    isActive: boolean;
    isDisabled?: boolean;
    onClick: () => void;
}

export const VoteButton: React.FC<VoteButtonProps> = ({
                                                          type,
                                                          count,
                                                          isActive,
                                                          isDisabled = false,
                                                          onClick
                                                      }) => {
    const baseClasses = "flex items-center justify-center gap-1 px-3 py-2 rounded-md transition-colors";

    // Different styles for upvote and downvote
    const getButtonClasses = () => {
        if (isDisabled) {
            return `${baseClasses} bg-gray-100 text-gray-400 cursor-not-allowed`;
        }

        if (type === 'UPVOTE') {
            return isActive
                ? `${baseClasses} bg-green-100 text-green-700 border border-green-300`
                : `${baseClasses} bg-gray-100 text-gray-600 hover:bg-green-50 hover:text-green-600`;
        } else {
            return isActive
                ? `${baseClasses} bg-red-100 text-red-700 border border-red-300`
                : `${baseClasses} bg-gray-100 text-gray-600 hover:bg-red-50 hover:text-red-600`;
        }
    };

    return (
        <button
            className={getButtonClasses()}
            onClick={onClick}
            disabled={isDisabled}
            aria-label={type === 'UPVOTE' ? 'Upvote' : 'Downvote'}
        >
            {type === 'UPVOTE' ? (
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M14.707 12.707a1 1 0 01-1.414 0L10 9.414l-3.293 3.293a1 1 0 01-1.414-1.414l4-4a1 1 0 011.414 0l4 4a1 1 0 010 1.414z" clipRule="evenodd" />
                </svg>
            ) : (
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd" />
                </svg>
            )}
            <span>{count}</span>
        </button>
    );
};