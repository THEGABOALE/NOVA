import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import NotFound from "./NotFound.jsx";

describe("NotFound", () => {
  it("explica que la página no existe y lleva al inicio", () => {
    render(<NotFound />);

    expect(screen.getByRole("heading", { level: 1, name: "No encontramos esta página" })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Volver al inicio" })).toHaveAttribute("href", "/");
  });
});
