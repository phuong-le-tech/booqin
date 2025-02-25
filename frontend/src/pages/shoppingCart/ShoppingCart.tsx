import React, { useState } from "react";

type Book = {
    id: string;
    title: string;
    author: string;
    price: number;
};

export const ShoppingCart: React.FC = () => {
    const [cartBooks, setCartBooks] = useState<Book[]>([
        { id: "1", title: "The Great Gatsby", author: "F. Scott Fitzgerald", price: 14.99 },
        { id: "2", title: "To Kill a Mockingbird", author: "Harper Lee", price: 12.99 },
        { id: "3", title: "1984", author: "George Orwell", price: 10.99 }
    ]);

    const deliveryFee = 5.99;


    const removeBook = (id: string) => {
        setCartBooks(cartBooks.filter((book) => book.id !== id));
    };


    const subtotal = cartBooks.reduce((sum, book) => sum + book.price, 0);
    const total = subtotal + deliveryFee;

    return (
        <div className="max-w-lg mx-auto bg-white dark:bg-gray-800 shadow-lg rounded-lg p-6">
            <h2 className="text-2xl font-semibold text-gray-900 dark:text-white mb-4">🛒 Votre Panier</h2>

            {cartBooks.length === 0 ? (
                <p className="text-gray-600 dark:text-gray-300">Votre panier est vide.</p>
            ) : (
                <>
                    <ul className="space-y-4">
                        {cartBooks.map((book) => (
                            <li key={book.id} className="flex items-center justify-between border-b pb-3">
                                <div>
                                    <h3 className="text-lg font-bold text-gray-900 dark:text-white">{book.title}</h3>
                                    <p className="text-gray-600 dark:text-gray-300">{book.author}</p>
                                    <p className="text-blue-600 dark:text-blue-400 font-semibold">{book.price.toFixed(2)} €</p>
                                </div>
                                <button
                                    onClick={() => removeBook(book.id)}
                                    className="bg-red-500 text-white px-3 py-1 rounded-lg hover:bg-red-600 transition"
                                >
                                    Supprimer
                                </button>
                            </li>
                        ))}
                    </ul>

                    <div className="mt-6 border-t pt-4">
                        <p className="text-gray-700 dark:text-gray-300">Sous-total: <span className="font-semibold">{subtotal.toFixed(2)} €</span></p>
                        <p className="text-gray-700 dark:text-gray-300">Livraison: <span className="font-semibold">{deliveryFee} €</span></p>
                        <p className="text-xl font-bold text-blue-600 dark:text-blue-400 mt-2">Total: {total.toFixed(2)} €</p>

                        <button
                            className="mt-4 w-full bg-blue-600 dark:bg-blue-500 text-white py-2 px-4 rounded-lg hover:bg-blue-700 dark:hover:bg-blue-400 transition"
                            onClick={() => alert(`Paiement simulé de ${total.toFixed(2)} €`)}
                        >
                            Payer {total.toFixed(2)} €
                        </button>
                    </div>
                </>
            )}
        </div>
    );
};
