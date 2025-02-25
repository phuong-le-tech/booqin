import { useState, type ChangeEvent, type FormEvent, useContext } from "react"
import { useNavigate, Link } from "react-router-dom"
import { toast } from "react-toastify"
import { AuthContext } from "../../context/AuthContext.tsx"
import AuthService from "../../services/AuthService.ts"
import { Eye, EyeOff } from "lucide-react"
import type { Address } from "../../model/Address.ts"
import { Layout } from "../Layout.tsx"

interface RegisterForm {
    firstName: string
    lastName: string
    email: string
    password: string
    address: Address
}

export const Register = () => {
    const [formData, setFormData] = useState<RegisterForm>({
        firstName: "",
        lastName: "",
        email: "",
        password: "",
        address: {
            streetNumber: "",
            streetName: "",
            city: "",
            postalCode: "",
        },
    })
    const [showPassword, setShowPassword] = useState<boolean>(false)
    const [isLoading, setIsLoading] = useState<boolean>(false)
    const navigate = useNavigate()
    const { setUser } = useContext(AuthContext)

    const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target
        if (["streetNumber", "streetName", "city", "postalCode"].includes(name)) {
            setFormData((prev) => ({
                ...prev,
                address: { ...prev.address, [name]: value },
            }))
        } else {
            setFormData((prev) => ({ ...prev, [name]: value }))
        }
    }

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault()
        setIsLoading(true)
        try {
            await AuthService.register(formData)
            toast.success("Registration successful!")
            const user = await AuthService.login({ login: formData.email, password: formData.password })
            setUser(user)
            navigate("/")
        } catch (error: any) {
            toast.error(error.message || "Registration failed. Please try again.")
        } finally {
            setIsLoading(false)
        }
    }

    return (
        <Layout>
            <div className="flex items-center justify-center min-h-screen bg-white px-4 py-6 sm:py-12">
                <div className="w-full max-w-lg">
                    <div className="bg-secondary py-6 px-4 sm:py-8 sm:px-6 md:px-10 shadow rounded-lg mb-6">
                        <h2 className="text-2xl sm:text-3xl font-extrabold text-primary text-center mb-4 sm:mb-6">Register</h2>
                        <form onSubmit={handleSubmit} className="space-y-4 sm:space-y-6">
                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 sm:gap-6">
                                <div>
                                    <label htmlFor="firstName" className="block text-sm font-medium text-primary mb-1">
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
                                    <label htmlFor="lastName" className="block text-sm font-medium text-primary mb-1">
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
                            <div>
                                <label htmlFor="password" className="block text-sm font-medium text-primary mb-1">
                                    Password
                                </label>
                                <div className="relative">
                                    <input
                                        type={showPassword ? "text" : "password"}
                                        id="password"
                                        name="password"
                                        value={formData.password}
                                        onChange={handleChange}
                                        required
                                        className="block w-full border border-blue-200 rounded-md shadow-sm py-2 px-3 pr-10
                                                   text-primary placeholder-gray-400
                                                   focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                                    />
                                    <button
                                        type="button"
                                        className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-500 focus:outline-none"
                                        onClick={() => setShowPassword(!showPassword)}
                                    >
                                        {showPassword ? (
                                            <EyeOff className="h-5 w-5" />
                                        ) : (
                                            <Eye className="h-5 w-5" />
                                        )}
                                    </button>
                                </div>
                            </div>

                            <div className="border-t border-gray-300 pt-4 mt-4">
                                <h3 className="text-lg font-medium text-primary mb-3">Address Information</h3>
                                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 sm:gap-6">
                                    <div>
                                        <label htmlFor="streetNumber" className="block text-sm font-medium text-primary mb-1">
                                            Street Number
                                        </label>
                                        <input
                                            type="text"
                                            id="streetNumber"
                                            name="streetNumber"
                                            value={formData.address.streetNumber}
                                            onChange={handleChange}
                                            required
                                            className="block w-full border border-blue-200 rounded-md shadow-sm py-2 px-3
                                                       text-primary placeholder-gray-400
                                                       focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                                        />
                                    </div>
                                    <div>
                                        <label htmlFor="streetName" className="block text-sm font-medium text-primary mb-1">
                                            Street Name
                                        </label>
                                        <input
                                            type="text"
                                            id="streetName"
                                            name="streetName"
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
                                        <label htmlFor="city" className="block text-sm font-medium text-primary mb-1">
                                            City
                                        </label>
                                        <input
                                            type="text"
                                            id="city"
                                            name="city"
                                            value={formData.address.city}
                                            onChange={handleChange}
                                            required
                                            className="block w-full border border-blue-200 rounded-md shadow-sm py-2 px-3
                                                       text-primary placeholder-gray-400
                                                       focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                                        />
                                    </div>
                                    <div>
                                        <label htmlFor="postalCode" className="block text-sm font-medium text-primary mb-1">
                                            Postal Code
                                        </label>
                                        <input
                                            type="text"
                                            id="postalCode"
                                            name="postalCode"
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
                                    {isLoading ? "Registering..." : "Register"}
                                </button>
                            </div>
                            <div className="mt-4 text-center text-sm">
                                <span className="text-primary">Already have an account? </span>
                                <Link to="/login" className="text-blue-600 hover:text-blue-800 font-medium">
                                    Login
                                </Link>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </Layout>
    )
}