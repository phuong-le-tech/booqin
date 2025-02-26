import React, {useState, useEffect, useContext} from "react";
import axios from "axios";
import { Layout } from "../Layout";
import {AuthContext} from "../../context/AuthContext.tsx";

type Book = {
    id: string;
    title: string;
    author: string;
    price: number;
};

type Cart = {
    books: Book[];
};

export const ShoppingCart: React.FC = () => {
     const {user} = useContext(AuthContext);
     const userId = user?.id;
    const [cart, setCart] = useState<Cart>({ books: [] });
    const [loading, setLoading] = useState(true);

    const deliveryFee = 5.99;


    const fetchCart = async () => {
        try {
            const response = await axios.get(`http://localhost:8080/api/cart/${userId}`);
            setCart(response.data);
        } catch (error) {
            console.error("Erreur lors de la récupération du panier", error);
        } finally {
            setLoading(false);
        }
    };


    const removeBook = async (bookId: string) => {
        try {
            await axios.delete(`http://localhost:8080/api/cart/remove?userId=${userId}&bookId=${bookId}`);
            fetchCart();
        } catch (error) {
            console.error("Erreur lors de la suppression du livre", error);
        }
    };

    const clearCart = async () => {
        try {
            await axios.delete(`http://localhost:8080/api/cart/clear/${userId}`);
            setCart({ books: [] });
        } catch (error) {
            console.error("Erreur lors de la suppression du panier", error);
        }
    };

    useEffect(() => {
        fetchCart();
    }, []);

    const subtotal = cart.books.reduce((sum, book) => sum + book.price, 0);
    const total = subtotal + deliveryFee;

    return (
        <Layout>
            <div className="container mx-auto px-4 py-8 flex flex-col items-center">
                <h2 className="text-3xl font-semibold mb-6 text-light-blue">Votre Panier</h2>

                {loading ? (
                    <p className="text-gray-300 text-lg">Chargement du panier...</p>
                ) : cart.books.length === 0 ? (
                    <p className="text-gray-300 text-lg">Votre panier est vide.></p>

                ) : (
                    <>
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 w-full max-w-4xl">
                            {cart.books.map((book) => (
                                <div key={book.id} className="bg-navbar shadow-md rounded-lg p-5 text-white flex flex-col items-center">
                                    <h3 className="text-xl font-bold">{book.title}</h3>
                                    <p className="text-gray-300">{book.author}</p>
                                    <p className="text-light-blue font-semibold text-lg">{book.price.toFixed(2)} €</p>
                                    <button
                                        onClick={() => removeBook(book.id)}
                                        className="mt-3 bg-red-500 text-white px-4 py-2 rounded-lg hover:bg-red-600 transition"
                                    >
                                        Supprimer
                                    </button>
                                </div>
                            ))}
                        </div>

                        <div className="mt-8 bg-navbar shadow-lg rounded-lg p-6 w-full max-w-md text-white text-center">
                            <h3 className="text-2xl font-bold text-light-blue mb-4">Récapitulatif</h3>
                            <p className="text-gray-300 text-lg">Sous-total: <span className="font-semibold">{subtotal.toFixed(2)} €</span></p>
                            <p className="text-gray-300 text-lg">Livraison: <span className="font-semibold">{deliveryFee} €</span></p>
                            <p className="text-2xl font-bold text-light-blue mt-3">Total: {total.toFixed(2)} €</p>

                            <button
                                className="mt-4 w-full bg-light-blue text-navbar py-3 rounded-lg hover:bg-blue-500 transition text-lg font-semibold"
                                onClick={() => alert(`Paiement simulé de ${total.toFixed(2)} €`)}
                            >
                                Payer {total.toFixed(2)} €
                            </button>
                            <button
                                className="mt-2 w-full bg-red-500 text-white py-3 rounded-lg hover:bg-red-600 transition text-lg font-semibold"
                                onClick={clearCart}
                            >
                                Vider le panier
                            </button>
                        </div>
                    </>
                )}
            </div>
        </Layout>
    );
};

export default ShoppingCart;
