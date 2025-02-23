import axiosInstance from "./AxiosInstance";
import { User } from "../model/User";
import { ApiResponse } from "../model/ApiResponse";

interface Credentials {
    login: string;
    password: string;
}

const AuthService = {
    login: async (credentials: Credentials): Promise<User> => {
        try {
            console.log("Attempting login with:", credentials);
            const response = await axiosInstance.post<ApiResponse<User>>(
                "/v1/auth/login",
                credentials
            );
            const apiResp = response.data;
            console.log("Login response:", apiResp);

            if (!apiResp.success || !apiResp.data) {
                throw new Error(apiResp.message || "Login failed");
            }

            // After a successful login, fetch user details
            try {
                const userResponse = await axiosInstance.get<ApiResponse<User>>(
                    "/v1/auth/me"
                );
                const userData = userResponse.data;
                console.log("User data after login:", userData);

                if (!userData.success || !userData.data) {
                    throw new Error("Login successful but failed to fetch user data");
                }
                return userData.data;
            } catch (userError) {
                console.error("Error fetching user after login:", userError);
                throw new Error("Login successful but failed to fetch user data");
            }
        } catch (error: any) {
            console.error("Login error:", error);
            if (error.response) {
                throw new Error(error.response.data?.message || "Error during login");
            } else if (error.request) {
                throw new Error("The server is not responding. Please try again later.");
            } else {
                throw new Error("Error setting up the request (login).");
            }
        }
    },

    register: async (userData: Partial<User>): Promise<void> => {
        try {
            const response = await axiosInstance.post<ApiResponse<null>>(
                "/v1/auth/register",
                userData
            );
            const apiResp = response.data;

            if (!apiResp.success) {
                throw new Error(apiResp.message || "Registration failed");
            }
        } catch (error: any) {
            if (error.response) {
                if (error.response.status === 409) {
                    const serverMessage =
                        error.response.data?.message || "Conflict: user already exists";
                    throw new Error(serverMessage);
                }
                throw new Error(
                    error.response.data?.message || "Server error (register)"
                );
            } else if (error.request) {
                throw new Error("No response from server. Please try again later.");
            } else {
                throw new Error("Error setting up the request (register).");
            }
        }
    },

    logout: async (): Promise<void> => {
        try {
            const response = await axiosInstance.post<ApiResponse<null>>(
                "/v1/auth/logout"
            );
            const apiResp = response.data;

            if (!apiResp.success) {
                throw new Error(apiResp.message || "Logout failed");
            }
        } catch (error: any) {
            if (error.response) {
                throw new Error(error.response.data?.message || "Server error (logout)");
            } else if (error.request) {
                throw new Error("No response from server. Please try again later.");
            } else {
                throw new Error("Error setting up the request (logout).");
            }
        }
    },

    fetchCurrentUser: async (): Promise<User | null> => {
        try {
            const response = await axiosInstance.get<ApiResponse<User>>("/v1/auth/me");
            const apiResp = response.data;

            if (!apiResp.success || !apiResp.data) {
                return null;
            }
            return apiResp.data;
        } catch (error) {
            console.error("Error fetching user:", error);
            return null;
        }
    },

    updateProfile: async (userData: Partial<User>): Promise<User> => {
        try {
            const response = await axiosInstance.put<ApiResponse<User>>(
                "/v1/auth/profile",
                userData
            );
            const apiResp = response.data;

            if (!apiResp.success || !apiResp.data) {
                throw new Error(apiResp.message || "Profile update failed");
            }
            return apiResp.data;
        } catch (error: any) {
            if (error.response) {
                throw new Error(
                    error.response.data?.message || "Server error while updating profile"
                );
            } else if (error.request) {
                throw new Error("No response from server (updateProfile).");
            } else {
                throw new Error("Error setting up the request (updateProfile).");
            }
        }
    },
};

export default AuthService;
