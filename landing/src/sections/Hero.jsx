import Button from "../components/Button.jsx";
import { SECTIONS } from "../config/navigation.js";
import { NovaSpark } from "../components/Decorations.jsx";
import LearningPreview from "../components/LearningPreview.jsx";

function Hero() {
  return (
    <section id={SECTIONS.inicio} className="relative overflow-hidden">
      <div className="absolute right-4 top-12 h-20 w-20 text-nova-purple/15 nova-float">
        <NovaSpark />
      </div>

      <div className="mx-auto grid w-full max-w-[88rem] items-center gap-10 px-4 py-12 sm:px-6 lg:grid-cols-2 lg:gap-14 lg:px-8 lg:py-16">
        <div>
          <p className="eyebrow mb-5 text-nova-purple">
            Aprende · juega · descubre
          </p>

          <h1 className="font-display max-w-3xl text-[clamp(2.5rem,11vw,3.75rem)] leading-[0.96] text-nova-ink md:text-7xl xl:text-[5.5rem]">
            Cada reto puede enseñarte{" "}
            <span className="text-nova-purple">
              algo para la vida.
            </span>
          </h1>

          <p className="mt-6 max-w-2xl text-lg leading-8 text-nova-body sm:text-[1.3rem]">
            Explora misiones, supera desafíos y aprende sobre igualdad,
            respeto y dignidad mientras avanzas junto a Zafiro. Para
            estudiantes de primaria alta y secundaria.
          </p>

          <div className="mt-8 flex flex-col items-start gap-4 sm:flex-row">
            <Button href={`#${SECTIONS.aventura}`}>
              Comenzar la aventura
            </Button>

            <Button href={`#${SECTIONS.descubre}`} variant="secondary">
              Descubrir NOVA
            </Button>
          </div>

          <div className="mt-7 flex items-center gap-3">
            <span
              aria-hidden="true"
              className="flex h-10 w-10 items-center justify-center rounded-full bg-nova-blue text-white"
            >
              ✦
            </span>

            <p className="text-sm font-bold text-nova-purple">
              Una forma diferente de aprender, pensar y participar.
            </p>
          </div>
        </div>

        <LearningPreview />
      </div>
    </section>
  );
}

export default Hero;
