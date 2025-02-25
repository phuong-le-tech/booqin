"use client";

import React, {useContext} from "react";
import {Link} from "react-router-dom";
import {BookCollection} from "../../../model/BookCollection.ts";
import {User} from "../../../model/User.ts";
import {Book} from "../../../model/Book.ts";
import {Author} from "../../../model/Author.ts";
import { collections } from "../../../assets/testData";



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