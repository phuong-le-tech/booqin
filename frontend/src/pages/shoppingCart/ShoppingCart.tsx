import { JSX, useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { toast } from "react-toastify";
import { Layout } from "../Layout";
import axiosInstance from "../../services/AxiosInstance.ts";
import { useAuth } from "../../context/AuthContext.tsx";
import { BookCover } from "../../components/book/BookCover";
import { notifyCartChanged } from "../../utils/cartEvents";

type CartBook = {
    id: number | string;
    title: string;
    authorNames?: string[];
    thumbnailPath?: string | null;
    price?: number;
};

type CartDto = {
    id: number;
    userId: number;
    books: CartBook[];
};

const DELIVERY_FEE_PER_BOOK = 6;
const euro = (amount: number) => `${amount.toFixed(2)} €`;

export const ShoppingCart: () => JSX.Element = () => {
    const [cart, setCart] = useState<CartDto | null>(null);
    const [loading, setLoading] = useState(true);
    const [paying, setPaying] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const { user, isLoading: authLoading } = useAuth();
    const userId = user?.id;

    // The server is the only source of truth for the cart (no local copy to go stale).
    const loadCart = useCallback(async () => {
        if (!userId) return;
        try {
            setError(null);
            const response = await axiosInstance.get(`/v1/carts/user-cart/${userId}`);
            if (response.data?.success && response.data?.data) {
                const books: CartBook[] = Array.isArray(response.data.data.books) ? response.data.data.books : [];
                // A book can only be in a cart once
                const unique = books.filter((book, index) => books.findIndex(other => other.id === book.id) === index);
                setCart({ ...response.data.data, books: unique });
            } else {
                throw new Error("Unexpected response format");
            }
        } catch (err: any) {
            console.error("Error loading cart:", err);
            setError(err.response?.data?.message || "We could not load your cart.");
        } finally {
            setLoading(false);
        }
    }, [userId]);

    useEffect(() => {
        loadCart();
    }, [loadCart]);

    const removeFromCart = async (bookId: number | string) => {
        if (!userId) return;
        try {
            const response = await axiosInstance.delete("/v1/carts/remove-book", { params: { userId, bookId } });
            if (response.status === 200) {
                setCart(prev => (prev ? { ...prev, books: prev.books.filter(book => book.id !== bookId) } : prev));
                notifyCartChanged();
                toast.success("Removed from your cart");
            } else {
                toast.error("Could not remove the book");
            }
        } catch (err) {
            console.error("Error removing book from cart:", err);
            toast.error("Could not remove the book");
        }
    };

    const clearCart = async () => {
        if (!userId || !confirm("Remove every book from your cart?")) return;
        try {
            const response = await axiosInstance.delete(`/v1/carts/clear-cart/${userId}`);
            if (response.status === 200) {
                setCart(prev => (prev ? { ...prev, books: [] } : prev));
                notifyCartChanged();
                toast.success("Your cart is empty");
            } else {
                toast.error("Could not empty the cart");
            }
        } catch (err) {
            console.error("Error clearing cart:", err);
            toast.error("Could not empty the cart");
        }
    };

    const handlePayment = async () => {
        if (!cart || !userId) return;
        setPaying(true);
        try {
            const response = await axiosInstance.post(`v1/payment/checkout?cartId=${cart.id}`);
            if (response.data?.success) {
                window.location.href = response.data.data;
            } else {
                toast.error("We could not start the payment. Please try again.");
                setPaying(false);
            }
        } catch (err) {
            console.error("Error creating checkout session:", err);
            toast.error("We could not start the payment. Please try again.");
            setPaying(false);
        }
    };

    const books = cart?.books ?? [];
    const subtotal = books.reduce((sum, book) => sum + (book.price ?? 0), 0);
    const delivery = DELIVERY_FEE_PER_BOOK * books.length;
    const total = subtotal + delivery;

    const shell = (content: React.ReactNode) => (
        <Layout>
            <div className="mx-auto w-full max-w-[1000px] px-4 py-8 sm:px-8 sm:py-10">{content}</div>
        </Layout>
    );

    if (!authLoading && !user) {
        return shell(
            <div className="py-20 text-center">
                <h1 className="page-title">Your cart</h1>
                <p className="mt-3 text-ink-2">Log in to see the books you are borrowing.</p>
                <Link to="/login" onClick={() => localStorage.setItem("redirectAfterLogin", "/cart")} className="btn-accent mt-6">
                    Log in
                </Link>
            </div>
        );
    }

    if (loading || authLoading) {
        return shell(
            <div aria-busy="true" aria-label="Loading your cart">
                <div className="skeleton mb-8 h-8 w-40 rounded-full"></div>
                <div className="grid gap-10 lg:grid-cols-[1fr_320px]">
                    <div className="space-y-6">
                        {[0, 1].map(i => (
                            <div key={i} className="flex gap-5">
                                <div className="skeleton h-[132px] w-[88px] rounded-cover"></div>
                                <div className="flex-1 space-y-3 pt-2">
                                    <div className="skeleton h-5 w-2/3 rounded-full"></div>
                                    <div className="skeleton h-4 w-1/3 rounded-full"></div>
                                </div>
                            </div>
                        ))}
                    </div>
                    <div className="skeleton h-64 rounded-surface"></div>
                </div>
            </div>
        );
    }

    if (error) {
        return shell(
            <div role="alert" className="max-w-xl rounded-surface bg-bad-tint p-6 text-bad">
                <p className="font-serif text-xl font-semibold">{error}</p>
                <button onClick={() => { setLoading(true); loadCart(); }} className="mt-3 text-sm font-medium underline underline-offset-2">
                    Try again
                </button>
            </div>
        );
    }

    if (books.length === 0) {
        return shell(
            <div className="py-20 text-center">
                <h1 className="page-title">Your cart is empty</h1>
                <p className="mt-3 text-ink-2">Borrow a book and it will wait for you here.</p>
                <Link to="/books" className="btn-accent mt-6">Browse books</Link>
            </div>
        );
    }

    return shell(
        <>
            <div className="mb-8 flex items-baseline gap-3">
                <h1 className="page-title">Your cart</h1>
                <span className="text-[15px] text-ink-3">{books.length} {books.length === 1 ? "book" : "books"}</span>
            </div>

            <div className="grid items-start gap-10 lg:grid-cols-[1fr_320px] lg:gap-14">
                <section aria-label="Books in your cart">
                    <ul className="divide-y divide-line">
                        {books.map(book => (
                            <li key={book.id} className="flex gap-5 py-6 first:pt-0">
                                <Link to={`/books/${book.id}`} className="block w-[88px] shrink-0 rounded-cover shadow-1" aria-label={book.title}>
                                    <BookCover
                                        compact
                                        title={book.title}
                                        authors={book.authorNames}
                                        src={book.thumbnailPath ? `/api/v1/books/images/${book.thumbnailPath}` : null}
                                    />
                                </Link>
                                <div className="flex min-w-0 flex-1 flex-col">
                                    <Link to={`/books/${book.id}`} className="font-serif text-lg font-semibold leading-snug text-ink hover:text-accent">
                                        {book.title}
                                    </Link>
                                    {book.authorNames && book.authorNames.length > 0 && (
                                        <p className="mt-0.5 text-sm text-ink-2">{book.authorNames.join(", ")}</p>
                                    )}
                                    <div className="mt-auto flex items-end justify-between gap-4 pt-4">
                                        <button
                                            onClick={() => removeFromCart(book.id)}
                                            className="text-[13px] text-ink-2 underline underline-offset-2 hover:text-ink"
                                        >
                                            Remove
                                        </button>
                                        <p className="text-[15px] font-medium tabular-nums text-ink">
                                            {book.price ? euro(book.price) : "Free to borrow"}
                                        </p>
                                    </div>
                                </div>
                            </li>
                        ))}
                    </ul>
                    <button onClick={clearCart} className="mt-2 text-[13px] text-ink-2 underline underline-offset-2 hover:text-ink">
                        Empty cart
                    </button>
                </section>

                <aside className="rounded-surface bg-card p-6 shadow-1 lg:sticky lg:top-24" aria-label="Order summary">
                    <h2 className="font-serif text-xl font-semibold text-ink">Summary</h2>
                    <dl className="mt-5 space-y-3 text-[15px]">
                        <div className="flex justify-between">
                            <dt className="text-ink-2">Books</dt>
                            <dd className="tabular-nums text-ink">{subtotal > 0 ? euro(subtotal) : "Free"}</dd>
                        </div>
                        <div className="flex justify-between">
                            <dt className="text-ink-2">Delivery ({books.length} × {euro(DELIVERY_FEE_PER_BOOK)})</dt>
                            <dd className="tabular-nums text-ink">{euro(delivery)}</dd>
                        </div>
                        <div className="flex justify-between border-t border-line pt-4 text-lg font-semibold">
                            <dt>Total</dt>
                            <dd className="tabular-nums">{euro(total)}</dd>
                        </div>
                    </dl>
                    <button onClick={handlePayment} disabled={paying} className="btn-accent mt-6 w-full">
                        {paying ? "Opening secure payment..." : "Pay now"}
                    </button>
                    <p className="mt-3 text-center text-[13px] text-ink-3">Payment is handled securely by Stripe.</p>
                </aside>
            </div>
        </>
    );
};

export default ShoppingCart;
