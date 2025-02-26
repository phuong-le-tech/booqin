import type React from "react";
import { Link } from "react-router-dom";
import type { Book } from "../model/Book";

interface BookCardProps extends Book {}

export const BookCard: React.FC<BookCardProps> = ({
                                                      id,
                                                      isbns,
                                                      title,
                                                      authors,
                                                      description,
                                                      publicationDate,
                                                      collections,
                                                      createdAt,
                                                      updatedAt
                                                  }) => {
    const generateBackgroundColor = (title: string) => {
        let hash = 0;
        for (let i = 0; i < title.length; i++) {
            hash = title.charCodeAt(i) + ((hash << 5) - hash);
        }
        const color = Math.floor(Math.abs((Math.sin(hash) * 16777215) % 1) * 16777215).toString(16);
        return "#" + "0".repeat(6 - color.length) + color;
    };

    const bgColor = generateBackgroundColor(title);

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
                    <p className="text-xs sm:text-sm text-primary mb-2">
                        {authors.map((author, index) => (
                            <span key={author.id}>
                                {author.firstName} {author.lastName}
                                {index < authors.length - 1 ? ", " : ""}
                            </span>
                        ))}
                    </p>
                    <div className="flex items-center mt-auto">
                        {/* rating logic here ?? */}
                    </div>
                </div>
            </Link>
        </div>
    );
};


/*
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
    </div>
)*/
