import { useState, type ChangeEvent, type FormEvent, useEffect } from "react"
import { useNavigate, Link, useLocation } from "react-router-dom"
import { toast } from "react-toastify"
import { useAuth } from "../../context/AuthContext"
import { Layout } from "../Layout.tsx"
import { Eye, EyeOff } from "lucide-react"
import type { LoginForm } from "../../model/AuthModel.ts"

export const Login = () => {
    const [formData, setFormData] = useState<LoginForm>({ login: "", password: "" })
    const [showPassword, setShowPassword] = useState<boolean>(false)
    const [isLoading, setIsLoading] = useState<boolean>(false)
    const navigate = useNavigate()
    const location = useLocation()

    // Use our enhanced auth hook instead of context
    const { login, isAuthenticated } = useAuth()

    // Check for a redirectUrl in localStorage or use home page
    const getRedirectUrl = () => {
        const storedRedirect = localStorage.getItem('redirectAfterLogin')
        if (storedRedirect) {
            localStorage.removeItem('redirectAfterLogin')
            return storedRedirect
        }
        return "/"
    }

    // Check for session expired or other query parameters
    useEffect(() => {
        const queryParams = new URLSearchParams(location.search)
        const sessionStatus = queryParams.get('session')

        if (sessionStatus === 'expired') {
            toast.info("Your session expired. Please log in again.")
        }
    }, [location])

    // If already authenticated, redirect
    useEffect(() => {
        if (isAuthenticated) {
            navigate(getRedirectUrl())
        }
    }, [isAuthenticated, navigate])

    const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
        setFormData({ ...formData, [e.target.name]: e.target.value })
    }

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault()
        setIsLoading(true)

        try {
            console.log("Submitting login form...")

            // Use the login function from our auth hook instead of service
            await login(formData.login, formData.password)

            console.log("Login successful")
            toast.success("Login successful!")

            // Redirect is handled by the useEffect above
        } catch (error: any) {
            console.error("Login error in component:", error)
            toast.error(error.message || "Login failed. Please check your credentials.")
        } finally {
            setIsLoading(false)
        }
    }

    return (
        <Layout>
            <div className="flex items-center justify-center min-h-screen bg-white px-4 py-6 sm:py-12">
                <div className="w-full max-w-md">
                    <div className="bg-secondary py-6 px-4 sm:py-8 sm:px-6 md:px-10 shadow rounded-lg">
                        <h2 className="text-2xl sm:text-3xl font-extrabold text-primary text-center mb-4 sm:mb-6">Login</h2>
                        <form onSubmit={handleSubmit} className="space-y-4 sm:space-y-6">
                            <div>
                                <label htmlFor="login" className="block text-sm font-medium text-primary mb-1">
                                    Email
                                </label>
                                <input
                                    type="email"
                                    name="login"
                                    id="login"
                                    autoComplete="email"
                                    required
                                    className="block w-full border border-blue-200 rounded-md shadow-sm py-2 px-3
                                           text-primary placeholder-primary
                                           focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                                    onChange={handleChange}
                                />
                            </div>
                            <div>
                                <label htmlFor="password" className="block text-sm font-medium text-primary mb-1">
                                    Password
                                </label>
                                <div className="relative">
                                    <input
                                        type={showPassword ? "text" : "password"}
                                        name="password"
                                        id="password"
                                        autoComplete="current-password"
                                        required
                                        className="block w-full border border-blue-200 rounded-md shadow-sm py-2 px-3 pr-10
                                               text-primary placeholder-white
                                               focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                                        onChange={handleChange}
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
                            <div>
                                <button
                                    type="submit"
                                    disabled={isLoading}
                                    className="w-full flex justify-center py-2 px-4 border border-transparent
                                           rounded-md shadow-sm text-sm font-medium text-white
                                           bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400
                                           focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                                >
                                    {isLoading ? "Logging in..." : "Login"}
                                </button>
                            </div>
                            <div className="mt-4 text-center text-sm">
                                <span className="text-primary">Don't have an account? </span>
                                <Link to="/register" className="text-blue-600 hover:text-blue-800 font-medium">
                                    Register
                                </Link>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </Layout>
    )
}