import {PageInfo} from "./Pagination.ts";
import {Author} from "./Author.ts";

export interface BookIndex {
    id: number;
    title: string;
    authorNames: string[];
    publicationDate: Date;
}

export interface BookShow {
    id: number;
    isbns: string[];
    title: string;
    authors: {
        id: number;
        firstName: string;
        lastName: string;
    }[];
    description: string;
    publicationDate: Date;
    createdAt: string;
    updatedAt: string;
    categories?: string[];
    coverImageUrl?: string;
    isExternalImage?: boolean;
}

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
    coverImageUrl?: string;
    isExternalImage?: boolean;
}

export interface BookUpdate {
    title?: string;
    description?: string;
    publicationDate?: string;
    isbns?: string[];
    coverImagePath?: string;
    isExternalImage?: boolean;
}

export interface BookPagedResponse {
    content: BookShow[];
    page: PageInfo;
}

export interface BookCollection {
    id: number;
    name: string;
    description?: string;
}