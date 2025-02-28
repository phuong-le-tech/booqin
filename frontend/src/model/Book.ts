import {Author} from "./Author.ts";

export interface Book {
    id: string;
    isbns: string[];
    title: string;
    authors: Author[];
    description: string;
    publicationDate: string;
    createdAt: string;
    updatedAt: string;
}