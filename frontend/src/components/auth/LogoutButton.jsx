import React from "react";
import { useNavigate } from "react-router-dom";
import AuthService from "../../services/AuthService";

const LogoutButton = () => {
    const navigate = useNavigate();

    const handleLogout = async () => {
        try {
            await AuthService.logout(); // calls POST /v1/auth/logout
            // Server clears cookies
            navigate("/login"); // redirect to login page
        } catch (error) {
            console.error("Error during logout:", error);
        }
    };

    return (
        <button onClick={handleLogout} className="bg-red-500 text-white px-4 py-2 rounded">
            Logout
        </button>
    );
};

export default LogoutButton;
