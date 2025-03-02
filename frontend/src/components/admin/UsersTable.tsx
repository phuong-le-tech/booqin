import React, { useState, useEffect } from "react"
import { Edit, Trash2, Search, X } from "lucide-react"
import UserService from "../../services/UserService"
import { User } from "../../model/User"
import { UserEditModal } from "../modal/UserEditModal"
import { DeleteConfirmModal } from "../modal/DeleteConfirmModal"
import { PageInfo } from "../../model/Pagination"
import { ApiResponse } from "../../model/ApiResponse"
import { Pagination } from "../Pagination.tsx"

interface PaginatedData<T> {
    content: T[];
    page: PageInfo;
}

export const UsersTable: React.FC = () => {
    const [users, setUsers] = useState<User[]>([])
    const [loading, setLoading] = useState<boolean>(true)
    const [error, setError] = useState<string | null>(null)
    const [searchTerm, setSearchTerm] = useState<string>("")
    const [currentPage, setCurrentPage] = useState<number>(0)
    const [pageInfo, setPageInfo] = useState<PageInfo>({
        size: 10,
        number: 0,
        totalElements: 0,
        totalPages: 0
    })
    const [editingUser, setEditingUser] = useState<User | null>(null)
    const [userToDelete, setUserToDelete] = useState<User | null>(null)

    const fetchUsers = async (page: number = 0) => {
        setLoading(true)
        try {
            const response = await fetch(`/api/v1/users?page=${page}&size=${pageInfo.size}&sortField=id&sortDirection=asc`)
            const data = await response.json() as ApiResponse<PaginatedData<User>>

            if (data.success && data.data) {
                setUsers(data.data.content)
                setPageInfo(data.data.page)
            } else {
                setError(data.message || "Failed to fetch users")
            }
        } catch (err) {
            setError("Error fetching users. Please try again.")
            console.error(err)
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        fetchUsers(currentPage)
    }, [currentPage])

    const handlePageChange = (page: number) => {
        setCurrentPage(page)
    }

    const handleEdit = (user: User) => {
        setEditingUser(user)
    }

    const handleDelete = (user: User) => {
        setUserToDelete(user)
    }

    const confirmDelete = async () => {
        if (!userToDelete) return

        try {
            await UserService.deleteUser(String(userToDelete.id))
            // Refresh the list
            fetchUsers(currentPage)
            setUserToDelete(null)
        } catch (err) {
            console.error("Failed to delete user:", err)
            setError("Failed to delete user. Please try again.")
        }
    }

    const handleUpdateUser = async (updatedUser: Partial<User>) => {
        if (!editingUser) return

        try {
            await UserService.updateUser(editingUser.id, updatedUser)
            // Refresh the list
            fetchUsers(currentPage)
            setEditingUser(null)
        } catch (err) {
            console.error("Failed to update user:", err)
            setError("Failed to update user. Please try again.")
        }
    }

    const filteredUsers = users.filter(user =>
        user.firstName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.lastName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.email?.toLowerCase().includes(searchTerm.toLowerCase())
    )

    if (loading && users.length === 0) {
        return <div className="flex justify-center p-8">Loading...</div>
    }

    return (
        <div>
            <div className="mb-4 flex flex-col sm:flex-row items-center justify-between gap-4">
                <h2 className="text-xl font-semibold">Users Management</h2>

                <div className="relative w-full sm:w-64">
                    <input
                        type="text"
                        placeholder="Search users..."
                        className="pl-10 pr-10 py-2 border rounded-md w-full"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                    <Search className="absolute left-3 top-2.5 h-4 w-4 text-gray-400" />
                    {searchTerm && (
                        <button
                            onClick={() => setSearchTerm("")}
                            className="absolute right-3 top-2.5"
                        >
                            <X className="h-4 w-4 text-gray-400" />
                        </button>
                    )}
                </div>
            </div>

            {error && (
                <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4">
                    {error}
                    <button
                        className="absolute top-0 bottom-0 right-0 px-4"
                        onClick={() => setError(null)}
                    >
                        <X className="h-4 w-4" />
                    </button>
                </div>
            )}

            <div className="overflow-x-auto">
                <table className="min-w-full bg-white border rounded-lg">
                    <thead className="bg-gray-50">
                    <tr>
                        <th className="py-2 px-4 border-b text-left">ID</th>
                        <th className="py-2 px-4 border-b text-left">First Name</th>
                        <th className="py-2 px-4 border-b text-left">Last Name</th>
                        <th className="py-2 px-4 border-b text-left">Email</th>
                        <th className="py-2 px-4 border-b text-left">Role</th>
                        <th className="py-2 px-4 border-b text-center">Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    {filteredUsers.length > 0 ? (
                        filteredUsers.map((user) => (
                            <tr key={user.id} className="hover:bg-gray-50">
                                <td className="py-2 px-4 border-b">{user.id}</td>
                                <td className="py-2 px-4 border-b">{user.firstName}</td>
                                <td className="py-2 px-4 border-b">{user.lastName}</td>
                                <td className="py-2 px-4 border-b">{user.email}</td>
                                <td className="py-2 px-4 border-b">
                                        <span className="px-2 py-1 bg-blue-100 text-blue-800 rounded-full text-xs">
                                            {user.role || 'USER'}
                                        </span>
                                </td>
                                <td className="py-2 px-4 border-b text-center">
                                    <div className="flex justify-center space-x-2">
                                        <button
                                            onClick={() => handleEdit(user)}
                                            className="p-1 text-blue-600 hover:text-blue-800"
                                            title="Edit user"
                                        >
                                            <Edit className="h-4 w-4" />
                                        </button>
                                        <button
                                            onClick={() => handleDelete(user)}
                                            className="p-1 text-red-600 hover:text-red-800"
                                            title="Delete user"
                                        >
                                            <Trash2 className="h-4 w-4" />
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))
                    ) : (
                        <tr>
                            <td colSpan={6} className="py-4 text-center text-gray-500">
                                {searchTerm ? "No users found matching your search" : "No users found"}
                            </td>
                        </tr>
                    )}
                    </tbody>
                </table>
            </div>

            {/* Pagination */}
            {!searchTerm && pageInfo.totalPages > 1 && (
                <div className="mt-4">
                    <div className="text-sm text-gray-700 mb-4">
                        Showing page {pageInfo.number + 1} of {pageInfo.totalPages} ({pageInfo.totalElements} total users)
                    </div>
                    <Pagination pageInfo={pageInfo} onPageChange={handlePageChange} />
                </div>
            )}

            {/* Edit Modal */}
            {editingUser && (
                <UserEditModal
                    user={editingUser}
                    onClose={() => setEditingUser(null)}
                    onSave={handleUpdateUser}
                />
            )}

            {/* Delete Confirmation Modal */}
            {userToDelete && (
                <DeleteConfirmModal
                    title="Delete User"
                    message={`Are you sure you want to delete the user ${userToDelete.firstName} ${userToDelete.lastName}? This action cannot be undone.`}
                    onConfirm={confirmDelete}
                    onCancel={() => setUserToDelete(null)}
                />
            )}
        </div>
    )
}

export default UsersTable