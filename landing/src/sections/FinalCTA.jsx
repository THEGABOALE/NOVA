import Button from "../components/Button.jsx";
import NovaMark from "../components/NovaMark.jsx";

function FinalCTA() {
  return (
    <section id="comenzar" className="py-14 sm:py-16">
      <div className="mx-auto w-full max-w-[72rem] px-4 text-center sm:px-6 lg:px-8">
        <div className="mx-auto h-14 w-14 text-nova-purple nova-pulse">
          <NovaMark className="h-14 w-14 text-nova-purple" />
        </div>

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
          href="mailto:contacto@novamindflow.com?subject=Quiero%20conocer%20m%C3%A1s%20sobre%20NOVA"
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
