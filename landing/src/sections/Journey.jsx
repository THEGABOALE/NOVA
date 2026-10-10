import { NovaSpark } from "../components/Decorations.jsx";
import { SECTIONS } from "../config/navigation.js";

// Cada paso trae sus hitos: lo que pasa dentro de esa etapa, tomado de su
// propia descripción. En el recorrido van entre ese paso y el siguiente. La
// descripción (`text`) queda en los datos, pero no se muestra ni se lee: los
// hitos ya la resumen.
const steps = [
  {
    number: "01",
    title: "Empieza tu ruta",
    text: "Descubre misiones preparadas para tu nivel y empieza a avanzar.",
    checkpoints: ["Descubre misiones", "Para tu nivel", "Empieza a avanzar"],
  },
  {
    number: "02",
    title: "Acepta el reto",
    text: "Responde, relaciona, decide y pon a prueba lo que sabes.",
    checkpoints: ["Responde", "Relaciona ideas", "Toma decisiones"],
  },
  {
    number: "03",
    title: "Aprende jugando",
    text: "Cada actividad te ayuda a comprender situaciones que también pasan en la vida real.",
    checkpoints: ["Explora situaciones", "Comprende", "Conecta con la vida real"],
  },
  {
    number: "04",
    title: "Mira cuánto avanzaste",
    text: "Completa misiones, desbloquea nuevos retos y observa tu progreso.",
    checkpoints: ["Completa misiones", "Desbloquea retos", "Observa tu progreso"],
  },
];

// El color avanza con el recorrido: morado claro del 01 al 02, morado del 02
// al 04 y azul del 04 a la estrella final. Cada tramo (línea, hitos y curva)
// es del color del paso donde empieza.
const tones = [
  { line: "bg-nova-purple/35", border: "border-nova-purple/35", node: "border-nova-purple" },
  { line: "bg-nova-purple", border: "border-nova-purple", node: "border-nova-purple" },
  { line: "bg-nova-purple", border: "border-nova-purple", node: "border-nova-purple" },
  { line: "bg-nova-blue", border: "border-nova-blue", node: "border-nova-blue" },
];

function EndStar({ className = "" }) {
  return (
    <span
      aria-hidden="true"
      className={`z-10 h-11 w-11 items-center justify-center rounded-full bg-nova-blue text-white ${className}`}
    >
      <NovaSpark className="h-6 w-6" />
    </span>
  );
}

