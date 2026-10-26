import { useState, type ChangeEvent, type FormEvent, useEffect } from "react"
import { useNavigate, Link, useLocation } from "react-router-dom"
import { toast } from "react-toastify"
import { useAuth } from "../../context/AuthContext"
import { Layout } from "../Layout.tsx"
import { AuthField } from "../../components/auth/AuthField"
import type { LoginForm } from "../../model/AuthModel.ts"

export const Login = () => {
    const [formData, setFormData] = useState<LoginForm>({ login: "", password: "" })
    const [error, setError] = useState<string | null>(null)
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
        setError(null)

        try {

            // Use the login function from our auth hook instead of service
            await login(formData.login, formData.password)

            toast.success("Login successful!")

            // Redirect is handled by the useEffect above
        } catch (error: any) {
            console.error("Login error in component:", error)
            setError(error.message || "Login failed. Please check your credentials.")
        } finally {
            setIsLoading(false)
        }
    }

    return (
        <Layout>
            <div className="mx-auto w-full max-w-[440px] px-4 py-12 sm:py-20">
                <h1 className="page-title">Welcome back</h1>
                <p className="mt-2 text-ink-2">Log in to borrow, lend and follow books.</p>

                <form onSubmit={handleSubmit} className="mt-8 space-y-5">
                    <AuthField
                        id="login"
                        name="login"
                        type="email"
                        label="Email"
                        autoComplete="email"
                        required
                        onChange={handleChange}
                    />
                    <AuthField
                        id="password"
                        name="password"
                        type="password"
                        label="Password"
                        autoComplete="current-password"
                        required
                        onChange={handleChange}
                    />

                    {error && (
                        <p role="alert" className="rounded-control bg-bad-tint px-4 py-3 text-sm text-bad">{error}</p>
                    )}

                    <button type="submit" disabled={isLoading} className="btn-accent w-full">
                        {isLoading ? "Logging in..." : "Log in"}
                    </button>
                </form>

                <p className="mt-6 text-center text-sm text-ink-2">
                    New to BooqIn?{" "}
                    <Link to="/register" className="font-medium text-accent underline underline-offset-2 hover:text-accent-hover">
                        Create an account
                    </Link>
                </p>
            </div>
        </Layout>
    )
}
