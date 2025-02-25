import type React from "react"
import { useState, useRef, useEffect, type ReactNode } from "react"

interface DropdownProps {
    trigger: ReactNode
    children: ReactNode
    align?: "left" | "right"
}

export const Dropdown: React.FC<DropdownProps> = ({ trigger, children, align = "right" }) => {
    const [isOpen, setIsOpen] = useState(false)
    const dropdownRef = useRef<HTMLDivElement>(null)

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
                setIsOpen(false)
            }
        }

        document.addEventListener("mousedown", handleClickOutside)
        return () => {
            document.removeEventListener("mousedown", handleClickOutside)
        }
    }, [])

    return (
        <div className="relative" ref={dropdownRef}>
            <div onClick={() => setIsOpen(!isOpen)}>{trigger}</div>
            {isOpen && (
                <div
                    className={`absolute ${align === "right" ? "right-0" : "left-0"} mt-2 w-56 sm:w-64 bg-gray-400 rounded-md shadow-lg z-10 max-h-96 overflow-y-auto`}
                    style={{ maxWidth: "calc(100vw - 20px)" }}
                >
                    {children}
                </div>
            )}
        </div>
    )
}