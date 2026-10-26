import axios, { AxiosRequestConfig, AxiosResponse } from "axios";

const axiosInstance = axios.create({
    baseURL: "/api",
    withCredentials: true, // Required for cookies
    timeout: 10000,
});

// Track refresh status
let isRefreshing = false;
// Queue for requests waiting for token refresh
let failedQueue: Array<{
    resolve: (value?: unknown) => void;
    reject: (reason?: any) => void;
}> = [];

/**
 * Process all queued requests after refresh
 */
const processQueue = (error: any = null) => {
    failedQueue.forEach((prom) => {
        if (error) {
            prom.reject(error);
        } else {
            prom.resolve();
        }
    });
    failedQueue = [];
};

// Dispatch auth events
const dispatchAuthEvent = (eventType: string) => {
    document.dispatchEvent(new CustomEvent(eventType));
};

// Public routes that don't require auth redirects
const publicRoutes = ['/login', '/', '/register', '/books', '/collections/public'];

/**
 * Handles redirection to login page when authentication fails
 */
const redirectToLogin = () => {
    // Only redirect if we're not already on a public route
    const currentPath = window.location.pathname;
    const isPublicRoute = publicRoutes.some(route => currentPath.startsWith(route));

    if (!isPublicRoute) {
        // Save current path for redirect after login
        localStorage.setItem('redirectAfterLogin', currentPath);
        window.location.href = "/login?session=expired";
    }
};

/**
 * Response interceptor that handles token refresh for 401/403 status codes
 */
axiosInstance.interceptors.response.use(
    (response: AxiosResponse) => response,
    async (error: any) => {
        // Handle network errors
        if (!error.response) {
            return Promise.reject(error);
        }

        const originalRequest = error.config as AxiosRequestConfig & {
            _retry?: boolean;
            _retryCount?: number;
        };

        // A 401 from login/register/refresh is the real answer (bad credentials), not an expired session
        if (/\/v1\/auth\/(login|register|refresh)/.test(originalRequest.url ?? "")) {
            return Promise.reject(error);
        }

        // Initialize retry count
        if (originalRequest._retryCount === undefined) {
            originalRequest._retryCount = 0;
        }

        // For 401/403 responses that haven't been retried yet
        if ((error.response?.status === 401 || error.response?.status === 403) &&
            !originalRequest._retry &&
            originalRequest._retryCount < 2) { // Limit retries

            originalRequest._retryCount++;

            // If already refreshing, queue this request
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

            // Start refreshing
            originalRequest._retry = true;
            isRefreshing = true;
            dispatchAuthEvent('auth:refreshStart');

            try {
                // Call backend refresh endpoint
                await axiosInstance.post("/v1/auth/refresh");

                isRefreshing = false;
                dispatchAuthEvent('auth:refreshSuccess');

                // Process queued requests
                processQueue();

                // Retry the original request with refreshed cookies
                return axiosInstance(originalRequest);
            } catch (refreshError) {
                isRefreshing = false;
                dispatchAuthEvent('auth:refreshError');

                // Reject all queued requests
                processQueue(refreshError);

                // If refresh fails for 401, logout and redirect to login
                if (error.response?.status === 401) {
                    dispatchAuthEvent('auth:logout');
                    redirectToLogin();
                }

                return Promise.reject(refreshError);
            }
        } else if (error.response?.status === 401 && originalRequest._retry) {
            // We've already tried to refresh and failed - user must login manually
            dispatchAuthEvent('auth:logout');
            redirectToLogin();
        }

        // For other errors, just reject the promise
        return Promise.reject(error);
    }
);

/**
 * Manually trigger a token refresh
 */
export const refreshToken = async (): Promise<boolean> => {
    if (isRefreshing) return true; // Already refreshing

    try {
        isRefreshing = true;
        dispatchAuthEvent('auth:refreshStart');

        await axiosInstance.post("/v1/auth/refresh");

        isRefreshing = false;
        dispatchAuthEvent('auth:refreshSuccess');
        return true;
    } catch (error: any) {
        isRefreshing = false;
        dispatchAuthEvent('auth:refreshError');

        // If refresh failed with 401, user needs to login again
        if (error.response?.status === 401) {
            dispatchAuthEvent('auth:logout');
        }

        return false;
    }
};

export default axiosInstance;