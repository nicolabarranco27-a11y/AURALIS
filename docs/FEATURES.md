# Catálogo de Funcionalidades - Auralis

Este documento detalla las capacidades técnicas y de usuario actualmente presentes en el código de Auralis.

> **Convención**:
> - ✅ **Implementado**: presente en el código (versión actual en `build.gradle.kts`: `0.2.0` / `2`, commit/tag pendientes).
> - ⬜ **Pendiente de implementación**: aún no existe en código.
>
> **v0.2.0 (2026-10-12)**: escaneo automático por defecto + carpetas adicionales de música (explorador del sistema / SAF). Commit/tag pendientes (no se toca `git` aún).

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
| Mini-player (`LinearProgressIndicator` 3dp) | ✅ | Barra pasiva; el seek se hace en `PlayerScreen` |
| Player con carátula y seek | ✅ | |
| Player rediseñado | ✅ | Vinilo circular + glow + visualizador animado |
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
| Vista Canciones | ✅ | |
| Vista Álbumes + Detalle | ✅ | |
| Vista Artistas + Detalle | ✅ | |
| Favoritos (modelo + DAO) | ✅ | `FavoritesDao` + `RoomFavoritesRepository` |
| Favoritos (toggle en filas) | ✅ | Home, Songs, AlbumDetail, ArtistDetail, PlaylistDetail |
| Vista dedicada de Favoritos | ✅ | Ruta en Drawer |
| Cola dinámica (swipe-to-queue) | ✅ | Home, Songs, AlbumDetail, ArtistDetail, PlaylistDetail |
| Diálogo "Detalles de la Pista" | ✅ | Título/artista/álbum/duración/origen |
| Bitrate / sample rate / formato | ⬜ | No extraído |
| Sleep Timer | ⬜ | Ítem muestra `Toast: "Próximamente"` |

---

## 🧭 Home, Filtros y Acceso Rápido

| Funcionalidad | Estado | Detalle |
|---|---|---|
| Carrusel "Acceso Rápido" | ✅ | `LazyRow` con hasta 5 canciones (preferentemente favoritas) |
| Filtros por chips | ✅ | "Todas" / "Favoritos" |
| Autocomplete de búsqueda | ✅ | Overlay de sugerencias |

---

## ⚙️ Ajustes y Preferencias

| Funcionalidad | Estado | Detalle |
|---|---|---|
| Pantalla de Ajustes real | ✅ | `SettingsScreen.kt` |
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
