import axiosInstance from "./AxiosInstance.js";

const AuthService = {
    // Sign in
    login: async (credentials) => {
        try {
            const response = await axiosInstance.post("/v1/auth/login", credentials);
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
            // 1) Vérifier si on a une réponse du serveur
            if (error.response) {
                // - error.response.data => contient le payload retourné par le serveur (par ex. ton ErrorResponse JSON)
                // - error.response.status => le code HTTP (ex. 409, 500, etc.)
                // - error.response.headers => les headers de la réponse
                if (error.response.status === 409) {
                    const serverMessage = error.response.data?.message || "Conflit: utilisateur déjà existant";
                    throw new Error(serverMessage);
                }
                throw new Error(error.response.data?.message || "Erreur serveur");
            }
            // 2) Vérifier si la requête a été émise mais sans réponse du serveur (ex. timeout)
            else if (error.request) {
                throw new Error("Aucune réponse du serveur. Veuillez réessayer plus tard.");
            }
            // 3) Erreur lors de la configuration de la requête
            else {
                throw new Error("Erreur lors de la configuration de la requête.");
            }
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

    fetchCurrentUser: async() => {
        try {
            const response = await axiosInstance.get('/v1/auth/me', {withCredentials: true});
            console.log('User info:', response.data);
            // e.g., { id: 1, firstName: "Jane", lastName: "Doe", email: "jane.doe@example.com" }
            return response.data;
        } catch (error) {
            if (error.response && error.response.status === 403) {
                console.log('Not authenticated');
                return null;
                // redirect to login, show modal, etc.
            } else {
                console.error('Error fetching current user:', error);
                return null;
            }
        }
    }

};

export default AuthService;
