import "@testing-library/jest-dom/vitest";
import { cleanup } from "@testing-library/react";
import { afterEach, vi } from "vitest";

// jsdom no implementa matchMedia. Por defecto ninguna media query coincide
// (pantalla de teléfono); un test puede reemplazarlo para simular un cambio.
function mockMatchMedia() {
  window.matchMedia = vi.fn((query) => ({
    matches: false,
    media: query,
    onchange: null,
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    addListener: vi.fn(),
    removeListener: vi.fn(),
    dispatchEvent: vi.fn(),
  }));
}

mockMatchMedia();

afterEach(() => {
  cleanup();
  mockMatchMedia();
});
