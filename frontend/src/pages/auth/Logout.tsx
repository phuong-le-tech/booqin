import React, { useState } from "react";
import { useAuth } from "../../context/AuthContext";
import { LogOut } from "lucide-react";

interface LogoutProps {
    variant?: "button" | "link" | "icon";
    className?: string;
}

const Logout: React.FC<LogoutProps> = ({ variant = "button", className = "" }) => {
    const { logout } = useAuth();
    const [isLoading, setIsLoading] = useState(false);

    const handleLogout = async () => {
        setIsLoading(true);
        try {
            await logout();
            // No need to set user manually - the auth context handles this
        } catch (error) {
            console.error("Logout failed:", error);
        } finally {
            setIsLoading(false);
        }
    };

    if (variant === "icon") {
        return (
            <button
                onClick={handleLogout}
                disabled={isLoading}
                className={`text-red-400 hover:text-red-600 focus:outline-none disabled:opacity-50 ${className}`}
                aria-label="Logout"
            >
                <LogOut className="h-5 w-5 sm:h-6 sm:w-6" />
            </button>
        );
    }

    if (variant === "link") {
        return (
            <button
                onClick={handleLogout}
                disabled={isLoading}
                className={`text-red-400 hover:text-red-600 font-medium focus:outline-none disabled:opacity-50 ${className}`}
            >
                {isLoading ? "Logging out..." : "Logout"}
            </button>
        );
    }

    return (
        <button
            onClick={handleLogout}
            disabled={isLoading}
            className={`px-3 py-1 sm:px-4 sm:py-2 bg-red-500 text-white text-sm sm:text-base rounded hover:bg-red-600 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-opacity-50 disabled:bg-red-400 transition-colors ${className}`}
        >
            {isLoading ? "Logging out..." : (
                <span className="flex items-center">
                    <LogOut className="h-4 w-4 mr-2" />
                    Logout
                </span>
            )}
        </button>
    );
};

export default Logout;