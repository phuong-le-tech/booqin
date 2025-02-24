import React, { useContext } from "react";
import { AuthContext } from "../../context/AuthContext.tsx";
import AuthService from "../../services/AuthService.ts"; // Assuming you have a service for auth

const Logout: React.FC = () => {
    const { setUser } = useContext(AuthContext);

    const handleLogout = async () => {
        try {
            await AuthService.logout();
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
