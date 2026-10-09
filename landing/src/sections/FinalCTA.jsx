import Button from "../components/Button.jsx";
import NovaMark from "../components/NovaMark.jsx";
import { SECTIONS, contactHref } from "../config/navigation.js";

function FinalCTA() {
  return (
    <section id={SECTIONS.comenzar} className="py-14 sm:py-16">
      <div className="mx-auto w-full max-w-[72rem] px-4 text-center sm:px-6 lg:px-8">
        <NovaMark className="nova-pulse mx-auto h-14 w-14" />

        <p className="eyebrow mt-6 text-nova-purple">
          NOVA
        </p>

        <h2 className="font-display mx-auto mt-4 max-w-5xl text-5xl leading-none text-nova-ink sm:text-7xl">
          Aprende. Avanza. Transforma.
        </h2>

        <p className="mx-auto mt-6 max-w-3xl text-lg leading-8 text-nova-muted">
          Una experiencia creada para aprender jugando, reflexionar sobre
          situaciones reales y descubrir que cada decisión también puede
          enseñarnos algo.
        </p>

        <Button
          href={contactHref("Quiero conocer más sobre NOVA")}
          className="mt-8 inline-flex"
        >
          Conocer más sobre NOVA
        </Button>

        <p className="mt-4 text-sm text-nova-purple">
          ¿Eres docente o representas una institución? Conversemos.
        </p>
      </div>
    </section>
  );
}

export default FinalCTA;
