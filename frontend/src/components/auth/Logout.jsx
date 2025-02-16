import React, { useContext } from "react";
import { AuthContext } from "../context/AuthContext";
import AuthService from "../../services/AuthService"; // assuming you have a service for auth

const Logout = () => {
    const { setUser } = useContext(AuthContext);

    const handleLogout = async () => {
        try {
            // Call your logout endpoint if available
            await AuthService.logout();
            // Clear the user in context
            setUser(null);
        } catch (error) {
            console.error("Logout failed:", error);
        }
    };

    return (
        <button
            onClick={handleLogout}
            className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600 transition-colors"
        >
            Logout
        </button>
    );
};

export default Logout;
