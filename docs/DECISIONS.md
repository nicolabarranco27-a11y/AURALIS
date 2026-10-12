# Registro de Decisiones Técnicas (ADR) - Auralis

Este documento registra las decisiones técnicas clave tomadas durante el desarrollo del proyecto y el razonamiento detrás de ellas.

---

## ADR 001: `SharedPreferences` para persistencia del Ecualizador
- **Fecha**: 2026-09-03
- **Estado**: Aceptado
- **Contexto**: Se necesitaba guardar 5 valores de ganancia (float) y un nombre de preset.
- **Decisión**: Utilizar `SharedPreferences` en lugar de Room o DataStore.
- **Razón**: Simplicidad y rapidez de implementación para una cantidad de datos mínima. Provee acceso síncrono necesario para la inicialización rápida del efecto de audio en el servicio.
- **Nota**: Migración futura a DataStore está prevista en el Milestone 1 del `docs/RELEASE_PLAN.md`.

---

## ADR 002: Normalización de búsqueda con `REPLACE` y `LOWER` en SQLite
- **Fecha**: 2026-08-29
- **Estado**: Aceptado
- **Contexto**: SQLite no soporta nativamente la comparación de caracteres acentuados (`á` vs `a`) en `LIKE`.
- **Decisión**: Cadena de funciones `REPLACE` y `LOWER` directamente en la consulta del DAO.
- **Razón**: Evita complejidad de funciones personalizadas en C y mantiene el esquema limpio.

---

## ADR 003: Gestión de `audioSessionId` dinámica
- **Fecha**: 2026-09-03
- **Estado**: Aceptado
- **Contexto**: ExoPlayer asigna el ID de sesión de audio solo después de iniciar la carga de la pista. Inicialmente es 0 (UNSET).
- **Decisión**: `Player.Listener.onAudioSessionIdChanged` en `PlaybackService` actualiza `MediaSession.extras` y reaplica el efecto.
- **Razón**: Asegura que el ecualizador se enganche a la sesión real, no a la sesión global 0 (restringida en Android moderno).

---

## ADR 004: `VerticalFader` nativo vs `Slider` rotado
- **Fecha**: 2026-09-03
- **Estado**: Aceptado
- **Contexto**: Los `Slider` de Material 3 son horizontales. Rotarlos 90° causa bugs visuales y zonas táctiles incorrectas.
- **Decisión**: Composable propio basado en `Box` + `detectVerticalDragGestures` + `detectTapGestures`.
- **Razón**: Control total del renderizado y comportamiento táctil coherente con un ecualizador real.

---

## ADR 005: Material Icons Rounded como estándar visual
- **Fecha**: 2026-09-05 (consolidado 2026-10-12)
- **Estado**: Aceptado (consolidado 2026-10-12)
- **Contexto**: En v0.1.0 se usaban glifos Unicode (`⌂`, `♪`, `⚙`, etc.) en botones, drawer y top bar. Esto generaba inconsistencias tipográficas entre dispositivos y familias de fuentes.
- **Decisión**: Reemplazar glifos Unicode por `androidx.compose.material.icons.Icons.Rounded` (vía `material-icons-extended`).
- **Razón**: Garantiza coherencia visual entre dispositivos, mejor accesibilidad (TalkBack lee descripciones reales) y consistencia con Material Design 3.
- **Migración**: requiere agregar `material-icons-extended` al catálogo.

---

## ADR 006: `LinearProgressIndicator` 3dp en mini-player
- **Fecha**: 2026-09-05 (consolidado 2026-10-12)
- **Estado**: Aceptado (consolidado 2026-10-12)
- **Contexto**: El mini-player en v0.1.0 usaba `Slider` para mostrar progreso, lo que ocupaba ~32dp verticales y mostraba el thumb.
- **Decisión**: Reemplazarlo por `LinearProgressIndicator` pasivo de 3dp sin interactividad.
- **Razón**: El seek se realiza en `PlayerScreen` (pantalla completa); el mini-player solo refleja estado. Reduce ruido visual y mantiene la mini-player compacta.

---

## ADR 007: Indicador de sección activa en Drawer con píldora (pilled highlight)
- **Fecha**: 2026-09-05 (consolidado 2026-10-12)
- **Estado**: Aceptado (consolidado 2026-10-12)
- **Contexto**: El Drawer en v0.1.0 no indicaba la sección actual; el usuario perdía contexto al navegar.
- **Decisión**: Usar `currentBackStackEntryAsState` para conocer la ruta y resaltar el item correspondiente con `secondaryContainer` + texto en negrita + color de acento, dentro de un `RoundedCornerShape(percent=50)`.
- **Razón**: Patrón estándar de Material 3 (NavigationRail/DrawerItem). Aporta orientación sin agregar peso visual significativo.

