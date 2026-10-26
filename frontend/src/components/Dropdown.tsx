import type React from "react"
import { useState, useRef, useEffect, type ReactNode } from "react"

interface DropdownProps {
    trigger: ReactNode
    children: ReactNode | ((close: () => void) => ReactNode)
    align?: "left" | "right"
    panelClassName?: string
}

export const Dropdown: React.FC<DropdownProps> = ({ trigger, children, align = "right", panelClassName = "w-64" }) => {
    const [isOpen, setIsOpen] = useState(false)
    const dropdownRef = useRef<HTMLDivElement>(null)
    const close = () => setIsOpen(false)

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
                setIsOpen(false)
            }
        }
        const handleEscape = (event: KeyboardEvent) => {
            if (event.key === "Escape") setIsOpen(false)
        }

        document.addEventListener("mousedown", handleClickOutside)
        document.addEventListener("keydown", handleEscape)
        return () => {
            document.removeEventListener("mousedown", handleClickOutside)
            document.removeEventListener("keydown", handleEscape)
        }
    }, [])

    return (
        <div className="relative" ref={dropdownRef}>
            <div onClick={() => setIsOpen(!isOpen)} aria-expanded={isOpen}>{trigger}</div>
            {isOpen && (
                <div
                    className={`absolute ${align === "right" ? "right-0" : "left-0"} top-full z-50 mt-2 max-h-[70vh] overflow-y-auto rounded-surface bg-card p-2 shadow-2 ${panelClassName}`}
                    style={{ maxWidth: "calc(100vw - 24px)" }}
                >
                    {typeof children === "function" ? children(close) : children}
                </div>
            )}
        </div>
    )
}
