// Después de `vite build`: genera con React el HTML de la portada y de la
// página 404 y lo deja en dist/. Así los buscadores y las vistas previas de
// WhatsApp, LinkedIn o Discord ven el contenido sin ejecutar JavaScript, y la
// primera pintura no espera a que cargue el bundle.
import { readFile, rm, writeFile } from "node:fs/promises";

const dist = new URL("../dist/", import.meta.url);
const ssr = new URL("../dist-ssr/", import.meta.url);

const { renderHome, renderNotFound } = await import(new URL("entry-server.js", ssr));

const template = await readFile(new URL("index.html", dist), "utf8");

const ROOT = '<div id="root"></div>';
const SEO = /<!-- seo:inicio[\s\S]*?<!-- seo:fin -->/;
const MODULE_SCRIPTS = /\s*<(script type="module"|link rel="modulepreload")[^>]*>(<\/script>)?/g;

if (!template.includes(ROOT) || !SEO.test(template)) {
  throw new Error("dist/index.html no tiene el contenedor #root o el bloque seo:inicio/seo:fin");
}

// Portada: el HTML dentro de #root; React lo hidrata en el navegador.
await writeFile(new URL("index.html", dist), template.replace(ROOT, `<div id="root">${renderHome()}</div>`));

// 404: título propio, noindex y sin canonical ni vista previa. No necesita
// JavaScript, así que no carga el bundle.
const notFound = template
  .replace(SEO, '<title>Página no encontrada | NOVA</title>\n    <meta name="robots" content="noindex" />')
  .replace(MODULE_SCRIPTS, "")
  .replace(ROOT, `<div id="root">${renderNotFound()}</div>`);
await writeFile(new URL("404.html", dist), notFound);

await rm(ssr, { recursive: true, force: true });
console.log("prerender: dist/index.html y dist/404.html listos");
