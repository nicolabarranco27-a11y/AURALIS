# Catálogo de Funcionalidades - Auralis

Este documento detalla las capacidades técnicas y de usuario actualmente presentes en el código de Auralis.

> **Convención**:
> - ✅ **Implementado**: presente en el código y consolidado en el repositorio (versión actual en `build.gradle.kts`: `0.2.0` / `2`, consolidada en `8304e58` + tag `v0.2.0`).
> - 🚧 **Pendiente de consolidación**: implementado pero aún no subido al repositorio (actualmente **no hay** ítems en este estado).
> - ⬜ **Pendiente de implementación**: aún no existe en código.
>
> **v0.2.0 (2026-10-12)**: escaneo automático por defecto + carpetas adicionales de música (explorador del sistema / SAF). Consolidada en `8304e58` + tag `v0.2.0`.
> **2026-10-13/14**: efectos visuales del reproductor (aura que late al ritmo real, controles con `Icons.Rounded`, seek rediseñado, mini-player con progreso interactivo) y **gestión de biblioteca/playlists** (ocultar canciones con `isHidden`/Room v5, chip "Playlists" en Inicio, rediseño de Playlists) — **consolidados en `origin/main`**.

---

## 🎵 Reproducción (Core)

| Funcionalidad | Estado | Detalle |
|---|---|---|
| Motor Media3 | ✅ | ExoPlayer + `MediaSessionService` |
| Controles universales | ✅ | Play, pausa, siguiente, anterior |
| Shuffle | ✅ | |
| Repeat (OFF / ONE / ALL) | ✅ | |
| Seek interactivo (Slider) | ✅ | Solo en pantalla completa Player |
| Sincronización mini-player ↔ Player | ✅ | `MusicServiceConnection` |

---

## 📱 Interfaz

| Funcionalidad | Estado | Detalle |
|---|---|---|
| Mini-player unificado (`MiniPlayerBar`, scrubber de 3dp) | ✅ | Barra minimalista única en todas las pantallas con **línea de progreso interactiva** (pulsar/arrastrar para saltar; el tiempo se muestra solo al ajustar) |
| Player con carátula y seek | ✅ | |
| Player rediseñado | ✅ | Vinilo circular + glow + visualizador animado |
| Aura de color animada y fluida en el reproductor | ✅ | Luz ambiental con disco de luz + halo + cuatro brumas que orbitan, respiran y dan un empuje angular en cada golpe (`VinylAura`), derivada del color dominante de la carátula; **late al ritmo real de la canción** (separa graves y agudos y detecta los golpes del PCM sin permisos, con caída adaptada al tempo y movimiento suavizado) con pulso sintético de respaldo; respaldo rojo vivo `#C62828` |
| Controles de transporte con `Icons.Rounded` | ✅ | Shuffle/Prev/Play-Pause/Next/Repeat con estado activo y micro-animación |
| Drawer con secciones | ✅ | |
| Drawer con item seleccionado (pill animado) | ✅ | `currentBackStackEntryAsState` |
| Tema Material 3 + tokens | ✅ | `ReproductorThemeTokens` |
| Iconos Material Rounded | ✅ | `Icons.Rounded.*` / `Icons.AutoMirrored.Rounded.*` |
| `AuralisArtwork` con fallback `MusicNote` | ✅ | Icono en color de acento |
| Logo/ícono de la app actualizado | ✅ | Launcher adaptativo + logo circular in-app |
| Versión visible en el Drawer | ✅ | `v0.2.0` junto a EstalingradoCorp |

---

## 🔍 Búsqueda

| Funcionalidad | Estado | Detalle |
|---|---|---|
| `searchSongs(query)` en repositorio | ✅ | Normalización SQLite (`REPLACE`+`LOWER`) + clave `searchKey` (Room v4) |
| Autocomplete con overlay de sugerencias | ✅ | `HomeViewModel._searchQuery` + `SearchSuggestionsOverlay` |
| Acciones por swipe en sugerencias | ⬜ | No implementado |

---

## 🎚 Audio Avanzado

| Funcionalidad | Estado | Detalle |
|---|---|---|
| Ecualizador real (AudioFX, 5 bandas) | ✅ | `PlaybackService.setupEqualizer` + UI en `PlayerScreen` |
| Presets (Normal / Bass Boost / Rock / Pop) | ✅ | |
| Persistencia del ecualizador | ✅ | `EqualizerPreferences` (SharedPreferences) |
| `audioSessionId` dinámica | ✅ | `Player.Listener.onAudioSessionIdChanged` |

---

## 📚 Biblioteca

