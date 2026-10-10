import { render, screen, within } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import Journey from "./Journey.jsx";

function renderJourney() {
  const { container } = render(<Journey />);
  const steps = screen.getAllByRole("list").find((list) => list.tagName === "OL");

  return { container, steps };
}

describe("Journey", () => {
  it("muestra las cuatro etapas como lista ordenada, en orden", () => {
    const { steps } = renderJourney();
    const items = Array.from(steps.children);

    expect(items).toHaveLength(4);

    ["01", "02", "03", "04"].forEach((number, index) => {
      expect(items[index]).toHaveTextContent(new RegExp(`^${number}`));
    });

    expect(within(steps).getAllByRole("heading", { level: 3 }).map((heading) => heading.textContent)).toEqual([
      "Empieza tu ruta",
      "Acepta el reto",
      "Aprende jugando",
      "Mira cuánto avanzaste",
    ]);
  });

  it("marca el inicio y el final del recorrido", () => {
    renderJourney();

    expect(screen.getByText("Comenzar")).toBeInTheDocument();
    expect(screen.getByText("Sigue avanzando")).toBeInTheDocument();
  });

  it("cada etapa trae sus hitos", () => {
    const { steps } = renderJourney();
    const [first, second, third, fourth] = Array.from(steps.children);

    expect(within(first).getByText("Descubre misiones")).toBeInTheDocument();
    expect(within(second).getByText("Relaciona ideas")).toBeInTheDocument();
    expect(within(third).getByText("Conecta con la vida real")).toBeInTheDocument();
    expect(within(fourth).getByText("Observa tu progreso")).toBeInTheDocument();
  });

  it("no tiene botones ni enlaces: el recorrido solo explica", () => {
    const { container } = renderJourney();

    expect(within(container).queryAllByRole("button")).toHaveLength(0);
    expect(within(container).queryAllByRole("link")).toHaveLength(0);
  });
});
