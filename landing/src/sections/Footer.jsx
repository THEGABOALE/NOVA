import NovaLogo from "../components/NovaLogo.jsx";

function Footer() {
  return (
    <footer className="border-t border-nova-purple/10 px-4 py-6 sm:px-6 lg:px-8">
      <div className="mx-auto flex w-full max-w-[88rem] flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <NovaLogo />

        <p className="max-w-xl text-sm leading-6 text-nova-muted">
          Una experiencia educativa para aprender sobre igualdad,
          dignidad, respeto y derechos de una forma diferente.
        </p>
      </div>
    </footer>
  );
}

export default Footer;
