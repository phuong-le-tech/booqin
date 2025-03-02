import React from 'react';
import { Link } from 'react-router-dom';
import { BookIndex } from '../model/Book';

interface BookCardProps {
    book: BookIndex;
}

export const BookCard: React.FC<BookCardProps> = ({ book }) => {
    // Fonction pour générer une couleur à partir de l'id du livre
    const getBookCoverColor = (bookId: number) => {
        const colors = [
            '#1e3a8a', // bleu foncé
            '#1e40af',
            '#1d4ed8',
            '#2563eb', // bleu moyen
            '#3b82f6',
            '#60a5fa', // bleu clair
            '#93c5fd'
        ];
        const colorIndex = bookId % colors.length;
        return colors[colorIndex];
    };

    // Fonction pour formater la date
    const formatDate = (dateString: string) => {
        try {
            const date = new Date(dateString);
            return date.toLocaleDateString(undefined, {
                year: 'numeric',
                month: 'long'
            });
        } catch (e) {
            return dateString;
        }
    };

    return (
        <Link to={`/books/${book.id}`} className="block h-full">
            <div className="bg-white rounded-lg overflow-hidden shadow-md hover:shadow-lg transition-shadow duration-300 flex flex-col h-full">
                {/* Couverture de livre stylisée */}
                <div
                    className="w-full h-48 flex items-center justify-center flex-shrink-0"
                    style={{ backgroundColor: getBookCoverColor(book.id) }}
                >
                    <span className="text-white text-5xl font-bold">
                        {book.title.charAt(0).toUpperCase()}
                    </span>
                </div>

                {/* Informations du livre */}
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
            </div>
        </Link>
    );
};