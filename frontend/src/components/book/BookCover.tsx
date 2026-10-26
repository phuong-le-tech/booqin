import React, { useState } from "react";

// Six warm paper tones for covers that have no image. Picked from the title so
// the same book always gets the same tone.
const TONES = ["#e8dfc9", "#d8b79c", "#c5cbb9", "#ddbfb8", "#e6d8b0", "#cdc8bc"];

const toneFor = (title: string): string => {
    let hash = 0;
    for (let i = 0; i < title.length; i++) {
        hash = (hash * 31 + title.charCodeAt(i)) >>> 0;
    }
    return TONES[hash % TONES.length];
};

interface BookCoverProps {
    title: string;
    authors?: string[];
    src?: string | null;
    className?: string;
    /** For thumbnails under ~120px wide: title only, small. */
    compact?: boolean;
}

/** 2:3 cover: the real image when there is one, a typographic cover otherwise. */
export const BookCover: React.FC<BookCoverProps> = ({ title, authors = [], src, className = "", compact = false }) => {
    const [failed, setFailed] = useState(false);
    const showImage = Boolean(src) && !failed;

    return (
        <div
            className={`relative aspect-[2/3] w-full overflow-hidden rounded-cover bg-sunk ${className}`}
            style={showImage ? undefined : { backgroundColor: toneFor(title) }}
        >
            {showImage ? (
                <img
                    src={src!}
                    alt={`Cover of ${title}`}
                    loading="lazy"
                    className="h-full w-full object-cover"
                    onError={() => setFailed(true)}
                />
            ) : (
                compact ? (
                    <div className="absolute inset-1 flex items-center justify-center border border-ink/25 p-1 text-center">
                        <span className="font-serif text-[13px] font-semibold leading-tight text-ink line-clamp-4">{title}</span>
                    </div>
                ) : (
                    <div className="absolute inset-2 flex flex-col items-center justify-between border border-ink/25 px-3 py-4 text-center">
                        <svg width="10" height="10" viewBox="0 0 10 10" aria-hidden="true" className="text-ink/60">
                            <path d="M5 0.5 9.5 5 5 9.5 0.5 5Z" fill="none" stroke="currentColor" />
                        </svg>
                        <span className="font-serif text-[17px] font-semibold leading-snug text-ink line-clamp-5">
                            {title}
                        </span>
                        <span className="text-[13px] leading-tight text-ink-2 line-clamp-2">
                            {authors.length > 0 ? authors.join(", ") : "Unknown author"}
                        </span>
                    </div>
                )
            )}
        </div>
    );
};
