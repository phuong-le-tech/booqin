import React, { useState, useEffect, useRef } from "react";

interface CommentFormProps {
    onSubmit: (content: string) => void;
    initialValue?: string;
    placeholder?: string;
    buttonText?: string;
    isReply?: boolean;
    onCancel?: () => void;
}

export const CommentForm: React.FC<CommentFormProps> = ({
                                                            onSubmit,
                                                            initialValue = "",
                                                            placeholder = "Write your comment...",
                                                            buttonText = "Post Comment",
                                                            isReply = false,
                                                            onCancel
                                                        }) => {
    const [content, setContent] = useState(initialValue);
    const [isFocused, setIsFocused] = useState(false);
    const [charCount, setCharCount] = useState(initialValue.length);
    const textAreaRef = useRef<HTMLTextAreaElement>(null);
    const maxLength = 1000;

    useEffect(() => {
        if (initialValue && textAreaRef.current) {
            textAreaRef.current.focus();
        }
    }, [initialValue]);

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (content.trim()) {
            onSubmit(content);
            setContent("");
            setIsFocused(false);
            setCharCount(0);
        }
    };

    const handleContentChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
        const newContent = e.target.value;
        if (newContent.length <= maxLength) {
            setContent(newContent);
            setCharCount(newContent.length);
        }
    };

    const handleCancel = () => {
        setContent("");
        setIsFocused(false);
        if (onCancel) {
            onCancel();
        }
    };

    const hasContent = content.trim().length > 0;
    const isNearLimit = charCount > maxLength * 0.8;

    return (
        <form onSubmit={handleSubmit} className={`${isReply ? 'mt-2' : 'mb-6'}`}>
            <div className={`flex flex-col transition-all duration-200 ${
                isReply
                    ? 'border-l-2 border-blue-100 pl-3'
                    : 'border border-gray-300 rounded-lg p-3 bg-white'
            } ${isFocused ? 'shadow-md' : ''}`}
            >
                <div className="flex items-start">
                    {!isReply && (
                        <div className="h-10 w-10 rounded-full bg-blue-600 text-white flex items-center justify-center text-sm font-medium mr-3 mt-2 flex-shrink-0">
                            Y
                        </div>
                    )}
                    <textarea
                        ref={textAreaRef}
                        value={content}
                        onChange={handleContentChange}
                        onFocus={() => setIsFocused(true)}
                        onBlur={() => setTimeout(() => setIsFocused(false), 100)}
                        placeholder={placeholder}
                        rows={isReply ? 2 : isFocused ? 4 : 2}
                        className={`w-full px-3 py-2 ${
                            isReply ? '' : 'border border-gray-300'
                        } rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none transition-all duration-200`}
                        required
                    />
                </div>

                <div className={`mt-2 flex ${isReply ? 'justify-end' : 'justify-between items-center'}`}>
                    <div className={`flex items-center ${isReply ? '' : 'ml-auto'}`}>
                        <div className={`mr-3 text-xs ${isNearLimit ? 'text-red-500 font-medium' : 'text-gray-500'}`}>
                            {charCount}/{maxLength}
                        </div>

                        {(isReply || initialValue) && (
                            <button
                                type="button"
                                onClick={handleCancel}
                                className="px-3 py-1 mr-2 text-sm text-gray-600 hover:text-gray-800 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-opacity-50 rounded-md"
                            >
                                Cancel
                            </button>
                        )}

                        <button
                            type="submit"
                            disabled={!hasContent}
                            className={`px-4 py-2 ${isReply ? 'text-sm' : ''} bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-opacity-50 transition-colors duration-200 ${!hasContent ? 'opacity-50 cursor-not-allowed' : ''}`}
                        >
                            {buttonText}
                        </button>
                    </div>
                </div>
            </div>
        </form>
    );
};