import {useState, useEffect, useContext, type ChangeEvent, type FormEvent} from "react"
import {useNavigate} from "react-router-dom"
import {toast} from "react-toastify"
import {AuthContext} from "../../context/AuthContext.tsx"
import UserService from "../../services/UserService.ts"
import {Layout} from "../Layout.tsx"
import {User, Mail, MapPin, Edit, Save, X} from "lucide-react"
import type {Address} from "../../model/Address.ts"

interface ProfileForm {
    firstName: string
    lastName: string
    email: string
    address: Address
}

export const Profile = () => {
    const {user, setUser} = useContext(AuthContext)
    const [formData, setFormData] = useState<ProfileForm>({
        firstName: user?.firstName || "",
        lastName: user?.lastName || "",
        email: user?.email || "",
        address: user?.address || {
            streetNumber: "",
            streetName: "",
            city: "",
            postalCode: "",
        },
    })
    const [isEditing, setIsEditing] = useState<boolean>(false)
    const [isLoading, setIsLoading] = useState<boolean>(false)
    const navigate = useNavigate()

    useEffect(() => {
        if (!user) {
            navigate("/login")
            return
        }

        // Set form data from user context
        setFormData({
            firstName: user.firstName,
            lastName: user.lastName,
            email: user.email,
            address: user.address || {
                streetNumber: "",
                streetName: "",
                city: "",
                postalCode: "",
            },
        })
    }, [user, navigate])

    const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
        const {name, value} = e.target

        // Gestion des champs d'adresse
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
            setFormData((prev) => ({...prev, [name]: value}))
        }
    }

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault()
        setIsLoading(true)
        try {
            if (!user?.id) {
                throw new Error("User ID is missing")
            }

            const userData = {
                firstName: formData.firstName,
                lastName: formData.lastName,
                email: formData.email,
                address: formData.address
            }

            const updatedUser = await UserService.updateUser(user.id, userData)
            setUser({...user, ...updatedUser})
            toast.success("Profile updated successfully!")
            setIsEditing(false)
        } catch (error: any) {
            toast.error(error.message || "Failed to update profile. Please try again.")
        } finally {
            setIsLoading(false)
        }
    }

    const toggleEdit = () => {
        if (isEditing) {
            // Reset form when canceling edit
            setFormData({
                firstName: user?.firstName || "",
                lastName: user?.lastName || "",
                email: user?.email || "",
                address: user?.address || {
                    streetNumber: "",
                    streetName: "",
                    city: "",
                    postalCode: "",
                },
            })
        }
        setIsEditing(!isEditing)
    }

    if (!user) {
        return null // Will redirect in useEffect
    }

    return (
        <Layout>
            <div className="flex items-center justify-center min-h-screen bg-white px-4 py-6 sm:py-12">
                <div className="w-full max-w-2xl">
                    <div className="bg-secondary py-6 px-4 sm:py-8 sm:px-6 md:px-10 shadow rounded-lg mb-6">
                        <div className="flex justify-between items-center mb-6">
                            <h2 className="text-2xl sm:text-3xl font-extrabold text-primary">My Profile</h2>
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
                        </div>

                        {isEditing ? (
                            // Edit Mode
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
                                        disabled={isLoading}
                                        className="w-full flex justify-center py-2 px-4 border border-transparent
                                                rounded-md shadow-sm text-sm font-medium text-white
                                                bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400
                                                focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                                    >
                                        {isLoading ? (
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
                                            <p className="text-base font-medium text-secondary">{user.firstName}</p>
                                        </div>
                                        <div className="space-y-2 mt-4">
                                            <p className="text-sm text-secondary">Last Name</p>
                                            <p className="text-base font-medium text-secondary">{user.lastName}</p>
                                        </div>
                                    </div>

                                    <div className="bg-primary p-4 rounded-lg shadow">
                                        <div className="flex items-center mb-2">
                                            <Mail className="h-5 w-5 text-blue-500 mr-2"/>
                                            <h3 className="text-lg font-medium text-secondary">Contact Information</h3>
                                        </div>
                                        <div className="space-y-2 mt-4">
                                            <p className="text-sm text-secondary">Email</p>
                                            <p className="text-base font-medium text-secondary break-words">{user.email}</p>
                                        </div>
                                    </div>
                                </div>

                                {user.address && (
                                    <div className="bg-primary p-4 rounded-lg shadow mt-4">
                                        <div className="flex items-center mb-2">
                                            <MapPin className="h-5 w-5 text-blue-500 mr-2"/>
                                            <h3 className="text-lg font-medium text-secondary">Address</h3>
                                        </div>
                                        <div className="mt-4">
                                            <p className="text-base font-medium text-secondary">
                                                {user.address.streetNumber} {user.address.streetName}
                                            </p>
                                            <p className="text-base font-medium text-secondary">
                                                {user.address.postalCode} {user.address.city}
                                            </p>
                                        </div>
                                    </div>
                                )}
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </Layout>
    )
}