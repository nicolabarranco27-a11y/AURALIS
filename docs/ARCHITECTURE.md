# Arquitectura Técnica - Auralis

Auralis está construido siguiendo los principios de **Clean Architecture** y las guías oficiales de arquitectura de Google.

> **Estado**: Este documento describe la arquitectura actualmente presente en el repositorio (2026-10-14; versión actual `0.2.0`, consolidada en `8304e58` + tag `v0.2.0`). La extracción de color y el **ritmo del aura** del reproductor, la **gestión de biblioteca (ocultar canciones, Room v5)** y el **chip/rediseño de Playlists** ya están **consolidados en `origin/main`**.

## 🧱 Capas del Sistema

### 1. Domain Layer (`domain/`)
La capa más interna y pura. Contiene la lógica de negocio y las reglas del reproductor.
- **Modelos**: Entidades simples como `Song`, `Album`, `PlaybackState`, `Playlist`, `PlaylistSummary`, `QueueItem`, `RepeatMode`.
- **Repositorios**: Interfaces que definen el contrato de datos (`LibraryRepository`, `LibraryFolderRepository`, `PlaybackRepository`, `PlaylistRepository`, `FavoritesRepository`, `ArtworkColorRepository`).
- *Nota*: No tiene dependencias de Android.

### 2. Data Layer (`data/`)
Implementación de la persistencia y servicios externos.
- **Room**: Base de datos SQLite local para el catálogo y metadatos. Incluye `FavoritesDao`, `PlaylistDao`, `LibraryDao`.
- **MediaStore**: Origen de datos para el escaneo de archivos del dispositivo. Recorre el subárbol de cada carpeta elegida por el usuario (tree URI de SAF) y **solo** acepta el audio que cae dentro de alguna de ellas, resolviendo la pertenencia por `VOLUME_NAME` + `RELATIVE_PATH` (API 29+) o por prefijo de `DATA` (API < 29).
- **Media3 (ExoPlayer)**: Implementación real de la reproducción mediante `MediaSessionService`.
- **Color de carátula**: `MediaArtworkColorRepository` decodifica la portada reducida (64px) y analiza sus píxeles para derivar un color dominante, con caché en memoria (`LruCache`). La heurística pura vive en `core/common/color/DominantColor.kt` (sin Android). El cálculo intensivo se ejecuta en `@DefaultDispatcher` y el decode en `@IoDispatcher`.
- **SharedPreferences**: Almacenamiento ligero para configuraciones de audio (Ecualizador) en `EqualizerPreferences`, preferencia de tema en `ThemeController` y **carpetas de música elegidas** en `MusicFolderPreferences` (`auralis_library_prefs`), todas expuestas como `StateFlow` y con migración a DataStore prevista (`docs/RELEASE_PLAN.md`).

### 3. UI Layer (`feature/` + `core/ui/`)
Capa de presentación organizada por funcionalidades.
- **MVVM**: Cada pantalla tiene su `ViewModel` que expone un `StateFlow`.
- **Jetpack Compose**: Interfaz de usuario declarativa.
- **Navigation Compose**: Gestión de rutas y transiciones (`ReproductorNavHost`).

## 💉 Inyección de Dependencias

Se utiliza **Hilt** para gestionar las dependencias en todo el grafo de la aplicación:
- `DatabaseModule`: Provee DAOs y la instancia de Room.
- `RepositoryModule`: Vincula las interfaces del dominio con sus implementaciones de datos.
- `DispatcherModule`: Provee los contextos de ejecución (IO, Default, Main).

## 🧵 Gestión de Hilos y Concurrencia

- **Coroutines**: Para todas las tareas asíncronas.
- **Dispatchers.Default**: Procesamiento intensivo de CPU. La generación de UUIDs en `HomeViewModel.onSongClick`, el análisis de color dominante de la carátula (`MediaArtworkColorRepository`), el mapping Room→Dominio de `RoomLibraryRepository.observe*()` y los derivados pesados de Inicio (`folders`/`popularSongs`) se ejecutan aquí.
- **Dispatchers.IO**: Lectura de base de datos y sistema de archivos.
- **Dispatchers.Main**: Actualizaciones de UI y llamadas obligatorias de Media3.

### Inicio: derivados de biblioteca vs. tick de reproducción

`HomeViewModel` separa el estado de reproducción de los derivados del catálogo para no recalcular trabajo O(n log n) en cada tick:

- `songsFlow` / `selectedFoldersFlow`: `observeSongs()` / `observeSelectedFolders()` con `distinctUntilChanged()`, `flowOn(Dispatchers.Default)` y `shareIn(WhileSubscribed(5 s), replay = 1)` (una sola suscripción a Room).
- `libraryDerivedFlow`: `combine(songsFlow, selectedFoldersFlow)` que calcula `HomeFolder` y `popularSongs`; se recalcula **solo cuando cambia el catálogo**, nunca por `positionMs`.
- `catalogFlow`: agrupa canciones/carpetas/estado de escaneo/playlists.
- El `combine` final (`coreFlow` + búsqueda + favoritos) solo **ensambla** referencias ya calculadas, por lo que un tick de reproducción (500 ms) es O(1).
- `RoomLibraryRepository.observeSongs()` aplica `.flowOn(Dispatchers.Default)` para que el mapping de entidades a dominio no corra en `Main`.

