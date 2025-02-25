import {Author} from "./Author.ts";
import {BookCollection} from "./BookCollection.ts";

export interface Book {
    id: number;
    isbns: string[];
    title: string;
    authors: Author[];
    description: string;
    publicationDate: string;
    collections: BookCollection[];
    createdAt: Date;
    updatedAt: Date;
}