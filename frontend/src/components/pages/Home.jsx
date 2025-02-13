import React from "react";

const Home = () => {
    return (
        <div className="flex flex-col min-h-screen w-full">
            {/* Navbar */}
            <nav className="w-full bg-white shadow-md">
                <div className="max-w-7xl mx-auto px-4 py-3 flex justify-between items-center w-full">
                    {/* Left side (logo or title) */}
                    <div>
                        <a
                            href="/home"
                            className="text-xl font-bold text-gray-700 hover:text-gray-900 transition-colors"
                        >
                            BooqIn
                        </a>
                    </div>

                    {/* Right side (auth links) */}
                    <div className="space-x-4">
                        <a
                            href="/login"
                            className="text-gray-600 hover:text-gray-900 transition-colors font-medium"
                        >
                            Sign In
                        </a>
                        <a
                            href="/register"
                            className="text-gray-600 hover:text-gray-900 transition-colors font-medium"
                        >
                            Sign Up
                        </a>
                    </div>
                </div>
            </nav>

            {/* Main content */}
            <main className="flex-grow flex items-center justify-center bg-gray-50 w-full">
                <div className="text-center p-8">
                    <h1 className="text-4xl font-bold text-gray-800 mb-4">Welcome to BooqIn!</h1>
                    <p className="text-gray-600 text-lg max-w-md mx-auto">
                        This is the home page. Please sign in or sign up to access more features.
                    </p>
                </div>
            </main>

            {/* Footer (optional) */}
            <footer className="bg-white shadow-inner py-4 text-center text-gray-500 w-full">
                © {new Date().getFullYear()} MyApp. All rights reserved.
            </footer>
        </div>
    );
};

export default Home;