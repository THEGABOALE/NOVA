import react from "@vitejs/plugin-react";
import { defineConfig } from "vitest/config";

// Tests de componentes en jsdom (`npm run test`). Solo hace falta el plugin de
// React: los componentes no importan CSS, así que Tailwind no participa.
export default defineConfig({
  plugins: [react()],
  test: {
    environment: "jsdom",
    setupFiles: ["./src/test/setup.js"],
  },
});
