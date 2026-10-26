// src/pages/BookDetail.tsx
import React, { useState, useEffect, useRef } from "react";
import { useParams, Link } from "react-router-dom";
import { ArrowLeft, ExternalLink } from "lucide-react";
import { Layout } from "../Layout";
import { Book } from "../../model/Book";
import { Author } from "../../model/Author";
import BookService from "../../services/BookService";
import { VoteSection } from "../../components/vote/VoteSection";
import { CommentSection } from "../../components/comment/CommentSection";
import FollowButton from "../../components/follow/FollowButton.tsx";
import { BookCover } from "../../components/book/BookCover";
import { useBookBorrowing } from "../../hooks/useBookBorrowing";
import { useCart } from "../../hooks/useCart";
import { useAuth } from "../../context/AuthContext.tsx";

const IMAGE_BASE_URL = "/api/v1/books/images";

const formatDate = (dateString: string) => {
    try {
        return new Date(dateString).toLocaleDateString("en-US", { year: "numeric", month: "long", day: "numeric" });
    } catch {
        return dateString;
    }
};

const authorName = (author: Author) => `${author.firstName} ${author.lastName}`.trim();

/** Status and the one primary action for this book. */
const BorrowPanel: React.FC<{ bookId: number; coverRef: React.RefObject<HTMLDivElement | null> }> = ({ bookId, coverRef }) => {
    const { isAvailable, queuePosition, isInQueue, loading, addToCart, joinQueue, leaveQueue } = useBookBorrowing(bookId);
    const inCart = useCart().bookIds.includes(bookId);

    if (inCart) {
        return (
            <div className="flex flex-wrap items-center gap-4">
                <Link to="/cart" className="btn-accent">Go to cart</Link>
                <span className="status status-off">In your cart</span>
            </div>
        );
    }

    let status = { label: "Unavailable", tone: "status-off" };
    if (isAvailable) status = { label: "Available", tone: "status-ok" };
    else if (isInQueue) status = { label: `Waiting list #${queuePosition}`, tone: "status-wait" };

    return (
        <div className="flex flex-wrap items-center gap-4">
            {isAvailable ? (
                <button onClick={() => addToCart(coverRef.current)} disabled={loading} className="btn-accent">
                    {loading ? "Working..." : "Borrow this book"}
                </button>
            ) : isInQueue ? (
                <button onClick={leaveQueue} disabled={loading} className="btn-outline">
                    {loading ? "Working..." : "Leave waiting list"}
                </button>
            ) : (
                <button onClick={joinQueue} disabled={loading} className="btn-outline">
                    {loading ? "Working..." : "Notify me when available"}
                </button>
            )}
            <span className={`status ${status.tone}`}>{status.label}</span>
        </div>
    );
};

