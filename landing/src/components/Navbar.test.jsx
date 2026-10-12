import { act, render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it } from "vitest";
import Navbar from "./Navbar.jsx";

function renderNavbar() {
  const user = userEvent.setup();
  render(<Navbar />);

  const button = screen.getByRole("button", { name: "Abrir menú" });
  // El panel es el elemento que el botón declara controlar.
  const menu = document.getElementById(button.getAttribute("aria-controls"));

  return { user, button, menu };
}

// Simula window.matchMedia y deja disparar el cambio a escritorio.
function mockBreakpoint() {
  const listeners = new Set();

  window.matchMedia = (query) => ({
    matches: false,
    media: query,
    addEventListener: (_type, listener) => listeners.add(listener),
    removeEventListener: (_type, listener) => listeners.delete(listener),
  });

  return {
    listeners,
    goDesktop() {
      act(() => {
        listeners.forEach((listener) => listener({ matches: true }));
      });
    },
  };
}

describe("Navbar: menú móvil", () => {
  it("empieza cerrado", () => {
    const { button, menu } = renderNavbar();

    expect(button).toHaveAttribute("aria-expanded", "false");
    expect(menu).toBeInTheDocument();
    expect(menu).not.toBeVisible();
  });

  it("se abre y se cierra con el botón", async () => {
    const { user, button, menu } = renderNavbar();

    await user.click(button);
    expect(button).toHaveAttribute("aria-expanded", "true");
    expect(button).toHaveAccessibleName("Cerrar menú");
    expect(menu).toBeVisible();

    await user.click(button);
    expect(button).toHaveAttribute("aria-expanded", "false");
    expect(button).toHaveAccessibleName("Abrir menú");
    expect(menu).not.toBeVisible();
  });

  it("Escape lo cierra y devuelve el foco al botón", async () => {
    const { user, button, menu } = renderNavbar();

    await user.click(button);
    await user.tab();
    expect(button).not.toHaveFocus();

    await user.keyboard("{Escape}");
    expect(button).toHaveAttribute("aria-expanded", "false");
    expect(menu).not.toBeVisible();
    expect(button).toHaveFocus();
  });

  it("se cierra al elegir un enlace", async () => {
    const { user, button, menu } = renderNavbar();

    await user.click(button);
    const [firstLink] = within(menu).getAllByRole("link");
    await user.click(firstLink);

    expect(button).toHaveAttribute("aria-expanded", "false");
    expect(menu).not.toBeVisible();
  });

  it("se cierra al pasar al tamaño de escritorio", async () => {
    const breakpoint = mockBreakpoint();
    const { user, button, menu } = renderNavbar();

    await user.click(button);
    expect(menu).toBeVisible();

    breakpoint.goDesktop();
    expect(button).toHaveAttribute("aria-expanded", "false");
    expect(menu).not.toBeVisible();
  });

  it("deja de escuchar el cambio de tamaño al desmontarse", () => {
    const breakpoint = mockBreakpoint();
    const { unmount } = render(<Navbar />);

    expect(breakpoint.listeners.size).toBe(1);
    unmount();
    expect(breakpoint.listeners.size).toBe(0);
  });
});
