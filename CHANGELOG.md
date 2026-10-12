# Changelog

Todas las modificaciones relevantes de Auralis se documentan en este archivo.
El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y
el proyecto se adhiere a [Semantic Versioning](https://semver.org/lang/es/).

## [Unreleased]

> **Estado**: cambios **consolidados en el repositorio** el **2026-10-12**.
> `versionName` se mantiene en `0.1.0` / `versionCode = 1` (sin bump de versión)
> hasta la próxima release (ver `docs/RELEASE_PLAN.md`).

### Added
- **Nuevo ícono de la app (launcher)**: foreground del ícono adaptativo regenerado con el nuevo logotipo de Auralis en todas las densidades (`mdpi`→`xxxhdpi`), respetando la zona segura; fondo negro (`ic_launcher_background = #000000`).
- **Nuevo logo in-app**: el logo circular del header de Inicio (`HomeScreen`) usa el nuevo logotipo (`drawable/logo_auralis.png`).
- **Versión visible en el Drawer**: se muestra `v0.1.0` (vía `BuildConfig.VERSION_NAME`) alineado a la derecha junto a la sección EstalingradoCorp.
- **Tests instrumentados** (`app/src/androidTest/`): `LibraryDaoInstrumentedTest` (SQLite real del dispositivo) y `AppLaunchSmokeTest` (arranque + Drawer/versión).
- **CI con matriz de emuladores**: workflow `.github/workflows/android-ci.yml` (job de unit+lint y job de instrumentados en API 26 / 29 / 33 / 36).
- **Script local** `tools/run-instrumented-tests.ps1` para correr los tests instrumentados en un dispositivo/emulador conectado.
- **Búsqueda con Autocomplete** en Home (`HomeViewModel._searchQuery` + `SearchSuggestionsOverlay`) sobre clave premormalizada (insensible a mayúsculas y acentos).
- **Toggle de Favoritos en filas** (Home, Songs, AlbumDetail, ArtistDetail, PlaylistDetail).
- **Vista de Favoritos** dedicada (ruta en el Drawer).
- **Ecualizador real (AudioFX, 5 bandas)** en `PlaybackService` + UI en `PlayerScreen`, con presets (Normal / Bass Boost / Rock / Pop).
- **Persistencia del ecualizador** (`EqualizerPreferences`, SharedPreferences).
- **Diálogo "Detalles de la Pista"** en `PlayerScreen` (título, artista, álbum, duración, origen).
- **Pantalla de Ajustes real** (`SettingsScreen`) con selección de tema.
- **Persistencia de tema** (claro / oscuro / sistema) vía `ThemeController`.
- **Drawer con item activo resaltado** (pill animado).
- **Carrusel "Acceso Rápido"** y **filtros por chips** ("Todas" / "Favoritos") en Home.
- **Cola dinámica** (swipe-to-queue) en Home, Songs, AlbumDetail, ArtistDetail y PlaylistDetail, con overlay de feedback.

### Changed
- **Permiso `POST_NOTIFICATIONS` (Android 13+)**: ahora se solicita en tiempo de ejecución, necesario para mostrar los controles de reproducción en la notificación y la pantalla de bloqueo.
- **`SettingsScreen`**: reserva el inset inferior de navegación (`navigationBarsPadding()`) para no quedar bajo la barra de navegación.
- **`AuralisArtwork`**: fallback con icono `Icons.Rounded.MusicNote` en color de acento (reemplaza el placeholder corporativo).
- **Mini-player**: `LinearProgressIndicator` de 3dp en lugar de `Slider` (el seek se hace en `PlayerScreen`).
- **Iconografía**: reemplazo de glifos Unicode por `Icons.Rounded.*` / `Icons.AutoMirrored.Rounded.*`.
- **PlayerScreen**: carátula estilo vinilo circular, botón Play con halo, visualizador animado en el top bar.
- **Renombrado** `drawable/estalingradocorp_logo.png` → `estalingradocorp_logo.jpg` (el archivo era un JPEG mal nombrado que rompía `mergeReleaseResources`).

### Fixed
- **Compilación de `PlaybackService`**: se corrigió el uso inexistente de `session.commands`, se migró a `MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS`, `SessionError.ERROR_NOT_SUPPORTED` y se añadió `@OptIn(UnstableApi::class)` al callback.
- **Lint**: `android:forceDarkAllowed` con `tools:targetApi="q"` (API 29 con `minSdk 26`).
- **`AddToPlaylistViewModel`**: acceso null-safe al argumento de navegación (evita crash si falta `playlistId`).
- **Escaneo no detectaba música descargada [causa raíz]**: se eliminó `MediaStore.Audio.Media.DATA` de la proyección (bloqueada en Android 11+ con `targetSdk >= 30`; el proveedor rechazaba toda la consulta) y se hicieron defensivas las lecturas por fila (`safeGetString`).
- **Escaneo no detectaba música descargada [IS_MUSIC]**: se eliminó el filtro `IS_MUSIC != 0` para incluir descargas indexadas con `IS_MUSIC = 0`.
- **Escaneo automático de arranque**: `HomeViewModel.onScreenShown` solicita el escaneo si ya hay permiso y la biblioteca está vacía.
- **Hilo principal en `HomeViewModel.onSongClick`**: la generación de UUIDs de `QueueItem` se movió a `Dispatchers.Default`.
- **Cover en AlbumDetail**: se prioriza el primer `coverReference` no vacío.

### Technical
- `minSdk 26`, `targetSdk 36`, `compileSdk 37`.
- Permiso **`WAKE_LOCK`** agregado al manifest (necesario para `ExoPlayer.setWakeMode(C.WAKE_MODE_LOCAL)`).
- **Room v4** con migración `3→4` (columna `searchKey`) y backfill.
- **Dependencias**: eliminada `coil-network-okhttp` (sin uso, no se declara `INTERNET`); añadidas dependencias de test instrumentado (`androidx.test.ext:junit`, `runner`, `rules`, `ui-test-junit4`, `ui-test-manifest`) al catálogo.
- **Recursos**: eliminados los `ic_launcher_foreground.webp` legacy (reemplazados por PNG); el `ImageLoader` global limita la caché al 25% de memoria con downsampling.

### Removed
- Dependencia `coil-network` (sin uso real).
- Iconos legacy `mipmap-*/ic_launcher_foreground.webp` (reemplazados por PNG de mayor fidelidad).

---

## [0.1.0] - 2025-01-XX (Initial Commit - `59403f7`)

### Added
- Reproducción de audio con ExoPlayer (Media3) + MediaSessionService
- Escaneo de biblioteca via MediaStore + WorkManager programado
- Vistas jerárquicas: Canciones, Álbumes, Artistas
- Pantalla Home con barra de reproducción persistente (mini-player con Slider)
- Pantalla Player completa con controles, seek, shuffle, repeat
- Navegación por Drawer (cajón lateral) con marcador textual
- Tema Material 3 personalizado (Color, Type, Shape, Spacing, Motion)
- Arquitectura Clean: Domain / Data / Feature / Core / DI
- Inyección de dependencias con Hilt
- Base de datos Room con Flow reactivo (incluye `FavoritesDao` y entidad)
- Kotlin Coroutines + Flow para async
- KSP para Room/Hilt
- ProGuard/R8 en release
- Tests unitarios base (JUnit, Robolectric, Coroutines Test) para capa de datos y dominio

---

## Convenciones de versionado

- **MAJOR**: Cambios incompatibles en API pública.
- **MINOR**: Funcionalidad nueva compatible hacia atrás.
- **PATCH**: Correcciones compatibles hacia atrás.

Ejemplo: `1.2.3` → `versionCode = 10203` (major*10000 + minor*100 + patch).
