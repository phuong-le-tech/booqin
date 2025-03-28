import React, { useState, useEffect } from "react";

interface BookCreateData {
    title: string;
    authors: string[];
    description: string;
    publicationDate: string;
    isbn: string[];
}

interface BookAddModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSubmit: (bookData: BookCreateData) => void;
    error?: string | null;
}

const BookAddModal: React.FC<BookAddModalProps> = ({ isOpen, onClose, onSubmit, error }) => {
    const [newBook, setNewBook] = useState<BookCreateData>({
        title: "",
        authors: [""],
        description: "",
        publicationDate: "",
        isbn: [""]
    });

    const [isSubmitting, setIsSubmitting] = useState(false);
    const [validationError, setValidationError] = useState<string | null>(null);

    useEffect(() => {
        if (!isOpen) {
            setNewBook({
                title: "",
                authors: [""],
                description: "",
                publicationDate: "",
                isbn: [""]
            });
            setIsSubmitting(false);
            setValidationError(null);
        }
    }, [isOpen]);

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value } = e.target;
        setNewBook({
            ...newBook,
            [name]: value
        });
    };

    const handleAuthorChange = (index: number, value: string) => {
        const updatedAuthors = [...newBook.authors];
        updatedAuthors[index] = value;
        setNewBook({
            ...newBook,
            authors: updatedAuthors
        });
    };

    const addAuthorField = () => {
        setNewBook({
            ...newBook,
            authors: [...newBook.authors, ""]
        });
    };

    const removeAuthorField = (index: number) => {
        const updatedAuthors = [...newBook.authors];
        updatedAuthors.splice(index, 1);
        setNewBook({
            ...newBook,
            authors: updatedAuthors
        });
    };

    const handleIsbnChange = (index: number, value: string) => {
        const updatedIsbns = [...newBook.isbn];
        updatedIsbns[index] = value;
        setNewBook({
            ...newBook,
            isbn: updatedIsbns
        });
    };

    const addIsbnField = () => {
        setNewBook({
            ...newBook,
            isbn: [...newBook.isbn, ""]
        });
    };

    const removeIsbnField = (index: number) => {
        const updatedIsbns = [...newBook.isbn];
        updatedIsbns.splice(index, 1);
        setNewBook({
            ...newBook,
            isbn: updatedIsbns
        });
    };

    const validateForm = (): string | null => {
        if (!newBook.title.trim()) {
            return "Le titre est obligatoire";
        }

        const validAuthors = newBook.authors.filter(author => author.trim() !== "");
        if (validAuthors.length === 0) {
            return "Au moins un auteur est requis";
        }

        if (newBook.publicationDate) {
            const publishDate = new Date(newBook.publicationDate);
            const today = new Date();
            if (publishDate > today) {
                return "La date de publication doit être dans le passé";
            }
        }

        const isbnRegex = /^(?:\d{10}|\d{13})$/;
        const invalidIsbns = newBook.isbn
            .filter(isbn => isbn.trim() !== "")
            .filter(isbn => !isbnRegex.test(isbn.trim().replace(/-/g, "")));

        if (invalidIsbns.length > 0) {
            return "Format d'ISBN invalide. Les ISBN doivent avoir 10 ou 13 chiffres";
        }

        return null;
    };

    const handleAddBook = async (e: React.FormEvent) => {
        e.preventDefault();

        const error = validateForm();
        if (error) {
            setValidationError(error);
            return;
        }

        setValidationError(null);
        setIsSubmitting(true);

        try {
            const formattedData = {
                ...newBook,
                authors: newBook.authors.filter(author => author.trim() !== ""),
                isbn: newBook.isbn.filter(isbn => isbn.trim() !== "")
            };

            await onSubmit(formattedData);
        } catch (error) {
            console.error(error);
        } finally {
            setIsSubmitting(false);
        }
    };


    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
            <div className="fixed inset-0 backdrop-blur-[1px]" onClick={onClose}></div>
            <div className="bg-white rounded-lg shadow-xl w-full max-w-3xl mx-4 max-h-[90vh] overflow-y-auto z-10">
                <div className="bg-blue-600 px-4 py-3 flex justify-between items-center">
                    <h3 className="text-lg font-medium text-white">Add New Book</h3>
                    <button
                        onClick={onClose}
                        className="text-white hover:text-gray-200 focus:outline-none"
                        disabled={isSubmitting}
                    >
                        <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
                        </svg>
                    </button>
                </div>

                {error && (
                    <div className="mx-6 mt-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded">
                        {error}
                    </div>
                )}

                {validationError && (
                    <div className="mx-6 mt-4 p-3 bg-orange-100 border border-orange-400 text-orange-700 rounded">
                        {validationError}
                    </div>
                )}

                <form onSubmit={handleAddBook} className="p-6">
                    {/* Title */}
                    <div className="mb-6">
                        <label htmlFor="title" className="block text-sm font-medium text-gray-700 mb-1">
                            Title *
                        </label>
                        <input
                            type="text"
                            id="title"
                            name="title"
                            value={newBook.title}
                            onChange={handleInputChange}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            required
                            disabled={isSubmitting}
                        />
                    </div>

                    {/* Authors */}
                    <div className="mb-6">
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Authors *
                        </label>
                        <div className="border border-gray-300 rounded-md p-4 max-h-48 overflow-y-auto">
                            {newBook.authors.map((author, index) => (
                                <div key={index} className="flex items-center mb-2">
                                    <input
                                        type="text"
                                        value={author}
                                        onChange={(e) => handleAuthorChange(index, e.target.value)}
                                        placeholder="First Name Last Name"
                                        className="flex-grow px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                        required={index === 0}
                                        disabled={isSubmitting}
                                    />
                                    <button
                                        type="button"
                                        onClick={() => removeAuthorField(index)}
                                        className="ml-2 text-red-500 hover:text-red-700 disabled:text-gray-400"
                                        disabled={newBook.authors.length === 1 || isSubmitting}
                                    >
                                        <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                        </svg>
                                    </button>
                                </div>
                            ))}
                        </div>
                        <button
                            type="button"
                            onClick={addAuthorField}
                            className="mt-2 text-sm text-blue-600 hover:text-blue-800 focus:outline-none flex items-center disabled:text-gray-400"
                            disabled={isSubmitting}
                        >
                            <svg className="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                            </svg>
                            Add another author
                        </button>
                    </div>

                    {/* Description */}
                    <div className="mb-6">
                        <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-1">
                            Description
                        </label>
                        <textarea
                            id="description"
                            name="description"
                            value={newBook.description}
                            onChange={handleInputChange}
                            rows={4}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            disabled={isSubmitting}
                        ></textarea>
                    </div>

                    {/* Publication Date */}
                    <div className="mb-6">
                        <label htmlFor="publicationDate" className="block text-sm font-medium text-gray-700 mb-1">
                            Publication Date
                        </label>
                        <input
                            type="date"
                            id="publicationDate"
                            name="publicationDate"
                            value={newBook.publicationDate}
                            onChange={handleInputChange}
                            max={new Date().toISOString().split('T')[0]} // Limite la date à aujourd'hui
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            disabled={isSubmitting}
                        />
                    </div>

                    {/* ISBNs */}
                    <div className="mb-6">
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            ISBN
                        </label>
                        <div className="border border-gray-300 rounded-md p-4 max-h-48 overflow-y-auto">
                            {newBook.isbn.map((isbn, index) => (
                                <div key={index} className="flex items-center mb-2">
                                    <input
                                        type="text"
                                        value={isbn}
                                        onChange={(e) => handleIsbnChange(index, e.target.value)}
                                        placeholder="e.g. 9781234567890"
                                        className="flex-grow px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                        disabled={isSubmitting}
                                    />
                                    <button
                                        type="button"
                                        onClick={() => removeIsbnField(index)}
                                        className="ml-2 text-red-500 hover:text-red-700 disabled:text-gray-400"
                                        disabled={newBook.isbn.length === 1 || isSubmitting}
                                    >
                                        <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                        </svg>
                                    </button>
                                </div>
                            ))}
                        </div>
                        <button
                            type="button"
                            onClick={addIsbnField}
                            className="mt-2 text-sm text-blue-600 hover:text-blue-800 focus:outline-none flex items-center disabled:text-gray-400"
                            disabled={isSubmitting || newBook.isbn.length >= 5} // Limite à 5 ISBNs max
                        >
                            <svg className="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                            </svg>
                            Add another ISBN
                        </button>
                        <p className="mt-1 text-xs text-gray-500">
                            Maximum 5 ISBNs. Format: 10 ou 13 chiffres.
                        </p>
                    </div>

                    {/* Submit Button */}
                    <div className="flex justify-end space-x-3">
                        <button
                            type="button"
                            onClick={onClose}
                            className="px-4 py-2 bg-gray-300 text-gray-800 rounded-md hover:bg-gray-400 focus:outline-none disabled:bg-gray-200"
                            disabled={isSubmitting}
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none disabled:bg-blue-400 flex items-center"
                            disabled={isSubmitting}
                        >
                            {isSubmitting ? (
                                <>
                                    <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                    </svg>
                                    Processing...
                                </>
                            ) : (
                                "Add Book"
                            )}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default BookAddModal;