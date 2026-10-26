import React, { useState } from "react";
import { Eye, EyeOff } from "lucide-react";

interface AuthFieldProps extends React.InputHTMLAttributes<HTMLInputElement> {
    id: string;
    label: string;
    /** Error text; only rendered when present. */
    error?: string;
    hint?: string;
}

/** Labelled input with inline error. Password fields get a show/hide toggle. */
export const AuthField: React.FC<AuthFieldProps> = ({ id, label, error, hint, type = "text", ...input }) => {
    const [visible, setVisible] = useState(false);
    const isPassword = type === "password";
    const describedBy = error ? `${id}-error` : hint ? `${id}-hint` : undefined;

    return (
        <div>
            <label htmlFor={id} className="form-label">{label}</label>
            <div className="relative">
                <input
                    id={id}
                    type={isPassword && visible ? "text" : type}
                    aria-invalid={error ? true : undefined}
                    aria-describedby={describedBy}
                    className={`form-input aria-[invalid=true]:border-bad ${isPassword ? "pr-12" : ""}`}
                    {...input}
                />
                {isPassword && (
                    <button
                        type="button"
                        onClick={() => setVisible(!visible)}
                        aria-label={visible ? "Hide password" : "Show password"}
                        className="absolute inset-y-0 right-0 flex w-12 items-center justify-center text-ink-2 hover:text-ink"
                    >
                        {visible ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
                    </button>
                )}
            </div>
            {error ? (
                <p id={`${id}-error`} className="mt-1.5 text-[13px] text-bad">{error}</p>
            ) : hint ? (
                <p id={`${id}-hint`} className="mt-1.5 text-[13px] text-ink-3">{hint}</p>
            ) : null}
        </div>
    );
};
