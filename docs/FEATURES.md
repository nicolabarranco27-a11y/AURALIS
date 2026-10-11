# Catálogo de Funcionalidades - Auralis

Este documento detalla las capacidades técnicas y de usuario actualmente presentes en el código de Auralis.

> **Convención**:
> - ✅ **Entregado**: presente en la versión `0.1.0` (commiteada en `59403f7`).
> - 🟡 **Pendiente de consolidación**: implementado en working tree, **no commiteado**, no forma parte de ninguna versión entregable.
> - ⬜ **Pendiente de implementación**: aún no existe en código.

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
| Mini-player con `Slider` | ✅ | Versión 0.1.0 |
| Mini-player rediseñado (`LinearProgressIndicator`) | 🟡 | Reemplaza `Slider` por barra de 3dp |
| Player con carátula y seek | ✅ | Versión 0.1.0 |
| Player rediseñado (vinilo circular + glow + visualizador) | 🟡 | Cambio solo en working tree |
| Drawer con secciones | ✅ | |
| Drawer con item seleccionado (pill animado) | 🟡 | Cambio solo en working tree |
| Tema Material 3 + tokens | ✅ | `ReproductorThemeTokens` |
| Iconos Material Rounded | 🟡 | Reemplazo de glifos Unicode, en working tree |
| `AuralisArtwork` con placeholder corporativo | ✅ | Versión 0.1.0 |
| `AuralisArtwork` con fallback `MusicNote` | 🟡 | Reemplaza placeholder corporativo, en working tree |

---

## 🔍 Búsqueda

| Funcionalidad | Estado | Detalle |
|---|---|---|
| `searchSongs(query)` en repositorio | ✅ | Implementado, no expuesto en UI |
| Autocomplete con normalización SQLite (`REPLACE`+`LOWER`) | 🟡 | `HomeViewModel._searchQuery` + overlay en `HomeScreen` |
| Acciones por swipe en sugerencias | ⬜ | No implementado |

> Nota: la versión 0.1.0 tiene `searchSongs` pero **sin** overlay de sugerencias en Home.

---

## 🎚 Audio Avanzado

| Funcionalidad | Estado | Detalle |
|---|---|---|
| Ecualizador con 5 bandas | 🟡 | UI completa + persistencia + AudioFX, todo en working tree |
| Persistencia de ecualización | 🟡 | `EqualizerPreferences` (SharedPreferences), en working tree |
| Presets (Normal / Bass Boost / Rock / Pop) | 🟡 | En working tree |
| Captura dinámica de `audioSessionId` | 🟡 | `PlaybackService.onAudioSessionIdChanged`, en working tree |
| Ecualizador en servicio (no solo UI) | 🟡 | Aplicación automática de bandas al cambiar sesión |

---

## 📋 Gestión de Biblioteca

| Funcionalidad | Estado | Detalle |
|---|---|---|
| Escaneo MediaStore + WorkManager | ✅ | |
| Playlists CRUD | ✅ | Crear, renombrar, eliminar, añadir/quitar canciones |
| Detalle de Playlist con reproducción | ✅ | |
| Vista Canciones | ✅ | |
| Vista Álbumes + Detalle | ✅ | |
| Vista Artistas + Detalle | ✅ | |
| Favoritos (modelo + DAO) | ✅ | `FavoritesDao` y `RoomFavoritesRepository` |
| Favoritos (toggle en filas) | 🟡 | Botón de corazón en cada `SongItem`, en working tree |
| Vista dedicada de Favoritos | ⬜ | No existe ruta en Drawer |
| Metadatos técnicos en Detalles de Pista | 🟡 | Diálogo con título/artista/álbum/duración/origen, en working tree |
| Bitrate / sample rate / formato | ⬜ | No extraído |
| Sleep Timer | ⬜ | Ítem muestra "Próximamente" en working tree |

---

## 🧭 Filtros y Acceso Rápido

| Funcionalidad | Estado | Detalle |
|---|---|---|
| Carrusel "Acceso Rápido" en Home | 🟡 | `LazyRow` con hasta 5 canciones (preferentemente favoritas), en working tree |
| Filtros por chips en Home | 🟡 | "Todas" / "Favoritos", en working tree |
| Cola dinámica (swipe-to-queue) | ✅ | `SongItem.onSwipeRight` en Home, Songs, AlbumDetail, ArtistDetail, PlaylistDetail |

---

## ⚙️ Ajustes

| Funcionalidad | Estado | Detalle |
|---|---|---|
| Pantalla de Ajustes (placeholder) | ✅ | "Próximamente" |
| Pantalla de Ajustes real | 🟡 | `SettingsScreen.kt` con lista de opciones visuales, en working tree |
| Persistencia de tema | ⬜ | No implementado |
| Persistencia de ajustes de audio | 🟡 | Solo ecualizador, en working tree |
| DataStore para preferencias | ⬜ | No migrado |

---

## 📊 Resumen de Cobertura

| Estado | Cantidad aprox. |
|---|---|
| ✅ Entregado en 0.1.0 | ~20 funcionalidades |
| 🟡 En working tree (no entregado) | ~22 funcionalidades |
| ⬜ Pendiente de implementación | ~5 funcionalidades |

Esta tabla puede verse desfasada respecto a `CHANGELOG.md` o `PROJECT_STATUS.md`. Si hay discrepancias, **PROJECT_STATUS.md gana** como fuente principal del estado real.
