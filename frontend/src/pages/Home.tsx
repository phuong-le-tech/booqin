import type React from "react"
import {Link} from "react-router-dom"
import {useContext} from "react"
import {AuthContext} from "../context/AuthContext"
import {Layout} from "./Layout.tsx";

export const Home: React.FC = () => {
    const {user} = useContext(AuthContext)
    //const [collections, setCollections] = useState<BookCollection[]>([]);

    return (
        <Layout>
            <div className="flex items-center justify-center min-h-screen w-full px-4 py-8 sm:py-12">
                <div className="w-full max-w-3xl mx-auto">
                    <div className="home-card bg-secondary p-4 sm:p-6 md:p-8 rounded-lg shadow-lg">
                        <h2 className="text-2xl sm:text-3xl md:text-4xl font-extrabold mb-4 sm:mb-6 md:mb-8 text-center text-primary">
                            Welcome to BooqIn
                        </h2>
                        {user ? (
                            <div className="text-center text-primary">
                                <p className="text-lg sm:text-xl mb-4 sm:mb-6">
                                    Hello,{" "}
                                    <span className="font-semibold text-light-blue">
                                        {user.firstName}
                                    </span>
                                    ! We're glad to have you here.
                                </p>
                                <Link to="/dashboard" className="btn btn-secondary inline-block px-4 py-2 sm:px-6 sm:py-3 md:px-8 md:py-4 text-base sm:text-lg">
                                    Go to Dashboard
                                </Link>
                            </div>
                        ) : (
                            <div className="text-center">
                                <p className="text-lg sm:text-xl mb-4 sm:mb-6 md:mb-8 text-primary">
                                    Discover the power of seamless booking with BooqIn.
                                </p>
                                <div className="flex flex-col sm:flex-row justify-center space-y-3 sm:space-y-0 sm:space-x-4">
                                    <Link to="/login" className="btn btn-secondary inline-block px-4 py-2 sm:px-6 sm:py-3 md:px-8 md:py-4 text-base sm:text-lg">
                                        Login
                                    </Link>
                                    <Link to="/register" className="btn btn-secondary inline-block px-4 py-2 sm:px-6 sm:py-3 md:px-8 md:py-4 text-base sm:text-lg">
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

