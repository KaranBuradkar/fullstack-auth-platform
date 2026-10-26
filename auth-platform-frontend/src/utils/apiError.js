
export const getApiErrorMessage = (error) => {
  // Backend returned an HTTP error response
  if (error?.response) {
    const status = error.response.status;
    const data = error.response.data;

    // Handle backend validation errors, if your API returns them
    if (data?.errors && typeof data.errors === "object") {
      const firstError = Object.values(data.errors)
        .flat()
        .find((message) => typeof message === "string" && message.trim());

      if (firstError) {
        return firstError;
      }
    }

    // Use backend message only when it is a valid string
    if (
      typeof data?.message === "string" &&
      data.message.trim()
    ) {
      return data.message;
    }

    // Fallbacks for common HTTP statuses
    switch (status) {
      case 400:
        return "Please check your submitted information.";
      case 401:
        return "Your session is invalid. Please log in again.";
      case 403:
        return "You are not allowed to perform this action.";
      case 404:
        return "The requested resource was not found.";
      case 409:
        return "This request conflicts with existing data.";
      case 422:
        return "Please check your submitted information.";
      case 429:
        return "Too many attempts. Please try again later.";
      default:
        if (status >= 500) {
          return "A server error occurred. Please try again later.";
        }

        return "The request could not be completed.";
    }
  }

  // Request was sent, but the server did not respond
  if (error?.request) {
    if (error.code === "ECONNABORTED" || error.code === "ETIMEDOUT") {
      return "The request timed out. Please try again.";
    }

    return "Unable to connect to the server. Check your connection.";
  }

  // Axios or application setup failed
  return "Something went wrong. Please try again.";
};
