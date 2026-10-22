import { createContext, useState } from "react";
import { tokenSessionStorage } from "../utils/SessionStorage";

export const UserContext = createContext(null);

export const UserProvider = ({ children }) => {

    const [user, setUser] = useState({ email: tokenSessionStorage.getUserEmail() } || null);

    const value = {
        user, setUser,
    };

    return (
        <UserContext.Provider
            value={value}
        >
            {children}
        </UserContext.Provider>
  );
};