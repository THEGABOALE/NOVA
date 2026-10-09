import { NovaDoodle } from "../components/Decorations.jsx";
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

function Journey() {
  return (
    <section
      id={SECTIONS.aventura}
      className="mx-auto w-full max-w-[88rem] px-4 py-14 sm:px-6 lg:px-8 lg:py-16"
    >
      <div className="grid gap-8 lg:grid-cols-[0.8fr_1.2fr] lg:gap-14">
        <div>
          <p className="eyebrow text-nova-purple">
            Así se vive NOVA
          </p>

          <h2 className="font-display mt-4 text-5xl leading-none text-nova-ink sm:text-6xl lg:text-[4.2rem]">
            Una misión.
            <br />
            Un reto.
            <br />
            Algo nuevo que aprender.
          </h2>

          <div className="mt-8 h-24 w-full max-w-xs text-nova-purple/25">
            <NovaDoodle />
          </div>
        </div>

        <div className="divide-y-2 divide-nova-purple/10 border-y-2 border-nova-purple/10">
          {steps.map((step) => (
            <article
              key={step.number}
              className="grid gap-4 py-6 sm:grid-cols-[70px_0.8fr_1fr] sm:items-start sm:gap-6"
            >
              <span className="font-display text-4xl text-nova-purple">
                {step.number}
              </span>

              <h3 className="font-display text-xl text-nova-ink">
                {step.title}
              </h3>

              <p className="leading-7 text-nova-muted">
                {step.text}
              </p>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}

export default Journey;
