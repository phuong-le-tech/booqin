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
                setUser(currentUser);
            } catch (error) {
                // If not logged in or token invalid, do nothing
                console.error("User not logged in or token invalid:", error);
            }
        };

        fetchUser().then(r => console.log("User fetched:", r));
    }, []);

    return (
        <AuthContext.Provider value={{ user, setUser }}>
            {children}
        </AuthContext.Provider>
    );
};
