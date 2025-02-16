import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import AuthService from '../../services/AuthService';
import { useContext } from 'react';
import { AuthContext } from '../context/AuthContext';

const Login = () => {
    const { setUser } = useContext(AuthContext);
    const [formData, setFormData] = useState({ login: "", password: "" });
    const [error, setError] = useState("");
    const [showPassword, setShowPassword] = useState(false);
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await AuthService.login(formData);
            const userResponse = await AuthService.fetchCurrentUser(); // TODO On fait 2 requetes, bizarre
            setUser(userResponse);
            toast.success("Connexion réussie !");
            navigate("/home");
        } catch (error) {
            toast.error("Échec de la connexion. Vérifie tes identifiants.");
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
                    <a href="/Register" className="text-blue-600 hover:underline font-medium">
                        Inscris-toi ici
                    </a>
                </p>
            </div>
        </div>
    );
};

export default Login;
