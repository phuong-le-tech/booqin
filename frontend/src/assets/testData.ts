import { User } from "../model/User";
import { Author } from "../model/Author";
import { Book } from "../model/Book";
import { BookCollection } from "../model/BookCollection";

// DATA TEST => REMOVE WHEN DONE, WITH REAL DATA
// TEST USERS
export const userTest: User = {
    id: 1,
    firstName: "John",
    lastName: "Doe",
    email: "john.doe@example.com"
};

// TEST AUTHORS
export const authorsTest: Author[] = [
    { id: 1, firstName: "Jane", lastName: "Austen", books: [] },
    { id: 2, firstName: "George", lastName: "Orwell", books: [] },
    { id: 3, firstName: "J.K.", lastName: "Rowling", books: [] }
];

// TEST BOOKS
export const booksTest: Book[] = [
    {
        id: 1,
        isbns: ["978-3-16-148410-0"],
        title: "Pride and Prejudice",
        authors: [authorsTest[0]],
        description: "A classic novel about love and society.",
        publicationDate: "1813-01-28",
        collections: [],
        createdAt: new Date("2024-01-01"),
        updatedAt: new Date("2024-02-01")
    },
    {
        id: 2,
        isbns: ["978-0-452-28423-4"],
        title: "1984",
        authors: [authorsTest[1]],
        description: "A dystopian novel about surveillance and control.",
        publicationDate: "1949-06-08",
        collections: [],
        createdAt: new Date("2024-01-05"),
        updatedAt: new Date("2024-02-05")
    },
    {
        id: 3,
        isbns: ["978-0-7475-3269-9"],
        title: "Harry Potter and the Philosopher's Stone",
        authors: [authorsTest[2]],
        description: "The first book of the Harry Potter series.",
        publicationDate: "1997-06-26",
        collections: [],
        createdAt: new Date("2024-01-10"),
        updatedAt: new Date("2024-02-10")
    }
];
// Associating books with authors
authorsTest[0].books.push(booksTest[0]);
authorsTest[1].books.push(booksTest[1]);
authorsTest[2].books.push(booksTest[2]);

// TEST BOOK COLLECTIONS
export const collections: BookCollection[] = [
    {
        id: "1",
        title: "Classic Novels",
        description: "A collection of timeless literary classics.",
        owner: userTest,
        isPublic: true,
        books: [booksTest[0], booksTest[1]],
        createdAt: new Date("2024-01-15"),
        updatedAt: new Date("2024-02-15")
    },
    {
        id: "2",
        title: "Fantasy Books",
        description: "Books from the fantasy genre.",
        owner: userTest,
        isPublic: true,
        books: [booksTest[2]],
        createdAt: new Date("2024-01-20"),
        updatedAt: new Date("2024-02-20")
    },
    {
        id: "3",
        title: "Non-Fiction Books",
        description: "Books that don't fit into the fantasy or classic genres.",
        owner: userTest,
        isPublic: true,
        books: [booksTest[0]],
        createdAt: new Date("2024-01-25"),
        updatedAt: new Date("2024-02-25")
    },
    {
        id: "4",
        title: "Harry Potter Series",
        description: "All books from the Harry Potter series.",
        owner: userTest,
        isPublic: true,
        books: [booksTest[2]],
        createdAt: new Date("2024-01-30"),
        updatedAt: new Date("2024-02-28")
    }
];
// Associating books with collections
booksTest[0].collections.push(collections[0], collections[2]);
booksTest[1].collections.push(collections[0]);
booksTest[2].collections.push(collections[1], collections[3]);
