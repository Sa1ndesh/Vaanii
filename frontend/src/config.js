// API Configuration for Web and Capacitor Native Android
const ENV_API_URL = import.meta.env.VITE_API_URL;
const PUBLIC_HTTPS_URL = "https://swift-windows-tan.loca.lt";

export const API_BASE_URL = ENV_API_URL
  ? ENV_API_URL
  : (typeof window !== 'undefined' && (window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1"))
    ? "http://127.0.0.1:8000"
    : PUBLIC_HTTPS_URL;

if (import.meta.env.DEV) {
  console.log("[Config] Resolved API Base URL:", API_BASE_URL);
}
