"use client";

import React, {useContext} from "react";
import {Link} from "react-router-dom";
import {BookCollection} from "../../../model/BookCollection.ts";
import {User} from "../../../model/User.ts";
import {Book} from "../../../model/Book.ts";
import {Author} from "../../../model/Author.ts";


const userTest: User = {
    id: 1,
    firstName: 'John',
    lastName: 'Doe',
    email: 'john.doe@example.com',
};

// TEST AUTHORS
const authorsTest: Author[] = [
  { id: 1, firstName: "Jane", lastName: "Austen", books: [] },
  { id: 2, firstName: "George", lastName: "Orwell", books: [] },
  { id: 3, firstName: "J.K.", lastName: "Rowling", books: [] }
];

// TEST BOOKS
const booksTest: Book[] = [
  {
    id: 1,
    isbns: ["978-3-16-148410-0"],
    title: "Pride and Prejudice",
    authors: [authorsTest[0]],
    description: "A classic novel about love and society.",
    publicationDate: "1813-01-28",
    collections: [],
    createdAt: new Date("2024-01-01"),
    updatedAt: new Date("2024-02-01")
  },
  {
    id: 2,
    isbns: ["978-0-452-28423-4"],
    title: "1984",
    authors: [authorsTest[1]],
    description: "A dystopian novel about surveillance and control.",
    publicationDate: "1949-06-08",
    collections: [],
    createdAt: new Date("2024-01-05"),
    updatedAt: new Date("2024-02-05")
  },
  {
    id: 3,
    isbns: ["978-0-7475-3269-9"],
    title: "Harry Potter and the Philosopher's Stone",
    authors: [authorsTest[2]],
    description: "The first book of the Harry Potter series.",
    publicationDate: "1997-06-26",
    collections: [],
    createdAt: new Date("2024-01-10"),
    updatedAt: new Date("2024-02-10")
  }
];
authorsTest[0].books.push(booksTest[0]);
authorsTest[1].books.push(booksTest[1]);
authorsTest[2].books.push(booksTest[2]);

// DATA TEST => REMOVE WHEN DONE, WITH REAL DATA
const collections: BookCollection[] = [
  {
    id: "1",
    title: "Classic Novels",
    description: "A collection of timeless literary classics.",
    owner: userTest,
    books: [booksTest[0], booksTest[1]],
    createdAt: new Date("2024-01-15"),
    updatedAt: new Date("2024-02-15")
  },
  {
    id: "2",
    title: "Fantasy Books",
    description: "Books from the fantasy genre.",
    owner: userTest,
    books: [booksTest[2]],
    createdAt: new Date("2024-01-20"),
    updatedAt: new Date("2024-02-20")
  }
];
booksTest[0].collections.push(collections[0]);
booksTest[1].collections.push(collections[0]);
booksTest[2].collections.push(collections[1]);

// const PublicCollections: React.FC<PublicCollectionsProps> = ({ collections }) => {
const PublicCollections: React.FC = () => {
  return (
    <div className="bg-gray-100 p-8">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-3xl font-bold text-gray-900 mb-6">Public Collections</h1>
        <div className="bg-white shadow-md rounded-lg overflow-hidden">
          <table className="min-w-full">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Name</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Description</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {collections.map((collection) => (
                <tr key={collection.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 text-sm text-gray-900">{collection.title}</td>
                  <td className="px-6 py-4 text-sm text-gray-500">{collection.description}</td>
                  <td className="px-6 py-4 text-sm font-medium">
                    {/*<a href={`/collections/${collection.id}`} className="text-indigo-600 hover:text-indigo-900">
                      View
                    </a>*/}
                    <Link to={`/collections/${collection.id}`}
                        className="text-indigo-600 hover:text-indigo-900">View</Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default PublicCollections;