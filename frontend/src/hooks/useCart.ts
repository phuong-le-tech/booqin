import { useCallback, useEffect, useState } from "react";
import axiosInstance from "../services/AxiosInstance";
import { useAuth } from "../context/AuthContext";
import { CART_CHANGED } from "../utils/cartEvents";

/** Books in the signed-in user's cart; refreshes whenever the cart changes. */
export const useCart = () => {
    const { user } = useAuth();
    const [bookIds, setBookIds] = useState<number[]>([]);
    const userId = user?.id;

    const load = useCallback(async () => {
        if (!userId) {
            setBookIds([]);
            return;
        }
        try {
            const response = await axiosInstance.get(`/v1/carts/user-cart/${userId}`);
            const books: { id: number }[] = response.data?.data?.books ?? [];
            setBookIds(books.map(book => Number(book.id)));
        } catch (error) {
            console.error("Error loading cart:", error);
        }
    }, [userId]);

    useEffect(() => {
        load();
        window.addEventListener(CART_CHANGED, load);
        return () => window.removeEventListener(CART_CHANGED, load);
    }, [load]);

    return { count: bookIds.length, bookIds };
};
