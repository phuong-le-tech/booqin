import axios from "axios";

const axiosInstance = axios.create({
    baseURL: "/api",
    withCredentials: true
});

axiosInstance.interceptors.response.use(
    (response) => {
        return response;
    },
    async (error) => {
        // If we get a 401 response from the server...
        if (error.response && error.response.status === 401) {
            try {
                // Optionally, prevent infinite retry loops:
                // Use a custom flag on the request if you want
                const originalRequest = error.config;
                if (!originalRequest._retry) {
                    originalRequest._retry = true;

                    await axiosInstance.post("/v1/auth/refresh");

                    return axiosInstance.request(originalRequest);
                }
            } catch (refreshError) {
                window.location.href = "/login";
                return Promise.reject(refreshError);
            }
        }

        return Promise.reject(error);
    }
);

export default axiosInstance;
