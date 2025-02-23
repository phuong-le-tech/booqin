"use client"

import {useState, type ChangeEvent, type FormEvent, useContext} from "react"
import {useNavigate} from "react-router-dom"
import {toast} from "react-toastify"
import {AuthContext} from "../../../context/AuthContext"
import AuthService from "../../../services/AuthService"
import {Layout} from "../Layout"
import {LoginForm} from "../../../model/AuthModel.ts"

export const Login = () => {
    const [formData, setFormData] = useState<LoginForm>({login: "", password: ""})
    const [showPassword, setShowPassword] = useState<boolean>(false)
    const navigate = useNavigate()
    const {setUser} = useContext(AuthContext)

    const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
        setFormData({...formData, [e.target.name]: e.target.value})
    }

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault()
        try {
            console.log("Submitting login form..." + JSON.stringify(formData))
            const user = await AuthService.login(formData)
            setUser(user)
            console.log("Login successful")
            toast.success("Login successful!")
            setTimeout(() => {
                navigate("/")
            }, 100)
        } catch (error: any) {
            console.error("Login error in component:", error)
            toast.error(error.message || "Login failed. Please check your credentials.")
        }
    }

    return (
        <Layout>
            <div className="flex items-center justify-center min-h-screen bg-blue-50 px-4 py-12 sm:px-6 lg:px-8">
                <div className="w-full max-w-md">
                    <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
                        <h2 className="text-3xl font-extrabold text-blue-800 text-center mb-6">Login</h2>
                        <form onSubmit={handleSubmit} className="space-y-6">
                            <div>
                                <label htmlFor="login" className="block text-sm font-medium text-gray-700">
                                    Email
                                </label>
                                <input
                                    type="email"
                                    name="login"
                                    id="login"
                                    autoComplete="login"
                                    required
                                    className="mt-1 block w-full border border-blue-200 rounded-md shadow-sm py-2 px-3
                                           focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                                    onChange={handleChange}
                                />
                            </div>
                            <div>
                                <label htmlFor="password" className="block text-sm font-medium text-gray-700">
                                    Password
                                </label>
                                <input
                                    type={showPassword ? "text" : "password"}
                                    name="password"
                                    id="password"
                                    autoComplete="current-password"
                                    required
                                    className="mt-1 block w-full border border-blue-200 rounded-md shadow-sm py-2 px-3
                                           focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                                    onChange={handleChange}
                                />
                            </div>
                            <div className="flex items-center">
                                <input
                                    id="show-password"
                                    name="show-password"
                                    type="checkbox"
                                    className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                                    onChange={() => setShowPassword(!showPassword)}
                                />
                                <label htmlFor="show-password" className="ml-2 block text-sm text-gray-900">
                                    Show password
                                </label>
                            </div>
                            <div>
                                <button
                                    type="submit"
                                    className="w-full flex justify-center py-2 px-4 border border-transparent
                                           rounded-md shadow-sm text-sm font-medium text-white
                                           bg-blue-600 hover:bg-blue-700
                                           focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                                >
                                    Login
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </Layout>
    )
}

