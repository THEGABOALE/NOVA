import { NovaDoodle, NovaSpark } from "./Decorations.jsx";
import NovaMark from "./NovaMark.jsx";

function LearningPreview() {
  const missions = [
    {
      number: "01",
      title: "Conocernos",
      status: "¡Listo!",
      active: false,
    },
    {
      number: "02",
      title: "Escuchar",
      status: "¡Listo!",
      active: false,
    },
    {
      number: "03",
      title: "Ponernos en su lugar",
      status: "Ahora",
      active: true,
    },
  ];

  return (
    <figure
      className="relative"
      aria-label="Ejemplo de una ruta de aprendizaje en NOVA"
    >
      <div className="absolute -left-8 -top-8 h-20 w-20 text-nova-purple/20 nova-float">
        <NovaSpark />
      </div>

      <div className="absolute -bottom-10 -right-6 h-28 w-28 text-nova-blue/15 nova-float-reverse">
        <NovaDoodle />
      </div>

      <div className="relative overflow-hidden rounded-[2.5rem] border-2 border-nova-purple/15 bg-white p-5 sm:p-6">
        <div className="flex items-start justify-between gap-5">
          <div>
            <p className="eyebrow text-nova-purple">
              Tu aventura
            </p>

            <p className="font-display mt-2 text-3xl text-nova-ink">
              Mi ruta de aprendizaje
            </p>
          </div>

          <span className="rounded-full bg-nova-purple/10 px-4 py-2 text-xs font-bold text-nova-purple">
            3 de 5
          </span>
        </div>

        <div className="mt-6 rounded-[1.8rem] bg-nova-purple/7 p-5">
          <div className="flex items-center justify-between gap-4 text-xs font-bold text-nova-purple">
            <span>Aprender para convivir</span>
            <span>60%</span>
          </div>

          <div className="mt-4 h-2 overflow-hidden rounded-full bg-nova-purple/15">
            <div className="h-full w-3/5 rounded-full bg-nova-blue" />
          </div>

          <div className="mt-5 grid gap-3 sm:grid-cols-3">
            {missions.map((mission) => (
              <article
                key={mission.number}
                className={`reveal-card rounded-[1.4rem] border-2 p-4 ${
                  mission.active
                    ? "border-nova-blue bg-nova-blue text-white"
                    : "border-nova-purple/10 bg-white text-nova-ink"
                }`}
              >
                <div className="flex items-center justify-between text-xs font-bold">
                  <span>{mission.number}</span>

                  <span
                    className={
                      mission.active
                        ? "text-white"
                        : "text-nova-purple"
                    }
                  >
                    {mission.status}
                  </span>
                </div>

                <p className="font-display mt-6 text-lg leading-tight">
                  {mission.title}
                </p>
              </article>
            ))}
          </div>
        </div>

        <div className="mt-4 flex items-center gap-4 rounded-[1.5rem] border-2 border-nova-purple/10 p-4">
          <NovaMark className="h-12 w-12 shrink-0 text-nova-purple" />

          <div>
            <p className="font-display text-lg text-nova-ink">
              ¡Nueva misión!
            </p>

            <p className="mt-1 text-sm text-nova-purple">
              Practica la empatía y descubre otra forma de mirar una situación.
            </p>
          </div>
        </div>
      </div>
    </figure>
  );
}

export default LearningPreview;
