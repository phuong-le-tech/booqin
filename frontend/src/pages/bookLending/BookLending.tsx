import {JSX, useEffect, useState} from "react";
import { Layout } from "../Layout";
import axiosInstance from "../../services/AxiosInstance.ts";
import { useAuth } from "../../context/AuthContext.tsx";
import { useNavigate } from "react-router-dom";

type Book = {
    id: number;
    title: string;
    isbns: string[];
    authors: { id: number; firstName: string; lastName: string }[];
    description: string;
    publicationDate: string;
};

type User = {
    id: number;
    firstName: string;
    lastName: string;
    email: string;
};

export type BookLendingPages = {
    id: number;
    book: Book;
    borrower: User;
    lender: User;
    status: string;
    reservedAt: string;
    startDate: string;
    dueDate: string;
    returnDate: string;
};

type LendingResponseDTO = {
    borrowedBooks: BookLendingPages[];
    lentBooks: BookLendingPages[];
    containsAll: boolean;
};

export const BookLending: () => JSX.Element = () => {
    const [lendingData, setLendingData] = useState<LendingResponseDTO | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [activeTab, setActiveTab] = useState<"borrowed" | "lent" | "all">("borrowed");
    const navigate = useNavigate();
    const {user} = useAuth();

    // Fetch book loans from the API
    useEffect(() => {
        if (!user?.id) return;

        const fetchLendings = async () => {
            try {
                console.log("📚 Fetching loans for userId:", user.id);
                const response = await axiosInstance.get(`/v1/loans/user/${user.id}?type=${activeTab}`);

                console.log("📚 Loans response:", response.data);

                if (response.data?.success && response.data?.data) {
                    setLendingData(response.data.data);
                } else {
                    throw new Error("Unexpected response format");
                }
            } catch (err: any) {
                console.error("❌ Detailed Axios error:", err);
                setError(err.response?.data?.message || "Error loading loans.");
            } finally {
                setLoading(false);
            }
        };

        fetchLendings();
    }, [user?.id, activeTab]);

    const handleChangeTab = (tab: "borrowed" | "lent" | "all") => {
        setActiveTab(tab);
        setLoading(true);
    };

    const formatDate = (dateString: string | null) => {
        if (!dateString) return "Not defined";
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    };

    const getStatusBadgeClass = (status: string) => {
        switch(status) {
            case "RESERVED":
                return "bg-yellow-100 text-yellow-800 border-yellow-300";
            case "BORROWED":
                return "bg-blue-100 text-blue-800 border-blue-300";
            case "RETURNED":
                return "bg-green-100 text-green-800 border-green-300";
            case "SHIPPED":
                return "bg-purple-100 text-purple-800 border-purple-300";
            case "AVAILABLE":
                return "bg-gray-100 text-gray-800 border-gray-300";
            case "PENDING_SHIPMENT":
                return "bg-orange-100 text-orange-800 border-orange-300";
            default:
                return "bg-gray-100 text-gray-800 border-gray-300";
        }
    };

    const getStatusLabel = (status: string) => {
        switch(status) {
            case "RESERVED": return "Reserved";
            case "BORROWED": return "Borrowed";
            case "RETURNED": return "Returned";
            case "SHIPPED": return "Shipped";
            case "AVAILABLE": return "Available";
            case "PENDING_SHIPMENT": return "Pending Shipment";
            default: return status;
        }
    };

    const handleReturn = async (lendingId: number) => {
        if (!user) return;

        try {
            const response = await axiosInstance.get(`/v1/loans/return-book?lendingId=${lendingId}`);
            if (response.data?.success) {
                // Refresh data
                const updatedResponse = await axiosInstance.get(`/v1/loans/user/${user.id}?type=${activeTab}`);
                setLendingData(updatedResponse.data.data);
                alert("The book has been marked as returned!");
            } else {
                alert("Unable to update the loan status.");
            }
        } catch (error) {
            console.error("Error while updating the loan:", error);
            alert("An error occurred.");
        }
    };

    const handleConfirmLoan = async (lendingId: number) => {
        if (!user) return;

        try {
            const response = await axiosInstance.get(`/v1/loans/confirm-loan?lendingId=${lendingId}`);
            if (response.data?.success) {
                // Refresh data
                const updatedResponse = await axiosInstance.get(`/v1/loans/user/${user.id}?type=${activeTab}`);
                setLendingData(updatedResponse.data.data);
                alert("The loan has been confirmed!");
            } else {
                alert("Unable to confirm the loan.");
            }
        } catch (error) {
            console.error("Error while confirming the loan:", error);
            alert("An error occurred.");
        }
    };

    const removeFromLoanable = async (id: number) => {
        if (!user?.id) return;

        setLoading(true);
        try {
            const response = await axiosInstance.delete("/v1/loans/remove-loanable", {
                params: {
                    bookId: id,
                    userId: user?.id
                }
            });

            if (response.data?.success) {
                alert("Book removed from your lending list");

                // Update the local state to remove the book
                setLendingData((prevData) => {
                    if (!prevData) return prevData;

                    const updatedLentBooks = prevData.lentBooks.filter((lending) => lending.book.id !== id);
                    return { ...prevData, lentBooks: updatedLentBooks };
                });
            } else {
                alert("Failed to remove book from lending list");
            }
        } catch (error: any) {
            console.error("Error removing book from lending list:", error);
            console.error("Response data:", error.response?.data);
            alert(error.response?.data?.message || "Failed to remove book from lending list");
        } finally {
            setLoading(false);
        }
    };

    const handleShipBook = async (lendingId: number) => {
        if (!user) return;

        try {
            const response = await axiosInstance.get(`/v1/loans/ship-book?lendingId=${lendingId}`);
            if (response.data?.success) {
                // Refresh data
                const updatedResponse = await axiosInstance.get(`/v1/loans/user/${user.id}?type=${activeTab}`);
                setLendingData(updatedResponse.data.data);
                alert("The book has been marked as shipped!");
            } else {
                alert("Unable to update shipping status.");
            }
        } catch (error) {
            console.error("Error while updating shipping status:", error);
            alert("An error occurred.");
        }
    };
    const handleDownloadTicket = async (lendingId: number) => {
        try {
            console.log(`Téléchargement du ticket pour le prêt ${lendingId}`);
            const token = localStorage.getItem('token');
            const url = `${axiosInstance.defaults.baseURL}/v1/loans/ticket-download/${lendingId}`;
            console.log(`URL de téléchargement: ${url}`);

            const response = await fetch(url, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            console.log(`Statut de la réponse: ${response.status} ${response.statusText}`);
            console.log(`Type de contenu: ${response.headers.get('Content-Type')}`);

            if (!response.ok) {
                 throw new Error(`Erreur HTTP: ${response.status}`);
            }

            const blob = await response.blob();
            console.log(`Taille du blob: ${blob.size} octets, type: ${blob.type}`);

            const blobUrl = window.URL.createObjectURL(blob);


            const a = document.createElement('a');
            a.href = blobUrl;
            a.download = `ticket_expedition_${lendingId}`;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);

            setTimeout(() => {
                window.URL.revokeObjectURL(blobUrl);
            }, 100);

        } catch (error) {
            console.error("Erreur lors du téléchargement du ticket:", error);
            alert("Impossible de télécharger le ticket d'expédition.");
        }
    };

    const getAuthorNames = (book: Book) => {
        if (!book.authors || book.authors.length === 0) return "Unknown author";
        return book.authors.map(author => `${author.firstName} ${author.lastName}`).join(", ");
    };

    return (
        <Layout>
            <div className="container mx-auto px-4 py-12 flex flex-col items-center min-h-screen">
                <h2 className="text-4xl font-bold mb-8 text-light-blue">
                    Your Book Loans
                </h2>

                {/* Tabs */}
                <div className="w-full max-w-6xl mb-8">
                    <div className="flex border-b border-gray-200">
                        <button
                            onClick={() => handleChangeTab("borrowed")}
                            className={`py-4 px-6 font-medium text-lg ${
                                activeTab === "borrowed"
                                    ? "border-b-2 border-light-blue text-light-blue"
                                    : "text-gray-500 hover:text-gray-700"
                            }`}
                        >
                            Borrowed Books
                        </button>
                        <button
                            onClick={() => handleChangeTab("lent")}
                            className={`py-4 px-6 font-medium text-lg ${
                                activeTab === "lent"
                                    ? "border-b-2 border-light-blue text-light-blue"
                                    : "text-gray-500 hover:text-gray-700"
                            }`}
                        >
                            Lent Books
                        </button>
                        <button
                            onClick={() => handleChangeTab("all")}
                            className={`py-4 px-6 font-medium text-lg ${
                                activeTab === "all"
                                    ? "border-b-2 border-light-blue text-light-blue"
                                    : "text-gray-500 hover:text-gray-700"
                            }`}
                        >
                            All Loans
                        </button>
                    </div>
                </div>

                {loading ? (
                    <div className="w-full max-w-6xl flex justify-center p-12">
                        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-light-blue"></div>
                    </div>
                ) : error ? (
                    <div className="w-full max-w-6xl bg-red-100 border border-red-300 rounded-lg p-6 text-center">
                        <p className="text-red-600 text-lg font-medium">{error}</p>
                    </div>
                ) : (
                    <div className="w-full max-w-6xl">
                        {/* Borrowed books */}
                        {(activeTab === "borrowed" || activeTab === "all") && (
                            <div className="mb-8">
                                {activeTab === "all" && (
                                    <h3 className="text-2xl font-semibold mb-4">Books you have borrowed</h3>
                                )}

                                {lendingData?.borrowedBooks && lendingData.borrowedBooks.length > 0 ? (
                                    <div className="bg-white rounded-xl overflow-hidden shadow-lg border border-gray-200">
                                        <div className="divide-y divide-gray-200">
                                            {lendingData.borrowedBooks.map((lending) => (
                                                <div key={lending.id} className="p-6 hover:bg-gray-50 transition-colors">
                                                    <div className="flex flex-col lg:flex-row gap-6">
                                                        <div className="h-40 w-28 bg-gradient-to-br from-blue-100 to-purple-100 rounded-lg flex items-center justify-center flex-shrink-0 border border-gray-200">
                                                            <svg className="w-12 h-12 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"></path>
                                                            </svg>
                                                        </div>

                                                        <div className="flex-1">
                                                            <div className="flex flex-col md:flex-row md:items-start md:justify-between">
                                                                <div>
                                                                    <h3 className="text-xl font-bold text-gray-800">{lending.book.title}</h3>
                                                                    <p className="text-gray-600 mb-2">{getAuthorNames(lending.book)}</p>

                                                                    <div className="flex items-center gap-2 mb-1">
                                                                        <span className="text-gray-600 font-medium">Lent by:</span>
                                                                        <span>{lending.lender.firstName} {lending.lender.lastName}</span>
                                                                    </div>

                                                                    <div className="flex items-center gap-2 mb-1">
                                                                        <span className="text-gray-600 font-medium">Status:</span>
                                                                        <span className={`px-3 py-1 rounded-full text-sm font-medium border ${getStatusBadgeClass(lending.status)}`}>
                                                                            {getStatusLabel(lending.status)}
                                                                        </span>
                                                                    </div>
                                                                </div>

                                                                <div className="mt-4 md:mt-0 md:text-right">
                                                                    <div className="text-sm text-gray-600 mb-1">
                                                                        <span className="font-medium">Borrow date:</span> {formatDate(lending.startDate)}
                                                                    </div>
                                                                    <div className="text-sm text-gray-600 mb-1">
                                                                        <span className="font-medium">Expected return date:</span> {formatDate(lending.dueDate)}
                                                                    </div>
                                                                    {lending.returnDate && (
                                                                        <div className="text-sm text-gray-600">
                                                                            <span className="font-medium">Returned on:</span> {formatDate(lending.returnDate)}
                                                                        </div>
                                                                    )}
                                                                </div>
                                                            </div>

                                                            {lending.status === "BORROWED" && (
                                                                <div className="mt-4">
                                                                    <button
                                                                        onClick={() => handleReturn(lending.id)}
                                                                        className="bg-light-blue hover:bg-blue-600 text-white px-4 py-2 rounded-lg transition-colors"
                                                                    >
                                                                        Mark as returned
                                                                    </button>
                                                                </div>
                                                            )}

                                                            {lending.status === "SHIPPED" && (
                                                                <div className="mt-4">
                                                                    <button
                                                                        onClick={() => handleConfirmLoan(lending.id)}
                                                                        className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-lg transition-colors"
                                                                    >
                                                                        Confirm receipt
                                                                    </button>
                                                                </div>
                                                            )}

                                                            {(lending.status === "PENDING_SHIPMENT" || lending.status === "SHIPPED") && (
                                                                <div className="mt-4">
                                                                    <button
                                                                        onClick={() => handleDownloadTicket(lending.id)}
                                                                        className="bg-light-blue hover:bg-blue-600 text-white px-4 py-2 rounded-lg transition-colors"
                                                                    >
                                                                        Télécharger l'étiquette d'expédition
                                                                    </button>
                                                                </div>
                                                            )}
                                                        </div>
                                                    </div>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                ) : (
                                    <div className="bg-gray-50 rounded-xl p-12 text-center border border-gray-200 shadow-md">
                                        <svg className="w-16 h-16 mx-auto text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"></path>
                                        </svg>
                                        <p className="text-gray-700 text-xl font-medium">You have no borrowed books</p>
                                        <p className="text-gray-500 mt-2 mb-6">Browse our catalog to find books to borrow</p>
                                        <button
                                            onClick={() => navigate("/books")}
                                            className="bg-light-blue hover:bg-blue-600 text-white font-semibold px-6 py-3 rounded-lg transition duration-300"
                                        >
                                            Browse books
                                        </button>
                                    </div>
                                )}
                            </div>
                        )}

                        {/* Lent books */}
                        {(activeTab === "lent" || activeTab === "all") && (
                            <div>
                                {/* Lent books with borrower */}
                                {lendingData?.lentBooks && lendingData.lentBooks.filter(lending => lending.borrower).length > 0 && (
                                    <div className="mb-8">
                                        {activeTab === "all" && (
                                            <h3 className="text-2xl font-semibold mb-4">Books currently lent</h3>
                                        )}

                                        <div className="bg-white rounded-xl overflow-hidden shadow-lg border border-gray-200">
                                            <div className="divide-y divide-gray-200">
                                                {lendingData.lentBooks
                                                    .filter(lending => lending.borrower)
                                                    .map((lending) => (
                                                        <div key={lending.id} className="p-6 hover:bg-gray-50 transition-colors">
                                                            <div className="flex flex-col lg:flex-row gap-6">
                                                                <div className="h-40 w-28 bg-gradient-to-br from-green-100 to-blue-100 rounded-lg flex items-center justify-center flex-shrink-0 border border-gray-200">
                                                                    <svg className="w-12 h-12 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"></path>
                                                                    </svg>
                                                                </div>

                                                                <div className="flex-1">
                                                                    <div className="flex flex-col md:flex-row md:items-start md:justify-between">
                                                                        <div>
                                                                            <h3 className="text-xl font-bold text-gray-800">{lending.book.title}</h3>
                                                                            <p className="text-gray-600 mb-2">{getAuthorNames(lending.book)}</p>

                                                                            <div className="flex items-center gap-2 mb-1">
                                                                                <span className="text-gray-600 font-medium">Borrowed by:</span>
                                                                                <span>{lending.borrower.firstName} {lending.borrower.lastName}</span>
                                                                            </div>

                                                                            <div className="flex items-center gap-2 mb-1">
                                                                                <span className="text-gray-600 font-medium">Status:</span>
                                                                                <span className={`px-3 py-1 rounded-full text-sm font-medium border ${getStatusBadgeClass(lending.status)}`}>
                                                                                    {getStatusLabel(lending.status)}
                                                                                </span>
                                                                            </div>
                                                                        </div>

                                                                        <div className="mt-4 md:mt-0 md:text-right">
                                                                            <div className="text-sm text-gray-600 mb-1">
                                                                                <span className="font-medium">Borrow date:</span> {formatDate(lending.startDate)}
                                                                            </div>
                                                                            <div className="text-sm text-gray-600 mb-1">
                                                                                <span className="font-medium">Expected return date:</span> {formatDate(lending.dueDate)}
                                                                            </div>
                                                                        </div>
                                                                    </div>

                                                                    {(lending.status === "PENDING_SHIPMENT" || lending.status === "SHIPPED") && (
                                                                        <div className="mt-4">
                                                                            <button
                                                                                onClick={() => handleDownloadTicket(lending.id)}
                                                                                className="bg-light-blue hover:bg-blue-600 text-white px-4 py-2 rounded-lg transition-colors mr-2"
                                                                            >
                                                                                Télécharger l'étiquette d'expédition
                                                                            </button>

                                                                            {lending.status === "PENDING_SHIPMENT" && (
                                                                                <button
                                                                                    onClick={() => handleShipBook(lending.id)}
                                                                                    className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-lg transition-colors"
                                                                                >
                                                                                    Marquer comme expédié
                                                                                </button>
                                                                            )}
                                                                        </div>
                                                                    )}
                                                                </div>
                                                            </div>
                                                        </div>
                                                    ))}
                                            </div>
                                        </div>
                                    </div>
                                )}

                                {/* Books available for lending */}
                                {lendingData?.lentBooks && lendingData.lentBooks.filter(lending => !lending.borrower).length > 0 && (
                                    <div>
                                        {activeTab === "all" && (
                                            <h3 className="text-2xl font-semibold mb-4">Books available for lending</h3>
                                        )}

                                        <div className="bg-white rounded-xl overflow-hidden shadow-lg border border-gray-200">
                                            <div className="divide-y divide-gray-200">
                                                {lendingData.lentBooks
                                                    .filter(lending => !lending.borrower)
                                                    .map((lending) => (
                                                        <div key={lending.id} className="p-6 hover:bg-gray-50 transition-colors">
                                                            <div className="flex flex-col lg:flex-row gap-6">
                                                                <div className="h-40 w-28 bg-gradient-to-br from-yellow-100 to-orange-100 rounded-lg flex items-center justify-center flex-shrink-0 border border-gray-200">
                                                                    <svg className="w-12 h-12 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                                                                    </svg>
                                                                </div>

                                                                <div className="flex-1">
                                                                    <div className="flex flex-col md:flex-row md:items-start md:justify-between">
                                                                        <div>
                                                                            <h3 className="text-xl font-bold text-gray-800">{lending.book.title}</h3>
                                                                            <p className="text-gray-600 mb-2">{getAuthorNames(lending.book)}</p>

                                                                            <div className="flex items-center gap-2 mb-1">
                                                                                <span className="text-gray-600 font-medium">Status:</span>
                                                                                <span className="px-3 py-1 rounded-full text-sm font-medium border bg-yellow-100 text-yellow-800 border-yellow-300">
                                                                                    Available for lending
                                                                                </span>
                                                                            </div>
                                                                            <button
                                                                                onClick={() => removeFromLoanable(lending.book.id)}
                                                                                className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-lg transition-colors mt-4"
                                                                            >
                                                                                Remove from lending list
                                                                            </button>
                                                                        </div>

                                                                        <div className="mt-4 md:mt-0 md:text-right">
                                                                            <div className="text-sm text-gray-600 mb-1">
                                                                                <span className="font-medium">Made available on:</span> {formatDate(lending.reservedAt)}
                                                                            </div>
                                                                        </div>
                                                                    </div>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    ))}
                                            </div>
                                        </div>
                                    </div>
                                )}

                                {/* Message if no book is available */}
                                {lendingData?.lentBooks && lendingData.lentBooks.length === 0 && (
                                    <div className="bg-gray-50 rounded-xl p-12 text-center border border-gray-200 shadow-md">
                                        <svg className="w-16 h-16 mx-auto text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                                        </svg>
                                        <p className="text-gray-700 text-xl font-medium">You have no books in your collection</p>
                                        <p className="text-gray-500 mt-2 mb-6">Add books to your collection to be able to lend them</p>
                                        <button
                                            onClick={() => navigate("/profile")}
                                            className="bg-light-blue hover:bg-blue-600 text-white font-semibold px-6 py-3 rounded-lg transition duration-300"
                                        >
                                            Manage my collection
                                        </button>
                                    </div>
                                )}
                            </div>
                        )}
                    </div>
                )}
            </div>
        </Layout>
    );
};

export default BookLending;