import {PageInfo} from "./Pagination.ts";

// Modèle Book pour les affichages liste (BookIndex)
export interface BookIndex {
    id: number;
    title: string;
    authorNames: string[];
    publicationDate: string;
}

// Modèle Book détaillé (BookShow)
export interface BookShow {
    id: number;
    isbns: string[];
    title: string;
    authors: AuthorDto[];
    description: string;
    publicationDate: string;
    collections: BookCollection[];
    createdAt: string;
    updatedAt: string;
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

export interface AuthorDto {
    id: number;
    firstName: string;
    lastName: string;
}

export interface BookCollection {
    id: number;
    name: string;
    description?: string;
}

// Type générique pour Book (utilisé pour les fonctions qui peuvent renvoyer BookIndex ou BookShow)
export type Book = BookIndex | BookShow;