import {User} from "./User.ts";
import {Book, BookIndex} from "./Book.ts";
import {PageInfo} from "./Pagination.ts";

export interface BookCollection {
    id: number
    title: string,
    description: string,
    isPublic: boolean,
    books: BookIndex[],
    owner: User,
    createdAt: Date,
    updatedAt: Date
}

export interface BookCollectionShow {
    id: number;
    title: string;
    description: string;
    isPublic: boolean;
    books: Book[];
    user: User;
    createdAt: Date;
    updatedAt: Date;
}

export interface BookCollectionIndex {
    id: number;
    title: string;
    description: string;
    owner: User; // Assuming owner is represented by a name or identifier string
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
