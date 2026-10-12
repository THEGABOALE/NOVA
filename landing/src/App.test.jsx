import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import App from "./App.jsx";

describe("App", () => {
  it("el enlace para saltar al contenido lleva al <main>", () => {
    render(<App />);

    const skipLink = screen.getByRole("link", { name: "Saltar al contenido" });
    const main = screen.getByRole("main");

    expect(skipLink).toHaveAttribute("href", "#contenido");
    expect(main).toHaveAttribute("id", "contenido");
  });
});
