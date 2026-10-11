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
- **Fecha**: 2026-09-05 (working tree)
- **Estado**: Aceptado (pendiente de consolidación)
- **Contexto**: En v0.1.0 se usaban glifos Unicode (`⌂`, `♪`, `⚙`, etc.) en botones, drawer y top bar. Esto generaba inconsistencias tipográficas entre dispositivos y familias de fuentes.
- **Decisión**: Reemplazar glifos Unicode por `androidx.compose.material.icons.Icons.Rounded` (vía `material-icons-extended`).
- **Razón**: Garantiza coherencia visual entre dispositivos, mejor accesibilidad (TalkBack lee descripciones reales) y consistencia con Material Design 3.
- **Migración**: requiere agregar `material-icons-extended` al catálogo.

---

## ADR 006: `LinearProgressIndicator` 3dp en mini-player
- **Fecha**: 2026-09-05 (working tree)
- **Estado**: Aceptado (pendiente de consolidación)
- **Contexto**: El mini-player en v0.1.0 usaba `Slider` para mostrar progreso, lo que ocupaba ~32dp verticales y mostraba el thumb.
- **Decisión**: Reemplazarlo por `LinearProgressIndicator` pasivo de 3dp sin interactividad.
- **Razón**: El seek se realiza en `PlayerScreen` (pantalla completa); el mini-player solo refleja estado. Reduce ruido visual y mantiene la mini-player compacta.

---

## ADR 007: Indicador de sección activa en Drawer con píldora (pilled highlight)
- **Fecha**: 2026-09-05 (working tree)
- **Estado**: Aceptado (pendiente de consolidación)
- **Contexto**: El Drawer en v0.1.0 no indicaba la sección actual; el usuario perdía contexto al navegar.
- **Decisión**: Usar `currentBackStackEntryAsState` para conocer la ruta y resaltar el item correspondiente con `secondaryContainer` + texto en negrita + color de acento, dentro de un `RoundedCornerShape(percent=50)`.
- **Razón**: Patrón estándar de Material 3 (NavigationRail/DrawerItem). Aporta orientación sin agregar peso visual significativo.

---

## ADR 008: `AuralisArtwork` fallback con `MusicNote`
- **Fecha**: 2026-09-05 (working tree)
- **Estado**: Aceptado (pendiente de consolidación)
- **Contexto**: En v0.1.0, `AuralisArtwork` mostraba el logo corporativo como placeholder cuando no había cover. Esto vinculaba la marca corporativa con la app.
- **Decisión**: Reemplazar el placeholder corporativo por un icono `Icons.Rounded.MusicNote` centrado, con `tint = colors.accent.copy(alpha = 0.35f)`.
- **Razón**: Refuerza la identidad visual de Auralis (no de EstalingradoCorp) y mantiene coherencia con el tema.
