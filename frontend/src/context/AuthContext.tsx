import React, { createContext, useContext, useEffect, useState } from 'react';
import axiosInstance from '../services/AxiosInstance';
import { User } from '../model/User';
import { ApiResponse } from '../model/ApiResponse';

interface AuthContextType {
    user: User | null;
    isAuthenticated: boolean;
    isLoading: boolean;
    login: (email: string, password: string) => Promise<User>;
    logout: () => Promise<void>;
    register: (userData: Partial<User>) => Promise<void>;
    refreshUser: () => Promise<User | null>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [user, setUser] = useState<User | null>(null);
    const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
    const [isLoading, setIsLoading] = useState<boolean>(true); // Start with loading=true

    // Setup event listeners for auth state changes from axios interceptor
    useEffect(() => {
        const handleRefreshStart = () => setIsLoading(true);
        const handleRefreshSuccess = () => setIsLoading(false);
        const handleRefreshError = () => setIsLoading(false);
        const handleLogout = () => {
            setUser(null);
            setIsAuthenticated(false);
            setIsLoading(false);
        };

        // Add event listeners
        document.addEventListener('auth:refreshStart', handleRefreshStart);
        document.addEventListener('auth:refreshSuccess', handleRefreshSuccess);
        document.addEventListener('auth:refreshError', handleRefreshError);
        document.addEventListener('auth:logout', handleLogout);

        // Cleanup function to remove event listeners
        return () => {
            document.removeEventListener('auth:refreshStart', handleRefreshStart);
            document.removeEventListener('auth:refreshSuccess', handleRefreshSuccess);
            document.removeEventListener('auth:refreshError', handleRefreshError);
            document.removeEventListener('auth:logout', handleLogout);
        };
    }, []);

    // Check authentication status on component mount
    useEffect(() => {
        const checkAuth = async () => {
            try {
                setIsLoading(true);
                await fetchCurrentUser();
                setIsLoading(false);
            } catch (error) {
                console.error("Initial auth check failed:", error);
                setIsLoading(false);
            }
        };

        checkAuth();
    }, []);

    /**
     * Fetch current user data
     */
    const fetchCurrentUser = async (): Promise<User | null> => {
        try {
            const response = await axiosInstance.get<ApiResponse<User>>("/v1/auth/me");

            if (!response.data.success || !response.data.data) {
                setUser(null);
                setIsAuthenticated(false);
                return null;
            }

            setUser(response.data.data);
            setIsAuthenticated(true);
            return response.data.data;
        } catch (error) {
            console.error("Error fetching current user:", error);
            setUser(null);
            setIsAuthenticated(false);
            return null;
        }
    };

    /**
     * Login with credentials
     */
    const login = async (email: string, password: string): Promise<User> => {
        try {
            setIsLoading(true);

            await axiosInstance.post<ApiResponse<any>>(
                "/v1/auth/login",
                { login: email, password }
            );

            // Fetch user data after successful login
            const userData = await fetchCurrentUser();

            if (!userData) {
                throw new Error("Login successful but failed to fetch user data");
            }

            // Handle redirect after login if needed
            const redirectPath = localStorage.getItem('redirectAfterLogin');
            if (redirectPath) {
                localStorage.removeItem('redirectAfterLogin');
                setTimeout(() => {
                    window.location.href = redirectPath;
                }, 100);
            }

            setIsLoading(false);
            return userData;
        } catch (error: any) {
            setIsLoading(false);
            console.error("Login error:", error);

            if (error.response) {
                throw new Error(error.response.data?.message || "Error during login");
            } else if (error.request) {
                throw new Error("The server is not responding. Please try again later.");
            } else {
                throw new Error("Error setting up the login request.");
            }
        }
    };

    /**
     * Register a new user
     */
    const register = async (userData: Partial<User>): Promise<void> => {
        try {
            setIsLoading(true);

            const response = await axiosInstance.post<ApiResponse<null>>(
                "/v1/auth/register",
                userData
            );

            setIsLoading(false);

            if (!response.data.success) {
                throw new Error(response.data.message || "Registration failed");
            }
        } catch (error: any) {
            setIsLoading(false);

            if (error.response) {
                if (error.response.status === 409) {
                    throw new Error(error.response.data?.message || "User already exists");
                }
                throw new Error(error.response.data?.message || "Server error");
            } else if (error.request) {
                throw new Error("No response from server. Please try again later.");
            } else {
                throw new Error("Error setting up the registration request.");
            }
        }
    };

    /**
     * Logout - clear cookies and auth state
     */
    const logout = async (): Promise<void> => {
        try {
            setIsLoading(true);

            await axiosInstance.post<ApiResponse<null>>("/v1/auth/logout");

            setUser(null);
            setIsAuthenticated(false);
            setIsLoading(false);

            // Redirect to home page
            window.location.href = "/";
        } catch (error: any) {
            // Even if server logout fails, clear local state
            setUser(null);
            setIsAuthenticated(false);
            setIsLoading(false);

            console.error("Logout error:", error);

            // Still redirect to home
            window.location.href = "/";
        }
    };

    /**
     * Refresh user data
     */
    const refreshUser = async (): Promise<User | null> => {
        setIsLoading(true);
        const user = await fetchCurrentUser();
        setIsLoading(false);
        return user;
    };

    return (
        <AuthContext.Provider
            value={{
                user,
                isAuthenticated,
                isLoading,
                login,
                logout,
                register,
                refreshUser,
            }}
        >
            {children}
        </AuthContext.Provider>
    );
};

// Custom hook to use auth context
export const useAuth = () => {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};

// Custom hook for protected routes
export const useRequireAuth = (adminOnly = false) => {
    const { user, isAuthenticated, isLoading } = useAuth();

    // Is admin check
    const isAdmin = user?.role === 'ADMIN';
    const hasAccess = isAuthenticated && (!adminOnly || isAdmin);

    return {
        isLoading,
        isAuthenticated,
        hasAccess,
        user
    };
};

export default AuthContext;