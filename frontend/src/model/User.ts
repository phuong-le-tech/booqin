import {Address} from "./Address.ts";

export interface User {
    id: number;
    firstName: string;
    lastName: string;
    email: string;
    role: string;
    createdAt?: string;
    updatedAt?: string;
    address: Address;
}

export interface UserUpdateData {
    id: string;
    firstName: string;
    lastName: string;
    email: string;
    address: Address;
}
