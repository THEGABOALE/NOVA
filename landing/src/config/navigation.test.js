import { describe, expect, it } from "vitest";
import { CONTACT_EMAIL, CONTACT_HREF, NAV_ITEMS, SECTIONS, contactHref } from "./navigation.js";

describe("contacto", () => {
  it("usa el correo del proyecto", () => {
    expect(CONTACT_EMAIL).toBe("contacto@novamindflow.com");
    expect(CONTACT_HREF).toBe("mailto:contacto@novamindflow.com");
  });

  it("agrega el asunto codificado", () => {
    expect(contactHref("Quiero hablar con NOVA")).toBe(
      "mailto:contacto@novamindflow.com?subject=Quiero%20hablar%20con%20NOVA",
    );
  });

  it("codifica acentos y signos del asunto", () => {
    const href = contactHref("¿Más información & precios?");

    expect(href).toBe(
      `mailto:contacto@novamindflow.com?subject=${encodeURIComponent("¿Más información & precios?")}`,
    );
    expect(decodeURIComponent(href.split("subject=")[1])).toBe("¿Más información & precios?");
  });
});

describe("menú", () => {
  it("cada enlace apunta a una sección configurada", () => {
    const ids = Object.values(SECTIONS);

    for (const item of NAV_ITEMS) {
      expect(item.href).toMatch(/^#/);
      expect(ids).toContain(item.href.slice(1));
    }
  });
});
