import { SECTIONS } from "../config/navigation.js";

const features = [
  {
    number: "01",
    title: "Misiones",
    text: "Pequeños desafíos para avanzar paso a paso.",
  },
  {
    number: "02",
    title: "Retos interactivos",
    text: "Actividades diferentes para aprender haciendo.",
  },
  {
    number: "03",
    title: "Niveles",
    text: "Una ruta adaptada a cada etapa de aprendizaje.",
  },
  {
    number: "04",
    title: "Recompensas",
    text: "Celebra tus logros mientras completas la experiencia.",
  },
];

function Features() {
  return (
    <section id={SECTIONS.descubre} className="py-14 sm:py-16">
      <div className="mx-auto w-full max-w-[88rem] px-4 sm:px-6 lg:px-8">
        <div className="max-w-4xl">
          <p className="eyebrow text-nova-purple">
            Descubre mientras avanzas
          </p>

          <h2 className="font-display mt-4 text-5xl leading-none text-nova-ink sm:text-6xl lg:text-[4.2rem]">
            Aprender puede sentirse como superar una misión.
          </h2>

          <p className="mt-5 max-w-3xl text-lg leading-8 text-nova-muted">
            En NOVA cada actividad tiene un propósito: pensar, elegir,
            comprender y descubrir cómo nuestras decisiones también pueden
            transformar lo que nos rodea.
          </p>
        </div>

        <div className="mt-10 grid overflow-hidden rounded-[2rem] border-2 border-nova-purple/10 sm:grid-cols-2">
          {features.map((feature) => (
            <article
              key={feature.number}
              className="reveal-card border-b-2 border-nova-purple/10 p-6 last:border-b-0 sm:border-b-0 sm:p-7 sm:odd:border-r-2 sm:[&:nth-child(-n+2)]:border-b-2"
            >
              <span className="font-display text-3xl text-nova-purple/75">
                {feature.number}
              </span>

              <h3 className="font-display mt-5 text-2xl text-nova-ink">
                {feature.title}
              </h3>

              <p className="mt-3 leading-7 text-nova-muted">
                {feature.text}
              </p>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}

export default Features;
