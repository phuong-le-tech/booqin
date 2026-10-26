import type React from "react";
import { useState } from "react";
import { toast } from "react-toastify";
import { useAuth } from "../../context/AuthContext";
import { DEMO_ACCOUNTS, IS_DEMO } from "../../utils/demo";

/** Slim notice above the app bar, shown only on the demo deployment. */
export const DemoBanner: React.FC = () => {
    const { isAuthenticated, login } = useAuth();
    const [busy, setBusy] = useState(false);

    if (!IS_DEMO) return null;

    const [reader] = DEMO_ACCOUNTS;
    const signInAsReader = async () => {
        setBusy(true);
        try {
            await login(reader.email, reader.password);
        } catch (error: any) {
            toast.error(error.message || "Could not sign in to the demo account");
        } finally {
            setBusy(false);
        }
    };

    return (
        <div className="bg-sunk text-[13px] leading-5 text-ink-2">
            <div className="mx-auto flex max-w-[1200px] flex-wrap items-center gap-x-3 gap-y-1 px-4 py-2 sm:px-8">
                <span>
                    <strong className="font-semibold text-ink">Demo site.</strong>{" "}
                    Payments run in Stripe test mode and the data can be reset.
                </span>
                {!isAuthenticated && (
                    <span className="flex flex-wrap items-center gap-x-2">
                        Log in with <code className="font-sans font-medium text-ink">{reader.email}</code> /{" "}
                        <code className="font-sans font-medium text-ink">{reader.password}</code>
                        <button
                            onClick={signInAsReader}
                            disabled={busy}
                            className="font-medium text-accent underline underline-offset-2 hover:text-accent-hover disabled:opacity-60"
                        >
                            {busy ? "Signing in..." : "or sign in as the demo reader"}
                        </button>
                    </span>
                )}
            </div>
        </div>
    );
};
