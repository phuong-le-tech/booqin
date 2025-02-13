import axiosInstance from "./AxiosInstance.js";

const AuthService = {
    // Sign in
    login: async (credentials) => {
        try {
            const response = await axiosInstance.post("/v1/auth/login", credentials);
            console.log("response", response);
            return response.data;
        } catch (error) {
            // You could log, transform, or throw the error
            console.error("Error in login:", error);
            throw error; // re-throw so the component can catch it
        }
    },

    // Sign up
    register: async (userData) => {
        try {
            const response = await axiosInstance.post("/v1/auth/register", userData);
            return response.data;
        } catch (error) {
            console.error("Error in register:", error);
            throw error;
        }
    },

    // Logout
    logout: async () => {
        try {
            const response = await axiosInstance.post("/v1/auth/logout");
            return response.data;
        } catch (error) {
            console.error("Error in logout:", error);
            throw error;
        }
    },
};

export default AuthService;
