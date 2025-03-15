import {Address} from "./Address.ts";
import {BookCollection} from "./BookCollection.ts";

export interface User {
    id: number;
    firstName: string;
    lastName: string;
    email: string;
    address: Address;
    collections: BookCollection[]
    role: string;
}

export interface UserUpdateData {
    id: string;
    firstName: string;
    lastName: string;
    email: string;
    address: Address;
}

export interface UserShow {
    id: number;
    firstName: string;
    lastName: string;
    email: string;
    address?: Address;
    role?: string;
}

