import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import AuthServices from "../../services/AuthService.js";
import { toast } from "react-toastify";

const Register = () => {
    const [formData, setFormData] = useState({
        firstName: "",
        lastName: "",
        email: "",
        password: "",
        address: {
            streetNumber: "1",
            streetName: "Rue Test",
            city: "Paris",
            postalCode: "75000",
        },
        role: "USER",
    });
    const navigate = useNavigate();

    const handleChange = (e) => {
        const { name, value } = e.target;
        if (["streetNumber", "streetName", "city", "postalCode"].includes(name)) {
            setFormData({ ...formData, address: { ...formData.address, [name]: value } });
        } else {
            setFormData({ ...formData, [name]: value });
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await AuthServices.register(formData);
            toast.success("Inscription réussie !");
            navigate("/login");
        } catch (error) {
            const errMsg = error.message || "Échec de l'inscription. Vérifie tes informations.";
            toast.error(errMsg);
        }
    };


    return (
        <div className="flex items-center justify-center min-h-screen bg-gray-100 px-4">
            <div className="w-full max-w-md bg-white p-6 rounded-xl shadow-lg border border-gray-200">
                <h2 className="text-3xl font-bold text-center text-gray-800">Inscription</h2>
                <form onSubmit={handleSubmit} className="mt-6 space-y-4">
                    {/* Informations personnelles */}
                    <div>
                        <label className="block text-gray-700">Prénom</label>
                        <input
                            type="text"
                            name="firstName"
                            placeholder="Prénom"
                            className="w-full p-3 border border-gray-300 rounded-md text-gray-800"
                            onChange={handleChange}
                            required
                        />
                    </div>
                    <div>
                        <label className="block text-gray-700">Nom</label>
                        <input
                            type="text"
                            name="lastName"
                            placeholder="Nom"
                            className="w-full p-3 border border-gray-300 rounded-md text-gray-800"
                            onChange={handleChange}
                            required
                        />
                    </div>
                    <div>
                        <label className="block text-gray-700">Email</label>
                        <input
                            type="email"
                            name="email"
                            placeholder="Email"
                            className="w-full p-3 border border-gray-300 rounded-md text-gray-800"
                            onChange={handleChange}
                            required
                        />
                    </div>
                    <div>
                        <label className="block text-gray-700">Mot de passe</label>
                        <input
                            type="password"
                            name="password"
                            placeholder="Mot de passe"
                            className="w-full p-3 border border-gray-300 rounded-md text-gray-800"
                            onChange={handleChange}
                            required
                        />
                    </div>

                    {/* Adresse */}
                    <fieldset className="border border-gray-300 p-4 rounded-md">
                        <legend className="text-gray-700 font-semibold px-2">Adresse</legend>
                        <div className="mt-2">
                            <label className="block text-gray-700">Numéro de rue</label>
                            <input
                                type="text"
                                name="streetNumber"
                                placeholder="Numéro de rue"
                                className="w-full p-3 border border-gray-300 rounded-md text-gray-800"
                                onChange={handleChange}
                                value={formData.address.streetNumber}
                                required
                            />
                        </div>
                        <div>
                            <label className="block text-gray-700">Nom de rue</label>
                            <input
                                type="text"
                                name="streetName"
                                placeholder="Nom de rue"
                                className="w-full p-3 border border-gray-300 rounded-md text-gray-800"
                                onChange={handleChange}
                                value={formData.address.streetName}
                                required
                            />
                        </div>
                        <div>
                            <label className="block text-gray-700">Ville</label>
                            <input
                                type="text"
                                name="city"
                                placeholder="Ville"
                                className="w-full p-3 border border-gray-300 rounded-md text-gray-800"
                                onChange={handleChange}
                                value={formData.address.city}
                                required
                            />
                        </div>
                        <div>
                            <label className="block text-gray-700">Code Postal</label>
                            <input
                                type="text"
                                name="postalCode"
                                placeholder="Code Postal"
                                className="w-full p-3 border border-gray-300 rounded-md text-gray-800"
                                onChange={handleChange}
                                value={formData.address.postalCode}
                                required
                            />
                        </div>
                    </fieldset>

                    <button
                        type="submit"
                        className="w-full bg-green-500 text-white p-3 rounded-md font-semibold text-lg hover:bg-green-600 transition"
                    >
                        S'inscrire
                    </button>
                </form>
                <p className="text-center mt-4 text-gray-700">
                    Déjà un compte ?{" "}
                    <a href="/Login" className="text-green-600 font-semibold hover:underline">
                        Connecte-toi ici
                    </a>
                </p>
            </div>
        </div>
    );
};

export default Register;
