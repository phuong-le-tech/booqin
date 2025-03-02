// src/components/BookCard.tsx
import React, {useContext} from 'react';
import { Link } from 'react-router-dom';
import { BookIndex } from '../model/Book';
import axiosInstance from "../services/AxiosInstance.ts";
import {AuthContext} from "../context/AuthContext.tsx";

interface BookCardProps {
    book: BookIndex;
}

export const BookCard: React.FC<BookCardProps> = ({ book }) => {


    const { id } = book;
    const { user } = useContext(AuthContext);
    const userId = user?.id;
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

    const addToCart = async () => {
        try {
            const response = await axiosInstance.post("/v1/cart/add-book", {
                userId,
                bookId: id,
            });

            if (response.status === 200) {
                alert("Le livre a été ajouté au panier");
            }
        } catch (error) {
            console.error("Erreur lors de l'ajout du livre au panier", error);
            console.log(error.response?.data);
        }
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
        <div className="bg-white rounded-lg overflow-hidden shadow-md hover:shadow-lg transition-shadow duration-300">
        <Link to={`/books/${book.id}`} className="block">

                {/* Couverture de livre stylisée */}
                <div
                    className="w-full h-48 flex items-center justify-center"
                    style={{ backgroundColor: getBookCoverColor(book.id) }}
                >
          <span className="text-white text-5xl font-bold">
            {book.title.charAt(0).toUpperCase()}
          </span>
                </div>

                {/* Informations du livre */}
                <div className="p-4">
                    <h3 className="text-lg font-semibold text-gray-800 line-clamp-2 mb-2">
                        {book.title}
                    </h3>

                    <p className="text-sm text-gray-600 line-clamp-1 mb-2">
                        {book.authorNames && book.authorNames.length > 0
                            ? book.authorNames.join(', ')
                            : 'Unknown author'}
                    </p>

                    {book.publicationDate && (
                        <p className="text-xs text-gray-500">
                            Published: {formatDate(book.publicationDate)}
                        </p>
                    )}
                </div>

        </Link>
        <button
            onClick={addToCart}
            className="m-3 bg-light-blue text-navbar px-4 py-2 rounded-lg hover:bg-blue-500 transition"
        >
            Add to cart
        </button>
        </div>
);
};