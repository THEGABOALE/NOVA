import Button from "./Button.jsx";
import NovaLogo from "./NovaLogo.jsx";

function Navbar() {
  return (
    <header className="sticky top-0 z-50 border-b border-nova-purple/10 bg-nova-mist/95 backdrop-blur">
      <div className="mx-auto flex w-full max-w-[88rem] items-center justify-between px-4 py-3 sm:px-6 lg:px-8">
        <a href="#inicio" aria-label="Ir al inicio">
          <NovaLogo />
        </a>

        <nav className="hidden items-center gap-8 text-sm font-bold text-nova-purple md:flex">
          <a
            href="#aventura"
            className="transition hover:text-nova-blue"
          >
            Tu aventura
          </a>

          <a
            href="#descubre"
            className="transition hover:text-nova-blue"
          >
            Descubre NOVA
          </a>

          <a
            href="#zafiro"
            className="transition hover:text-nova-blue"
          >
            Conoce a Zafiro
          </a>
        </nav>

        <Button href="#comenzar" size="sm">
          Conocer NOVA
        </Button>
      </div>
    </header>
  );
}

export default Navbar;
