// src/context/AuthContext.tsx
import {
    createContext,
    useState,
    useEffect,
    ReactNode,
    Dispatch,
    SetStateAction,
} from "react";
import AuthService from "../services/AuthService";
import { User } from "../model/User";

/** The shape of the AuthContext’s value. */
interface AuthContextType {
    user: User | null;
    setUser: Dispatch<SetStateAction<User | null>>;
}

/** Default context value (can be `null` if you prefer). */
export const AuthContext = createContext<AuthContextType>({
    user: null,
    // Provide a no-op for setUser so TypeScript doesn't complain;
    // the real function will come from useState.
    setUser: () => {},
});

interface AuthProviderProps {
    children: ReactNode;
}

export const AuthProvider = ({ children }: AuthProviderProps) => {
    const [user, setUser] = useState<User | null>(null);

    useEffect(() => {
        const fetchUser = async () => {
            try {
                const currentUser = await AuthService.fetchCurrentUser();
                setUser(currentUser);
            } catch (error) {
                // If not logged in or token invalid, do nothing
                console.error("User not logged in or token invalid:", error);
            }
        };

        fetchUser().then((r) => console.log("User fetched:", r));
    }, []);

    return (
        <AuthContext.Provider value={{ user, setUser }}>
            {children}
        </AuthContext.Provider>
    );
};
