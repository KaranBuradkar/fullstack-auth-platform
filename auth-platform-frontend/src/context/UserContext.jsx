import { createContext, useContext } from "react";
import { fetchUser } from '../api/userApi';

const UserContext = createContext(null);

export const UserProvider = ({ children }) => {

    const fetchUserProfile = async () => {
        try {
            const response = await fetchUser();
            if(response.success) console.log("Fetch User Profile Successfully");
            return response;
        } catch(error) {
            console.log("Failed to fetch user profile: ", error.response?.data || error.message);
            throw error;
        }
    };

    return (
        <UserContext.Provider
            value={{
                fetchUserProfile
            }}
        >
            {children}
        </UserContext.Provider>
  );
};

export const useUser = () => {
    const context = useContext(UserContext);

    if(!context) {
        throw new Error("useUser must be used within a UserProvider");
    }

    return context;
}