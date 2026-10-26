import axiosInstance, { refreshToken } from "./AxiosInstance";
import { User } from "../model/User";
import { ApiResponse } from "../model/ApiResponse";

interface Credentials {
    login: string;
    password: string;
}

interface AuthState {
    user: User | null;
    isRefreshing: boolean;
    isAuthenticated: boolean;
    isCheckingAuth: boolean; // Track initial auth check
}

// Initialize auth state
let authState: AuthState = {
    user: null,
    isRefreshing: false,
    isAuthenticated: false,
    isCheckingAuth: true // Start with checking=true until initial check completes
};

// Set up event listeners for auth state
document.addEventListener('auth:refreshStart', () => {
    authState.isRefreshing = true;
    notifyListeners();
});

document.addEventListener('auth:refreshSuccess', () => {
    authState.isRefreshing = false;
    notifyListeners();
});

document.addEventListener('auth:refreshError', () => {
    authState.isRefreshing = false;
    notifyListeners();
});

document.addEventListener('auth:logout', () => {
    authState = {
        user: null,
        isRefreshing: false,
        isAuthenticated: false,
        isCheckingAuth: false
    };
    notifyListeners();
});

// Subscription system for auth state changes
const listeners: Array<(state: AuthState) => void> = [];

export const subscribeToAuthChanges = (callback: (state: AuthState) => void) => {
    listeners.push(callback);
    // Immediately notify with current state
    callback(authState);
    return () => {
        const index = listeners.indexOf(callback);
        if (index > -1) {
            listeners.splice(index, 1);
        }
    };
};

const notifyListeners = () => {
    listeners.forEach(listener => listener(authState));
};

const AuthService = {
    /**
     * Login with credentials
     * The backend will set JWT cookies automatically
     */
    login: async (credentials: Credentials): Promise<User> => {
        try {

            await axiosInstance.post<ApiResponse<any>>(
                "/v1/auth/login",
                credentials
            );


            // Fetch user details after successful login
            const userData = await AuthService.fetchCurrentUser();

            if (!userData) {
                throw new Error("Login successful but failed to fetch user data");
            }


            return userData;
        } catch (error: any) {
            console.error("Login error:", error);
            if (error.response) {
                throw new Error(error.response.data?.message || "Error during login");
            } else if (error.request) {
                throw new Error("The server is not responding. Please try again later.");
            } else {
                throw new Error("Error setting up the request.");
            }
        }
    },

    /**
     * Register a new user
     */
    register: async (userData: any): Promise<void> => {
        try {
            // Create registration dto
            const registerDto = {
                firstName: userData.firstName,
                lastName: userData.lastName,
                email: userData.email,
                password: userData.password,
                address: {
                    streetNumber: userData.address.streetNumber,
                    streetName: userData.address.streetName,
                    city: userData.address.city,
                    postalCode: userData.address.postalCode
                }
            };


            const response = await axiosInstance.post<ApiResponse<User>>(
                "/v1/auth/register",
                registerDto,
                {
                    headers: {
                        'Content-Type': 'application/json'
                    }
                }
            );

            // Successful registration (Spring returns 201 Created)
            if (response.status === 201 || (response.data && response.data.success)) {
                return;
            } else {
                console.error("Unexpected response:", response);
                throw new Error("Registration failed with unexpected response");
            }
        } catch (error: any) {
            console.error("Registration error:", error);

            if (error.response) {
                console.error("Response status:", error.response.status);
                console.error("Response data:", error.response.data);

                if (error.response.status === 409) {
                    throw new Error(error.response.data?.message || "User already exists");
                } else if (error.response.status === 400) {
                    throw new Error(error.response.data?.message || "Invalid registration data");
                } else {
                    throw new Error(error.response.data?.message || `Server error (${error.response.status})`);
                }
            } else if (error.request) {
                throw new Error("No response from server. Please try again later.");
            } else {
                throw new Error(`Error processing request: ${error.message}`);
            }
        }
    },

    /**
     * Logout - the backend will clear cookies
     */
    logout: async (): Promise<void> => {
        try {
            await axiosInstance.post<ApiResponse<null>>("/v1/auth/logout");

            // Dispatch logout event regardless of server response
            document.dispatchEvent(new CustomEvent('auth:logout'));

            // Redirect to home page
            window.location.href = "/";
        } catch (error: any) {
            // Even if server logout fails, clear local state
            document.dispatchEvent(new CustomEvent('auth:logout'));
            console.error("Logout error:", error);

            // Still redirect to home
            window.location.href = "/";
        }
    },

    /**
     * Check if user is authenticated by fetching current user data
     * Called on app initialization
     */
    checkAuth: async (): Promise<User | null> => {
        authState.isCheckingAuth = true;
        notifyListeners();

        try {
            const user = await AuthService.fetchCurrentUser();

            authState.isCheckingAuth = false;
            notifyListeners();

            return user;
        } catch (error) {
            console.error("Auth check failed:", error);

            authState.isCheckingAuth = false;
            notifyListeners();

            return null;
        }
    },

    /**
     * Fetch current user data - this validates if the user is authenticated
     */
    fetchCurrentUser: async (): Promise<User | null> => {
        try {
            const response = await axiosInstance.get<ApiResponse<User>>("/v1/auth/me");

            if (!response.data.success || !response.data.data) {
                // Invalid session
                authState.isAuthenticated = false;
                authState.user = null;
                notifyListeners();
                return null;
            }

            // Valid session with user data
            authState.isAuthenticated = true;
            authState.user = response.data.data;
            notifyListeners();

            return response.data.data;
        } catch (error: any) {
            console.error("Error fetching user:", error);

            // The refresh attempt is handled by the axios interceptor
            // If we get here, either:
            // 1. The refresh failed, or
            // 2. It's some other error

            authState.isAuthenticated = false;
            authState.user = null;
            notifyListeners();

            return null;
        }
    },

    /**
     * Get current auth state
     */
    getAuthState: (): AuthState => {
        return {...authState};
    },

    /**
     * Force token refresh manually
     * Useful for extending session before it expires
     */
    refreshToken: async (): Promise<boolean> => {
        return refreshToken();
    },

    /**
     * Check if user is authenticated
     */
    isAuthenticated: (): boolean => {
        return authState.isAuthenticated && !!authState.user;
    }
};

// Perform initial auth check when this module is loaded
// This helps determine if the user is already logged in via cookies
AuthService.checkAuth().catch(err => {
    console.warn("Initial auth check failed:", err);
});

export default AuthService;