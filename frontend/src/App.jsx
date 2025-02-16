import {BrowserRouter as Router, Routes, Route, Link} from "react-router-dom";
import "./App.css";
import Login from "./components/auth/Login.jsx";
import Register from "./components/auth/Register.jsx";
import Home from "./components/pages/Home.jsx";
import { AuthProvider } from "./components/context/AuthContext.jsx";
import { ToastContainer } from 'react-toastify';

function App() {
    return (
        <AuthProvider>
        <Router>
            <Routes>
                <Route path="/" element={<Home/>}/>
                <Route path="/home" element={<Home/>}/>
                <Route path="/login" element={<Login/>}/>
                <Route path="/register" element={<Register/>}/>
                <Route path="/" element={<Home/>}/>
            </Routes>
        </Router>
            <ToastContainer />
            </AuthProvider>
    );
}

export default App;
