import { BrowserRouter as Router, Routes, Route, Link } from "react-router-dom";
import { useState } from "react";
import reactLogo from "./assets/react.svg";
import viteLogo from "/vite.svg";
import "./App.css";
import SignIn from "./components/auth/SignIn";
import SignUp from "./components/auth/SignUp";

function App() {
    const [count, setCount] = useState(0);

    return (
        <Router>
            <div className="app-container">
                <header className="header">
                    <div>
                        <a href="https://vite.dev" target="_blank">
                            <img src={viteLogo} className="logo" alt="Vite logo" />
                        </a>
                        <a href="https://react.dev" target="_blank">
                            <img src={reactLogo} className="logo react" alt="React logo" />
                        </a>
                    </div>
                    <h1 className="text-red-500">Vite + React</h1>
                </header>

                <nav className="navigation flex justify-center space-x-4 py-4">
                    <Link to="/" className="text-blue-600 hover:underline">Accueil</Link>
                    <Link to="/signin" className="text-blue-600 hover:underline">Se connecter</Link>
                    <Link to="/signup" className="text-blue-600 hover:underline">S'inscrire</Link>
                </nav>

                <main className="p-4">
                    <Routes>
                        <Route path="/" element={
                            <div className="card text-center">
                                <button
                                    onClick={() => setCount(count + 1)}
                                    className="bg-blue-500 text-white p-2 rounded-md hover:bg-blue-600 transition"
                                >
                                    Compteur : {count}
                                </button>
                                <p>
                                    Éditez <code>src/App.jsx</code> et sauvegardez pour tester HMR.
                                </p>
                            </div>
                        } />
                        <Route path="/signin" element={<SignIn />} />
                        <Route path="/signup" element={<SignUp />} />
                    </Routes>
                </main>

                <footer className="text-center py-4">
                    <p className="read-the-docs">
                        Cliquez sur les logos de Vite et React pour en savoir plus.
                    </p>
                </footer>
            </div>
        </Router>
    );
}

export default App;
