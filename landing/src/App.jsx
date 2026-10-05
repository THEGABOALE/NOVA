import Navbar from "./components/Navbar.jsx";
import FinalCTA from "./sections/FinalCTA.jsx";
import Features from "./sections/Features.jsx";
import Footer from "./sections/Footer.jsx";
import Hero from "./sections/Hero.jsx";
import Journey from "./sections/Journey.jsx";
import Zafiro from "./sections/Zafiro.jsx";

function App() {
  return (
    <div className="min-h-screen bg-nova-mist text-nova-ink">
      <a
        href="#contenido"
        className="sr-only rounded-full bg-white px-5 py-3 font-bold text-nova-purple focus:not-sr-only focus:fixed focus:left-4 focus:top-4 focus:z-[60]"
      >
        Saltar al contenido
      </a>

      <Navbar />

      <main id="contenido">
        <Hero />
        <Journey />
        <Features />
        <Zafiro />
        <FinalCTA />
      </main>

      <Footer />
    </div>
  );
}

export default App;
