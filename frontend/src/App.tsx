import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import { AuthProvider } from "./context/AuthContext";
import { BooksProvider } from "./context/BooksContext";
import { Home } from "./pages/Home";
import { Login } from "./pages/auth/Login";
import { Register } from "./pages/auth/Register";
import CollectionDetails from "./components/collections/CollectionDetails.tsx";
import PublicCollections from "./components/collections/PublicCollections.tsx";
import { Books } from "./pages/book/Books";
import { BookDetail } from "./pages/book/BookDetail";
import {ShoppingCart} from "./pages/shoppingCart/ShoppingCart";
import {Profile} from "./pages/user/Profile.tsx";
import Collections from "./components/collections/Collections.tsx";
import AdminPage from "./pages/admin/AdminPage.tsx";
import CreateCollection from "./components/collections/CreateCollection.tsx";
import { BookLending } from "./pages/bookLending/BookLending.tsx";
import CreateSmartCollection from "./components/collections/CreateSmartCollection";
import Following from "./pages/follow/Following.tsx";
import UserProfile from "./pages/user/UserProfile.tsx";
import {NotificationProvider} from "./context/NotificationContext"

const App: React.FC = () => {
    return (
        <div className="w-full min-h-screen">
            <Router>
                <AuthProvider>
                    <NotificationProvider>
                    <Routes>
                        <Route path="/" element={<Home />} />
                        <Route path="/home" element={<Home />} />
                        <Route path="/login" element={<Login />} />
                        <Route path="/register" element={<Register />} />
                        <Route path="/books" element={
                            <BooksProvider>
                                <Books />
                            </BooksProvider>
                        } />
                        <Route path="/books/:id" element={
                            <BooksProvider>
                                <BookDetail />
                            </BooksProvider>
                        } />
                        <Route path={"/profile"} element={<Profile />} />
                        <Route path="/users/:id" element={<UserProfile />} />
                        <Route path="/cart" element={<ShoppingCart />} />
                        <Route path="/dashboard" element={<BookLending />} />
                        <Route path="/collections/user" element={<Collections />} />
                        <Route path="/collections/public" element={<PublicCollections />} />
                        <Route path="/collections/:id" element={<CollectionDetails />} />
                        <Route path="/admin" element={<AdminPage/>} />
                        <Route path="/collections/new" element={<CreateCollection />} />
                        <Route path="/following" element={<Following/>} />
                        <Route path="/collections/smart/new" element={<CreateSmartCollection />} />
                    </Routes>
                    <ToastContainer
                        position="bottom-left"
                        autoClose={5000}
                        hideProgressBar={false}
                        newestOnTop={false} closeOnClick
                        rtl={false}
                        pauseOnFocusLoss
                        draggable
                        pauseOnHover
                    />
                    </NotificationProvider>
                </AuthProvider>
            </Router>
        </div>
    );
};

export default App;