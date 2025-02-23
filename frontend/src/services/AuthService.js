import axiosInstance from "./AxiosInstance.js";

const AuthService = {
    login: async (credentials) => {
        try {
            const response = await axiosInstance.post("/v1/auth/login", credentials);
            const apiResp = response.data; // { success, message, data }

            if (!apiResp.success) {
                throw new Error(apiResp.message || "Login failed");
            }
            return apiResp.data;
        } catch (error) {
            if (error.response) {
                throw new Error(error.response.data?.message || "Erreur lors du login");
            } else if (error.request) {
                throw new Error("Le serveur ne répond pas. Veuillez réessayer plus tard.");
            } else {
                throw new Error("Erreur de configuration de la requête (login).");
            }
        }
    },

    register: async (userData) => {
        try {
            const response = await axiosInstance.post("/v1/auth/register", userData);
            const apiResp = response.data;

            if (!apiResp.success) {
                throw new Error(apiResp.message || "Échec de l'enregistrement");
            }
            return apiResp.data;
        } catch (error) {
            if (error.response) {
                if (error.response.status === 409) {
                    const serverMessage = error.response.data?.message ||
                        "Conflit: utilisateur déjà existant";
                    throw new Error(serverMessage);
                }
                throw new Error(error.response.data?.message || "Erreur serveur (register)");
            } else if (error.request) {
                throw new Error("Aucune réponse du serveur. Veuillez réessayer plus tard.");
            } else {
                throw new Error("Erreur lors de la configuration de la requête (register).");
            }
        }
    },

    logout: async () => {
        try {
            const response = await axiosInstance.post("/v1/auth/logout");
            const apiResp = response.data;

            if (!apiResp.success) {
                throw new Error(apiResp.message || "Échec de la déconnexion");
            }
            return apiResp;
        } catch (error) {
            if (error.response) {
                throw new Error(error.response.data?.message || "Erreur serveur (logout)");
            } else if (error.request) {
                throw new Error("Aucune réponse du serveur. Veuillez réessayer plus tard.");
            } else {
                throw new Error("Erreur lors de la configuration de la requête (logout).");
            }
        }
    },

    fetchCurrentUser: async () => {
        try {
            const response = await axiosInstance.get('/v1/auth/me', { withCredentials: true });
            const apiResp = response.data;

            if (!apiResp.success) {
                throw new Error(apiResp.message || "Impossible de récupérer l'utilisateur courant");
            }
            return apiResp.data;
        } catch (error) {
            if (error.response) {
                if (error.response.status === 403) {
                    console.log('Utilisateur non authentifié');
                    return null;
                }
                throw new Error(error.response.data?.message ||
                    "Erreur serveur lors de la récupération de l'utilisateur");
            } else if (error.request) {
                throw new Error("Aucune réponse du serveur (fetchCurrentUser).");
            } else {
                throw new Error("Erreur de configuration de la requête (fetchCurrentUser).");
            }
        }
    },
};

export default AuthService;
