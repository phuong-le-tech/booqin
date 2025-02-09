import React, { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

const SignIn = () => {
    const [formData, setFormData] = useState({ login: "", password: "" });
    const [error, setError] = useState("");
    const [showPassword, setShowPassword] = useState(false); // Nouvel état
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await axios.post("/api/v1/auth/signin", formData, {
                withCredentials: true, // Active l'envoi des cookies
            });
            console.log("response", response.headers);
            // POUR TESTER
            alert("Connexion réussie !");

            const cookies = response.headers["set-cookie"];
            if (!cookies) {
                console.error("❌ Aucun cookie JWT reçu !");
                return;
            }
            console.log("🔹 Cookies reçus :", cookies);

            console.log("🔹 Test de la route protégée...");

            const testResponse = await axios.get(`/api/v1/auth/user/test`, {
                headers: { Cookie: cookies.join("; ") }, // Envoie les cookies
                withCredentials: true
            });

            console.log("✅ Réponse de la route test :", testResponse.data);
            navigate("/home");
        } catch (error) {
            setError("Échec de la connexion. Vérifie tes identifiants.");
        }
    };

    return (
        <div className="flex items-center justify-center min-h-screen bg-gradient-to-br from-blue-100 to-blue-300 px-4">
            <div className="w-full max-w-md bg-white p-8 rounded-xl shadow-lg">
                <h2 className="text-3xl font-bold text-center text-gray-700 mb-6">
                    Connexion
                </h2>

                {error && <p className="text-red-500 text-center mb-4">{error}</p>}

                <form onSubmit={handleSubmit} className="space-y-5">
                    <div>
                        <label htmlFor="login" className="block mb-1 font-medium text-gray-600">
                            Email
                        </label>
                        <input
                            type="text"
                            name="login"
                            id="login"
                            placeholder="Adresse email"
                            className="w-full p-3 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent text-gray-900"
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div>
                        <label htmlFor="password" className="block mb-1 font-medium text-gray-600">
                            Mot de passe
                        </label>
                        <input
                            type={showPassword ? "text" : "password"} // Changement dynamique du type
                            name="password"
                            id="password"
                            placeholder="Tapez votre mot de passe"
                            className="w-full p-3 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent text-gray-900"
                            onChange={handleChange}
                            required
                        />
                        <div className="mt-2">
                            <label className="inline-flex items-center">
                                <input
                                    type="checkbox"
                                    className="form-checkbox"
                                    onChange={() => setShowPassword(!showPassword)} // Toggle visibilité
                                />
                                <span className="ml-2 text-gray-600">Afficher le mot de passe</span>
                            </label>
                        </div>
                    </div>

                    <button
                        type="submit"
                        className="w-full py-3 bg-blue-500 text-white font-semibold rounded-md hover:bg-blue-600 transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-blue-400"
                    >
                        Se connecter
                    </button>
                </form>

                <p className="text-center mt-6 text-gray-600">
                    Pas encore de compte ?{" "}
                    <a href="/signup" className="text-blue-600 hover:underline font-medium">
                        Inscris-toi ici
                    </a>
                </p>
            </div>
        </div>
    );
};

export default SignIn;
