# Arquitectura Técnica - Auralis

Auralis está construido siguiendo los principios de **Clean Architecture** y las guías oficiales de arquitectura de Google.

> **Estado**: Este documento describe la arquitectura actualmente presente en el repositorio (2026-10-12; versión actual `0.2.0`, commit/tag pendientes).

## 🧱 Capas del Sistema

### 1. Domain Layer (`domain/`)
La capa más interna y pura. Contiene la lógica de negocio y las reglas del reproductor.
- **Modelos**: Entidades simples como `Song`, `Album`, `PlaybackState`, `Playlist`, `QueueItem`, `RepeatMode`.
- **Repositorios**: Interfaces que definen el contrato de datos (`LibraryRepository`, `LibraryFolderRepository`, `PlaybackRepository`, `PlaylistRepository`, `FavoritesRepository`).
- *Nota*: No tiene dependencias de Android.

### 2. Data Layer (`data/`)
Implementación de la persistencia y servicios externos.
- **Room**: Base de datos SQLite local para el catálogo y metadatos. Incluye `FavoritesDao`, `PlaylistDao`, `LibraryDao`.
- **MediaStore**: Origen de datos para el escaneo de archivos del dispositivo. Traduce cada carpeta elegida por el usuario (tree URI de SAF) a su ruta dentro de MediaStore y filtra por `RELATIVE_PATH` en API 29+ o `DATA` en API < 29.
- **Media3 (ExoPlayer)**: Implementación real de la reproducción mediante `MediaSessionService`.
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
- **Dispatchers.Default**: Procesamiento intensivo de CPU. La generación de UUIDs en `HomeViewModel.onSongClick` se ejecuta aquí.
- **Dispatchers.IO**: Lectura de base de datos y sistema de archivos.
- **Dispatchers.Main**: Actualizaciones de UI y llamadas obligatorias de Media3.

## 🗂 Selección de Carpetas y Escaneo

- El usuario elige carpetas con el **explorador de documentos de Android** (`ActivityResultContracts.OpenDocumentTree`, SAF) desde la vista **Canciones** (icono de carpeta) o desde el estado vacío de Inicio; Ajustes ofrece la misma gestión.
- Cada carpeta elegida se persiste como **tree URI** en `MusicFolderPreferences` (`SharedPreferences`, `auralis_library_prefs`, expuesta como `StateFlow`) a través de `LibraryFolderRepository` (`addFolder`/`removeFolder`). La UI usa `MusicFoldersSheet` (`feature/folders/FolderSelection.kt`) y `folderDisplayName()` para mostrar la ruta legible.
- La app **siempre arranca en `Routes.HOME`** (no hay onboarding ni gate de `startDestination`).
- `MediaStoreSource.getAudioFiles(folderTreeUris)` escanea por defecto **toda la música del dispositivo** (`IS_MUSIC != 0`) y **suma** el audio de las carpetas elegidas: de cada tree URI (`DocumentsContract.getTreeDocumentId`) deriva `(volumen, ruta relativa)` y comprueba pertenencia por `VOLUME_NAME` + `RELATIVE_PATH` (API 29+, el volumen SAF `primary` se mapéa a `external_primary`) o por prefijo de `DATA` en memoria (API < 29). `AudioPathFilter` descarta siempre el audio que no es música.
- `DefaultLibraryScanner.sync()` lee `MusicFolderPreferences.selectedFolders()` y **escanea siempre**, aunque no haya carpetas (el escaneo por defecto ya cubre la música del dispositivo).
- Al re-escanear, las canciones que ya no aparecen se marcan `isAvailable = 0` (eliminación lógica), preservando playlists y favoritos.

## 🔊 Flujo de Audio y Efectos

La reproducción es el estándar de ExoPlayer (Media3), sin efectos extra por defecto.

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
- Capa de datos: `LibraryDaoTest`, `PlaylistDaoTest`, `MappersTest`, `LibraryScanSchedulerTest`, `LibraryScanWorkerTest`, `LibraryScannerTest`, `RoomFavoritesRepositoryTest`, `RoomPlaylistRepositoryTest`, `InMemoryPlaybackRepositoryTest`, `MusicFolderPreferencesTest`.
- Dominio: `DomainPurityTest`, `DomainModelsTest`, `AppResultTest`.
- UI: `HomeViewModelTest` (con `FakeFavoritesRepository`).

### Tests instrumentados (`app/src/androidTest/`)
- `LibraryDaoInstrumentedTest`: operaciones del DAO contra el SQLite real del dispositivo.
- `AppLaunchSmokeTest`: arranque de la app y verificación del Drawer/versión (con `GrantPermissionRule` + `RuleChain`).

La CI (`.github/workflows/android-ci.yml`) ejecuta ambos conjuntos: unit+lint y los instrumentados en una matriz de emuladores (API 26 / 29 / 33 / 36).
