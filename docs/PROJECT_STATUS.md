# Estado del Proyecto - Auralis

**Fecha de última auditoría**: 2026-10-12
**Última versión entregable (release)**: v0.1.0 (`59403f7`)
**Versión en build.gradle.kts**: `versionName = "0.1.0"`, `versionCode = 1`
**Working tree**: **limpio** (los cambios acumulados se consolidaron en el commit de consolidación del 2026-10-12).

> **Nota de versionado**: el commit de consolidación introduce funcionalidad y mejoras nuevas **sin** bumpear `versionName`. Estas mejoras constituyen el contenido previsto para la próxima **v0.2.0** (ver `docs/RELEASE_PLAN.md`), pero todavía no se ha creado el tag ni la release.

---

## 🎯 Resumen Ejecutivo

Auralis está en una fase funcional madura. El motor de reproducción Media3, las tres vistas de biblioteca (Canciones, Álbumes, Artistas), las playlists, los favoritos persistidos en Room, el escaneo automático vía MediaStore y la arquitectura Clean completa (Hilt, Coroutines/Flow, KSP) están **implementados y consolidados en el repositorio**.

Además, cambios significativos que antes vivían en el working tree ya están consolidados: búsqueda con autocomplete, ecualizador real con AudioFX y persistencia, pantalla de Ajustes real con persistencia de tema, drawer con item activo, rediseño del reproductor y la capa visual con Material Icons Rounded.

En esta consolidación (2026-10-12) se sumaron: **nuevo ícono de app y logo in-app**, **versión visible en el Drawer**, **solicitud runtime de `POST_NOTIFICATIONS`** (Android 13+), **tests instrumentados** y un **workflow de CI con matriz de emuladores**.

---

## ✅ Funcionalidades Implementadas (Consolidadas en el repositorio)

### Reproducción Core
- Play / Pause / Next / Previous / Seek
- Shuffle on/off
- Repeat: OFF / ONE / ALL
- `MediaSessionService` con foreground service
- **Ecualizador real (AudioFX, 5 bandas)** integrado en `PlaybackService`, con presets (Normal / Bass Boost / Rock / Pop) y persistencia (`EqualizerPreferences`)
- Cola dinámica (swipe-to-queue) con overlay de feedback

### Biblioteca
- Escaneo automático vía MediaStore + `LibraryScanner` programado por `WorkManager`
- Escaneo de arranque cuando ya hay permiso y la biblioteca está vacía
- Vistas: Canciones, Álbumes, Artistas (lista + detalle)
- Playlists: crear, renombrar, eliminar, añadir/quitar canciones (`PlaylistRepository`)

### Búsqueda
- `searchSongs(query)` con normalización SQLite (`REPLACE`+`LOWER`) y clave `searchKey` premormalizada (Room v4)
- Autocomplete con overlay de sugerencias en Home (`HomeViewModel._searchQuery` + `SearchSuggestionsOverlay`)

### Favoritos
- Persistencia en Room vía `FavoritesDao`
- `FavoritesRepository` expone `toggleFavorite` / `isFavorite` / `observeFavorites`
- **Vista dedicada de Favoritos** (ruta en el Drawer)
- Toggle de favorito en filas (Home, Songs, AlbumDetail, ArtistDetail, PlaylistDetail)

### Navegación y Ajustes
- Drawer lateral con secciones: Inicio, Canciones, Álbumes, Artistas, Playlists, Favoritos, Ajustes
- Drawer con **item activo resaltado** (pill animado)
- Pantalla de Ajustes real (`SettingsScreen`) con **selección de tema persistida** (claro / oscuro / sistema) vía `ThemeController`
- `Drawer` muestra la **versión de la app** (`v0.1.0`) junto a EstalingradoCorp
- Diálogo "Detalles de la Pista" en `PlayerScreen`

### Tema y Sistema de Diseño
- Material 3 con `ReproductorThemeTokens` (Color, Type, Shape, Spacing, Motion)
- Tema por defecto oscuro
- `AuralisArtwork` con fallback `Icons.Rounded.MusicNote` en color de acento
- Iconografía con `Icons.Rounded.*` / `Icons.AutoMirrored.Rounded.*` (`material-icons-extended`)
- **Nuevo logotipo**: ícono de app adaptativo (fondo negro) y logo circular in-app en Home

### Arquitectura
- Clean Architecture: Domain / Data / Feature / Core / DI
- Hilt para DI (DatabaseModule, RepositoryModule, DispatcherModule, etc.)
- Room con KSP (Room v4, migraciones `1→2`, `2→3`, `3→4`)
- Kotlin Coroutines + Flow reactivo
- ProGuard/R8 en release

