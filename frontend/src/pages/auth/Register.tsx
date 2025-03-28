import { useState, type ChangeEvent, type FormEvent, useEffect } from "react"
import { useNavigate, Link, useLocation } from "react-router-dom"
import { toast } from "react-toastify"
import { useAuth } from "../../context/AuthContext"
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

interface ValidationErrors {
    firstName?: string
    lastName?: string
    email?: string
    password?: string
    address?: {
        streetNumber?: string
        streetName?: string
        city?: string
        postalCode?: string
    }
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
    const [errors, setErrors] = useState<ValidationErrors>({})
    const [showPassword, setShowPassword] = useState<boolean>(false)
    const [isLoading, setIsLoading] = useState<boolean>(false)
    const [touched, setTouched] = useState<Record<string, boolean>>({})
    const navigate = useNavigate()
    const location = useLocation()

    // Use our enhanced auth hook instead of context
    const { login, isAuthenticated } = useAuth()

    // If already authenticated, redirect to home
    useEffect(() => {
        if (isAuthenticated) {
            navigate("/")
        }
    }, [isAuthenticated, navigate])

    // Validate form data whenever it changes
    useEffect(() => {
        validateForm()
    }, [formData])

    // Validation function
    const validateForm = () => {
        const newErrors: ValidationErrors = {}

        // First name validation
        if (!formData.firstName) {
            newErrors.firstName = "First name is required"
        } else if (formData.firstName.length < 2 || formData.firstName.length > 50) {
            newErrors.firstName = "First name must be between 2 and 50 characters"
        } else if (!/^[A-Za-zÀ-ÿ'-]+$/.test(formData.firstName)) {
            newErrors.firstName = "First name can only contain letters, hyphens, and apostrophes"
        }

        // Last name validation
        if (!formData.lastName) {
            newErrors.lastName = "Last name is required"
        } else if (formData.lastName.length < 2 || formData.lastName.length > 50) {
            newErrors.lastName = "Last name must be between 2 and 50 characters"
        } else if (!/^[A-Za-zÀ-ÿ'-]+$/.test(formData.lastName)) {
            newErrors.lastName = "Last name can only contain letters, hyphens, and apostrophes"
        }

        // Email validation
        if (!formData.email) {
            newErrors.email = "Email is required"
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
            newErrors.email = "Invalid email format"
        } else if (formData.email.length > 100) {
            newErrors.email = "Email must be less than 100 characters"
        }

        // Password validation
        if (!formData.password) {
            newErrors.password = "Password is required"
        } else if (formData.password.length < 8 || formData.password.length > 100) {
            newErrors.password = "Password must be between 8 and 100 characters"
        }

        // Address validation
        const addressErrors: ValidationErrors["address"] = {}

        if (!formData.address.streetNumber) {
            addressErrors.streetNumber = "Street number is required"
        } else if (formData.address.streetNumber.length > 10) {
            addressErrors.streetNumber = "Street number cannot exceed 10 characters"
        }

        if (!formData.address.streetName) {
            addressErrors.streetName = "Street name is required"
        } else if (formData.address.streetName.length > 100) {
            addressErrors.streetName = "Street name cannot exceed 100 characters"
        }

        if (!formData.address.city) {
            addressErrors.city = "City is required"
        } else if (formData.address.city.length > 50) {
            addressErrors.city = "City cannot exceed 50 characters"
        }

        if (!formData.address.postalCode) {
            addressErrors.postalCode = "Postal code is required"
        } else if (!/^[0-9]{5}(?:-[0-9]{4})?$/.test(formData.address.postalCode)) {
            addressErrors.postalCode = "Invalid postal code format (e.g. 12345 or 12345-6789)"
        }

        if (Object.keys(addressErrors).length > 0) {
            newErrors.address = addressErrors
        }

        setErrors(newErrors)
        return Object.keys(newErrors).length === 0
    }

    const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target

        // Mark field as touched
        setTouched(prev => ({
            ...prev,
            [name]: true
        }))

        if (["streetNumber", "streetName", "city", "postalCode"].includes(name)) {
            setFormData((prev) => ({
                ...prev,
                address: { ...prev.address, [name]: value },
            }))

            // Also mark the address field as touched
            setTouched(prev => ({
                ...prev,
                [`address.${name}`]: true
            }))
        } else {
            setFormData((prev) => ({ ...prev, [name]: value }))
        }
    }

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault()

        // Mark all fields as touched
        const allFields = {
            firstName: true,
            lastName: true,
            email: true,
            password: true,
            'address.streetNumber': true,
            'address.streetName': true,
            'address.city': true,
            'address.postalCode': true
        }
        setTouched(allFields)

        // Validate the form
        const isValid = validateForm()

        if (!isValid) {
            toast.error("Please fix the errors in the form")
            return
        }

        setIsLoading(true)
        try {
            // First register the user

            await AuthService.register(formData)
            toast.success("Registration successful!")

            // Short delay before login to ensure registration is processed
            await new Promise(resolve => setTimeout(resolve, 500));

            try {

                // Create login credentials in the format expected by backend
                const loginCredentials = {
                    login: formData.email,
                    password: formData.password
                };

                // Use AuthService directly to avoid issues with the login function from context
                const userData = await AuthService.login(loginCredentials);

                // After successful login, navigate to home or saved redirect URL
                const redirectUrl = localStorage.getItem('redirectAfterLogin') || "/"
                if (localStorage.getItem('redirectAfterLogin')) {
                    localStorage.removeItem('redirectAfterLogin')
                }

                // Force navigation
                navigate(redirectUrl, { replace: true })
            } catch (loginError: any) {
                console.error("Login after registration failed:", loginError)
                toast.warning("Registration successful, but automatic login failed. Please log in manually.")
                navigate("/login")
            }
        } catch (error: any) {
            console.error("Registration error:", error)
            toast.error(`Registration failed: ${error.message || "Unknown error"}`)
        } finally {
            setIsLoading(false)
        }
    }

