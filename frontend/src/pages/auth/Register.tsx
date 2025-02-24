import { useState, type ChangeEvent, type FormEvent, useContext } from "react"
import { useNavigate } from "react-router-dom"
import { toast } from "react-toastify"
import { AuthContext } from "../../context/AuthContext.tsx"
import AuthService from "../../services/AuthService.ts"
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
        try {
            await AuthService.register(formData)
            toast.success("Registration successful!")
            const user = await AuthService.login({ login: formData.email, password: formData.password })
            setUser(user)
            navigate("/")
        } catch (error: any) {
            toast.error(error.message || "Registration failed. Please try again.")
        }
    }

    return (
        <Layout>
            <div className="flex items-center justify-center min-h-screen bg-white px-4 py-12 sm:px-6 lg:px-8">
                <div className="w-full max-w-md">
                    <div className="bg-secondary py-8 px-4 shadow sm:rounded-lg sm:px-10">
                        <h2 className="text-3xl font-extrabold text-primary text-center mb-6">Register</h2>
                        <form onSubmit={handleSubmit} className="space-y-6">
                            <div>
                                <label htmlFor="firstName" className="block text-sm font-medium text-primary">
                                    First Name
                                </label>
                                <input
                                    type="text"
                                    id="firstName"
                                    name="firstName"
                                    value={formData.firstName}
                                    onChange={handleChange}
                                    required
                                    className="mt-1 block w-full border border-blue-200 rounded-md shadow-sm py-2 px-3
                                               text-primary placeholder-gray-400
                                               focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                                />
                            </div>
                            <div>
                                <label htmlFor="lastName" className="block text-sm font-medium text-primary">
                                    Last Name
                                </label>
                                <input
                                    type="text"
                                    id="lastName"
                                    name="lastName"
                                    value={formData.lastName}
                                    onChange={handleChange}
                                    required
                                    className="mt-1 block w-full border border-blue-200 rounded-md shadow-sm py-2 px-3
                                               text-primary placeholder-gray-400
                                               focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                                />
                            </div>
                            <div>
                                <label htmlFor="email" className="block text-sm font-medium text-primary">
                                    Email
                                </label>
                                <input
                                    type="email"
                                    id="email"
                                    name="email"
                                    value={formData.email}
                                    onChange={handleChange}
                                    required
                                    className="mt-1 block w-full border border-blue-200 rounded-md shadow-sm py-2 px-3
                                               text-primary placeholder-gray-400
                                               focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                                />
                            </div>
                            <div>
                                <label htmlFor="password" className="block text-sm font-medium text-primary">
                                    Password
                                </label>
                                <input
                                    type="password"
                                    id="password"
                                    name="password"
                                    value={formData.password}
                                    onChange={handleChange}
                                    required
                                    className="mt-1 block w-full border border-blue-200 rounded-md shadow-sm py-2 px-3
                                               text-primary placeholder-gray-400
                                               focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                                />
                            </div>
                            <div>
                                <label htmlFor="streetNumber" className="block text-sm font-medium text-primary">
                                    Street Number
                                </label>
                                <input
                                    type="text"
                                    id="streetNumber"
                                    name="streetNumber"
                                    value={formData.address.streetNumber}
                                    onChange={handleChange}
                                    required
                                    className="mt-1 block w-full border border-blue-200 rounded-md shadow-sm py-2 px-3
                                               text-primary placeholder-gray-400
                                               focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                                />
                            </div>
                            <div>
                                <label htmlFor="streetName" className="block text-sm font-medium text-primary">
                                    Street Name
                                </label>
                                <input
                                    type="text"
                                    id="streetName"
                                    name="streetName"
                                    value={formData.address.streetName}
                                    onChange={handleChange}
                                    required
                                    className="mt-1 block w-full border border-blue-200 rounded-md shadow-sm py-2 px-3
                                               text-primary placeholder-gray-400
                                               focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                                />
                            </div>
                            <div>
                                <label htmlFor="city" className="block text-sm font-medium text-primary">
                                    City
                                </label>
                                <input
                                    type="text"
                                    id="city"
                                    name="city"
                                    value={formData.address.city}
                                    onChange={handleChange}
                                    required
                                    className="mt-1 block w-full border border-blue-200 rounded-md shadow-sm py-2 px-3
                                               text-primary placeholder-gray-400
                                               focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                                />
                            </div>
                            <div>
                                <label htmlFor="postalCode" className="block text-sm font-medium text-primary">
                                    Postal Code
                                </label>
                                <input
                                    type="text"
                                    id="postalCode"
                                    name="postalCode"
                                    value={formData.address.postalCode}
                                    onChange={handleChange}
                                    required
                                    className="mt-1 block w-full border border-blue-200 rounded-md shadow-sm py-2 px-3
                                               text-primary placeholder-gray-400
                                               focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                                />
                            </div>
                            <div>
                                <button
                                    type="submit"
                                    className="w-full flex justify-center py-2 px-4 border border-transparent
                                               rounded-md shadow-sm text-sm font-medium text-white
                                               bg-blue-600 hover:bg-blue-700
                                               focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                                >
                                    Register
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </Layout>
    )
}

