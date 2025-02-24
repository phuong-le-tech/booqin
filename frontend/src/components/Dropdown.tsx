import type React from "react"
import { useState, useRef, useEffect, type ReactNode } from "react"

interface DropdownProps {
    trigger: ReactNode
    children: ReactNode
}

export const Dropdown: React.FC<DropdownProps> = ({ trigger, children }) => {
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
            {isOpen && <div className="absolute right-0 mt-2 w-64 bg-gray-400 rounded-md shadow-lg z-10">{children}</div>}
        </div>
    )
}
