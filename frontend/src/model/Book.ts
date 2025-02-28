import {Author} from "./Author.ts";

export interface Book {
    id: number;
    isbns: string[];
    title: string;
    authors: Author[];
    description: string;
    publicationDate: string;
    createdAt: Date;
    updatedAt: Date;
}