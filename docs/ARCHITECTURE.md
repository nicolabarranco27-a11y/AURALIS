# Arquitectura Técnica - Auralis

Auralis está construido siguiendo los principios de **Clean Architecture** y las guías oficiales de arquitectura de Google.

> **Estado**: Este documento describe la arquitectura de la versión entregable v0.1.0. Los cambios arquitectónicos que viven en el working tree se marcan explícitamente como **(pendiente de consolidación)**.

## 🧱 Capas del Sistema

### 1. Domain Layer (`domain/`)
La capa más interna y pura. Contiene la lógica de negocio y las reglas del reproductor.
- **Modelos**: Entidades simples como `Song`, `Album`, `PlaybackState`, `Playlist`, `QueueItem`, `RepeatMode`.
- **Repositorios**: Interfaces que definen el contrato de datos (`LibraryRepository`, `PlaybackRepository`, `PlaylistRepository`, `FavoritesRepository`).
- *Nota*: No tiene dependencias de Android.

### 2. Data Layer (`data/`)
Implementación de la persistencia y servicios externos.
- **Room**: Base de datos SQLite local para el catálogo y metadatos. Incluye `FavoritesDao`, `PlaylistDao`, `LibraryDao`.
- **MediaStore**: Origen de datos para el escaneo de archivos del dispositivo.
- **Media3 (ExoPlayer)**: Implementación real de la reproducción mediante `MediaSessionService`.
- **SharedPreferences (pendiente de consolidación)**: Almacenamiento ligero para configuraciones de audio (Ecualizador) en `EqualizerPreferences`.

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
- **Dispatchers.Default**: Procesamiento intensivo de CPU. **(pendiente de consolidación)** La generación de UUIDs en `HomeViewModel.onSongClick` se ha migrado aquí.
- **Dispatchers.IO**: Lectura de base de datos y sistema de archivos.
- **Dispatchers.Main**: Actualizaciones de UI y llamadas obligatorias de Media3.

## 🔊 Flujo de Audio y Efectos (v0.1.0)

En la versión 0.1.0, el procesamiento de audio es el estándar de ExoPlayer sin efectos extra.

### Ecualizador (pendiente de consolidación)

La implementación prevista es:
- `PlaybackService` detecta el `audioSessionId` dinámico vía `Player.Listener.onAudioSessionIdChanged`.
- `EqualizerPreferences` (`SharedPreferences`) provee los valores guardados.
- Al cambiar sesión, se libera el `Equalizer` previo y se crea uno nuevo con la sesión actual.
- Cada banda se mapea de dB a milibel usando `bandLevelRange` del dispositivo.

> **Importante**: Instanciar `android.media.audiofx.Equalizer` siempre dentro de `try-catch` para tolerar dispositivos sin soporte de hardware.

## 📦 Distribución de Paquetes

- `core/`: tipos transversales (Result, Dispatchers) y sistema de diseño.
- `data/`: persistencia, MediaStore, Media3, repositorios.
- `domain/`: modelos e interfaces (sin Android).
- `feature/<pantalla>/`: composables + ViewModels + estados UI.
- `di/`: módulos Hilt.

## ✅ Cobertura de Tests

Existen tests unitarios en `app/src/test/` para:
- Capa de datos: `LibraryDaoTest`, `PlaylistDaoTest`, `MappersTest`, `LibraryScanSchedulerTest`, `LibraryScanWorkerTest`, `LibraryScannerTest`, `RoomFavoritesRepositoryTest`, `RoomPlaylistRepositoryTest`, `InMemoryPlaybackRepositoryTest`.
- Dominio: `DomainPurityTest`, `DomainModelsTest`, `AppResultTest`.
- UI: `HomeViewModelTest` (incluye `FakeFavoritesRepository` **(pendiente de consolidación)**).
