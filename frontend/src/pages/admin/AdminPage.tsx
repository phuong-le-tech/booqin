import type React from "react"
import { useState } from "react"
import { Layout } from "../Layout"
import { UsersTable } from "../../components/admin/UsersTable"
import { BooksTable } from "../../components/admin/BooksTable"
import { CollectionsTable } from "../../components/admin/CollectionsTable"

export const AdminPage: React.FC = () => {
    const [activeTab, setActiveTab] = useState<'users' | 'books' | 'collections'>('users')

    return (
        <Layout>
            <div className="p-4 sm:p-6 md:p-8">
                <h1 className="text-2xl sm:text-3xl font-bold mb-6 text-black">Admin Dashboard</h1>

                {/* Tabs */}
                <div className="mb-6 border-b border-gray-200">
                    <ul className="flex flex-wrap -mb-px">
                        <li className="mr-2">
                            <button
                                className={`inline-block p-4 rounded-t-lg ${
                                    activeTab === 'users'
                                        ? 'text-black font-semibold border-b-2 border-primary'
                                        : 'text-gray-500 hover:text-gray-600'
                                }`}
                                onClick={() => setActiveTab('users')}
                            >
                                Users
                            </button>
                        </li>
                        <li className="mr-2">
                            <button
                                className={`inline-block p-4 rounded-t-lg ${
                                    activeTab === 'books'
                                        ? 'text-black font-semibold border-b-2 border-primary'
                                        : 'text-gray-500 hover:text-gray-600'
                                }`}
                                onClick={() => setActiveTab('books')}
                            >
                                Books
                            </button>
                        </li>
                        <li>
                            <button
                                className={`inline-block p-4 rounded-t-lg ${
                                    activeTab === 'collections'
                                        ? 'text-black font-semibold border-b-2 border-primary'
                                        : 'text-gray-500 hover:text-gray-600'
                                }`}
                                onClick={() => setActiveTab('collections')}
                            >
                                Collections
                            </button>
                        </li>
                    </ul>
                </div>

                {/* Content */}
                <div className="bg-white rounded-lg shadow p-4 sm:p-6">
                    {activeTab === 'users' && <UsersTable />}
                    {activeTab === 'books' && <BooksTable />}
                    {activeTab === 'collections' && <CollectionsTable />}
                </div>
            </div>
        </Layout>
    )
}

export default AdminPage