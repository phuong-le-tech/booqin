// src/context/AuthContext.js
import React, { createContext, useState, useEffect } from "react";
import AuthService from "../../services/AuthService";

export const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    useEffect(() => {
        const fetchUser = async () => {
            try {
                const currentUser = await AuthService.fetchCurrentUser();
                console.log("Current user:", currentUser);
                setUser(currentUser);
            } catch (error) {
                // If not logged in or token invalid, do nothing
                console.error("User not logged in or token invalid:", error);
            }
        };

        fetchUser();
    }, []);

    return (
        <AuthContext.Provider value={{ user, setUser }}>
            {children}
        </AuthContext.Provider>
    );
};
