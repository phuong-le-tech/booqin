import {PageInfo} from "./Pagination.ts";
import {Author} from "./Author.ts";

// Modèle Book pour les affichages liste (BookIndex)
export interface BookIndex {
    id: number;
    title: string;
    authorNames: string[];
    publicationDate: Date;
}

// Modèle Book détaillé (BookShow)
export interface Book {
    id: number;
    isbns: string[];
    title: string;
    authors: Author[];
    description: string;
    publicationDate: string;
    createdAt: string;
    updatedAt: string;
    categories?: string[];
}

// Modèle pour la mise à jour partielle de Book
export interface BookUpdate {
    title?: string;
    description?: string;
    publicationDate?: string;
    isbns?: string[];
}

export interface BookPagedResponse {
    content: BookIndex[];
    page: PageInfo;
}

export interface BookCollection {
    id: number;
    name: string;
    description?: string;
}