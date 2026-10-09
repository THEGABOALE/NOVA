import { NovaSpark } from "../components/Decorations.jsx";
import NovaMark from "../components/NovaMark.jsx";
import { SECTIONS } from "../config/navigation.js";

function Zafiro() {
  return (
    <section
      id={SECTIONS.zafiro}
      className="relative overflow-hidden py-14 sm:py-16"
    >
      <div className="absolute right-6 top-8 h-24 w-24 text-nova-purple/15 nova-pulse">
        <NovaSpark />
      </div>

      <div className="mx-auto grid w-full max-w-[88rem] items-center gap-10 px-4 sm:px-6 lg:grid-cols-2 lg:gap-16 lg:px-8">
        <div>
          <p className="eyebrow text-nova-purple">
            Tu compañero de ruta
          </p>

          <h2 className="font-display mt-4 text-5xl leading-none text-nova-ink sm:text-6xl lg:text-[4.2rem]">
            No tienes que recorrer el camino solo.
          </h2>

          <p className="mt-6 max-w-2xl text-lg leading-8 text-nova-muted">
            Zafiro te acompaña durante las actividades, te orienta cuando
            aparece un nuevo reto y celebra contigo cada paso que completas.
          </p>
        </div>

        <div className="relative flex min-h-[320px] items-center justify-center rounded-[3rem] bg-nova-purple px-7 py-10 text-center text-white">
          <div className="absolute left-10 top-10 h-14 w-14 text-white/20 nova-float">
            <NovaSpark />
          </div>

          <div>
            <NovaMark className="mx-auto h-28 w-28 text-white" />

            <p className="font-display mt-6 text-3xl">
              ¡Vamos, puedes hacerlo!
            </p>

            <p className="mx-auto mt-3 max-w-sm text-white/75">
              Cada misión superada es una nueva oportunidad para aprender.
            </p>
          </div>
        </div>
      </div>
    </section>
  );
}

export default Zafiro;
