export interface Author {
    id: string;
    firstName: string;
    lastName: string;
    fullName: string;
}

export interface Book {
    id: string;
    isbns: string[];
    title: string;
    authors: Author[];
    description: string;
    publicationDate: string;
    createdAt: string;
    updatedAt: string;
}