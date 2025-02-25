"use client"

import type React from "react"

import {Link} from "react-router-dom"
import {useContext, useState} from "react"
import {AuthContext} from "../../context/AuthContext"
import {Layout} from "./Layout.tsx";
import PublicCollections from "./collections/PublicCollections.tsx";
import {BookCollection} from "../../model/BookCollection.ts";

export const Home: React.FC = () => {
    const {user} = useContext(AuthContext)
    //const [collections, setCollections] = useState<BookCollection[]>([]);

    return (
        <Layout>
            <div className="flex items-center justify-center min-h-screen w-full px-4 py-12">
                <div className="w-full max-w-3xl mx-auto">
                    <div
                        className="bg-white py-12 px-8 shadow-lg sm:rounded-2xl sm:px-12 transition-all duration-300 hover:shadow-xl border"
                        style={{borderColor: "var(--border)"}}
                    >
                        <h2 className="text-4xl font-extrabold mb-8 text-center" style={{color: "var(--text)"}}>
                            Welcome to BooqIn
                        </h2>
                        {user ? (
                            <div className="text-center">
                                <p className="text-xl mb-6" style={{color: "var(--text-light)"}}>
                                    Hello,{" "}
                                    <span className="font-semibold" style={{color: "var(--primary)"}}>
                  {user.firstName}
                </span>
                                    ! We're glad to have you here.
                                </p>
                                <Link to="/dashboard" className="btn btn-primary inline-block px-8 py-4 text-lg">
                                    Go to Dashboard
                                </Link>
                            </div>
                        ) : (
                            <div className="text-center">
                                <p className="text-xl mb-8" style={{color: "var(--text-light)"}}>
                                    Discover the power of seamless booking with BooqIn.
                                </p>
                                <div className="space-x-4">
                                    <Link to="/login" className="btn btn-primary inline-block px-8 py-4 text-lg">
                                        Sign In
                                    </Link>
                                    <Link to="/register" className="btn btn-secondary inline-block px-8 py-4 text-lg">
                                        Sign Up
                                    </Link>
                                </div>
                            </div>
                        )}

                        <div className="mt-8">
                            {/*<PublicCollections collections={collections}/>*/}
                            <PublicCollections />
                        </div>
                    </div>
                </div>
            </div>
        </Layout>
    )
}
