import { NovaSpark } from "../components/Decorations.jsx";
import { SECTIONS } from "../config/navigation.js";

const steps = [
  {
    number: "01",
    title: "Entra a tu ruta",
    text: "Descubre misiones preparadas para tu nivel y empieza a avanzar.",
  },
  {
    number: "02",
    title: "Acepta el reto",
    text: "Responde, relaciona, decide y pon a prueba lo que sabes.",
  },
  {
    number: "03",
    title: "Aprende jugando",
    text: "Cada actividad te ayuda a comprender situaciones que también pasan en la vida real.",
  },
  {
    number: "04",
    title: "Mira cuánto avanzaste",
    text: "Completa misiones, desbloquea nuevos retos y observa tu progreso.",
  },
];

// Recorrido en escritorio: una S de dos filas. El DOM sigue en orden 01 → 04;
// 03 y 04 solo se colocan de derecha a izquierda en la segunda fila. Cada paso
// dibuja el tramo de línea que pasa por su nodo (a 1.375rem del borde, el
// centro del nodo); los tramos cruzan el espacio entre columnas (4rem) para
// unirse con el del paso vecino.
const desktopTrack = [
  { place: "", line: "left-[1.375rem] -right-16" },
  { place: "", line: "left-0 right-0", curve: true },
  { place: "lg:col-start-2 lg:row-start-2", line: "-left-16 right-0" },
  { place: "lg:col-start-1 lg:row-start-2", line: "left-[1.375rem] right-0" },
];

function StepNode({ last }) {
  if (last) {
    return (
      <span
        aria-hidden="true"
        className="relative z-10 flex h-11 w-11 items-center justify-center rounded-full bg-nova-blue text-white"
      >
        <NovaSpark className="h-6 w-6" />
      </span>
    );
  }

  return (
    <span
      aria-hidden="true"
      className="relative z-10 flex h-11 w-11 items-center justify-center"
    >
      <span className="h-6 w-6 rounded-full border-4 border-nova-purple bg-nova-mist" />
    </span>
  );
}

function Journey() {
  const lastIndex = steps.length - 1;

  return (
    <section
      id={SECTIONS.aventura}
      className="mx-auto w-full max-w-[88rem] px-4 py-14 sm:px-6 lg:px-8 lg:py-16"
    >
      <div className="lg:text-center">
        <p className="eyebrow text-nova-purple">
          Así se vive NOVA
        </p>

        <h2 className="font-display mt-4 text-5xl leading-none text-nova-ink sm:text-6xl lg:text-[4.2rem]">
          Una misión.
          <br className="lg:hidden" /> Un reto.
          <br />
          Algo nuevo que aprender.
        </h2>
      </div>

      <p className="eyebrow mt-12 text-nova-purple lg:mt-16">
        Comienza
      </p>

      {/* Teléfono y tablet: línea vertical con los nodos a la izquierda.
          Escritorio (lg): la S, con 4rem a la derecha para la curva. */}
      <ol className="mt-4 grid gap-y-10 lg:grid-cols-2 lg:gap-x-16 lg:gap-y-16 lg:pr-16">
        {steps.map((step, index) => {
          const last = index === lastIndex;
          const track = desktopTrack[index];

          return (
            <li
              key={step.number}
              className={`relative grid grid-cols-[2.75rem_1fr] gap-x-4 lg:block ${track.place}`}
            >
              {!last && (
                <span
                  aria-hidden="true"
                  className="absolute left-5 top-[1.375rem] h-[calc(100%+2.5rem)] w-1 rounded-full bg-nova-purple lg:hidden"
                />
              )}

              <span
                aria-hidden="true"
                className={`absolute top-5 hidden h-1 bg-nova-purple lg:block ${track.line}`}
              />

              {/* La curva baja desde el final de la primera fila hasta la
                  línea de la segunda: mide el alto del paso 02 más el espacio
                  entre filas (4rem) y el grosor del borde. */}
              {track.curve && (
                <span
                  aria-hidden="true"
                  className="absolute left-full top-5 hidden h-[calc(100%+4rem+4px)] w-16 rounded-r-full border-4 border-l-0 border-nova-purple lg:block"
                />
              )}

              <StepNode last={last} />

              <div className="pt-2.5 lg:max-w-sm lg:pt-0 lg:mt-5">
                <span className="font-display text-lg text-nova-purple">
                  {step.number}
                </span>

                <h3 className="font-display mt-2 text-2xl text-nova-ink">
                  {step.title}
                </h3>

                <p className="mt-2 leading-7 text-nova-muted">
                  {step.text}
                </p>
              </div>
            </li>
          );
        })}
      </ol>

      <p className="eyebrow mt-10 flex items-center gap-2 text-nova-blue lg:mt-14">
        Sigue avanzando
        <NovaSpark className="h-4 w-4" />
      </p>
    </section>
  );
}

export default Journey;
