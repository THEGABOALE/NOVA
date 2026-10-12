# Preparación de producción de NOVA mobile

Base de esta revisión: `main` en `12e1183`. Este cambio conserva el flujo
Room → cola local → `POST /api/sync/attempts` → recalificación del servidor.
No introduce el protocolo online start/finish en el aprendizaje cotidiano.

## Riesgos tratados en código

- Release nunca usa la firma debug. `preReleaseBuild` valida HTTPS y exige
  credenciales del keystore, excepto cuando se solicita explícitamente una
  compilación **sin firma** para CI. El verificador rechaza Android Debug.
- Release activa R8 y reducción de recursos, con reglas para DTOs Gson.
- HTTP al emulador solo existe en `src/debug`; release prohíbe cleartext.
- Caché de misiones por `(userId, missionId)`, peticiones con token fijado a
  esa cuenta y descarte de respuestas que llegan después de cambiar sesión.
  Un 401/403/404 explícito nunca se sustituye por caché.
- Room v4 descarta únicamente contenido antiguo sin dueño verificable.
  Conserva pendientes, avisos y progreso. Tras actualizar desde v3 se necesita
  conexión para redescargar misiones; no se inventa dueño para la caché vieja.
  Logout elimina caché y contenido
  activo de la cuenta en una transacción; no elimina resultados pendientes.
- El intento conserva ID, hora de inicio/fin, pausas, potenciador, pregunta,
  respuestas, selección, plumas y resultado ante recreación de Android.
  Room guarda el contenido exacto del intento; los Bundles solo guardan estado
  pequeño. Restaurar sin contenido disponible muestra error, no mezcla
  respuestas antiguas con preguntas nuevas. Un error de guardado reintenta el
  mismo intento y sus respuestas.
- El mapa bloquea mecánicas desconocidas. No salta requisitos secuenciales.
  El contenido se valida antes de abrir: verdadero/falso completo, selección
  con una sola correcta y matching con una pregunta con pares.
- Docentes consumen `GET /api/teacher/me/students`: salas, roster, semillas,
  misiones y promedio real por mecánica. Loading, error y lista vacía son
  explícitos. No se cachea el roster y no quedan datos mock de producción.
- Se elimina portrait lock. El contenido tiene ancho máximo de 720 dp y
  listas/áreas de preguntas desplazables. Los controles del login tienen
  mínimo 48 dp. Esto necesita además prueba visual en dispositivos reales.
- CI incorpora lint, JVM, APK debug, bundle release sin firma y compilación
  de pruebas Android. Las pruebas instrumentadas cubren estado guardado,
  contrato UI docente y migraciones reales en teléfono y tablet horizontal.
- Se exporta el esquema Room actual. Como v1-v3 no exportaban esquema, las
  pruebas recrean su DDL histórico y Room valida la migración al abrir v4.

## Activación de firma (requisito externo pendiente)

Crear un keystore de subida de producción y conservar su respaldo privado.
Registrar en el environment GitHub `mobile-production`:

| Tipo | Nombre | Uso |
| --- | --- | --- |
| Secret | `NOVA_KEYSTORE_BASE64` | Keystore codificado para el workflow manual |
| Secret | `NOVA_KEYSTORE_PASSWORD` | Contraseña del archivo |
| Secret | `NOVA_KEY_ALIAS` | Alias del certificado |
| Secret | `NOVA_KEY_PASSWORD` | Contraseña de la clave |
| Variable | `NOVA_API_BASE_URL` | URL HTTPS del backend, terminada en `/` |
| Variable | `NOVA_SENTRY_DSN` | Opcional; activa diagnóstico en release |

Para compilar localmente usar `NOVA_KEYSTORE_PATH` en vez de BASE64.
No guardar keystore ni contraseñas en el repo ni en mensajes. Los archivos
`.jks`, `.keystore` y propiedades de firma están ignorados.

El workflow **Mobile signed release** solo admite main. Crea un AAB firmado
como artifact, elimina el keystore temporal y no publica en Play Store.
Actualizar `versionCode` para cada subida a Play y registrar la huella de
firma correcta en el cliente Android de Google Auth. La firma de subida y
la firma de distribución de Play pueden ser distintas; Google necesita
la correspondiente a la app instalada.

