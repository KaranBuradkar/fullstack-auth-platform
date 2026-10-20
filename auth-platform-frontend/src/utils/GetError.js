export function getApiError(error) {
  if (error.response) {
    return {
      message:
        error.response.data?.message ||
        "Request failed. Please try again.",
      errorCode: error.response.data?.errorCode || null,
      status: error.response.status,
    };
  }

  if (error.code === "ECONNABORTED") {
    return {
      message: "Request timed out. Please try again.",
      errorCode: "REQUEST_TIMEOUT",
    };
  }

  if (!error.response) {
    return {
      message: "Unable to connect to the server.",
      errorCode: "NETWORK_ERROR",
    };
  }

  return {
    message: "An unexpected error occurred.",
    errorCode: "UNKNOWN_ERROR",
  };
}