| Funcionalidad | Estado | Detalle |
|---|---|---|
| Escaneo MediaStore + `WorkManager` | ✅ | Escaneo automático de toda la música del dispositivo (`IS_MUSIC`); proyección compatible (sin `DATA`) |
| Carpetas adicionales de música (SAF) | ✅ | Explorador del sistema (`OpenDocumentTree`); añadir/quitar carpetas que se **suman** a la biblioteca |
| Escaneo por defecto + carpetas que se suman | ✅ | `MediaStoreSource` escanea por defecto toda la música y añade el audio de las carpetas elegidas (`VOLUME_NAME` + `RELATIVE_PATH` en API 29+, o `DATA` en API < 29) aunque `IS_MUSIC = 0` |
| Exclusión de audio que no es música | ✅ | `AudioPathFilter` descarta mensajería (WhatsApp/Telegram/Signal), grabaciones y sonidos del sistema |
| Gestión de carpetas desde Canciones y Ajustes | ✅ | Hoja inferior `MusicFoldersSheet`; CTA en el estado vacío de Inicio |
| Escaneo de arranque si biblioteca vacía | ✅ | `HomeViewModel.onScreenShown` |
| Playlists (crear/renombrar/eliminar/añadir/quitar) | ✅ | `PlaylistRepository` |
| Playlists: estadísticas, edición y reordenación | ✅ | `PlaylistSummary` (canciones visibles/duración/portada); editar nombre **y** descripción; mover arriba/abajo; **eliminar desde Inicio** (long-press revela un tacho a la derecha → confirmación minimalista). Rediseño de lista y detalle |
| Ocultar canciones de la biblioteca (reversible) | ✅ | `isHidden` (Room v5) + `SongActionsSheet` (long-press) + `HiddenSongsScreen` (restaurar); no borra el archivo |
| Fila de canción unificada | ✅ | `SongListItem` compartido por todas las listas |
| Vista Canciones | ✅ | |
| Vista Álbumes + Detalle | ✅ | |
| Vista Artistas + Detalle | ✅ | |
| Favoritos (modelo + DAO) | ✅ | `FavoritesDao` + `RoomFavoritesRepository` |
| Favoritos (toggle en filas) | ✅ | Home, Songs, AlbumDetail, ArtistDetail, PlaylistDetail |
| Vista dedicada de Favoritos | ✅ | Ruta en Drawer |
| Cola dinámica (swipe-to-queue) | ✅ | Home, Songs, AlbumDetail, ArtistDetail, PlaylistDetail |
| Diálogo "Detalles de la Pista" | ✅ | Título/artista/álbum/duración/origen |
| Bitrate / sample rate / formato | ⬜ | No extraído |
| Sleep Timer | ✅ | `PlayerViewModel.startSleepTimer`/`cancelSleepTimer` + diálogo de presets |

---

## 🧭 Home, Filtros y Acceso Rápido

| Funcionalidad | Estado | Detalle |
|---|---|---|
| Carrusel "Acceso Rápido" | ✅ | `LazyRow` con hasta 5 canciones (preferentemente favoritas) |
| Filtros por chips | ✅ | "Todas" / "Favoritos" (+ **"Playlists"** inline con crear/eliminar) |
| Autocomplete de búsqueda | ✅ | Overlay de sugerencias |

---

## ⚙️ Ajustes y Preferencias

| Funcionalidad | Estado | Detalle |
|---|---|---|
| Pantalla de Ajustes real | ✅ | `SettingsScreen.kt` |
| Acceso a "Canciones ocultas" | ✅ | Tarjeta en Ajustes → `HiddenSongsScreen` (restaurar) |
| Persistencia de tema (claro/oscuro/sistema) | ✅ | `ThemeController` |
| Persistencia de ajustes de audio | ✅ | Solo ecualizador (SharedPreferences) |
| DataStore para preferencias | ⬜ | No migrado |

---

## 🧪 Calidad e Infraestructura

| Funcionalidad | Estado | Detalle |
|---|---|---|
| Tests unitarios (`app/src/test/`) | ✅ | Datos, dominio y UI (`HomeViewModelTest`, `MusicFolderPreferencesTest`) |
| Tests instrumentados (`app/src/androidTest/`) | ✅ | `LibraryDaoInstrumentedTest`, `AppLaunchSmokeTest` |
| CI unit + lint | ✅ | Workflow GitHub Actions |
| CI matriz de emuladores (API 26/29/33/36) | ✅ | `.github/workflows/android-ci.yml` (no verificado en remoto aún) |
| Script local de instrumentados | ✅ | `tools/run-instrumented-tests.ps1` |
| Permiso runtime `POST_NOTIFICATIONS` | ✅ | Android 13+ |

---

## 📊 Resumen de Cobertura

| Estado | Cantidad aprox. |
|---|---|
| ✅ Implementado y consolidado | ~40 funcionalidades |
| ⬜ Pendiente de implementación | ~5 funcionalidades |

Si hay discrepancias con otros documentos, **`PROJECT_STATUS.md` gana** como fuente principal del estado real.
