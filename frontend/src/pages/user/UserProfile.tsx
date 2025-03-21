import { useState, useEffect, useContext, type ChangeEvent, type FormEvent } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { toast } from "react-toastify"
import { AuthContext } from "../../context/AuthContext.tsx"
import UserService from "../../services/UserService.ts"
import { Layout } from "../Layout.tsx"
import { User, Mail, MapPin, Edit, Save, X, BookOpen, Library } from "lucide-react"
import type { Address } from "../../model/Address.ts"
import FollowButton from "../../components/follow/FollowButton.tsx"
import { Link } from "react-router-dom"
import userService from "../../services/UserService.ts";

interface ProfileForm {
    firstName: string
    lastName: string
    email: string
    address: Address
}

interface UserProfileData {
    id: number
    firstName: string
    lastName: string
    email: string
    address?: Address
    role: string
}

export const UserProfile = () => {
    const { id } = useParams<{ id: string }>()
    const { user: currentUser, setUser } = useContext(AuthContext)
    const [profileUser, setProfileUser] = useState<UserProfileData | null>(null)
    const [formData, setFormData] = useState<ProfileForm>({
        firstName: "",
        lastName: "",
        email: "",
        address: {
            streetNumber: "",
            streetName: "",
            city: "",
            postalCode: "",
        },
    })
    const [isEditing, setIsEditing] = useState<boolean>(false)
    const [isLoading, setIsLoading] = useState<boolean>(true)
    const [isSubmitting, setIsSubmitting] = useState<boolean>(false)
    const [publicCollections, setPublicCollections] = useState<any[]>([])
    const [loadingCollections, setLoadingCollections] = useState<boolean>(true)
    const navigate = useNavigate()

    const isOwnProfile = currentUser?.id === Number(id)

    // Fetch user profile data
    useEffect(() => {
        const fetchUserProfile = async () => {
            try {
                setIsLoading(true)
                // If viewing own profile, use current user data
                if (currentUser && currentUser.id === Number(id)) {
                    setProfileUser(currentUser)
                    setFormData({
                        firstName: currentUser.firstName,
                        lastName: currentUser.lastName,
                        email: currentUser.email,
                        address: currentUser.address || {
                            streetNumber: "",
                            streetName: "",
                            city: "",
                            postalCode: "",
                        },
                    })
                } else {
                    // Fetch other user's profile
                    const userData = await UserService.getUserById(Number(id))
                    if (userData) {
                        setProfileUser(userData)
                    } else {
                        toast.error("User not found")
                        navigate("/")
                    }
                }
            } catch (error: any) {
                console.error("Failed to fetch user profile:", error)
                toast.error(error.message || "Failed to load user profile")
                navigate("/")
            } finally {
                setIsLoading(false)
            }
        }

        if (id) {
            fetchUserProfile()
        }
    }, [id, currentUser, navigate])

    // Fetch user's public collections
    useEffect(() => {
        const fetchUserCollections = async () => {
            if (!id) return

            try {
                setLoadingCollections(true)
                // You'll need to create this method in your BookCollectionService
                const collections = await userService.getUserPublicCollections(id)
                setPublicCollections(collections || [])
            } catch (error) {
                console.error("Failed to fetch user collections:", error)
            } finally {
                setLoadingCollections(false)
            }
        }

        fetchUserCollections()
    }, [id])

    const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target

        // Handle address fields
        if (name.startsWith("address.")) {
            const addressField = name.split(".")[1]
            setFormData((prev) => ({
                ...prev,
                address: {
                    ...prev.address,
                    [addressField]: value
                },
            }))
        } else {
            setFormData((prev) => ({ ...prev, [name]: value }))
        }
    }

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault()

        // Only allow editing own profile
        if (!isOwnProfile) {
            toast.error("You can only edit your own profile")
            return
        }

        setIsSubmitting(true)
        try {
            if (!currentUser?.id) {
                throw new Error("User ID is missing")
            }

            const userData = {
                firstName: formData.firstName,
                lastName: formData.lastName,
                email: formData.email,
                address: formData.address
            }

            const updatedUser = await UserService.updateUser(currentUser.id, userData)
            setUser({ ...currentUser, ...updatedUser })
            setProfileUser({ ...profileUser!, ...updatedUser })
            toast.success("Profile updated successfully!")
            setIsEditing(false)
        } catch (error: any) {
            toast.error(error.message || "Failed to update profile. Please try again.")
        } finally {
            setIsSubmitting(false)
        }
    }

    const toggleEdit = () => {
        // Only allow editing own profile
        if (!isOwnProfile) {
            toast.error("You can only edit your own profile")
            return
        }

        if (isEditing) {
            // Reset form when canceling edit
            if (profileUser) {
                setFormData({
                    firstName: profileUser.firstName,
                    lastName: profileUser.lastName,
                    email: profileUser.email,
                    address: profileUser.address || {
                        streetNumber: "",
                        streetName: "",
                        city: "",
                        postalCode: "",
                    },
                })
            }
        }
        setIsEditing(!isEditing)
    }

    if (isLoading) {
        return (
            <Layout>
                <div className="flex items-center justify-center min-h-screen">
                    <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
                </div>
            </Layout>
        )
    }

    if (!profileUser) {
        return (
            <Layout>
                <div className="flex items-center justify-center min-h-screen">
                    <div className="text-center">
                        <h2 className="text-2xl font-bold text-gray-700">User not found</h2>
                        <p className="mt-2 text-gray-500">The user you're looking for doesn't exist or has been removed.</p>
                    </div>
                </div>
            </Layout>
        )
    }

    return (
        <Layout>
            <div className="flex items-center justify-center min-h-screen bg-white px-4 py-6 sm:py-12">
                <div className="w-full max-w-5xl">
                    {/* Profile Header */}
                    <div className="bg-secondary py-6 px-4 sm:py-8 sm:px-6 md:px-10 shadow rounded-lg mb-6">
                        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4 mb-6">
                            <div className="flex items-center">
                                {/* User Avatar */}
                                <div className="h-16 w-16 bg-blue-600 text-white rounded-full flex items-center justify-center text-2xl font-bold mr-4">
                                    {profileUser.firstName.charAt(0)}{profileUser.lastName.charAt(0)}
                                </div>

                                {/* User Name and Role */}
                                <div>
                                    <h2 className="text-2xl sm:text-3xl font-extrabold text-primary">
                                        {profileUser.firstName} {profileUser.lastName}
                                    </h2>
                                    <p className="text-sm text-gray-600">
                                        {profileUser.role === 'ADMIN' ? 'Administrator' : 'Member'}
                                    </p>
                                </div>
                            </div>

                            <div className="flex items-center gap-3">
                                {/* Follow button - only show if not viewing own profile */}
                                {!isOwnProfile && currentUser && (
                                    <FollowButton
                                        entityId={profileUser.id}
                                        entityType="USER"
                                        showFollowersCount={true}
                                        size="md"
                                    />
                                )}

                                {/* Edit button - only show if viewing own profile */}
                                {isOwnProfile && (
                                    <button
                                        onClick={toggleEdit}
                                        className={`flex items-center py-2 px-4 rounded-md text-sm font-medium 
                                            ${isEditing ? 'bg-gray-500 text-white' : 'bg-blue-600 text-white'}`}
                                    >
                                        {isEditing ? (
                                            <>
                                                <X className="h-4 w-4 mr-2"/>
                                                Cancel
                                            </>
                                        ) : (
                                            <>
                                                <Edit className="h-4 w-4 mr-2"/>
                                                Edit Profile
                                            </>
                                        )}
                                    </button>
                                )}
                            </div>
                        </div>

                        {isEditing ? (
                            // Edit Mode - only shown for own profile
                            <form onSubmit={handleSubmit} className="space-y-4 sm:space-y-6">
                                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 sm:gap-6">
                                    <div>
                                        <label htmlFor="firstName"
                                               className="block text-sm font-medium text-primary mb-1">
                                            First Name
                                        </label>
                                        <input
                                            type="text"
                                            id="firstName"
                                            name="firstName"
                                            value={formData.firstName}
                                            onChange={handleChange}
                                            required
                                            className="block w-full border border-blue-200 rounded-md shadow-sm py-2 px-3
                                                   text-primary placeholder-gray-400
                                                   focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                                        />
                                    </div>
                                    <div>
                                        <label htmlFor="lastName"
                                               className="block text-sm font-medium text-primary mb-1">
                                            Last Name
                                        </label>
                                        <input
                                            type="text"
                                            id="lastName"
                                            name="lastName"
                                            value={formData.lastName}
                                            onChange={handleChange}
                                            required
                                            className="block w-full border border-blue-200 rounded-md shadow-sm py-2 px-3
                                                   text-primary placeholder-gray-400
                                                   focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                                        />
                                    </div>
                                </div>
                                <div>
                                    <label htmlFor="email" className="block text-sm font-medium text-primary mb-1">
                                        Email
                                    </label>
                                    <input
                                        type="email"
                                        id="email"
                                        name="email"
                                        value={formData.email}
                                        onChange={handleChange}
                                        required
                                        className="block w-full border border-blue-200 rounded-md shadow-sm py-2 px-3
                                               text-primary placeholder-gray-400
                                               focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                                    />
                                </div>

                                <div className="border-t border-gray-300 pt-4 mt-4">
                                    <h3 className="text-lg font-medium text-primary mb-3">Address Information</h3>
                                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 sm:gap-6">
                                        <div>
                                            <label htmlFor="address.streetNumber"
                                                   className="block text-sm font-medium text-primary mb-1">
                                                Street Number
                                            </label>
                                            <input
                                                type="text"
                                                id="address.streetNumber"
                                                name="address.streetNumber"
                                                value={formData.address.streetNumber}
                                                onChange={handleChange}
                                                required
                                                className="block w-full border border-blue-200 rounded-md shadow-sm py-2 px-3
                                                       text-primary placeholder-gray-400
                                                       focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                                            />
                                        </div>
                                        <div>
                                            <label htmlFor="address.streetName"
                                                   className="block text-sm font-medium text-primary mb-1">
                                                Street Name
                                            </label>
                                            <input
                                                type="text"
                                                id="address.streetName"
                                                name="address.streetName"
                                                value={formData.address.streetName}
                                                onChange={handleChange}
                                                required
                                                className="block w-full border border-blue-200 rounded-md shadow-sm py-2 px-3
                                                       text-primary placeholder-gray-400
                                                       focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                                            />
                                        </div>
                                    </div>
                                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 sm:gap-6 mt-4">
                                        <div>
                                            <label htmlFor="address.city"
                                                   className="block text-sm font-medium text-primary mb-1">
                                                City
                                            </label>
                                            <input
                                                type="text"
                                                id="address.city"
                                                name="address.city"
                                                value={formData.address.city}
                                                onChange={handleChange}
                                                required
                                                className="block w-full border border-blue-200 rounded-md shadow-sm py-2 px-3
                                                       text-primary placeholder-gray-400
                                                       focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                                            />
                                        </div>
                                        <div>
                                            <label htmlFor="address.postalCode"
                                                   className="block text-sm font-medium text-primary mb-1">
                                                Postal Code
                                            </label>
                                            <input
                                                type="text"
                                                id="address.postalCode"
                                                name="address.postalCode"
                                                value={formData.address.postalCode}
                                                onChange={handleChange}
                                                required
                                                className="block w-full border border-blue-200 rounded-md shadow-sm py-2 px-3
                                                       text-primary placeholder-gray-400
                                                       focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                                            />
                                        </div>
                                    </div>
                                </div>

                                <div className="pt-4">
                                    <button
                                        type="submit"
                                        disabled={isSubmitting}
                                        className="w-full flex justify-center py-2 px-4 border border-transparent
                                                rounded-md shadow-sm text-sm font-medium text-white
                                                bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400
                                                focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                                    >
                                        {isSubmitting ? (
                                            "Updating..."
                                        ) : (
                                            <>
                                                <Save className="h-4 w-4 mr-2"/>
                                                Save Changes
                                            </>
                                        )}
                                    </button>
                                </div>
                            </form>
                        ) : (
                            // View Mode
                            <div className="space-y-4">
                                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                    <div className="bg-primary p-4 rounded-lg shadow">
                                        <div className="flex items-center mb-2">
                                            <User className="h-5 w-5 text-blue-500 mr-2"/>
                                            <h3 className="text-lg font-medium text-secondary">Personal Information</h3>
                                        </div>
                                        <div className="space-y-2 mt-4">
                                            <p className="text-sm text-secondary">First Name</p>
                                            <p className="text-base font-medium text-secondary">{profileUser.firstName}</p>
                                        </div>
                                        <div className="space-y-2 mt-4">
                                            <p className="text-sm text-secondary">Last Name</p>
                                            <p className="text-base font-medium text-secondary">{profileUser.lastName}</p>
                                        </div>
                                    </div>

                                    <div className="bg-primary p-4 rounded-lg shadow">
                                        <div className="flex items-center mb-2">
                                            <Mail className="h-5 w-5 text-blue-500 mr-2"/>
                                            <h3 className="text-lg font-medium text-secondary">Contact Information</h3>
                                        </div>
                                        <div className="space-y-2 mt-4">
                                            <p className="text-sm text-secondary">Email</p>
                                            <p className="text-base font-medium text-secondary break-words">{profileUser.email}</p>
                                        </div>
                                    </div>
                                </div>

                                {profileUser.address && (
                                    <div className="bg-primary p-4 rounded-lg shadow mt-4">
                                        <div className="flex items-center mb-2">
                                            <MapPin className="h-5 w-5 text-blue-500 mr-2"/>
                                            <h3 className="text-lg font-medium text-secondary">Address</h3>
                                        </div>
                                        <div className="mt-4">
                                            <p className="text-base font-medium text-secondary">
                                                {profileUser.address.streetNumber} {profileUser.address.streetName}
                                            </p>
                                            <p className="text-base font-medium text-secondary">
                                                {profileUser.address.postalCode} {profileUser.address.city}
                                            </p>
                                        </div>
                                    </div>
                                )}
                            </div>
                        )}
                    </div>

                    {/* User's Public Collections Section */}
                    <div className="bg-secondary py-6 px-4 sm:py-8 sm:px-6 md:px-10 shadow rounded-lg mb-6">
                        <div className="flex items-center justify-between mb-6">
                            <h2 className="text-xl font-bold text-primary flex items-center">
                                <Library className="h-5 w-5 mr-2" />
                                Public Collections
                            </h2>

                            {isOwnProfile && (
                                <Link
                                    to="/collections/new"
                                    className="bg-blue-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-blue-700 transition-colors"
                                >
                                    Create Collection
                                </Link>
                            )}
                        </div>

                        {loadingCollections ? (
                            <div className="text-center py-10">
                                <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-blue-500 mx-auto"></div>
                                <p className="mt-3 text-gray-600">Loading collections...</p>
                            </div>
                        ) : publicCollections.length > 0 ? (
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                {publicCollections.map(collection => (
                                    <Link
                                        key={collection.id}
                                        to={`/collections/${collection.id}`}
                                        className="bg-white p-4 rounded-lg shadow hover:shadow-md transition-shadow"
                                    >
                                        <h3 className="font-bold text-lg mb-1 flex items-center">
                                            <BookOpen className="h-4 w-4 mr-2 text-blue-600" />
                                            {collection.title}
                                        </h3>
                                        <p className="text-sm text-gray-600 line-clamp-2 mb-2">
                                            {collection.description}
                                        </p>
                                        <div className="text-xs text-gray-500">
                                            {collection.books?.length || 0} book{collection.books?.length !== 1 ? 's' : ''}
                                        </div>
                                    </Link>
                                ))}
                            </div>
                        ) : (
                            <div className="text-center py-8 bg-gray-50 rounded-lg">
                                <BookOpen className="h-10 w-10 text-gray-400 mx-auto mb-2" />
                                <p className="text-gray-600">
                                    {isOwnProfile
                                        ? "You haven't created any public collections yet."
                                        : "This user hasn't created any public collections yet."}
                                </p>
                                {isOwnProfile && (
                                    <Link
                                        to="/collections/new"
                                        className="mt-4 inline-block text-blue-600 hover:text-blue-800 font-medium"
                                    >
                                        Create your first collection
                                    </Link>
                                )}
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </Layout>
    )
}

export default UserProfile