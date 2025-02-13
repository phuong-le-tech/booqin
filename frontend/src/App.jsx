import {BrowserRouter as Router, Routes, Route, Link} from "react-router-dom";
import "./App.css";
import Login from "./components/auth/Login.jsx";
import Register from "./components/auth/Register.jsx";
import Home from "./components/pages/Home.jsx";


function App() {
    return (
        <Router>
            <Routes>
                <Route path="/home" element={<Home/>}/>
                <Route path="/login" element={<Login/>}/>
                <Route path="/register" element={<Register/>}/>
                <Route path="/" element={<Home/>}/>
            </Routes>
        </Router>
    );
}

export default App;
