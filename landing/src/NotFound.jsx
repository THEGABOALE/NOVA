import novaLogo from "./assets/nova-logo.webp";

// Página 404. Se genera como HTML estático al compilar (scripts/prerender.js)
// y Cloudflare la sirve con estado 404 para cualquier ruta que no exista.
function NotFound() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-center bg-nova-mist px-4 py-16 text-center text-nova-ink">
      <img
        src={novaLogo}
        alt="NOVA Aplicación Educativa"
        width="552"
        height="144"
        className="nova-logo"
      />

      <h1 className="font-display mt-10 text-4xl text-nova-ink sm:text-5xl">
        No encontramos esta página
      </h1>

      <p className="mt-4 max-w-md text-lg leading-8 text-nova-muted">
        Puede que el enlace esté mal escrito o que la página ya no exista.
      </p>

      <a
        href="/"
        className="mt-8 inline-flex rounded-full bg-nova-purple px-8 py-4 font-bold text-white transition hover:-translate-y-1 hover:bg-nova-blue"
      >
        Volver al inicio
      </a>
    </main>
  );
}

export default NotFound;
