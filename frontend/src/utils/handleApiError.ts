import { toast } from 'react-toastify';

/**
 * Custom error handler for API errors with special handling for auth errors
 *
 * @param error The error object from the API call
 * @param defaultMessage Default message to show if error doesn't have a specific message
 * @param silent If true, doesn't show toast notifications
 * @returns The error message
 */
export const handleApiError = async (
    error: any,
    defaultMessage = 'An error occurred',
    silent = false
): Promise<string> => {
    let errorMessage = defaultMessage;
    let status = 0;

    // Extract status code and message
    if (error.response) {
        status = error.response.status;
        errorMessage = error.response.data?.message || errorMessage;
    } else if (error.request) {
        errorMessage = 'No response from server. Please check your connection.';
    } else if (error.message) {
        errorMessage = error.message;
    }

    // Handle authentication errors (401/403)
    if (status === 401 || status === 403) {
        if (!silent) {
            toast.error(status === 401
                ? 'Your session has expired. Please log in again.'
                : 'You don\'t have permission to perform this action.');
        }
        return status === 401 ? 'Authentication required' : 'Access denied';
    }

    // Show error toast for non-auth errors or auth errors that couldn't be recovered
    if (!silent) {
        toast.error(errorMessage);
    }

    return errorMessage;
};

export default handleApiError;