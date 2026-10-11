# Changelog

Todas las versiones notables de este proyecto se documentan en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/),
y este proyecto adhiere a [Semantic Versioning](https://semver.org/lang/es/).

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

### Technical
- Min SDK 26 (Android 8.0)
- Target SDK 36 (Android 14)
- Compile SDK 37 (Android 15)
- Kotlin 2.3.21, AGP 9.3.2
- Compose BOM 2026.08.00
- `versionName = "0.1.0"`, `versionCode = 1`

---

## [Unreleased] — En desarrollo

> **IMPORTANTE**: Los cambios listados a continuación existen únicamente en el **working tree** del repositorio local. **No están commiteados todavía** y, por lo tanto, no forman parte de ninguna versión entregable. Se identifican aquí para mantener trazabilidad hasta su consolidación.

### Pendientes de consolidación (working tree)

#### Funcionalidad nueva (terminada en working tree)
- **Búsqueda Inteligente (Autocomplete)**: Sistema de sugerencias en `HomeViewModel` con `transformLatest` + debounce, normalización de acentos y mayúsculas vía `REPLACE`/`LOWER` en SQLite. UI en `HomeScreen` con `SearchSuggestionsOverlay`.
- **Toggle de Favoritos en filas**: Botón de corazón en cada `SongItem` (Home, Songs, AlbumDetail, ArtistDetail, PlaylistDetail) cableado a `FavoritesRepository.toggleFavorite`.
- **Ecualizador real con AudioFX**: Integración en `PlaybackService` con captura dinámica de `audioSessionId` y aplicación automática de bandas guardadas. UI completa en `PlayerScreen` (ModalBottomSheet) con presets (Normal, Bass Boost, Rock, Pop) y `VerticalFader` nativo.
- **Persistencia de Ecualizador**: `EqualizerPreferences` (SharedPreferences) guarda preset + 5 bandas.
- **Detalles de Pista**: Diálogo accesible desde el menú `MoreVert` del Player con título, artista, álbum, duración y origen.
- **Pantalla de Ajustes (SettingsScreen)**: Reemplaza el placeholder anterior. Lista de opciones visuales con scroll.
- **Drawer con item seleccionado**: Pill visual + texto en negrita cuando coincide con `currentRoute`.
- **Carrusel "Acceso Rápido" en Home**: `LazyRow` con hasta 5 canciones (preferentemente favoritas).
- **Filtros por chips en Home**: "Todas" / "Favoritos".

#### UI/UX
- **AuralisArtwork fallback con `MusicNote`**: Cuando no hay carátula, se muestra un icono de nota musical en el color de acento en lugar del placeholder corporativo.
- **PlayerScreen rediseñado**: Carátula estilo vinilo circular con shadow, botón Play con halo radial glow, visualizador animado en el top bar (`AudioEqualizerVisualizer`).
- **Mini-player rediseñado**: Reemplaza `Slider` por `LinearProgressIndicator` de 3dp.
- **Slider de cover en AlbumDetail**: Prioriza el primer `coverReference` no vacío.
- **Iconos Material Rounded**: Reemplazo de glifos Unicode por `Icons.Rounded.*` (Search, Shuffle, PlayArrow, Pause, MoreVert, Tune, Timer, Info, MusicNote).

#### Correcciones
- **HomeViewModel.onSongClick**: La generación de UUIDs para `QueueItem` se mueve a `Dispatchers.Default` para no bloquear el hilo principal al reproducir desde listas grandes.
- **AlbumDetail cover**: Filtra `coverReference` vacíos para evitar mostrar placeholder cuando hay canciones con cover válido.
- **Escaneo no detectaba música descargada [causa raíz]**: `MediaStore.Audio.Media.DATA` se incluía en la proyección de `MediaStoreSource.queryAudio`. En Android 11+ con `targetSdk >= 30` esa columna interna está bloqueada y el proveedor **rechaza la consulta completa** con `SecurityException`, abortando el escaneo. Se elimina `DATA` de la proyección (el campo `path` queda como `null`) y las lecturas de string por fila se hacen defensivas (`safeGetString`) para que un valor anómalo en una fila no tumbe todo el scan.
- **Escaneo no detectaba música descargada [IS_MUSIC]**: El filtro `IS_MUSIC != 0` excluía archivos indexados con `IS_MUSIC = 0` (p. ej. descargas en carpetas `Download/`). Se elimina el filtro: se consultan todos los audios de MediaStore.
- **Escaneo automático de arranque**: `HomeViewModel.onScreenShown` solicita un escaneo si ya hay permiso de audio y la biblioteca está vacía, para que la música descargada aparezca sin pulsar manualmente "Escanear".

#### Permisos y dependencias
- `MODIFY_AUDIO_SETTINGS` agregado a `AndroidManifest.xml` (necesario para `android.media.audiofx.Equalizer`).
- `material-icons-extended` y `material-icons-core` agregados al catálogo (`libs.versions.toml`) e implementados en `app/build.gradle.kts`.

#### Tests
- `HomeViewModelTest`: Se agrega `FakeFavoritesRepository` y se incorpora a la factory de construcción del VM.

---

## Próxima versión pública

La próxima versión entregable consolidará los cambios listados arriba. Hasta que se produzca un commit formal y se bumpee la versión, **no debe considerarse como parte estable del repositorio**.

### Convenciones de versionado
- **MAJOR**: Cambios incompatibles en API pública
- **MINOR**: Funcionalidad nueva compatible hacia atrás
- **PATCH**: Bugfixes compatibles hacia atrás

Ejemplo: `1.2.3` → `versionCode = 10203` (major*10000 + minor*100 + patch)
