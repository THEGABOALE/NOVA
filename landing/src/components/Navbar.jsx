import { useEffect, useRef, useState } from "react";
import { NAV_ITEMS, SECTIONS } from "../config/navigation.js";
import Button from "./Button.jsx";
import NovaLogo from "./NovaLogo.jsx";

const MENU_ID = "menu-movil";

function MenuIcon({ open }) {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="h-6 w-6"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
    >
      {open ? (
        <path d="M6 6l12 12M18 6L6 18" />
      ) : (
        <path d="M4 7h16M4 12h16M4 17h16" />
      )}
    </svg>
  );
}

// En pantallas medianas y grandes el menú va en la barra; en el teléfono se
// abre con el botón y se despliega debajo de la barra, encima del contenido.
function Navbar() {
  const [open, setOpen] = useState(false);
  const buttonRef = useRef(null);
  const close = () => setOpen(false);

  // Al pasar a escritorio (`md`, 48rem) el panel queda oculto por `md:hidden`:
  // se cierra para que no reaparezca abierto al volver al teléfono. No se
  // devuelve el foco porque en escritorio el botón tampoco se ve.
  useEffect(() => {
    const desktop = window.matchMedia("(min-width: 48rem)");
    const onChange = (event) => {
      if (event.matches) setOpen(false);
    };

    desktop.addEventListener("change", onChange);
    return () => desktop.removeEventListener("change", onChange);
  }, []);

  // Escape cierra el menú y devuelve el foco al botón que lo abrió.
  useEffect(() => {
    if (!open) return undefined;

    const onKeyDown = (event) => {
      if (event.key === "Escape") {
        setOpen(false);
        buttonRef.current?.focus();
      }
    };

    document.addEventListener("keydown", onKeyDown);
    return () => document.removeEventListener("keydown", onKeyDown);
  }, [open]);

  return (
    <header className="sticky top-0 z-50 border-b border-nova-purple/10 bg-nova-mist/95 backdrop-blur">
      <div className="mx-auto flex w-full max-w-[88rem] items-center justify-between gap-4 px-4 py-3 sm:px-6 lg:px-8">
        <a href={`#${SECTIONS.inicio}`} aria-label="Ir al inicio" onClick={close}>
          <NovaLogo className="h-10 sm:h-12" />
        </a>

        <nav
          aria-label="Principal"
          className="hidden items-center gap-8 text-sm font-bold text-nova-purple md:flex"
        >
          {NAV_ITEMS.map((item) => (
            <a
              key={item.href}
              href={item.href}
              className="py-2 transition hover:text-nova-blue"
            >
              {item.label}
            </a>
          ))}
        </nav>

        <div className="flex items-center gap-2">
          <Button
            href={`#${SECTIONS.comenzar}`}
            size="sm"
            className="hidden sm:inline-flex"
          >
            Conocer NOVA
          </Button>

          <button
            ref={buttonRef}
            type="button"
            aria-expanded={open}
            aria-controls={MENU_ID}
            aria-label={open ? "Cerrar menú" : "Abrir menú"}
            onClick={() => setOpen((value) => !value)}
            className="inline-flex h-11 w-11 items-center justify-center rounded-full text-nova-purple transition hover:bg-nova-purple/10 md:hidden"
          >
            <MenuIcon open={open} />
          </button>
        </div>
      </div>

      <nav
        id={MENU_ID}
        aria-label="Principal"
        hidden={!open}
        className="absolute inset-x-0 top-full border-b border-nova-purple/10 bg-nova-mist md:hidden"
      >
        <ul className="mx-auto flex w-full max-w-[88rem] flex-col gap-1 px-4 py-3 sm:px-6">
          {NAV_ITEMS.map((item) => (
            <li key={item.href}>
              <a
                href={item.href}
                onClick={close}
                className="flex min-h-12 items-center rounded-2xl px-3 font-bold text-nova-purple transition hover:bg-nova-purple/10"
              >
                {item.label}
              </a>
            </li>
          ))}
        </ul>

        <div className="px-4 pb-5 sm:hidden">
          <Button
            href={`#${SECTIONS.comenzar}`}
            onClick={close}
            className="flex w-full justify-center"
          >
            Conocer NOVA
          </Button>
        </div>
      </nav>
    </header>
  );
}

export default Navbar;