Validación de CI sin secretos:

```bash
./gradlew lintDebug testDebugUnitTest assembleDebug bundleRelease assembleDebugAndroidTest -PNOVA_ALLOW_UNSIGNED_RELEASE=true
./gradlew connectedDebugAndroidTest
```

Un bundle sin firma sirve para validar la compilación; no es un release
listo para distribuir. No configurar una firma debug para evitar el error.

## Diagnóstico y métricas (activación externa pendiente)

Sentry queda desactivado en debug y cuando `NOVA_SENTRY_DSN` está vacío.
No se crearon cuentas ni proyectos externos. Antes de activarlo, configurar
el proyecto para impedir almacenamiento de IP, limitar retención y accesos,
y completar la política de privacidad del producto.

La integración desactiva screenshots, jerarquía de vistas, breadcrumbs,
tracing, sesiones automáticas y persistencia de scope. El filtro elimina
usuario, request, extras, tags y mensajes de excepciones. Solo admite como
texto los seis eventos cerrados de `ProductEvent`; no recibe parámetros,
identificadores, respuestas, nombres ni correos. Conserva tipos y frames
para diagnóstico. Los eventos no sirven para medir retención por usuario;
permiten contar inicios, finales, abandonos y problemas de sincronización.

La compilación firmada usa `--no-configuration-cache` para no conservar la
configuración de firma con credenciales. Usar lo mismo en builds firmados locales.

Retener `mapping.txt` de **cada** release junto a su AAB y cargar el mapping
en el servicio para interpretar correctamente crashes ofuscados. Verificar
un fallo controlado en un entorno de prueba antes del piloto. La presencia
del SDK no significa que el monitoreo esté activo ni comprobado de extremo
a extremo.

## Modelo de confianza de las respuestas offline

El contenido descargable incluye soluciones porque el alumno necesita
feedback y calificación formativa sin red. Room/cifrado/R8 no convierten un
cliente en una fuente confiable: las soluciones y los tiempos pueden
inspeccionarse o manipularse. El servidor recalifica respuestas y controla
acceso, recompensas e idempotencia, pero esto no prueba autoría ni evita que
el alumno haya leído la solución.

**El flujo actual es exclusivamente práctica formativa.** No utilizar sus
resultados como examen supervisado, calificación de alto impacto,
certificación o competencia premiada. Para esos usos se necesita un
contrato separado que nunca entregue soluciones, autorización explícita
de la modalidad en servidor, variantes/pools y cierre online. No se añade
un selector de examen que prometa garantías que el backend no implementa.

## Diferencias deliberadas con backend

- El contexto de sala/nivel ya recibido por `/api/auth/me` guía al alumno;
  no se añade una segunda consulta equivalente como requisito para el modo
  offline. La pantalla docente usa el endpoint que antes no se consumía.
- Creación de cuentas, gestión administrativa y coordinación siguen fuera
  de este cliente. No se exponen endpoints privilegiados al estudiante.
- `word_search` permanece no jugable hasta implementar su mecánica.
- Zona horaria al viajar: el contrato actual atribuye la racha mediante un
  offset de consulta/lote para todo el historial. Guardar el offset del
  intento no resuelve por sí solo la semántica. Antes de expansión entre
  zonas, definir zona estable por estudiante o días por intento en backend,
  migrar datos y probar viajes/DST. No se declara resuelto aquí.

## Validación y límites

Las pruebas de estado guardado simulan recreación de UI y nueva instancia
ViewModel. No sustituyen la prueba de matar el proceso desde el sistema con
la app en background y volver desde Recientes. El force-stop o borrar datos
no promete recuperación: Android puede eliminar estado guardado.

Antes del piloto: comprobar cambio de cuenta, background + process death,
cola offline, reinicio sin red, matching con pausa larga y +30 s, TalkBack,
fuente al 200 %, tablet horizontal y la build optimizada instalada.
Firma real, recepción de errores y revisión visual requieren un dispositivo
/configuración externos. El PR debe conservarlos como pendientes, no como
riesgos cerrados por tener código o un test compilado.
