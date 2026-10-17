# Changelog

Todas las modificaciones relevantes de Auralis se documentan en este archivo.
El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y
el proyecto se adhiere a [Semantic Versioning](https://semver.org/lang/es/).

## [Unreleased]

### Added
- **Aura de color animada y viva en el reproductor**: el vinilo de `PlayerScreen` se rodea de un aura derivada del **color dominante de la carátula** (`VinylAura`), reforzada para que **se note de verdad** aunque la portada sea oscura: el color se **satura y aclara en HSV** (`vividly`) y se dibuja un **disco de luz amplio** más un **halo con el pico en el borde de la carátula** y **cuatro brumas** de color (base, aclarado y dos matices desviados) que orbitan a velocidades distintas. Toda la luz **respira** (escala global) y las brumas **laten y dan un empuje angular en cada golpe** de la canción, con un **movimiento fluido** (la energía se suaviza en el tiempo y se le suma una **deriva ambiental continua** para que el aura nunca quede quieta). El ritmo se obtiene **sin permisos** analizando el PCM que ya pasa por Media3 (`RhythmAudioProcessor` + `PlaybackRhythm`): se separan **graves (bombo/bajo) y agudos (caja/platillos)** y se detectan los **ataques** contra una media adaptativa, con una caída del pulso **adaptada al tempo** estimado; cuando no hay señal se usa un pulso sintético. El halo del botón Play y el visualizador del top bar se tiñen con ese color. Si la carátula no aporta color útil se usa el rojo vivo de respaldo `AuraFallbackColor` (`#C62828`).
- **Barra inferior (mini-player) unificada y minimalista**: una única `MiniPlayerBar` compartida —carátula, metadatos, play/pausa y una **línea de progreso fina e interactiva**— en **todas** las pantallas. Se elimina la `PlaybackBar` privada de Inicio y el `Slider` con etiquetas de tiempo del componente compartido; el progreso se puede **pulsar o arrastrar para saltar** (scrubber) mostrando el tiempo **solo mientras se ajusta**.
- **Extracción de color dominante sin dependencias** (`core/common/color/DominantColor.kt`, `MediaArtworkColorRepository`): decodifica la carátula reducida a 64px, analiza los píxeles y cachea el resultado en memoria (`LruCache`). Cálculo pesado fuera del hilo principal.
- **Ocultar canciones de la biblioteca (reversible)**: long-press sobre cualquier fila de canción abre una hoja inferior (`SongActionsSheet`) con la acción "Ocultar de la biblioteca". La canción desaparece de Inicio, Canciones, Favoritos, Álbumes, Artistas y Playlists, pero **no se borra el archivo** del dispositivo; el flag `isHidden` sobrevive a los re-escaneos. Nueva pantalla **Ajustes → Canciones ocultas** (`HiddenSongsScreen`) para **restaurar** una o todas.
- **Fila de canción unificada** (`core/ui/components/SongListItem.kt`): componente único compartido por todas las listas (tap, long-press, swipe-to-queue, toggle de favoritos y `trailingContent` contextual), en sustitución de las filas duplicadas por pantalla.
- **Chip "Playlists" en Inicio**: junto a "Todas" y "Favoritos", muestra las playlists inline (portada, número de canciones y duración total) con un botón "+" para crear una playlist rápida; tocar una navega a su detalle y **mantener pulsada revela un botón de tacho de basura** a la derecha de la fila, que abre una **confirmación minimalista** antes de borrar. El tacho **se oculta solo a los 3 segundos** si el usuario no interactúa y **se cancela si se toca cualquier otra parte de la pantalla**.
- **Estadísticas de playlist** (`PlaylistSummary`): número de canciones visibles, duración total y portada de referencia, calculadas en SQL (subconsultas correlacionadas) y expuestas vía `PlaylistRepository.observePlaylistSummaries()`.
- **Rediseño del apartado de Playlists**: la lista usa portadas reales (`AuralisArtwork`) y un menú por playlist (Editar / Eliminar con confirmación); el detalle muestra portada, descripción, métricas, "Reproducir / Aleatorio / Añadir" y reordenación de canciones (Mover arriba / abajo).
- **Edición de playlist**: nombre **y descripción** (`CreatePlaylistDialog` / `EditPlaylistDialog` / `ConfirmDeleteDialog` en `core/ui/components/PlaylistDialogs.kt`), reutilizados por Inicio y ambas pantallas de playlists.
- **Catálogo por carpetas seleccionadas**: la app cataloga **solo** las canciones que están dentro de las carpetas elegidas por el usuario en Ajustes (SAF). En Inicio → "Todas" se muestra una **fila horizontal persistente** con las **carpetas seleccionadas** (portada, número de canciones y botón ▶ para reproducirla). La carpeta activa se **resalta** y se puede **cambiar con un solo toque** (carpeta 1 → 2 → 3 → 2 → 1), sin pasos de "entrar/volver"; **re-tocar la carpeta activa la colapsa** (deja de mostrar sus canciones) y la fila se **desplaza automáticamente** hasta la carpeta activa. La selección de carpeta **se mantiene al cambiar entre "Todas", "Favoritos" y "Playlists"**. Las canciones de la carpeta elegida aparecen debajo como su **lista plana recursiva** (incluye subcarpetas, sin niveles intermedios) y la **cola de reproducción se limita a esa carpeta** (ya no se mezcla toda la biblioteca). Mientras **no haya una carpeta seleccionada no se listan canciones** (se muestra una pista breve). Botón "Añadir carpeta" en la fila; si no hay ninguna, se muestra un **estado vacío** con ese botón y el resto de apartados quedan vacíos. Las canciones fuera de las carpetas seleccionadas no aparecen en ninguna pantalla.
- **Acceso rápido basado en lo más escuchado**: la fila de "Acceso rápido" de Inicio muestra las canciones **más reproducidas** (`playCount`), desempatando por la última reproducción; si aún no hay reproducciones usa las **favoritas** y, si tampoco, las primeras de la biblioteca. Se empieza a registrar cada reproducción al iniciar una canción distinta.

### Changed
- **Licencia**: el código pasa de **Apache License 2.0** a **PolyForm Noncommercial License 1.0.0** (source-available, **uso no comercial**); el titular reserva todos los derechos. Se actualizan `README.md` y `CONTRIBUTING.md` (contribuciones solo con acuerdo previo/CLA), se agrega `THIRD_PARTY_NOTICES.md`, cabeceras de copyright en el código (`app/src`) y el aviso de derechos/licencia en la página web. Las versiones ya distribuidas bajo Apache 2.0 conservan sus derechos.
- **`PlayerScreen` — controles de transporte**: los glifos Unicode (`↝ « » ⟳ ‖ ▶`) se reemplazan por `Icons.Rounded` (`Shuffle`, `SkipPrevious`, `PlayArrow`/`Pause`, `SkipNext`, `Repeat`/`RepeatOne`) con diseño minimalista (sin relleno salvo el estado activo), micro-animación de presión y distribución `SpaceEvenly`.
- **`PlayerScreen` — barra de progreso (seek)**: track con gradiente activo y **thumb de un solo color sólido** (`auraColor`, con escala al arrastrar) — sin el anillo de dos colores anterior.
- **`PlayerScreen` — proporción del vinilo/aura**: la carátula pasa al 72% del contenedor (que sube al 98%) para que el aura tenga más espacio alrededor de la portada.
- **`PlayerScreen` — aura reactiva al ritmo real (afinada)**: más movimiento y mejor seguimiento del ritmo, ahora **más fluido**. El aura responde a **graves y agudos** (sin permisos), con un pulso que se **apaga según el tempo**, y suma una respiración global (escala) y un empuje angular de las brumas en cada golpe. La señal se **suaviza en el tiempo** (subida rápida, bajada fluida) y se le añade una **deriva ambiental continua** para que nunca quede quieta; el pulso sintético solo rellena cuando no hay señal. El tono rojo de respaldo es más vivo (`#C62828`).
- **Inicio — encabezado de sección según el filtro**: el título muestra "CANCIONES" / "FAVORITOS" / "PLAYLISTS" según el chip activo (antes "CANCIONES" fijo).
- **Inicio — el filtro se conserva al navegar**: `selectedFilter` usa `rememberSaveable`, de modo que al volver del detalle de una playlist el chip no se resetea a "Todas".
- **Mini-player — progreso interactivo**: la línea de progreso pasa a ser un scrubber con área táctil de 22dp (pulsar o arrastrar para saltar); la línea se engrosa al ajustar y se muestra el tiempo (`mm:ss / mm:ss`) **solo durante el gesto**.
- **Refresco de posición más fluido**: `MusicServiceConnection` actualiza la posición cada **500 ms** durante la reproducción (antes 1000 ms), para que la barra avance con suavidad.

### Technical
- **Detección de ritmo sin permisos** (`RhythmAudioProcessor` + `PlaybackRhythm` + `AuraRenderersFactory`): un `AudioProcessor` passthrough de Media3 lee el PCM del audio sink, separa **graves** (paso bajo ~150 Hz) y **agudos** (resta), calcula la envolvente de pico por banda y detecta los **ataques** contra una media adaptativa; el "pulso" se apaga con una caída **adaptada al tempo** estimado entre ataques. `PlaybackService` usa un `DefaultRenderersFactory` personalizado para inyectarlo en `DefaultAudioSink`.
- **Room v5** con migración `4→5` (columna `isHidden` en `songs`, `DEFAULT 0`). Las consultas de biblioteca (canciones, álbumes, artistas, búsqueda, favoritos y canciones de playlist) filtran `isHidden = 0`; el escaneo preserva el flag.
- **Room v6** con migración `5→6` que añade la columna `sourceFolderUri` en `songs` (+ índice `index_songs_sourceFolderUri`, `DEFAULT NULL`). `MediaStoreSource` indexa **solo** audio dentro de las carpetas seleccionadas y asocia cada canción al tree URI de la carpeta **más específica** que la contiene (`matchedSelectedFolder`); las canciones sin carpeta se descartan. `DefaultLibraryScanner` persiste `sourceFolderUri` y `hasChanged` la compara. Helpers puros en `core/common/folder/SelectedFolders.kt` (`songsInSelectedFolder`, `libraryFolders`) y `core/common/library/MostPlayedSongs.kt` (`mostPlayedSongs`).
- **Registro de reproducciones**: `LibraryDao.registerPlay(id, playedAtEpochMs)` incrementa `playCount` y actualiza `lastPlayedAtEpochMs`; `LibraryRepository.registerPlay(...)` lo expone y `Media3PlaybackRepository` registra una reproducción por canción distinta al empezar a sonar (soporta el "más escuchado").
- **`PlaylistDao`**: `observePlaylistSummaries()` (proyección `PlaylistSummaryRow`) y `updateDescription()`.
- **Rendimiento de Inicio (gama media-baja)**: `HomeViewModel` separa el catálogo compartido (`songsFlow`/`selectedFoldersFlow` con `shareIn(WhileSubscribed(5s))`), los derivados pesados (`libraryDerivedFlow` con `flowOn(Default)`) y el estado de reproducción; el `combine` final solo ensambla referencias (O(1) por tick). El mapping Room→Dominio de `RoomLibraryRepository` se movió a `Dispatchers.Default`. Prueba nueva: `los ticks de reproduccion no recalculan los derivados de biblioteca` (verifica identidad de `folders`/`popularSongs`).

### Fixed
- **La biblioteca ya no se vacía por un escaneo sin resultados**: `DefaultLibraryScanner` **no** marca canciones como no disponibles cuando el descubrimiento vuelve vacío (MediaStore aún no listo tras el arranque, volumen externo desmontado, SAF sin resolver o sin carpetas seleccionadas). Antes, ese escaneo vacío marcaba **toda** la biblioteca `isAvailable = 0` y la hacía desaparecer hasta el siguiente escaneo correcto. Nueva prueba `escaneo vacio no vacia la biblioteca`.
- **Las playlists y carpetas dejan de "desaparecer" con la biblioteca vacía**: la pantalla guiada "Sin música" de Inicio solo se muestra cuando no hay **nada** (ni canciones, ni playlists, ni carpetas). Si hay una biblioteca vacía pero existen playlists o carpetas, Inicio se mantiene visible para que sigan accesibles (antes la condición era solo `songs.isEmpty()` y ocultaba todo, incluida la lista de playlists, dando la sensación de que se habían borrado — no se borra ninguna playlist más allá de la acción explícita del usuario).
- **Carpetas elegidas explícitamente se respetan**: `MediaStoreSource` ya no aplica el `AudioPathFilter` (mensajería, grabaciones, sonidos del sistema) cuando la pista cae en una **subcarpeta concreta** elegida por el usuario; el filtro solo actúa cuando la selección es amplia (el volumen completo). Así una carpeta de música elegida a propósito no queda vacía por su nombre.
- **Copia de seguridad desactivada**: `android:allowBackup="false"` y reglas de exclusión (`backup_rules.xml` / `data_extraction_rules.xml`) que excluyen base de datos y preferencias, para que una restauración o transferencia no revierta playlists, favoritos ni el catálogo.
- **Inicio sin trabajo pesado en el hilo principal durante la reproducción**: se desacoplaron los *ticks* de reproducción (cada 500 ms) de los derivados de biblioteca. `folders` (agrupación por carpeta) y `popularSongs` (ordenado por reproducciones) ya **no** se recalculan sobre todo el catálogo en cada tick, sino **solo cuando cambia el catálogo** (canciones o carpetas seleccionadas). Los cómputos pesados y el mapeo Room→Dominio se ejecutan en `Dispatchers.Default` (`RoomLibraryRepository.observe*().flowOn(Default)` + `HomeViewModel` con `shareIn`/`distinctUntilChanged`). Mejora directa de fluidez y batería en gama media-baja.

## [0.2.0] - 2026-10-12

> **Estado**: release **v0.2.0** (`versionName = "0.2.0"`, `versionCode = 2`).
> Incluye el escaneo automático por defecto de la música del dispositivo y las
> carpetas adicionales elegidas con el explorador del sistema (SAF).

### Added
- **Selección de carpetas de música con el explorador del sistema** (`feature/folders/FolderSelection.kt`): desde la vista **Canciones** (icono de carpeta) y desde el estado vacío de Inicio se abre el selector de documentos de Android (`ActivityResultContracts.OpenDocumentTree`, Storage Access Framework) para **añadir una o más carpetas**; una hoja inferior lista las carpetas elegidas y permite quitarlas. Ajustes ofrece la misma gestión.
- **`LibraryFolderRepository`** (+ `DefaultLibraryFolderRepository`): `observeSelectedFolders()`, `selectedFolders()`, `addFolder(treeUri)`, `removeFolder(treeUri)`.
- **`MusicFolderPreferences`**: persistencia de los URIs de carpeta (tree URIs) en `SharedPreferences` (`auralis_library_prefs`) expuesta como `StateFlow`.
- **Helper `folderDisplayName(treeUri)`** (`core/common/format/FolderLabel.kt`) para mostrar la ruta legible de una carpeta elegida.
- **Nuevo ícono de la app (launcher)**: foreground del ícono adaptativo regenerado con el nuevo logotipo de Auralis en todas las densidades (`mdpi`→`xxxhdpi`), respetando la zona segura; fondo negro (`ic_launcher_background = #000000`).
- **Nuevo logo in-app**: el logo circular del header de Inicio (`HomeScreen`) usa el nuevo logotipo (`drawable/logo_auralis.png`).
- **Versión visible en el Drawer**: se muestra `v0.2.0` (vía `BuildConfig.VERSION_NAME`) alineado a la derecha junto a la sección EstalingradoCorp.
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
- **Escaneo automático por defecto + carpetas que se suman**: `MediaStoreSource` escanea por defecto toda la música del dispositivo (`IS_MUSIC`) y **suma** el audio de las carpetas elegidas por el usuario (tree URI de SAF) aunque el sistema no lo marque como música (p. ej. descargas). `DefaultLibraryScanner` inyecta `MusicFolderPreferences` y escanea siempre; `AudioPathFilter` descarta mensajería, grabaciones y sonidos del sistema.
- **Inicio sin bloqueo (sin onboarding)**: la app arranca siempre en Inicio (`Routes.HOME`); la selección de carpetas es una acción dentro de la app (Canciones / Ajustes), no una pantalla obligatoria previa.
- **Permiso `POST_NOTIFICATIONS` (Android 13+)**: ahora se solicita en tiempo de ejecución, necesario para mostrar los controles de reproducción en la notificación y la pantalla de bloqueo.
- **`SettingsScreen`**: reserva el inset inferior de navegación (`navigationBarsPadding()`) para no quedar bajo la barra de navegación.
- **`AuralisArtwork`**: fallback con icono `Icons.Rounded.MusicNote` en color de acento (reemplaza el placeholder corporativo).
- **Mini-player**: `LinearProgressIndicator` de 3dp en lugar de `Slider` (el seek se hace en `PlayerScreen`).
- **Iconografía**: reemplazo de glifos Unicode por `Icons.Rounded.*` / `Icons.AutoMirrored.Rounded.*`.
- **PlayerScreen**: carátula estilo vinilo circular, botón Play con halo, visualizador animado en el top bar.
- **Renombrado** `drawable/estalingradocorp_logo.png` → `estalingradocorp_logo.jpg` (el archivo era un JPEG mal nombrado que rompía `mergeReleaseResources`).

### Fixed
- **Audio que no es música mezclado con las canciones**: el escaneo ahora descarta audio de mensajería (WhatsApp, Telegram, Signal), grabaciones de voz/llamadas y sonidos del sistema (tonos, notificaciones, alarmas) aunque caigan dentro de una carpeta elegida. Nuevo `AudioPathFilter` (marcadores sobre `RELATIVE_PATH`/`DATA`).
- **Estado vacío en Inicio**: tras un escaneo sin resultados el estado vacío ya no cae en la vista principal vacía; se muestra la guía "Agregar carpetas" / "No encontramos música en tu dispositivo".
- **Filtrado por volumen en el escaneo**: `MediaStoreSource` combina `VOLUME_NAME` + `RELATIVE_PATH` (API 29+) para no mezclar carpetas homónimas de distintos volúmenes (p. ej. `Music` interno y `Music` en la SD); mapéa el volumen SAF `primary` → `external_primary`.
- **Compilación de `PlaybackService`**: se corrigió el uso inexistente de `session.commands`, se migró a `MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS`, `SessionError.ERROR_NOT_SUPPORTED` y se añadió `@OptIn(UnstableApi::class)` al callback.
- **Lint**: `android:forceDarkAllowed` con `tools:targetApi="q"` (API 29 con `minSdk 26`).
- **`AddToPlaylistViewModel`**: acceso null-safe al argumento de navegación (evita crash si falta `playlistId`).
- **Escaneo no detectaba música descargada [causa raíz]**: se eliminó `MediaStore.Audio.Media.DATA` de la proyección (bloqueada en Android 11+ con `targetSdk >= 30`; el proveedor rechazaba toda la consulta) y se hicieron defensivas las lecturas por fila (`safeGetString`).
- **Escaneo de música descargada**: el escaneo por defecto incluye toda la música del dispositivo (`IS_MUSIC`); el audio con `IS_MUSIC = 0` (p. ej. descargas) se sigue incorporando al agregar manualmente su carpeta, y `AudioPathFilter` mantiene fuera el audio que no es música.
- **Escaneo automático de arranque**: `HomeViewModel.onScreenShown` solicita el escaneo si ya hay permiso y la biblioteca está vacía.
- **Hilo principal en `HomeViewModel.onSongClick`**: la generación de UUIDs de `QueueItem` se movió a `Dispatchers.Default`.
- **Cover en AlbumDetail**: se prioriza el primer `coverReference` no vacío.

### Technical
- **Carpetas de audio**: `MediaStoreSource.getAudioFiles(folderTreeUris)` escanea por defecto toda la música (`IS_MUSIC`) y **suma** el audio de las carpetas elegidas (tree URI de SAF, `DocumentsContract.getTreeDocumentId`); la pertenencia a una carpeta se resuelve por `VOLUME_NAME` + `RELATIVE_PATH` (API 29+) o prefijo de `DATA` (API < 29).
- **Tests**: `FolderLabelTest` cubre `folderDisplayName()` (ruta relativa, raíz del volumen, decodificación) y `AudioPathFilterTest` cubre la exclusión de audio que no es música (mensajería, grabaciones, sonidos del sistema). Se eliminan `AudioFolder` y `getAudioFolders()`. `MusicFolderPreferencesTest` se reescribe (tree URIs) y `LibraryScannerTest` se ajusta a la nueva firma.
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
