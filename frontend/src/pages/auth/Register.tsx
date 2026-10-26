import { useState, type ChangeEvent, type FormEvent, useEffect } from "react"
import { useNavigate, Link } from "react-router-dom"
import { toast } from "react-toastify"
import { useAuth } from "../../context/AuthContext"
import AuthService from "../../services/AuthService.ts"
import { AuthField } from "../../components/auth/AuthField"
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
    const [isLoading, setIsLoading] = useState<boolean>(false)
    const [touched, setTouched] = useState<Record<string, boolean>>({})
    const navigate = useNavigate()

    // Use our enhanced auth hook instead of context
    const { isAuthenticated } = useAuth()

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
                await AuthService.login(loginCredentials);

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

    const err = (name: keyof Omit<ValidationErrors, "address">) =>
        shouldShowError(name) ? errors[name] : undefined
    const addrErr = (name: keyof NonNullable<ValidationErrors["address"]>) =>
        shouldShowAddressError(name) ? errors.address?.[name] : undefined

    return (
        <Layout>
            <div className="mx-auto w-full max-w-[520px] px-4 py-12 sm:py-16">
                <h1 className="page-title">Create your account</h1>
                <p className="mt-2 text-ink-2">Borrow books from other readers, and lend yours.</p>

                <form onSubmit={handleSubmit} className="mt-8 space-y-5">
                    <div className="grid grid-cols-1 gap-5 sm:grid-cols-2">
                        <AuthField id="firstName" name="firstName" label="First name" autoComplete="given-name"
                                   value={formData.firstName} onChange={handleChange} required error={err("firstName")} />
                        <AuthField id="lastName" name="lastName" label="Last name" autoComplete="family-name"
                                   value={formData.lastName} onChange={handleChange} required error={err("lastName")} />
                    </div>
                    <AuthField id="email" name="email" type="email" label="Email" autoComplete="email"
                               value={formData.email} onChange={handleChange} required error={err("email")} />
                    <AuthField id="password" name="password" type="password" label="Password" autoComplete="new-password"
                               value={formData.password} onChange={handleChange} required error={err("password")}
                               hint="At least 8 characters." />

                    <fieldset className="space-y-5 border-t border-line pt-6">
                        <legend className="mb-1 pr-3 font-serif text-lg font-semibold text-ink">Delivery address</legend>
                        <div className="grid grid-cols-[110px_1fr] gap-5">
                            <AuthField id="streetNumber" name="streetNumber" label="Number" autoComplete="off"
                                       value={formData.address.streetNumber} onChange={handleChange} required
                                       error={addrErr("streetNumber")} />
                            <AuthField id="streetName" name="streetName" label="Street" autoComplete="address-line1"
                                       value={formData.address.streetName} onChange={handleChange} required
                                       error={addrErr("streetName")} />
                        </div>
                        <div className="grid grid-cols-1 gap-5 sm:grid-cols-2">
                            <AuthField id="city" name="city" label="City" autoComplete="address-level2"
                                       value={formData.address.city} onChange={handleChange} required error={addrErr("city")} />
                            <AuthField id="postalCode" name="postalCode" label="Postal code" autoComplete="postal-code"
                                       value={formData.address.postalCode} onChange={handleChange} required
                                       error={addrErr("postalCode")} />
                        </div>
                    </fieldset>

                    <button type="submit" disabled={isLoading} className="btn-accent w-full">
                        {isLoading ? "Creating your account..." : "Create account"}
                    </button>
                </form>

                <p className="mt-6 text-center text-sm text-ink-2">
                    Already have an account?{" "}
                    <Link to="/login" className="font-medium text-accent underline underline-offset-2 hover:text-accent-hover">
                        Log in
                    </Link>
                </p>
            </div>
        </Layout>
    )
}
