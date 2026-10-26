// src/components/Pagination.tsx
import React from "react";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { PageInfo } from "../model/Pagination";

interface PaginationProps {
    pageInfo: PageInfo;
    onPageChange: (page: number) => void;
}

/** Page numbers to show: first, last, and a window around the current page. */
const visiblePages = (current: number, total: number): (number | "gap")[] => {
    const wanted = new Set([0, total - 1, current - 1, current, current + 1]);
    const pages = [...wanted].filter(p => p >= 0 && p < total).sort((a, b) => a - b);
    const result: (number | "gap")[] = [];
    pages.forEach((page, index) => {
        if (index > 0 && page - pages[index - 1] > 1) result.push("gap");
        result.push(page);
    });
    return result;
};

export const Pagination: React.FC<PaginationProps> = ({ pageInfo, onPageChange }) => {
    const currentPage = typeof pageInfo.number === "number" && !isNaN(pageInfo.number) ? pageInfo.number : 0;
    const totalPages = pageInfo.totalPages > 0 ? pageInfo.totalPages : 1;

    if (totalPages <= 1) {
        return null;
    }

    const goTo = (page: number) => onPageChange(Math.max(0, Math.min(page, totalPages - 1)));
    const arrow =
        "flex h-10 w-10 items-center justify-center rounded-full text-ink hover:bg-sunk disabled:cursor-not-allowed disabled:text-ink-3 disabled:opacity-50 disabled:hover:bg-transparent";

    return (
        <nav className="mt-14 flex items-center justify-center gap-1" aria-label="Pagination">
            <button onClick={() => goTo(currentPage - 1)} disabled={currentPage === 0} className={arrow} aria-label="Previous page">
                <ChevronLeft className="h-5 w-5" />
            </button>

            {visiblePages(currentPage, totalPages).map((page, index) =>
                page === "gap" ? (
                    <span key={`gap-${index}`} className="px-1 text-ink-3" aria-hidden="true">...</span>
                ) : (
                    <button
                        key={page}
                        onClick={() => goTo(page)}
                        aria-current={page === currentPage ? "page" : undefined}
                        className={`h-10 min-w-10 rounded-full px-3 text-sm font-medium ${
                            page === currentPage ? "bg-ink text-paper" : "text-ink hover:bg-sunk"
                        }`}
                    >
                        {page + 1}
                    </button>
                )
            )}

            <button onClick={() => goTo(currentPage + 1)} disabled={currentPage === totalPages - 1} className={arrow} aria-label="Next page">
                <ChevronRight className="h-5 w-5" />
            </button>
        </nav>
    );
};