## 🗂 Selección de Carpetas y Escaneo

- El usuario elige carpetas con el **explorador de documentos de Android** (`ActivityResultContracts.OpenDocumentTree`, SAF) desde la vista **Canciones** (icono de carpeta) o desde el estado vacío de Inicio; Ajustes ofrece la misma gestión.
- Cada carpeta elegida se persiste como **tree URI** en `MusicFolderPreferences` (`SharedPreferences`, `auralis_library_prefs`, expuesta como `StateFlow`) a través de `LibraryFolderRepository` (`addFolder`/`removeFolder`). La UI usa `MusicFoldersSheet` (`feature/folders/FolderSelection.kt`) y `folderDisplayName()` para mostrar la ruta legible.
- La app **siempre arranca en `Routes.HOME`** (no hay onboarding ni gate de `startDestination`).
- `MediaStoreSource.getAudioFiles(folderTreeUris)` indexa **solo** el audio que está dentro de las carpetas elegidas: de cada tree URI (`DocumentsContract.getTreeDocumentId`) deriva `(volumen, ruta relativa)` y comprueba pertenencia por `VOLUME_NAME` + `RELATIVE_PATH` (API 29+, el volumen SAF `primary` se mapéa a `external_primary`) o por prefijo de `DATA` en memoria (API < 29). `matchedSelectedFolder` devuelve la carpeta **más específica** que contiene cada archivo (o se descarta la fila) y esa carpeta se persiste en `sourceFolderUri`. `AudioPathFilter` sigue descartando el audio que no es música.
- `DefaultLibraryScanner.sync()` lee `MusicFolderPreferences.selectedFolders()` y escanea las carpetas elegidas; sin carpetas la biblioteca queda vacía (Inicio muestra el estado "Añadir carpeta").
- Al re-escanear, las canciones que ya no aparecen se marcan `isAvailable = 0` (eliminación lógica), preservando playlists y favoritos.

## 🙈 Biblioteca: ocultar canciones y resumen de playlists

- **Ocultar (reversible)** — Room v5: `SongEntity.isHidden` (migración `MIGRATION_4_5`, `DEFAULT 0`). `LibraryDao` filtra `isHidden = 0` en canciones, álbumes, artistas, búsqueda y canciones de playlist; `FavoritesDao.observeFavoriteIds` también excluye ocultas. `DefaultLibraryScanner` **preserva** `isHidden` en el upsert (un re-escaneo no restaura una canción oculta). La restauración usa `LibraryDao.observeHiddenSongs`/`getHiddenSongs` y `setHidden`.
- **Estadísticas de playlist** — `PlaylistDao.observePlaylistSummaries()` devuelve `PlaylistSummaryRow` (proyección) calculando por SQL `songCount`, `totalDurationMs` y `coverUri` mediante subconsultas que **excluyen canciones ocultas** (`s.isHidden = 0`). La materia prima es la tabla `songs`, por lo que el Flow se reemite cuando cambia la biblioteca. `PlaylistRepository` la expone como `List<PlaylistSummary>`.
- Ambos están **consolidados en `origin/main`** (2026-10-14).

## 🗂 Catálogo por carpetas seleccionadas

- **Alcance de la biblioteca** — el catálogo son **exclusivamente** las canciones dentro de las carpetas elegidas por el usuario (SAF). `MediaStoreSource` recorre el árbol de cada carpeta seleccionada y descarta cualquier audio que no esté dentro de una de ellas (`matchedSelectedFolder` devuelve la carpeta **más específica** que lo contiene, o se omite la fila). Ya no se indexa toda la música del dispositivo (`IS_MUSIC` global eliminado).
- **Persistencia** — Room v6: `SongEntity.sourceFolderUri` (migración `MIGRATION_5_6`, `DEFAULT NULL`, índice `index_songs_sourceFolderUri`) guarda el **tree URI** de la carpeta que contiene la canción; es la identidad con la que Inicio agrupa. `DefaultLibraryScanner` la persiste y `hasChanged` la compara.
- **Helpers puros** — `core/common/folder/SelectedFolders.kt`: `songsInSelectedFolder(songs, folderUri)` (filtra por carpeta; `null` = toda la biblioteca) y `libraryFolders(songs, selectedUris)` (resumen por carpeta: uri, `songCount`, portada). `core/common/library/MostPlayedSongs.kt`: `mostPlayedSongs(songs, limit)` (más reproducidas → favoritas → primeras). Sin dependencias de Android.
- **UI y cola acotada** — En Inicio → "Todas" (`HomeScreen`) la fila de **carpetas** (`FolderBrowser`/`FolderItem`) es **persistente**: no hay pasos de entrar/volver; la carpeta activa se **resalta** (`selected` en `FolderItem`) y se **cambia con un solo toque**; **re-tocar la carpeta activa la colapsa** (`selectedFolderUri = null`) y la fila se **auto-desplaza** a la activa (`rememberLazyListState` + `animateScrollToItem`). **La selección de carpeta persiste al cambiar entre chips** (`selectedFilter`), ya que no se resetea al salir de "Todas". `selectedFolderUri` (`rememberSaveable`) selecciona la carpeta; debajo se muestra su **lista plana recursiva** (`songsForList = if (selectedFolderUri != null) folderSongs else emptyList()`), de modo que **sin carpeta seleccionada no se listan canciones** (pista breve). El botón ▶ encabezado reproduce la carpeta; `HomeViewModel.onSongClick(song, scope)` y `playAll(scope)` fijan la **cola a la carpeta mostrada**, evitando mezclar toda la biblioteca.
- **Acceso rápido** — `HomeViewModel` expone `popularSongs` (vía `mostPlayedSongs`), alimentado por el registro de reproducciones de `Media3PlaybackRepository`.
- **Consolidado en `origin/main`** (2026-10-15).

