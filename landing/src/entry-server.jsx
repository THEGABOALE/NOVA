import { StrictMode } from "react";
import { renderToString } from "react-dom/server";
import App from "./App.jsx";
import NotFound from "./NotFound.jsx";

// Entrada del build de servidor (vite build --ssr). scripts/prerender.js la
// usa para generar el HTML de cada página al compilar.
export function renderHome() {
  return renderToString(
    <StrictMode>
      <App />
    </StrictMode>,
  );
}

export function renderNotFound() {
  return renderToString(
    <StrictMode>
      <NotFound />
    </StrictMode>,
  );
}
