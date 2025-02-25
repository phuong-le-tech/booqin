import {Book} from "./Book.ts";

export interface Author {
    id: number;
    firstName: string;
    lastName: string;
    books: Book[];
}