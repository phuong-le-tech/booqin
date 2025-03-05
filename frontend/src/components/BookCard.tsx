import React, { useContext } from 'react';
import { Link } from 'react-router-dom';
import { BookIndex } from '../model/Book';
import axiosInstance from "../services/AxiosInstance";
import { AuthContext } from "../context/AuthContext";

interface BookCardProps {
    book: BookIndex;
}

export const BookCard: React.FC<BookCardProps> = ({ book }) => {
    const { id } = book;
    const { user } = useContext(AuthContext);
    const userId = user?.id;

    // Function to generate a color from the book id
    const getBookCoverColor = (bookId: number) => {
        const colors = [
            '#1e3a8a', // dark blue
            '#1e40af',
            '#1d4ed8',
            '#2563eb', // medium blue
            '#3b82f6',
            '#60a5fa', // light blue
            '#93c5fd'
        ];
        const colorIndex = bookId % colors.length;
        return colors[colorIndex];
    };

    // Function to add a book to cart
    const addToCart = async () => {
        try {
            const response = await axiosInstance.post("/v1/carts/add-book", {
                userId,
                bookId: id,
            });

            if (response.status === 200) {
                alert("The book has been added to cart");
            }
        } catch (error: any) {
            console.error("Error adding book to cart", error);
            console.log(error.response?.data);
        }
    };

    // Function to format date in English
    const formatDate = (dateString: string) => {
        try {
            const date = new Date(dateString);
            return date.toLocaleDateString('en-US', {
                year: 'numeric',
                month: 'long'
            });
        } catch (e) {
            return dateString;
        }
    };

    return (
        <div className="bg-white rounded-lg overflow-hidden shadow-md hover:shadow-lg transition-shadow duration-300 flex flex-col h-full">
            <Link to={`/books/${book.id}`} className="block flex-grow">
                {/* Styled book cover */}
                <div
                    className="w-full h-48 flex items-center justify-center flex-shrink-0"
                    style={{ backgroundColor: getBookCoverColor(book.id) }}
                >
                    <span className="text-white text-5xl font-bold">
                        {book.title.charAt(0).toUpperCase()}
                    </span>
                </div>

                {/* Book information */}
                <div className="p-4 flex flex-col flex-grow">
                    <h3 className="text-lg font-semibold text-gray-800 line-clamp-2 mb-2 h-14">
                        {book.title}
                    </h3>

                    <p className="text-sm text-gray-600 line-clamp-1 mb-2 h-5">
                        {book.authorNames && book.authorNames.length > 0
                            ? book.authorNames.join(', ')
                            : 'Unknown author'}
                    </p>

                    <div className="mt-auto">
                        {book.publicationDate && (
                            <p className="text-xs text-gray-500">
                                Published: {formatDate(book.publicationDate)}
                            </p>
                        )}
                    </div>
                </div>
            </Link>

            {/* Add to Cart button */}
            <button
                onClick={addToCart}
                className="m-3 bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600 transition"
            >
                Add to cart
            </button>
        </div>
    );
};