## 🔊 Flujo de Audio y Efectos

La reproducción es el estándar de ExoPlayer (Media3), sin efectos extra por defecto.

### Amplitud para el aura (sin permisos)

- `PlaybackService` construye ExoPlayer con `AuraRenderersFactory` (un `DefaultRenderersFactory` propio) que inyecta `RhythmAudioProcessor` en el `DefaultAudioSink`.
- `RhythmAudioProcessor` (`feature/playback`) es un `AudioProcessor` passthrough: analiza el PCM de 16 bits que ya circula, separa **graves** (paso bajo ~150 Hz) y **agudos** (resta), sigue una envolvente de pico por banda y detecta los **ataques** contra una media adaptativa; publica en `PlaybackRhythm` (`core/common/audio`) un nivel que combina el volumen con un "pulso" de caída **adaptada al tempo** estimado.
- `PlaybackEnergy` (aura) lee `PlaybackRhythm.currentLevel()` en fase de dibujo y lo mezcla con su pulso sintético; si no hay señal fresca (500 ms) usa solo el sintético. **No se usa `Visualizer` ni `RECORD_AUDIO`.**

### Ecualizador (AudioFX)

- `PlaybackService` detecta el `audioSessionId` dinámico vía `Player.Listener.onAudioSessionIdChanged`.
- `EqualizerPreferences` (`SharedPreferences`) provee los valores guardados y el preset activo (Normal / Bass Boost / Rock / Pop).
- Al cambiar sesión, se libera el `Equalizer` previo y se crea uno nuevo con la sesión actual.
- Cada banda se mapea de dB a milibel usando `bandLevelRange` del dispositivo.
- La UI del ecualizador vive en `PlayerScreen` y usa el composable propio `VerticalFader`.

> **Importante**: Instanciar `android.media.audiofx.Equalizer` siempre dentro de `try-catch` para tolerar dispositivos sin soporte de hardware.

## 📦 Distribución de Paquetes

- `core/`: tipos transversales (Result, Dispatchers) y sistema de diseño.
- `data/`: persistencia, MediaStore, Media3, repositorios.
- `domain/`: modelos e interfaces (sin Android).
- `feature/<pantalla>/`: composables + ViewModels + estados UI.
- `di/`: módulos Hilt.

## ✅ Cobertura de Tests

### Tests unitarios (`app/src/test/`)
- Capa de datos: `LibraryDaoTest`, `PlaylistDaoTest`, `MappersTest`, `LibraryScanSchedulerTest`, `LibraryScanWorkerTest`, `LibraryScannerTest`, `RoomFavoritesRepositoryTest`, `RoomPlaylistRepositoryTest` (incluye resúmenes de playlist y edición de descripción), `InMemoryPlaybackRepositoryTest`, `MusicFolderPreferencesTest`, `AudioPathFilterTest`, `QueueInsertIndexCalculatorTest`.
- Dominio: `DomainPurityTest`, `DomainModelsTest`, `AppResultTest`.
- Core: `FolderLabelTest`, `TimeFormatTest`, `ThemeModeTest`, `DominantColorTest` (heurística de color del aura).
- UI: `HomeViewModelTest`, `FavoritesViewModelTest`, `QueueViewModelTest`, `PlayerViewModelTest` (incluye el aura: respaldo y color dominante), `HiddenSongsViewModelTest`, `PlaylistsViewModelTest`.

### Tests instrumentados (`app/src/androidTest/`)
- `LibraryDaoInstrumentedTest`: operaciones del DAO contra el SQLite real del dispositivo.
- `AppLaunchSmokeTest`: arranque de la app y verificación del Drawer/versión (con `GrantPermissionRule` + `RuleChain`).

La CI (`.github/workflows/android-ci.yml`) ejecuta ambos conjuntos: unit+lint y los instrumentados en una matriz de emuladores (API 26 / 29 / 33 / 36).
