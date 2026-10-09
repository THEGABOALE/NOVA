// Destinos de la portada en un solo lugar: los `id` de las secciones, el menú
// y el correo de contacto. Solo hay destinos que existen; lo que todavía no
// tiene página (FAQ, legales, docentes…) se queda sin enlace en el pie.

export const SECTIONS = {
  inicio: "inicio",
  aventura: "aventura",
  descubre: "descubre",
  zafiro: "zafiro",
  comenzar: "comenzar",
};

export const NAV_ITEMS = [
  { label: "Tu aventura", href: `#${SECTIONS.aventura}` },
  { label: "Descubre NOVA", href: `#${SECTIONS.descubre}` },
  { label: "Conoce a Zafiro", href: `#${SECTIONS.zafiro}` },
];

export const CONTACT_EMAIL = "contacto@novamindflow.com";
export const CONTACT_HREF = `mailto:${CONTACT_EMAIL}`;

// Abre el correo con el asunto ya escrito.
export function contactHref(subject) {
  return `${CONTACT_HREF}?subject=${encodeURIComponent(subject)}`;
}
