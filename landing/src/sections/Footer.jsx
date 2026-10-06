import Button from "../components/Button.jsx";
import NovaLogo from "../components/NovaLogo.jsx";

const CONTACT = "mailto:contacto@novamindflow.com";

// Lo que no tiene `href` todavía no existe (secciones para docentes,
// instituciones, FAQ, páginas legales): se muestra como texto, sin enlace,
// hasta que se cree.
const columns = [
  {
    title: "Producto",
    links: [
      { label: "Cómo funciona", href: "#aventura" },
      { label: "Experiencia", href: "#descubre" },
      { label: "Misiones", href: "#descubre" },
      { label: "Zafiro", href: "#zafiro" },
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
      { label: "Contacto", href: CONTACT },
    ],
  },
];

const legal = [
  { label: "Privacidad" },
  { label: "Términos" },
  { label: "Accesibilidad" },
  { label: "Contacto", href: CONTACT },
];

function FooterLink({ label, href }) {
  if (!href) {
    return <span>{label}</span>;
  }

  return (
    <a href={href} className="transition hover:text-nova-purple">
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
        <div className="max-w-sm">
          <NovaLogo />

          <p className="font-display mt-5 text-xl text-nova-ink">
            Aprende. Avanza. Transforma.
          </p>

          <p className="mt-3 max-w-sm text-sm leading-6 text-nova-muted">
            Una experiencia educativa para aprender sobre igualdad,
            dignidad, respeto y derechos de una forma diferente.
          </p>

          <Button
            href={`${CONTACT}?subject=Quiero%20hablar%20con%20el%20equipo%20de%20NOVA`}
            size="sm"
            className="mt-6 inline-flex"
          >
            Hablar con el equipo
          </Button>
        </div>

        <div className="grid grid-cols-2 gap-x-8 gap-y-10 sm:grid-cols-3 sm:gap-x-12 lg:gap-x-16 xl:gap-x-20">
          {columns.map((column) => (
            <div key={column.title}>
              <h2 className="eyebrow text-nova-purple">
                {column.title}
              </h2>

              <ul className="mt-4 space-y-3 text-sm text-nova-body">
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

          <ul className="flex flex-wrap gap-x-6 gap-y-2">
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
