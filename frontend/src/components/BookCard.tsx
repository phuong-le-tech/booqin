import type React from "react"
import { Link } from "react-router-dom"
import type { Book } from "../model/Book"
import { useContext } from "react";
import { AuthContext } from "../context/AuthContext.tsx";
import axiosInstance from "../services/AxiosInstance.ts";
import {ApiResponse} from "../model/ApiResponse.ts";


interface BookCardProps extends Book { }

export const BookCard: React.FC<BookCardProps> = ({ id, title, author, rating }) => {
    const { user } = useContext(AuthContext);
    const userId = user?.id;
    const generateBackgroundColor = (title: string) => {
        let hash = 0
        for (let i = 0; i < title.length; i++) {
            hash = title.charCodeAt(i) + ((hash << 5) - hash)
        }
        const color = Math.floor(Math.abs((Math.sin(hash) * 16777215) % 1) * 16777215).toString(16)
        return "#" + "0".repeat(6 - color.length) + color
    }

    const addToCart = async () => {
        try {
            const response = await axiosInstance.post("/v1/cart/add-book", {
                userId,
                bookId: id,
            });

            if (response.status === 200) {
                alert("Le livre a été ajouté au panier");
            }
        } catch (error) {
            console.error("Erreur lors de l'ajout du livre au panier", error);
            console.log(error.response?.data);
        }
    };



    const bgColor = generateBackgroundColor(title)

    return (
        <div className="bg-secondary rounded-lg shadow-md overflow-hidden transition-transform duration-300 hover:scale-105 h-full flex flex-col">
            <Link to={`/books/${id}`} className="flex flex-col h-full">
                <div
                    className="w-full h-32 sm:h-40 md:h-48 flex items-center justify-center text-white text-xl sm:text-2xl font-bold"
                    style={{ backgroundColor: bgColor }}
                >
                    {title.substring(0, 2).toUpperCase()}
                </div>
                <div className="p-3 sm:p-4 flex-grow flex flex-col">
                    <h3 className="text-base sm:text-lg font-semibold text-primary mb-1 line-clamp-2">{title}</h3>
                    <p className="text-xs sm:text-sm text-primary mb-2">{author}</p>
                    <div className="flex items-center mt-auto">
                        <span className="text-yellow-400 mr-1">★</span>
                        <span className="text-xs sm:text-sm text-primary">{rating.toFixed(1)}</span>
                    </div>
                </div>
            </Link>
            <button
                onClick={addToCart}
                className="m-3 bg-light-blue text-navbar px-4 py-2 rounded-lg hover:bg-blue-500 transition"
            >
                Add to cart
            </button>
        </div>
    )
}