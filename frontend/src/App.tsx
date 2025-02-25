import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import { AuthProvider } from "./context/AuthContext";
import { Home } from "./components/pages/Home";
import { Login } from "./components/pages/auth/Login";
import { Register } from "./components/pages/auth/Register";
import CollectionDetails from "./components/pages/collections/CollectionDetails.tsx";
import PublicCollections from "./components/pages/collections/PublicCollections.tsx";

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
                        <Route path="/collections" element={<PublicCollections />} />
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