export const BookDetail: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const [book, setBook] = useState<Book | null>(null);
    const [isLoading, setIsLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [isbnExpanded, setIsbnExpanded] = useState<boolean>(false);
    const { user } = useAuth();
    const coverRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const fetchBookDetails = async () => {
            if (!id) return;

            try {
                setIsLoading(true);
                const bookData = await BookService.getBookById(id);
                if (bookData) {
                    setBook(bookData);
                    setError(null);
                } else {
                    setError("Book not found");
                    setBook(null);
                }
            } catch (error) {
                console.error("Failed to fetch book details:", error);
                setError("Failed to load book details. Please try again later.");
            } finally {
                setIsLoading(false);
            }
        };

        fetchBookDetails();
    }, [id]);

    const shell = (content: React.ReactNode) => (
        <Layout>
            <div className="mx-auto w-full max-w-[1200px] px-4 py-8 sm:px-8 sm:py-10">{content}</div>
        </Layout>
    );

    const backLink = (
        <Link to="/books" className="mb-8 inline-flex items-center gap-2 text-sm font-medium text-ink-2 hover:text-ink">
            <ArrowLeft className="h-4 w-4" aria-hidden="true" /> All books
        </Link>
    );

    if (isLoading) {
        return shell(
            <div className="grid gap-10 md:grid-cols-[360px_1fr]" aria-busy="true" aria-label="Loading book">
                <div className="skeleton aspect-[2/3] w-full max-w-[360px] rounded-cover"></div>
                <div>
                    <div className="skeleton mb-4 h-10 w-2/3 rounded-full"></div>
                    <div className="skeleton mb-8 h-5 w-1/3 rounded-full"></div>
                    <div className="skeleton mb-3 h-4 w-full rounded-full"></div>
                    <div className="skeleton mb-3 h-4 w-full rounded-full"></div>
                    <div className="skeleton h-4 w-3/4 rounded-full"></div>
                </div>
            </div>
        );
    }

    if (error || !book) {
        return shell(
            <>
                {backLink}
                <div role="alert" className="max-w-xl rounded-surface bg-bad-tint p-6 text-bad">
                    <p className="font-serif text-xl font-semibold">{error || "Book not found"}</p>
                    <p className="mt-1 text-sm">It may have been removed, or the link is wrong.</p>
                </div>
            </>
        );
    }

    const authors = book.authors ?? [];
    const authorNames = authors.map(authorName);
    const isbns = book.isbns ?? [];
    const shownIsbns = isbnExpanded ? isbns : isbns.slice(0, 3);
    const coverPath = book.coverImagePath ?? book.thumbnailPath;
    const coverSrc = coverPath ? `${IMAGE_BASE_URL}/${coverPath}` : null;

    return shell(
        <>
            {backLink}

            <div className="grid gap-10 md:grid-cols-[360px_1fr] lg:gap-16">
                <div className="md:sticky md:top-24 md:self-start">
                    <div ref={coverRef} className="max-w-[360px] rounded-cover shadow-2">
                        <BookCover title={book.title} authors={authorNames} src={coverSrc} />
                    </div>
                </div>

                <div className="min-w-0">
                    <h1 className="font-serif text-[32px] font-semibold leading-[1.1] tracking-[-0.02em] text-ink sm:text-[40px]">
                        {book.title}
                    </h1>
                    {authorNames.length > 0 && (
                        <p className="mt-3 text-lg text-ink-2">by {authorNames.join(", ")}</p>
                    )}

                    <div className="mt-8">
                        <BorrowPanel bookId={book.id} coverRef={coverRef} />
                    </div>

                    <div className="mt-6 flex flex-wrap items-center gap-3">
                        <VoteSection entityId={book.id} entityType="BOOK" />
                        {user && (
                            <FollowButton entityId={book.id} entityType="BOOK" showFollowersCount={true} />
                        )}
                    </div>

                    {book.description && (
                        <section className="mt-10 max-w-[65ch]" aria-labelledby="about-heading">
                            <h2 id="about-heading" className="mb-3 text-xl font-semibold tracking-[-0.01em] text-ink">About this book</h2>
                            <p className="whitespace-pre-line text-[15px] leading-6 text-ink-2">{book.description}</p>
                        </section>
                    )}

                    <dl className="mt-10 grid max-w-[65ch] grid-cols-[110px_1fr] gap-x-6 gap-y-3 border-t border-line pt-6 text-sm">
                        {book.publicationDate && (
                            <>
                                <dt className="text-ink-3">Published</dt>
                                <dd className="text-ink">{formatDate(book.publicationDate)}</dd>
                            </>
                        )}
                        {isbns.length > 0 && (
                            <>
                                <dt className="text-ink-3">ISBN</dt>
                                <dd className="text-ink tabular-nums">
                                    {shownIsbns.join(", ")}
                                    {isbns.length > 3 && (
                                        <button
                                            onClick={() => setIsbnExpanded(!isbnExpanded)}
                                            className="ml-2 text-accent underline underline-offset-2 hover:text-accent-hover"
                                        >
                                            {isbnExpanded ? "Show less" : `+${isbns.length - 3} more`}
                                        </button>
                                    )}
                                </dd>
                            </>
                        )}
                        {book.categories && book.categories.length > 0 && (
                            <>
                                <dt className="text-ink-3">Categories</dt>
                                <dd className="flex flex-wrap gap-2">
                                    {book.categories.map(category => (
                                        <span key={category} className="status status-off">{category}</span>
                                    ))}
                                </dd>
                            </>
                        )}
                        <dt className="text-ink-3">Added</dt>
                        <dd className="text-ink">{formatDate(book.createdAt)}</dd>
                        <dt className="text-ink-3">Updated</dt>
                        <dd className="text-ink">{formatDate(book.updatedAt)}</dd>
                    </dl>

                    {isbns.length > 0 && (
                        <a
                            href={`https://www.amazon.com/s?k=${encodeURIComponent(isbns[0])}`}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="mt-6 inline-flex items-center gap-2 text-sm text-ink-2 underline underline-offset-2 hover:text-ink"
                        >
                            Find a copy to buy <ExternalLink className="h-4 w-4" aria-hidden="true" />
                        </a>
                    )}
                </div>
            </div>

            <section className="mt-16 border-t border-line pt-10" aria-label="Discussion">
                <CommentSection entityId={book.id} entityType="BOOK" />
            </section>
        </>
    );
};
