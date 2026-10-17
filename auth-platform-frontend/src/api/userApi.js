import api from "./axios";

export const fetchUser = async () => {
    const response = await api.get("/users/me");
    return response.data;
}