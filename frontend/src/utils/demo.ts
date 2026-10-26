/** True only in the deployed demo (the Dockerfile sets VITE_DEMO at build time). */
export const IS_DEMO = import.meta.env.VITE_DEMO === "true";

export const DEMO_ACCOUNTS = [
    { label: "Reader", email: "user@booqin.com", password: "user123" },
    { label: "Admin", email: "admin@booqin.com", password: "admin123" },
] as const;

export const DEMO_TEST_CARD = "4242 4242 4242 4242";