### Calidad
- Tests unitarios en `app/src/test/` para datos y dominio
- **Tests instrumentados** en `app/src/androidTest/` (`LibraryDaoInstrumentedTest`, `AppLaunchSmokeTest`)
- **CI con matriz de emuladores** (`.github/workflows/android-ci.yml`)
- `assembleDebug` + `assembleRelease` + `testDebugUnitTest` + `lintDebug` + `assembleDebugAndroidTest` verificados localmente (**BUILD SUCCESSFUL**)

---

## ⚠️ Funcionalidades Parciales / En Desarrollo

- **Temporizador (Sleep Timer)**: No implementado. El ítem de menú muestra `Toast: "Próximamente"`.
- **Metadatos técnicos** (bitrate, sample rate, formato): El diálogo de detalles muestra título/artista/álbum/duración/origen, pero no los campos técnicos.
- **DataStore**: Las preferencias (tema, ecualizador) aún usan `SharedPreferences`; la migración a DataStore está prevista en el Milestone 1.
- **Multi-idioma**: Solo español.

---

## 🐛 Bugs Conocidos

### Resueltos en esta consolidación
- **Hilo principal en `HomeViewModel.onSongClick`**: generación de UUIDs de `QueueItem` migrada a `Dispatchers.Default`.
- **Cover vacío en AlbumDetail**: se prioriza el primer `coverReference` no vacío.
- **Escaneo no detectaba música descargada [causa raíz]**: `MediaStore.Audio.Media.DATA` eliminado de la proyección (bloqueada en Android 11+ con `targetSdk >= 30`) y lecturas defensivas (`safeGetString`); además se quitó el filtro `IS_MUSIC != 0`.
- **Compilación de `PlaybackService`**: corregido el uso inexistente de `session.commands` → `DEFAULT_SESSION_COMMANDS`, `SessionError.ERROR_NOT_SUPPORTED`, `@OptIn`.
- **`mergeReleaseResources`**: renombrado `estalingradocorp_logo.png` → `.jpg` (era un JPEG mal nombrado).
- **Sin controles de reproducción en Android 13+**: se solicita `POST_NOTIFICATIONS` en runtime.

### Sin resolución
- Ningún bug crítico conocido.

---

## 🚀 Próximos Pasos Recomendados (Roadmap)

Ver `docs/RELEASE_PLAN.md` para los hitos detallados hacia v1.0.0.

1. **Bumpear a v0.2.0** (versionName/versionCode) y crear el tag de release.
2. **Compatibilidad multi-dispositivo**: el usuario probará en equipos físicos y guiará ajustes de optimización.
2. **Implementar Sleep Timer** real (reemplazar el `Toast: "Próximamente"`).
2. **Migrar a DataStore** para configuraciones de tema y audio.
3. **Extracción de metadatos técnicos** (bitrate, sample rate, formato) en el diálogo de detalles.

---

## 🛠 Estado de CI/CD e Infraestructura

- **Java**: Build local en JDK 21. CI configurado en JDK 21.
- **Build local verificado (2026-10-12)**: `assembleDebug` + `assembleRelease` + `testDebugUnitTest` + `lintDebug` + `assembleDebugAndroidTest` → **BUILD SUCCESSFUL**.
- **CI**: GitHub Actions. El workflow existente `CI - Build & Test` (`assembleDebug` + `testDebugUnitTest` + `lintDebug`) tuvo runs en estado **success** (#8, #9).
- **CI nuevo**: `.github/workflows/android-ci.yml` agrega un job de unit+lint y un job de **tests instrumentados en matriz de emuladores** (API 26 / 29 / 33 / 36). Aún **no verificado en el remoto** (recién consolidado).
- **Tests instrumentados locales**: requieren un dispositivo/emulador conectado; en este entorno no había ninguno disponible (`adb devices` vacío). Se puede usar `tools/run-instrumented-tests.ps1`.
- **Lint**: sin errores críticos.
- **Tests unitarios**: 13 archivos en `app/src/test/`; **tests instrumentados** en `app/src/androidTest/` (2 archivos). No se ha medido cobertura en este snapshot.

---

## 📌 Historial de Consolidación

| Fecha | Commit | Contenido |
|---|---|---|
| 2025-01-XX | `59403f7` | v0.1.0 inicial |
| 2026-09-05 | `8239939` | Base de funcionalidad (working tree previo) |
| 2026-10-12 | (consolidación) | Features core + nuevo logo/ícono + versión en drawer + POST_NOTIFICATIONS + tests instrumentados + CI matriz |
