// Antes del build de producción: descarga los webfonts con licencia, que no
// pueden versionarse en este repositorio público, desde un bucket privado de
// R2. Hoy es solo Agenor Neue Regular (licencia Webfonts de MyFonts para
// novamindflow.com).
//
// - Workers Builds en `main` (producción): descarga obligatoria. Si falta un
//   secreto o la descarga falla, el build se detiene, para no publicar sin la
//   fuente sin darse cuenta.
// - Workers Builds en otra rama (previews): no descarga; se ve la fuente del
//   sistema. La licencia es para un solo dominio.
// - Fuera de Workers Builds (GitHub Actions, en local): no hace nada. En local
//   se puede copiar el archivo a mano a public/fonts/ para verlo con `npm run dev`.
import { spawnSync } from "node:child_process";
import { mkdirSync, readFileSync, rmSync, statSync } from "node:fs";
import { dirname } from "node:path";
import { fileURLToPath } from "node:url";

const OBJECT = "nova-licensed-assets/fonts/agenor-neue-regular.woff2";
const destination = fileURLToPath(new URL("../public/fonts/agenor-neue-regular.woff2", import.meta.url));
// Se llama a Wrangler con Node y sin shell, para que una ruta con espacios no
// se parta en varios argumentos.
const wrangler = fileURLToPath(new URL("../node_modules/wrangler/bin/wrangler.js", import.meta.url));
const PRODUCTION_BRANCH = "main";

function fail(message) {
  // Si Wrangler dejó un archivo a medias, no debe llegar al build.
  rmSync(destination, { force: true });
  console.error(`prepare:licensed-assets: ${message}`);
  process.exit(1);
}

if (process.env.WORKERS_CI !== "1") {
  console.log("prepare:licensed-assets: fuera de Workers Builds, no se descarga nada.");
  process.exit(0);
}

const branch = process.env.WORKERS_CI_BRANCH;

if (branch !== PRODUCTION_BRANCH) {
  console.log(`prepare:licensed-assets: rama "${branch}" (preview), se usa la fuente del sistema.`);
  process.exit(0);
}

const missing = ["R2_READ_API_TOKEN", "CLOUDFLARE_ACCOUNT_ID"].filter((name) => !process.env[name]);

if (missing.length > 0) {
  fail(`faltan los secretos de build ${missing.join(" y ")}; no se puede publicar producción sin Agenor Neue.`);
}

mkdirSync(dirname(destination), { recursive: true });

// Wrangler se autentica con CLOUDFLARE_API_TOKEN; el token de solo lectura de
// R2 se le pasa solo a este proceso, sin tocar el que usa el deploy.
const result = spawnSync(
  process.execPath,
  [wrangler, "r2", "object", "get", OBJECT, "--file", destination, "--remote"],
  {
    stdio: "inherit",
    env: {
      ...process.env,
      CLOUDFLARE_API_TOKEN: process.env.R2_READ_API_TOKEN,
      CLOUDFLARE_ACCOUNT_ID: process.env.CLOUDFLARE_ACCOUNT_ID,
    },
  },
);

if (result.status !== 0) {
  fail(`no se pudo descargar ${OBJECT} desde R2.`);
}

// Un WOFF2 válido empieza con la firma "wOF2".
const { size } = statSync(destination);
const signature = readFileSync(destination).subarray(0, 4).toString("latin1");

if (signature !== "wOF2" || size < 1024) {
  fail(`el archivo descargado no parece un WOFF2 válido (${size} bytes).`);
}

console.log(`prepare:licensed-assets: Agenor Neue lista (${Math.round(size / 1024)} KB).`);
