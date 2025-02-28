// src/components/BookCard.tsx
import React from 'react';
import { Link } from 'react-router-dom';
import { Book } from '../model/Book';

interface BookCardProps extends Book {}

export const BookCard: React.FC<BookCardProps> = ({
                                                      id,
                                                      title,
                                                      authors,
                                                      description,
                                                      publicationDate
                                                  }) => {
    // Limiter la description à un nombre de caractères
    const truncateDescription = (text: string, maxLength: number = 120) => {
        if (!text) return '';
        if (text.length <= maxLength) return text;
        return text.substring(0, maxLength).trim() + '...';
    };

    // Obtenir les noms d'auteurs formatés avec une limite
    const formatAuthors = (authors: any[], maxToShow: number = 2) => {
        if (!authors || authors.length === 0) return 'Unknown Author';

        if (authors.length <= maxToShow) {
            return authors.map(author => author.fullName).join(', ');
        } else {
            const shownAuthors = authors.slice(0, maxToShow).map(author => author.fullName).join(', ');
            return `${shownAuthors} +${authors.length - maxToShow} more`;
        }
    };

    const authorNames = formatAuthors(authors);

    // Générer une couleur de fond aléatoire dans des tons de bleu pour la couverture
    // On utilise l'ID du livre pour avoir une couleur constante pour le même livre
    const getBookCoverColor = () => {
        const colors = [
            '#1e3a8a', // bleu foncé
            '#1e40af',
            '#1d4ed8',
            '#2563eb', // bleu moyen
            '#3b82f6',
            '#60a5fa', // bleu clair
            '#93c5fd'
        ];
        const colorIndex = parseInt(id.toString().slice(-1)) % colors.length;
        return colors[colorIndex];
    };

    return (
        <Link to={`/books/${id}`} className="block h-full">
            <div className="h-full bg-white rounded-xl shadow-md overflow-hidden hover:shadow-lg transition-all duration-300 transform hover:-translate-y-1 border border-gray-100 flex flex-col">
                {/* Image de couverture (placeholder coloré) */}
                <div
                    className="w-full h-48 flex items-center justify-center text-white font-bold text-xl"
                    style={{ backgroundColor: getBookCoverColor() }}
                >
                    {title.substring(0, 1).toUpperCase()}
                </div>

                <div className="p-5 flex-grow flex flex-col">
                    {/* Titre et auteur */}
                    <div className="flex-grow">
                        <h3 className="text-lg font-bold text-gray-800 mb-1 line-clamp-2 hover:text-blue-600 transition-colors">{title}</h3>
                        <p className="text-sm text-blue-700 mb-2 font-medium line-clamp-1" title={authors && authors.length > 0 ? authors.map(author => author.fullName).join(', ') : ''}>{authorNames}</p>

                        {publicationDate && (
                            <p className="text-xs text-gray-500 mb-3">Published: {publicationDate}</p>
                        )}

                        {/* Description avec taille maximale */}
                        <div className="mt-2">
                            <p className="text-sm text-gray-600 line-clamp-3 h-14 overflow-hidden">
                                {truncateDescription(description)}
                            </p>
                        </div>
                    </div>

                    {/* Bouton "View details" */}
                    <div className="mt-4 pt-3 border-t border-gray-100">
                        <div className="flex justify-center">
              <span className="inline-block px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors">
                View details
              </span>
                        </div>
                    </div>
                </div>
            </div>
        </Link>
    );
};