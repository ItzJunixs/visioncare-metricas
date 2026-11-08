/// <reference types="vitest" />
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// En desarrollo, las llamadas a /api se redirigen al backend Spring Boot (vc-api).
const apiTarget = process.env.VITE_API_PROXY ?? "http://localhost:8080";

export default defineConfig({
  plugins: [react()],
  test: { environment: "jsdom", setupFiles: "./src/test-setup.ts", globals: true },
  server: {
    port: 5173,
    proxy: {
      "/api": { target: apiTarget, changeOrigin: true },
    },
  },
});
