import axios, { AxiosRequestConfig, AxiosResponse } from "axios";

const axiosInstance = axios.create({
    baseURL: "/api",
    withCredentials: true, // <--- Important if you're using cookies for auth
});

// Track refresh status
let isRefreshing = false;
// Queue for requests waiting for token refresh
let failedQueue: Array<{
    resolve: (value?: unknown) => void;
    reject: (reason?: any) => void;
}> = [];

/**
 * processQueue calls resolve/reject on all
 * queued requests once a token is refreshed or an error occurs.
 */
const processQueue = (error: any, token: string | null = null) => {
    failedQueue.forEach((prom) => {
        if (error) {
            prom.reject(error);
        } else {
            prom.resolve(token);
        }
    });
    failedQueue = [];
};

axiosInstance.interceptors.response.use(
    (response: AxiosResponse) => {
        // Any status code in the range of 2xx triggers this function
        return response;
    },
    async (error: any) => {
        // For 4xx or 5xx errors
        const originalRequest = error.config as AxiosRequestConfig & {
            _retry?: boolean;
        };

        // If we get a 401 (unauthorized) and haven't retried yet:
        if (error.response?.status === 401 && !originalRequest._retry) {
            // If we're already refreshing, push the request into the queue
            if (isRefreshing) {
                return new Promise((resolve, reject) => {
                    failedQueue.push({ resolve, reject });
                })
                    .then(() => {
                        return axiosInstance(originalRequest);
                    })
                    .catch((err) => {
                        return Promise.reject(err);
                    });
            }

            // Mark request as retried
            originalRequest._retry = true;
            isRefreshing = true;

            try {
                // Attempt to refresh token/cookie
                await axiosInstance.post("/v1/auth/refresh");
                isRefreshing = false;
                // Resolve all requests in the queue with the new token
                processQueue(null);
                // Retry the original request
                return axiosInstance(originalRequest);
            } catch (refreshError) {
                isRefreshing = false;
                // Reject all requests in the queue
                processQueue(refreshError);
                // Optionally redirect to login page if refresh fails
                window.location.href = "/login";
                return Promise.reject(refreshError);
            }
        }

        // If not a 401, or we've already retried, just reject
        return Promise.reject(error);
    }
);

export default axiosInstance;
