import {User} from "./User.ts";
import {Book} from "./Book.ts";

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