import type React from "react"
import {Link} from "react-router-dom"
import {useContext} from "react"
import {AuthContext} from "../context/AuthContext.tsx"
import {Layout} from "./Layout.tsx";

export const Home: React.FC = () => {
    const {user} = useContext(AuthContext)

    return (
        <Layout>
            <div className="flex items-center justify-center min-h-screen w-full px-4 py-12">
                <div className="w-full max-w-3xl mx-auto">
                    <div className="home-card bg-secondary p-8 rounded-lg shadow-lg">
                        <h2 className="text-4xl font-extrabold mb-8 text-center text-primary">
                            Welcome to BooqIn
                        </h2>
                        {user ? (
                            <div className="text-center text-primary">
                                <p className="text-xl mb-6">
                                    Hello,{" "}
                                    <span className="font-semibold text-light-blue">
                  {user.firstName}
                </span>
                                    ! We're glad to have you here.
                                </p>
                                <Link to="/dashboard" className="btn btn-secondary inline-block px-8 py-4 text-lg">
                                    Go to Dashboard
                                </Link>
                            </div>
                        ) : (
                            <div className="text-center">
                                <p className="text-xl mb-8 text-primary">
                                    Discover the power of seamless booking with BooqIn.
                                </p>
                                <div className="space-x-4">
                                    <Link to="/login" className="btn btn-secondary inline-block px-8 py-4 text-lg">
                                        Login
                                    </Link>
                                    <Link to="/register" className="btn btn-secondary inline-block px-8 py-4 text-lg">
                                        Register
                                    </Link>
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </Layout>
    )
}