// Escritorio (lg): una serpiente de cuatro filas, una por paso. Las filas pares
// van de izquierda a derecha y las impares al revés; el orden del DOM no
// cambia. Cada fila tiene su nodo al empezar, sus hitos y, salvo la última, una
// curva hacia la fila siguiente por el lado donde termina. La línea va a la
// altura del centro del nodo (2.75rem de etiquetas + 1.375rem) y deja 1.375rem en
// cada punta: ahí está el centro del nodo, de la estrella o el comienzo de la
// curva, así los tramos no se pisan (con el morado claro se notaría). La curva
// mide el alto de la fila más el espacio entre filas (1.25rem) y el grosor del
// borde, así cae en la línea de abajo. El `<ol>` deja 4rem a cada lado para
// las curvas y, a la izquierda de la primera fila, para el punto de inicio.
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

      {/* El recorrido arranca en "Comenzar", un punto pequeño antes del 01:
          arriba de la línea en el teléfono y a la izquierda en escritorio. */}
      <div className="relative mt-12 lg:mt-16">
        <p className="grid h-6 grid-cols-[2.75rem_1fr] items-center gap-x-4 lg:absolute lg:left-0 lg:top-7 lg:block lg:h-auto">
          <span
            aria-hidden="true"
            className="mx-auto h-3.5 w-3.5 rounded-full bg-nova-purple lg:hidden"
          />

          <span className="eyebrow text-nova-purple">
            Comenzar
          </span>
        </p>

        <ol className="mt-6 grid gap-y-10 lg:mt-0 lg:gap-y-5 lg:px-16">
          {steps.map((step, index) => {
            const tone = tones[index];
            const reverse = index % 2 === 1;
            const last = index === lastIndex;

            return (
              <li
                key={step.number}
                className={`relative grid grid-cols-[2.75rem_1fr] gap-x-4 lg:gap-x-0 lg:pt-11 ${
                  reverse ? "lg:grid-cols-[1fr_2.75rem]" : ""
                }`}
              >
                {/* Teléfono: línea vertical del nodo de este paso al siguiente;
                    en el último, hasta la estrella final. */}
                <span
                  aria-hidden="true"
                  className={`absolute left-5 top-[1.375rem] w-1 rounded-full lg:hidden ${tone.line} ${
                    last ? "bottom-[1.375rem]" : "h-[calc(100%+2.5rem)]"
                  }`}
                />

                <span
                  aria-hidden="true"
                  className={`absolute left-[1.375rem] right-[1.375rem] top-[4rem] hidden h-1 lg:block ${tone.line}`}
                />

                {/* Tramo desde el punto de "Comenzar" hasta el 01: en el
                    teléfono baja desde arriba; en escritorio llega desde 3rem a
                    la izquierda. */}
                {index === 0 && (
                  <>
                    <span
                      aria-hidden="true"
                      className={`absolute -top-9 left-5 h-[3.625rem] w-1 lg:hidden ${tone.line}`}
                    />

                    <span
                      aria-hidden="true"
                      className={`absolute -left-12 top-[4rem] hidden h-1 w-[4.375rem] lg:block ${tone.line}`}
                    />

                    <span
                      aria-hidden="true"
                      className="absolute left-[calc(-3rem-7px)] top-[calc(4.125rem-7px)] z-10 hidden h-3.5 w-3.5 rounded-full bg-nova-purple lg:block"
                    />
                  </>
                )}

                {!last && (
                  <span
                    aria-hidden="true"
                    className={`absolute top-[4rem] hidden h-[calc(100%+1.25rem+4px)] w-[5.375rem] border-4 lg:block ${tone.border} ${
                      reverse
                        ? "right-[calc(100%-1.375rem)] rounded-l-[3.5rem] border-r-0"
                        : "left-[calc(100%-1.375rem)] rounded-r-[3.5rem] border-l-0"
                    }`}
                  />
                )}

                {last && <EndStar className="absolute left-0 top-11 hidden lg:flex" />}

                <span
                  aria-hidden="true"
                  className={`relative z-10 flex h-11 w-11 items-center justify-center lg:row-start-1 ${
                    reverse ? "lg:col-start-2" : "lg:col-start-1"
                  }`}
                >
                  <span className={`h-6 w-6 rounded-full border-4 bg-nova-mist ${tone.node}`} />
                </span>

                <div
                  className={`pt-3 lg:col-span-2 lg:col-start-1 lg:row-start-2 lg:mt-1 lg:pt-0 ${
                    reverse ? "lg:justify-self-end lg:text-right" : "lg:justify-self-start"
                  }`}
                >
                  <span className="font-display block text-lg leading-none text-nova-purple">
                    {step.number}
                  </span>

                  <h3 className="font-display mt-1.5 text-2xl text-nova-ink">
                    {step.title}
                  </h3>
                </div>

                <ul
                  className={`col-span-2 mt-5 grid gap-3 lg:col-span-1 lg:row-start-1 lg:mt-0 lg:flex lg:justify-around ${
                    reverse ? "lg:col-start-1 lg:flex-row-reverse" : "lg:col-start-2"
                  } ${last ? "lg:pl-11" : ""}`}
                >
                  {step.checkpoints.map((checkpoint) => (
                    <li
                      key={checkpoint}
                      className="grid grid-cols-[2.75rem_1fr] items-center gap-x-4 lg:relative lg:flex lg:h-11 lg:w-3.5 lg:justify-center"
                    >
                      <span
                        aria-hidden="true"
                        className={`relative z-10 mx-auto h-3.5 w-3.5 rounded-full border-[3px] bg-nova-mist ${tone.border}`}
                      />

                      <span className="text-sm text-nova-body lg:absolute lg:bottom-full lg:left-1/2 lg:mb-1 lg:w-36 lg:-translate-x-1/2 lg:text-center lg:text-[0.9375rem] lg:font-medium lg:leading-5">
                        {checkpoint}
                      </span>
                    </li>
                  ))}
                </ul>

                {last && (
                  <p className="col-span-2 mt-5 grid grid-cols-[2.75rem_1fr] items-center gap-x-4 lg:col-span-1 lg:col-start-1 lg:row-start-2 lg:mt-3 lg:flex lg:items-center lg:gap-2 lg:justify-self-start">
                    <EndStar className="flex lg:hidden" />

                    <span className="eyebrow flex items-center gap-2 text-nova-blue">
                      Sigue avanzando
                    </span>
                  </p>
                )}
              </li>
            );
          })}
        </ol>
      </div>
    </section>
  );
}

export default Journey;
