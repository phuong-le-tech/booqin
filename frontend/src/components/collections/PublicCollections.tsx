"use client";

import React, {useContext} from "react";
import {Link} from "react-router-dom";
import {BookCollection} from "../../model/BookCollection.ts";
import {User} from "../../model/User.ts";
import {Book} from "../../model/Book.ts";
import {Author} from "../../model/Author.ts";
import { collections } from "../../assets/testData.ts";
import {SidebarNav} from "../../pages/SidebarNav.tsx";
import {Navbar} from "../../pages/Navbar.tsx";
import Layout from "../../pages/Layout.tsx";



// const PublicCollections: React.FC<PublicCollectionsProps> = ({ collections }) => {
const PublicCollections: React.FC = () => {
    return (
        <Layout>
            <div className="flex h-screen">
                <div className="flex-1 flex flex-col">
                    <div className="flex-1 bg-gray-100 p-8">
                        <div className="max-w-4xl mx-auto">
                            <h1 className="text-4xl font-bold text-gray-800 mb-8">Public Collections</h1>
                            <div className="bg-white shadow rounded-lg overflow-hidden">
                                <table className="min-w-full">
                                    <thead className="bg-gray-100">
                                    <tr>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">Name</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">Description</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">Actions</th>
                                    </tr>
                                    </thead>
                                    <tbody className="divide-y divide-gray-200">
                                    {collections.map((collection) => (
                                        <tr key={collection.id} className="hover:bg-gray-100">
                                            <td className="px-6 py-4 text-sm text-gray-800">{collection.title}</td>
                                            <td className="px-6 py-4 text-sm text-gray-600">{collection.description}</td>
                                            <td className="px-6 py-4 text-sm font-medium">
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
                </div>
            </div>
        </Layout>
    );
};

export default PublicCollections;