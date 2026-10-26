import { useCallback, useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import axiosInstance from "../services/AxiosInstance";
import { useAuth } from "../context/AuthContext";
import { flyToCart, notifyCartChanged } from "../utils/cartEvents";

interface BookNotification {
    id: number;
    bookId: number;
}

/**
 * Borrowing side of a book: is it available, am I on its waiting list, and the
 * actions to borrow it, join or leave the list. Shared by the catalogue card and
 * the book detail page.
 */
export const useBookBorrowing = (bookId: number) => {
    const { user } = useAuth();
    const userId = user?.id;
    const navigate = useNavigate();
    const location = useLocation();

    const [isAvailable, setIsAvailable] = useState<boolean>(true);
    const [queuePosition, setQueuePosition] = useState<number>(-1);
    const [notificationId, setNotificationId] = useState<number | null>(null);
    const [loading, setLoading] = useState<boolean>(false);

    const checkAvailability = useCallback(async () => {
        try {
            const response = await axiosInstance.get(`/v1/carts/check-book-availability/${bookId}`);
            if (response.data?.data !== undefined) {
                setIsAvailable(response.data.data);
            }
        } catch (error) {
            console.error("Error checking book availability:", error);
            setIsAvailable(false);
        }
    }, [bookId]);

    const checkQueue = useCallback(async () => {
        if (!userId) return;
        try {
            const response = await axiosInstance.get(`/v1/book-queue/check`, { params: { bookId, userId } });
            if (response.data?.success) {
                setQueuePosition(response.data.data);
            }
        } catch (error) {
            console.error("Error checking queue position:", error);
        }
    }, [bookId, userId]);

    const loadNotification = useCallback(async () => {
        if (!userId) return;
        try {
            const response = await axiosInstance.get(`/v1/book-queue/active`, { params: { userId } });
            if (response.data?.success) {
                const mine = (response.data.data as BookNotification[]).find(n => n.bookId === bookId);
                if (mine) setNotificationId(mine.id);
            }
        } catch (error) {
            console.error("Error fetching notifications:", error);
        }
    }, [bookId, userId]);

    useEffect(() => {
        checkAvailability();
        checkQueue();
        loadNotification();
    }, [checkAvailability, checkQueue, loadNotification]);

    // Borrowing and queueing need an account: send visitors to login and bring them back.
    const requireLogin = (): boolean => {
        if (userId) return true;
        localStorage.setItem("redirectAfterLogin", location.pathname);
        navigate("/login");
        return false;
    };

    /** Borrow = put it in the cart. `cover` is the element that flies to the cart icon. */
    const addToCart = async (cover?: HTMLElement | null) => {
        if (!requireLogin()) return;

        setLoading(true);
        try {
            const response = await axiosInstance.post("/v1/carts/add-book", { userId, bookId });

            if (response.status === 200 && response.data?.success && response.data?.data === false) {
                // The API answers success with data=false when the book is already in the cart
                toast.info(response.data.message);
            } else if (response.status === 200 && response.data?.success) {
                toast.success("Book has been added to cart");
                flyToCart(cover ?? null);
                notifyCartChanged();
                checkAvailability();
            } else {
                toast.error(response.data?.message || "Failed to add to cart");
            }
        } catch (error: any) {
            toast.error(error.response?.data?.message || "Error adding book to cart");
        } finally {
            setLoading(false);
        }
    };

    const joinQueue = async () => {
        if (!requireLogin()) return;

        setLoading(true);
        try {
            const params = new URLSearchParams();
            params.append("bookId", bookId.toString());
            params.append("userId", String(userId));

            const response = await axiosInstance.post("/v1/book-queue/join", params);

            if (response.status === 200 && response.data?.success) {
                toast.success("You will be notified when the book is available");
                checkQueue();
                loadNotification();
            } else {
                toast.error(response.data?.message || "You are already subscribed to notifications");
            }
        } catch (error: any) {
            toast.error(error.response?.data?.message || "Error subscribing to notifications");
        } finally {
            setLoading(false);
        }
    };

    const leaveQueue = async () => {
        if (!userId || !notificationId) return;

        setLoading(true);
        try {
            const response = await axiosInstance.post("/v1/book-queue/cancel", null, {
                params: { notificationId, userId }
            });

            if (response.status === 200 && response.data?.success) {
                toast.success("Notification cancelled successfully");
                setQueuePosition(-1);
                setNotificationId(null);
                loadNotification();
            } else {
                toast.error(response.data?.message || "Failed to cancel notification");
            }
        } catch (error: any) {
            toast.error(error.response?.data?.message || "Error cancelling notification");
        } finally {
            setLoading(false);
        }
    };

    return {
        isAvailable,
        queuePosition,
        isInQueue: queuePosition > 0,
        loading,
        addToCart,
        joinQueue,
        leaveQueue
    };
};
