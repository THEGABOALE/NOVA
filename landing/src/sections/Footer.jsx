import Button from "../components/Button.jsx";
import NovaLogo from "../components/NovaLogo.jsx";
import { CONTACT_HREF, SECTIONS, contactHref } from "../config/navigation.js";

// Lo que no tiene `href` todavía no existe (secciones para docentes,
// instituciones, FAQ, páginas legales): se muestra como texto atenuado, sin
// enlace, hasta que se cree.
const columns = [
  {
    title: "Producto",
    links: [
      { label: "Cómo funciona", href: `#${SECTIONS.aventura}` },
      { label: "Experiencia", href: `#${SECTIONS.descubre}` },
      { label: "Misiones", href: `#${SECTIONS.descubre}` },
      { label: "Zafiro", href: `#${SECTIONS.zafiro}` },
    ],
  },
  {
    title: "Educación",
    links: [
      { label: "Para estudiantes" },
      { label: "Para docentes" },
      { label: "Para instituciones" },
    ],
  },
  {
    title: "Recursos",
    links: [
      { label: "FAQ" },
      { label: "Soporte" },
      { label: "Contacto", href: CONTACT_HREF },
    ],
  },
];

const legal = [
  { label: "Privacidad" },
  { label: "Términos" },
  { label: "Accesibilidad" },
  { label: "Contacto", href: CONTACT_HREF },
];

function FooterLink({ label, href }) {
  if (!href) {
    return <span className="inline-block py-1 text-nova-muted">{label}</span>;
  }

  return (
    <a
      href={href}
      className="inline-block py-1 underline-offset-4 transition hover:text-nova-purple hover:underline"
    >
      {label}
    </a>
  );
}

function Footer() {
  return (
    <footer className="border-t border-nova-purple/10">
      <nav
        aria-label="Pie de página"
        className="mx-auto flex w-full max-w-[88rem] flex-col gap-12 px-4 py-14 sm:px-6 lg:flex-row lg:justify-between lg:gap-16 lg:px-8 lg:py-16"
      >
        <div className="max-w-md">
          <NovaLogo />

          <p className="font-display mt-5 text-xl text-nova-ink">
            Aprende. Avanza. Transforma.
          </p>

          <p className="mt-3 text-sm leading-6 text-nova-muted">
            Una experiencia educativa para aprender sobre igualdad,
            dignidad, respeto y derechos de una forma diferente.
          </p>

          <Button
            href={contactHref("Quiero hablar con el equipo de NOVA")}
            size="sm"
            className="mt-6 inline-flex"
          >
            Hablar con el equipo
          </Button>
        </div>

        <div className="grid grid-cols-2 gap-x-8 gap-y-10 sm:grid-cols-[repeat(3,minmax(9rem,auto))] sm:gap-x-12 xl:gap-x-20 2xl:gap-x-28">
          {columns.map((column) => (
            <div key={column.title}>
              <h2 className="eyebrow text-nova-purple">
                {column.title}
              </h2>

              <ul className="mt-3 space-y-1 text-sm text-nova-body">
                {column.links.map((link) => (
                  <li key={link.label}>
                    <FooterLink {...link} />
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>
      </nav>

      <div className="border-t border-nova-purple/10">
        <div className="mx-auto flex w-full max-w-[88rem] flex-col gap-4 px-4 py-6 text-sm text-nova-muted sm:flex-row sm:items-center sm:justify-between sm:px-6 lg:px-8">
          <p>© 2026 NOVA · MindFlow</p>

          <ul className="flex flex-wrap gap-x-6 gap-y-1">
            {legal.map((link) => (
              <li key={link.label}>
                <FooterLink {...link} />
              </li>
            ))}
          </ul>
        </div>
      </div>
    </footer>
  );
}

export default Footer;