---

## ADR 008: `AuralisArtwork` fallback con `MusicNote`
- **Fecha**: 2026-09-05 (consolidado 2026-10-12)
- **Estado**: Aceptado (consolidado 2026-10-12)
- **Contexto**: En v0.1.0, `AuralisArtwork` mostraba el logo corporativo como placeholder cuando no había cover. Esto vinculaba la marca corporativa con la app.
- **Decisión**: Reemplazar el placeholder corporativo por un icono `Icons.Rounded.MusicNote` centrado, con `tint = colors.accent.copy(alpha = 0.35f)`.
- **Razón**: Refuerza la identidad visual de Auralis (no de EstalingradoCorp) y mantiene coherencia con el tema.

---

## ADR 009: Solicitud runtime de `POST_NOTIFICATIONS` (Android 13+)
- **Fecha**: 2026-10-12
- **Estado**: Aceptado
- **Contexto**: En Android 13 (API 33)+ el permiso `POST_NOTIFICATIONS` debe concederse en tiempo de ejecución. Sin él, el `MediaSessionService` no puede mostrar los controles de reproducción en la notificación ni en la pantalla de bloqueo, degradando la experiencia y la estabilidad del foreground service (riesgo de `SecurityException` al iniciar la reproducción).
- **Decisión**: Solicitar el permiso desde `MainActivity` con `registerForActivityResult(RequestPermission)` + `ContextCompat.checkSelfPermission`, y declararlo en el manifest con `maxSdkVersion` no aplicable (`POST_NOTIFICATIONS`).
- **Razón**: Requisito obligatorio de la plataforma para una app de reproducción con controles multimedia.

---

## ADR 010: Nuevo logotipo e ícono de app de Auralis
- **Fecha**: 2026-10-12
- **Estado**: Aceptado
- **Contexto**: El ícono previo usaba `ic_launcher_foreground.webp` de baja fidelidad y el header de Inicio usaba un placeholder corporativo.
- **Decisión**:
  - Regenerar el **foreground** del ícono adaptativo como PNG en todas las densidades (`mdpi`→`xxxhdpi`), con el logotipo dentro de la zona segura (~61%) sobre fondo negro (`ic_launcher_background = #000000`).
  - Usar `drawable/logo_auralis.png` como logo circular in-app en el header de Inicio.
- **Razón**: Mejora la nitidez y consistencia del ícono en todas las densidades y separa la identidad visual de Auralis de la marca corporativa (EstalingradoCorp). Se eliminaron los `.webp` legacy.

---

## ADR 010b: Versión de la app visible en el Drawer
- **Fecha**: 2026-10-12
- **Estado**: Aceptado
- **Contexto**: No había forma de conocer la versión instalada desde la UI.
- **Decisión**: Mostrar `v${BuildConfig.VERSION_NAME}` junto a EstalingradoCorp en la sección inferior del Drawer, alineado a la derecha (el nombre usa `Modifier.weight(1f)`) y en color de acento.
- **Razón**: Facilita el soporte y la verificación de builds sin costo visual.

---

## ADR 011: Tests instrumentados + CI con matriz de emuladores
- **Fecha**: 2026-10-12
- **Estado**: Aceptado
- **Contexto**: El proyecto tenía tests unitarios en JVM pero ninguna verificación en dispositivo, pese a que la causa raíz de varios bugs (p. ej. el escaneo) solo se manifestaba en Android real. Tampoco había verificación de compatibilidad entre versiones de API.
- **Decisión**:
  - Añadir `app/src/androidTest/` con tests instrumentados (`LibraryDaoInstrumentedTest` para el DAO en SQLite real y `AppLaunchSmokeTest` para el arranque y el Drawer).
  - Configurar `testInstrumentationRunner` y las dependencias `androidx.test` en `app/build.gradle.kts`.
  - Añadir `.github/workflows/android-ci.yml` con un job de unit+lint y un job de instrumentados en una matriz de emuladores (API 26 / 29 / 33 / 36).
  - Proveer `tools/run-instrumented-tests.ps1` para ejecución local en un dispositivo/emulador conectado.
- **Razón**: Detecta regresiones dependientes de la plataforma (permisos, MediaStore, Room) y valida la compatibilidad mínima (`minSdk 26`) hasta la última API (`targetSdk 36`).
- **Nota**: En el entorno de desarrollo no había dispositivo/emulador disponible; los tests quedan listos para ejecutarse localmente y en CI.