    // Helper to determine if error should be shown
    const shouldShowError = (fieldName: string): boolean => {
        return touched[fieldName] === true
    }

    // Helper for address fields
    const shouldShowAddressError = (fieldName: string): boolean => {
        return touched[`address.${fieldName}`] === true
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
                                        className={`block w-full border ${
                                            shouldShowError('firstName') && errors.firstName
                                                ? 'border-red-500'
                                                : 'border-blue-200'
                                        } rounded-md shadow-sm py-2 px-3
                                                   text-primary placeholder-gray-400
                                                   focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm`}
                                    />
                                    {shouldShowError('firstName') && errors.firstName && (
                                        <p className="mt-1 text-sm text-red-600">{errors.firstName}</p>
                                    )}
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
                                        className={`block w-full border ${
                                            shouldShowError('lastName') && errors.lastName
                                                ? 'border-red-500'
                                                : 'border-blue-200'
                                        } rounded-md shadow-sm py-2 px-3
                                                   text-primary placeholder-gray-400
                                                   focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm`}
                                    />
                                    {shouldShowError('lastName') && errors.lastName && (
                                        <p className="mt-1 text-sm text-red-600">{errors.lastName}</p>
                                    )}
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
                                    className={`block w-full border ${
                                        shouldShowError('email') && errors.email
                                            ? 'border-red-500'
                                            : 'border-blue-200'
                                    } rounded-md shadow-sm py-2 px-3
                                               text-primary placeholder-gray-400
                                               focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm`}
                                />
                                {shouldShowError('email') && errors.email && (
                                    <p className="mt-1 text-sm text-red-600">{errors.email}</p>
                                )}
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
                                        className={`block w-full border ${
                                            shouldShowError('password') && errors.password
                                                ? 'border-red-500'
                                                : 'border-blue-200'
                                        } rounded-md shadow-sm py-2 px-3 pr-10
                                                   text-primary placeholder-gray-400
                                                   focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm`}
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
                                {shouldShowError('password') && errors.password && (
                                    <p className="mt-1 text-sm text-red-600">{errors.password}</p>
                                )}
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
                                            className={`block w-full border ${
                                                shouldShowAddressError('streetNumber') && errors.address?.streetNumber
                                                    ? 'border-red-500'
                                                    : 'border-blue-200'
                                            } rounded-md shadow-sm py-2 px-3
                                                       text-primary placeholder-gray-400
                                                       focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm`}
                                        />
                                        {shouldShowAddressError('streetNumber') && errors.address?.streetNumber && (
                                            <p className="mt-1 text-sm text-red-600">{errors.address.streetNumber}</p>
                                        )}
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
                                            className={`block w-full border ${
                                                shouldShowAddressError('streetName') && errors.address?.streetName
                                                    ? 'border-red-500'
                                                    : 'border-blue-200'
                                            } rounded-md shadow-sm py-2 px-3
                                                       text-primary placeholder-gray-400
                                                       focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm`}
                                        />
                                        {shouldShowAddressError('streetName') && errors.address?.streetName && (
                                            <p className="mt-1 text-sm text-red-600">{errors.address.streetName}</p>
                                        )}
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
                                            className={`block w-full border ${
                                                shouldShowAddressError('city') && errors.address?.city
                                                    ? 'border-red-500'
                                                    : 'border-blue-200'
                                            } rounded-md shadow-sm py-2 px-3
                                                       text-primary placeholder-gray-400
                                                       focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm`}
                                        />
                                        {shouldShowAddressError('city') && errors.address?.city && (
                                            <p className="mt-1 text-sm text-red-600">{errors.address.city}</p>
                                        )}
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
                                            className={`block w-full border ${
                                                shouldShowAddressError('postalCode') && errors.address?.postalCode
                                                    ? 'border-red-500'
                                                    : 'border-blue-200'
                                            } rounded-md shadow-sm py-2 px-3
                                                       text-primary placeholder-gray-400
                                                       focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm`}
                                            placeholder="12345 or 12345-6789"
                                        />
                                        {shouldShowAddressError('postalCode') && errors.address?.postalCode && (
                                            <p className="mt-1 text-sm text-red-600">{errors.address.postalCode}</p>
                                        )}
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