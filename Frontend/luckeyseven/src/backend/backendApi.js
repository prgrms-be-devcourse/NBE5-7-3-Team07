
const getApiBaseUrl = () => {
  
  if (process.env.REACT_APP_API_BASE_URL) {
    return process.env.REACT_APP_API_BASE_URL;
  }
  
  if (process.env.NODE_ENV === 'development') {
    return "http://localhost:8080";
  }
  
  return "";
};

export const API_BASE_URL = getApiBaseUrl();