# NOVA — Landing page

Página pública de NOVA, hecha con React 19, Vite 8 y Tailwind CSS 4, y publicada en Cloudflare Workers como sitio estático (Workers Static Assets).

El build prerenderiza la portada, así que el HTML llega con el contenido y React solo lo hidrata en el navegador. También genera una página 404 estática real para cualquier ruta que no existe.

## Desarrollo

```bash
cd landing
npm install
npm run dev
```

Queda disponible en `http://localhost:5173`.

La tipografía de texto, Agenor Neue, es un webfont con licencia que no forma parte del repositorio (ver [Tipografía](#tipografía)). Sin ese archivo, la página se ve con la fuente del sistema, que es su respaldo; todo lo demás funciona igual.

## Scripts

| Comando | Qué hace |
| --- | --- |
| `npm run dev` | Servidor de desarrollo con recarga en caliente |
| `npm run build` | Build del cliente con Vite, build de servidor (SSR) y prerender: deja en `dist/` la portada (`index.html`) ya renderizada y la página `404.html` |
| `npm run preview` | Sirve `dist/` para revisarlo antes de publicar |
| `npm run lint` | Revisa el código con oxlint |
| `npm run test` | Corre los tests una vez |
| `npm run test:watch` | Corre los tests y los repite al guardar cambios |
| `npm run prepare:licensed-assets` | En el build de producción, descarga el webfont de Agenor Neue desde un bucket privado de R2. En cualquier otro contexto no hace nada |

`prepare:licensed-assets` (`scripts/prepare-licensed-assets.mjs`) solo descarga la fuente en el build de Cloudflare de la rama `main`. Los previews, CI y el desarrollo local no la necesitan. El archivo nunca se versiona en Git. En producción, si no puede descargarse (porque falta la configuración del build o porque la descarga falla), el build se detiene en vez de publicar la página sin la fuente.

## Tests

Tests de componentes con Vitest, jsdom, `@testing-library/react`, `@testing-library/user-event` y `@testing-library/jest-dom`. Viven junto al código (`*.test.jsx`, `*.test.js`). La configuración está en `vitest.config.js`, y `src/test/setup.js` carga jest-dom, limpia el DOM entre tests y simula `window.matchMedia`.

Prueban comportamiento y semántica, no clases ni estilos, para que sobrevivan a cambios de diseño. Hoy cubren:

- **Navbar:** abrir y cerrar el menú móvil; cerrarlo con Escape devolviendo el foco al botón, al elegir un enlace y al pasar a tamaño de escritorio.
- **Navegación (`src/config/navigation.js`):** el correo de contacto, el `mailto` con el asunto codificado y que el menú apunte a secciones que existen.
- **Journey:** las cuatro etapas como lista ordenada, en orden, con sus hitos, sin botones ni enlaces.
- **NotFound:** el título y el enlace de vuelta al inicio.
- **App:** el enlace "Saltar al contenido" apunta al `<main>`.

```bash
npm run test
npm run test:watch
```

No hay medición de cobertura.

## CI

`.github/workflows/ci.yml` valida el backend, la app móvil y la landing en cada push y PR hacia `main`. El job de la landing:

1. comprueba que no haya ningún archivo versionado en `landing/public/fonts/` (los webfonts con licencia no pueden estar en Git);
2. `npm ci`;
3. `npm run test`;
4. `npm run lint`;
5. `npm run build`.

CI no tiene acceso a la fuente con licencia: el build corre con la fuente de respaldo.

## Prerender y página 404

`npm run build` termina con `scripts/prerender.js`, que usa `src/entry-server.jsx` para generar:

- `dist/index.html`: la portada con el contenido ya renderizado. Buscadores y vistas previas al compartir la ven sin ejecutar JavaScript.
- `dist/404.html`: página "No encontramos esta página", con `noindex` y sin JavaScript.

`wrangler.jsonc` usa `"not_found_handling": "404-page"`, así que una ruta que no existe responde con estado 404 y esa página, en lugar de devolver la portada con 200 (lo que los buscadores marcan como "soft 404").

## SEO

`index.html` incluye título, meta descripción, canonical, `meta robots`, Open Graph, Twitter Card (con la imagen `public/og-image.png`) y datos estructurados JSON-LD (`WebSite`). `public/robots.txt` permite todo y apunta a `public/sitemap.xml`, que lista solo la portada porque las secciones son anclas, no páginas aparte.

## Tipografía

- **Chillax** (títulos y eyebrows): se carga desde Fontshare, con su hoja de estilos oficial en `index.html`.
- **Agenor Neue Regular** (texto): con licencia oficial Webfonts de MyFonts/Monotype para `novamindflow.com`.

Las familias se definen una sola vez en `src/index.css`, como `--font-display` y `--font-body`, con la fuente del sistema como respaldo. Ahí también está el `@font-face` de Agenor Neue, junto al aviso de licencia del kit de MyFonts.

### Webfont con licencia

Agenor Neue Regular no forma parte del repositorio público. Su licencia permite servirla en el sitio, pero no redistribuir el archivo. El WOFF2 oficial se guarda en un bucket privado de Cloudflare R2 y solo se incorpora durante el build de producción:

```txt
MyFonts/Monotype → WOFF2 oficial → R2 privado
→ npm run prepare:licensed-assets   (solo en producción)
→ public/fonts/ (temporal) → Vite → dist/fonts/ → novamindflow.com
```

- `landing/.gitignore` excluye `public/fonts/`, y CI falla si algún archivo de esa carpeta queda versionado (por ejemplo con `git add -f`).
- **No se debe agregar al repositorio** ningún `.woff`, `.woff2`, `.ttf` ni `.otf` en `public/fonts/`.
- El build de producción falla si no puede descargar la fuente; los previews y CI usan la fuente de respaldo.
- En producción, `/fonts/*` se sirve con `Cross-Origin-Resource-Policy: same-origin` y sin cabeceras CORS, así que otros sitios no pueden usar la fuente.

## Marca

- **Colores oficiales:** morado `#82368c` y azul `#1600b5`. Se definen en `src/index.css` (`@theme`) y se usan como clases de Tailwind (`text-nova-purple`, `bg-nova-blue/10`, etc.), igual que en la app móvil. Los demás colores de ese bloque (`nova-ink`, `nova-body`, `nova-muted`, `nova-mist`) son tokens de interfaz, no colores de marca.
- **Imágenes:** los originales del logo y del ícono están en `design/` (no se publican). Si cambian, se exportan con fondo transparente y al tamaño en que se muestran a `src/assets/`.

## Despliegue en Cloudflare

Se publica con Workers Builds, conectado al repositorio:

| Ajuste | Valor |
| --- | --- |
| Root directory | `/landing` |
| Build command | `npm run prepare:licensed-assets && npm run build` |
| Deploy command | `npx wrangler deploy` |

La descarga de la fuente usa un secreto de build con acceso de solo lectura al bucket y la configuración de la cuenta, definidos en los ajustes del proyecto en Cloudflare, nunca en el repositorio.

`wrangler.jsonc` publica `dist/` y define:

- **Producción:** solo en los dominios propios `novamindflow.com` y `www.novamindflow.com` (`workers_dev: false`).
- **Previews:** cada rama distinta de `main` se publica en una URL de `workers.dev` (`preview_urls: true`). Los previews no descargan Agenor Neue y se ven con la fuente de respaldo.

## Cabeceras y seguridad

`public/_headers` define para todo el sitio `Content-Security-Policy` (solo recursos propios, Fontshare y Cloudflare Web Analytics), `X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy` y `Permissions-Policy`. Además:

- `/assets/*`: caché de un año, `immutable` (Vite pone un hash en cada nombre).
- `/fonts/*`: caché de 30 días y `Cross-Origin-Resource-Policy: same-origin`.

## Estructura

```txt
landing/
├─ design/            Originales del logo y del ícono (no se publican)
├─ public/            Se sirve tal cual: _headers, favicon.png, apple-touch-icon.png,
│                     og-image.png, robots.txt, sitemap.xml
├─ scripts/
│  ├─ prerender.js                  Genera dist/index.html y dist/404.html
│  └─ prepare-licensed-assets.mjs   Descarga el webfont con licencia (producción)
├─ src/
│  ├─ assets/         Logo e ícono optimizados (WebP)
│  ├─ components/     Navbar, Button, NovaLogo, NovaMark, LearningPreview, decoraciones
│  ├─ config/         navigation.js: secciones, menú y contacto
│  ├─ sections/       Hero, Journey, Features, Zafiro, FinalCTA, Footer
│  ├─ test/           setup.js de los tests
│  ├─ App.jsx         Arma la portada
│  ├─ NotFound.jsx    Página 404
│  ├─ entry-server.jsx  Entrada del prerender
│  ├─ index.css       Colores, tipografía y estilos globales
│  └─ main.jsx        Punto de entrada en el navegador (hidrata o renderiza)
├─ index.html         Metadatos SEO y de vista previa
├─ package.json
├─ vite.config.js
├─ vitest.config.js
└─ wrangler.jsonc     Configuración de Cloudflare
```

Los tests están junto a cada componente (`*.test.jsx`).
