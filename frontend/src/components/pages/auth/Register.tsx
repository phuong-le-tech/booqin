"use client"

import { useState, type ChangeEvent, type FormEvent, useContext } from "react"
import { useNavigate } from "react-router-dom"
import { toast } from "react-toastify"
import { AuthContext } from "../../../context/AuthContext"
import AuthService from "../../../services/AuthService"
import {Address} from "../../../model/Address.ts";

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
            const user = await AuthService.login({ email: formData.email, password: formData.password })
            setUser(user)
            navigate("/")
        } catch (error: any) {
            toast.error(error.message || "Registration failed. Please try again.")
        }
    }

    return (
        <div className="flex items-center justify-center min-h-screen bg-gray-100 px-4 py-12 sm:px-6 lg:px-8">
            <div className="w-full max-w-md">
                <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
                    <h2 className="text-3xl font-extrabold text-center text-gray-900 mb-6">Register</h2>
                    <form onSubmit={handleSubmit} className="space-y-6">
                        <div className="form-group">
                            <label htmlFor="firstName">First Name</label>
                            <input
                                type="text"
                                className="form-control"
                                id="firstName"
                                name="firstName"
                                value={formData.firstName}
                                onChange={handleChange}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="lastName">Last Name</label>
                            <input
                                type="text"
                                className="form-control"
                                id="lastName"
                                name="lastName"
                                value={formData.lastName}
                                onChange={handleChange}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="email">Email</label>
                            <input
                                type="email"
                                className="form-control"
                                id="email"
                                name="email"
                                value={formData.email}
                                onChange={handleChange}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="password">Password</label>
                            <input
                                type="password"
                                className="form-control"
                                id="password"
                                name="password"
                                value={formData.password}
                                onChange={handleChange}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="streetNumber">Street Number</label>
                            <input
                                type="text"
                                className="form-control"
                                id="streetNumber"
                                name="streetNumber"
                                value={formData.address.streetNumber}
                                onChange={handleChange}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="streetName">Street Name</label>
                            <input
                                type="text"
                                className="form-control"
                                id="streetName"
                                name="streetName"
                                value={formData.address.streetName}
                                onChange={handleChange}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="city">City</label>
                            <input
                                type="text"
                                className="form-control"
                                id="city"
                                name="city"
                                value={formData.address.city}
                                onChange={handleChange}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="postalCode">Postal Code</label>
                            <input
                                type="text"
                                className="form-control"
                                id="postalCode"
                                name="postalCode"
                                value={formData.address.postalCode}
                                onChange={handleChange}
                                required
                            />
                        </div>
                        <button
                            type="submit"
                            className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
                        >
                            Register
                        </button>
                    </form>
                </div>
            </div>
        </div>
    )
}

