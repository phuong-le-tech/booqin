import {User} from "./User.ts";
import {Book} from "./Book.ts";
import {PageInfo} from "./Pagination.ts";

export interface BookCollection {
    id: String,
    title: String,
    description: String,
    owner: User,
    isPublic: Boolean,
    books: Book[],
    createdAt: Date,
    updatedAt: Date
}

export interface BookCollectionShow {
    id: number;
    title: string;
    description: string;
    owner: User;
    isPublic: boolean;
    books: Book[];
    createdAt: string;
    updatedAt: string;
}

export interface BookCollectionIndex {
    id: number;
    title: string;
    description: string;
    owner: string; // Assuming owner is represented by a name or identifier string
    isPublic: boolean;
}

export interface BookCollectionUpdate {
    title?: string;
    description?: string;
    books?: Book[];
}

export interface BookCollectionPagedResponse {
    content: BookCollectionIndex[];
    page: PageInfo;
}
