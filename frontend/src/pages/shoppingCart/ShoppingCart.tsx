import React, { useContext, useEffect, useState } from "react";
import { Layout } from "../Layout";
import axiosInstance from "../../services/AxiosInstance.ts";
import { AuthContext } from "../../context/AuthContext.tsx";
import { useNavigate } from "react-router-dom";


type Book = {
    id: string;
    title: string;
    author: string;
    price: number;
};

type CartDto = {
    id: number;
    userId: number;
    books: Book[];
};

export const ShoppingCart: React.FC = () => {
    const [cart, setCart] = useState<CartDto | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const navigate = useNavigate(); //
    const auth = useContext(AuthContext);

    useEffect(() => {
        const savedCart = localStorage.getItem("cart");
        if (savedCart) {
            setCart(JSON.parse(savedCart));
        }
    }, []);


    useEffect(() => {
        if (auth === undefined) {
            console.log("🔄 Contexte Auth en cours d'initialisation...");
            return;
        }

        if (auth.user === undefined) {
            console.log("⏳ AuthContext pas encore prêt, on attend...");
            return;
        }

        if (!auth.user || !auth.user.id) {
            console.log("🚨 Utilisateur non connecté. Attente avant de récupérer le panier...");
            return; // ❌ Ne pas afficher l'erreur immédiatement
        }

        console.log("✅ Utilisateur détecté, ID:", auth.user.id);

        const fetchCart = async () => {
            try {
                console.log("📦 Récupération du panier pour userId:", auth.user.id);
                const response = await axiosInstance.get(`/v1/cart/user-cart/${auth.user.id}`);
                console.log("📦 Réponse API complète:", response.data);

                if (response.data && response.data.success && response.data.data) {
                    let booksArray = Array.isArray(response.data.data.books)
                        ? response.data.data.books
                        : Object.values(response.data.data.books || {});

                    const newCart = { ...response.data.data, books: booksArray };
                    setCart(newCart);
                    localStorage.setItem("cart", JSON.stringify(newCart));
                } else {
                    throw new Error("Format de réponse inattendu");
                }
            } catch (err: any) {
                console.error("❌ Erreur Axios détaillée:", err);
                setError(err.response?.data?.message || "Erreur lors du chargement du panier.");
            } finally {
                setLoading(false);
            }
        };

        fetchCart();
    }, [auth.user]);

    const removeFromCart = async (bookId: string) => {
        if (!cart || !auth.user) {
            console.warn("⚠️ Impossible de supprimer: `cart` ou `auth.user` est `null`", { cart, user: auth.user });
            return;
        }


        const previousCart = { ...cart };

        setCart((prevCart) => {
            if (!prevCart) return null;
            return {
                ...prevCart,
                books: prevCart.books.filter((book) => book.id !== bookId),
            };
        });

        try {
            console.log("📤 Envoi de la requête de suppression au backend...", {
                userId: auth.user.id,
                bookId: bookId
            });

            const response = await axiosInstance.delete("/v1/cart/remove-book", {
                params: { userId: auth.user.id, bookId: bookId },
            });

            console.log("✅ Réponse API après suppression:", response);

            if (response.status === 200) {
                console.log("🗑️ Suppression réussie !");
                localStorage.setItem("cart", JSON.stringify(cart));
            } else {
                console.warn("⚠️ Suppression échouée, statut:", response.status);
                alert("Impossible de supprimer le livre.");


                setCart(previousCart);
            }
        } catch (error: any) {
            console.error("❌ Erreur lors de la suppression du livre du panier", error);

            setCart(previousCart);

            if (error.response) {
                console.error("📡 Réponse d'erreur de l'API:", {
                    status: error.response.status,
                    data: error.response.data,
                    headers: error.response.headers,
                });

                alert(error.response.data?.message || "Erreur lors de la suppression du livre.");
            } else if (error.request) {
                console.error("⏳ Aucune réponse du serveur, problème de connexion", error.request);
                alert("Le serveur ne répond pas. Vérifiez votre connexion.");
            } else {
                console.error("⚙️ Erreur de configuration Axios", error.message);
                alert("Une erreur est survenue.");
            }
        }
    };

    const clearCart = async () => {
        if (!cart || !auth.user) {
            console.warn("⚠️ Impossible de vider le panier: `cart` ou `auth.user` est `null`", { cart, user: auth.user });
            return;
        }

        setCart(null);
        localStorage.removeItem("cart");

        try {
            console.log("📤 Envoi de la requête pour vider le panier de l'utilisateur ID:", auth.user.id);

            const response = await axiosInstance.delete(`/v1/cart/clear-cart/${auth.user.id}`);

            console.log("✅ Réponse API après vidage du panier:", response);

            if (response.status === 200) {
                console.log("🗑️ Panier vidé avec succès !");
                alert("Votre panier a été vidé !");
            } else {
                console.warn("⚠️ Échec du vidage du panier, statut:", response.status);
                alert("Impossible de vider le panier.");
            }
        } catch (error: any) {
            console.error("❌ Erreur lors du vidage du panier", error);
            alert("Une erreur est survenue lors du vidage du panier.");
        }
    };





    const removeBook = async (id: string) => {
        if (!cart || !auth.user) return;

        try {
            const updatedCart = {
                ...cart,
                books: cart.books.filter((book) => book.id !== id)
            };
            setCart(updatedCart);
            localStorage.setItem("cart", JSON.stringify(updatedCart));
        } catch (err: any) {
            setError("Impossible de supprimer le livre.");
        }
    };

    const calculateSubtotal = () => {
        if (!cart || !cart.books) return 0;
        return cart.books.reduce((sum, book) => sum + (book.price ?? 0), 0);
    };

    const deliveryFee = 5.99;
    const subtotal = 5.99;
    const total = subtotal ;

    return (
        <Layout>
            <div className="container mx-auto px-4 py-12 flex flex-col items-center  min-h-screen">
                <h2 className="text-4xl font-bold mb-8 text-light-blue">
                    Votre Panier
                </h2>

                {loading ? (
                    <div className="w-full max-w-4xl flex justify-center p-12">
                        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-light-blue"></div>
                    </div>
                ) : error ? (
                    <div className="w-full max-w-4xl bg-red-100 border border-red-300 rounded-lg p-6 text-center">
                        <p className="text-red-600 text-lg font-medium">{error}</p>
                    </div>
                ) : !cart?.books?.length ? (
                    <div className="w-full max-w-4xl bg-gray-50 rounded-lg p-12 text-center border border-gray-200 shadow-md">
                        <svg className="w-24 h-24 mx-auto text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"></path>
                        </svg>
                        <p className="text-gray-700 text-xl font-medium">Votre panier est vide</p>
                        <p className="text-gray-500 mt-2 mb-6">Ajoutez des livres à votre panier pour commencer vos achats</p>
                        <button
                            onClick={() => navigate("/books")}
                            className="bg-light-blue hover:bg-blue-500 text-white font-semibold px-6 py-3 rounded-lg transition duration-300 transform hover:scale-105"
                        >
                            Parcourir la boutique
                        </button>

                    </div>
                ) : (
                    <div className="w-full max-w-6xl flex flex-col lg:flex-row gap-8">
                        <div className="flex-1">
                            <div className="bg-navbar rounded-xl overflow-hidden shadow-xl">
                                <div className="p-6 border-b border-gray-700">
                                    <h3 className="text-xl font-semibold text-white">Articles ({cart.books.length})</h3>
                                </div>
                                <div className="divide-y divide-gray-700">
                                    {cart.books.map((book) => (
                                        <div key={book.id} className="p-6 flex flex-col sm:flex-row gap-6 items-center hover:bg-gray-800 transition-colors">
                                            <div className="h-32 w-24 bg-gradient-to-br from-blue-900 to-purple-900 rounded-lg flex items-center justify-center flex-shrink-0">
                                                <svg className="w-12 h-12 text-blue-300/70" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"></path>
                                                </svg>
                                            </div>
                                            <div className="flex-1 text-center sm:text-left">
                                                <h3 className="text-xl font-bold text-white">{book.title}</h3>
                                                <p className="text-gray-300 text-sm">{book.author}</p>
                                                <div className="mt-2 flex flex-col sm:flex-row sm:items-center sm:justify-between">
                                                    <p className="text-light-blue font-semibold text-xl">{(book.price ?? 0).toFixed(2)} €</p>
                                                    <button
                                                        onClick={() => removeFromCart(book.id)}
                                                        className="mt-3 sm:mt-0 text-sm group flex items-center text-red-400 hover:text-red-300 transition-colors"
                                                    >
                                                        <svg className="w-5 h-5 mr-1 group-hover:scale-110 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                                                        </svg>
                                                        Supprimer
                                                    </button>
                                                </div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        </div>

                        <div className="lg:w-80 w-full">
                            <div className="bg-navbar rounded-xl overflow-hidden shadow-xl sticky top-24">
                                <div className="p-6 border-b border-gray-700">
                                    <h3 className="text-xl font-semibold text-white">Récapitulatif</h3>
                                </div>
                                <div className="p-6 space-y-4">
                                    <div className="flex justify-between text-gray-300">
                                        <span>Sous-total</span>
                                        <span className="font-medium">{subtotal.toFixed(2)} €</span>
                                    </div>
                                    <div className="flex justify-between text-gray-300">
                                        <span>Livraison</span>
                                        <span className="font-medium">{deliveryFee.toFixed(2)} €</span>
                                    </div>
                                    <div className="pt-4 border-t border-gray-700">
                                        <div className="flex justify-between text-xl">
                                            <span className="font-semibold text-white">Total</span>
                                            <span className="font-bold text-light-blue">{total.toFixed(2)} €</span>
                                        </div>
                                    </div>
                                    <button
                                        onClick={() => alert(`Paiement simulé de ${total.toFixed(2)} €`)}
                                        className="mt-6 w-full bg-light-blue text-white py-4 rounded-lg font-semibold text-lg shadow-lg hover:bg-blue-500 transition-all duration-300 transform hover:scale-105 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-opacity-50"
                                    >
                                        Payer maintenant
                                    </button>
                                    <button
                                        onClick={clearCart}
                                        className="mt-4 w-full bg-red-500 text-white py-3 rounded-lg hover:bg-red-600 transition text-lg font-semibold"
                                    >
                                        🗑️ Vider le panier
                                    </button>

                                    <p className="text-center text-gray-400 text-sm mt-4">
                                        Paiement 100% sécurisé
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </Layout>
    );
};

export default ShoppingCart;