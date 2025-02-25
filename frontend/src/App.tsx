import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import { AuthProvider } from "./context/AuthContext";
import { Home } from "./pages/Home";
import { Login } from "./pages/auth/Login";
import { Register } from "./pages/auth/Register";
import CollectionDetails from "./components/collections/CollectionDetails.tsx";
import PublicCollections from "./components/collections/PublicCollections.tsx";
import { Books } from "./pages/book/Books";
import Collections from "./components/collections/Collections.tsx";

const App: React.FC = () => {
    return (
        <div className="w-full min-h-screen">
            <Router>
                <AuthProvider>
                    <Routes>
                        <Route path="/" element={<Home />} />
                        <Route path="/home" element={<Home />} />
                        <Route path="/login" element={<Login />} />
                        <Route path="/register" element={<Register />} />
                        <Route path="/books" element={<Books />} />
                        <Route path="/collections" element={<Collections />} />
                        <Route path="/collections/public" element={<PublicCollections />} />
                        <Route path="/collections/:id" element={<CollectionDetails />} />
                    </Routes>
                    <ToastContainer
                        position="top-right"
                        autoClose={5000}
                        hideProgressBar={false}
                        newestOnTop={false}
                        closeOnClick
                        rtl={false}
                        pauseOnFocusLoss
                        draggable
                        pauseOnHover
                    />
                </AuthProvider>
            </Router>
        </div>
    );
};

export default App;
