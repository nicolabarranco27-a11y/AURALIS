# Estado del Proyecto - Auralis

**Fecha de última auditoría**: 2026-10-15

**Última versión entregable (release)**: v0.2.0 (`8304e58` + tag `v0.2.0`, subida a `origin/main`)
**Versión en build.gradle.kts**: `versionName = "0.2.0"`, `versionCode = 2`
**Estado de `main` (consolidado)**: **gestión de biblioteca, playlists y vida visual del reproductor** (2026-10-13/14): ocultar/restaurar canciones (`isHidden`, Room v5), fila de canción unificada (`SongListItem` + `SongActionsSheet`), chip "Playlists" en Inicio (crear + **eliminar por long-press**: tacho a la derecha, confirmación minimalista, auto-ocultado a los 3 s y cancelable al tocar cualquier parte) y rediseño completo del apartado de Playlists (estadísticas, edición con descripción, reordenación). Incluye los efectos visuales del reproductor: **aura reactiva al ritmo real de la canción** (`RhythmAudioProcessor` + `PlaybackEnergy`, sin permisos, respaldo rojo `#C62828`), seek con **thumb de un solo color sólido**, controles `Icons.Rounded` y **mini-player con progreso interactivo (scrub)** y refresco de posición cada 500 ms.

> **Nota de versionado**: v0.2.0 está consolidada en el commit `8304e58` con el tag anotado `v0.2.0`. `main` incluye la gestión de biblioteca/playlists y los efectos visuales del reproductor, **consolidados y subidos a `origin/main` (2026-10-14)**.
>
> **Consolidado en `origin/main` (2026-10-15)**: **catálogo por carpetas seleccionadas** (`sourceFolderUri`/Room v6), **Acceso rápido = más reproducidas**, **navegación persistente de carpetas en Inicio** (fila persistente, colapso de carpeta activa, persistencia entre chips, auto-scroll), **blindajes** para evitar el "vacío" de la biblioteca por escaneo sin resultados y para desacoplar las playlists del estado vacío de Inicio, y **optimización de rendimiento para gama media-baja** (los *ticks* de reproducción dejan de recalcular los derivados de biblioteca).
>
> **Último APK de debug (consolidación 2026-10-15)**: `Auralis-debug.apk` = **25.604.514 bytes**, SHA-256 `07720B4D6C330F606F981852FB031B6DAEE60019ECFBE7EEFC1645F3EF6948D5` (raíz del repositorio).

---

## 🎯 Resumen Ejecutivo

Auralis está en una fase funcional madura. El motor de reproducción Media3, las tres vistas de biblioteca (Canciones, Álbumes, Artistas), las playlists, los favoritos persistidos en Room, el escaneo automático vía MediaStore y la arquitectura Clean completa (Hilt, Coroutines/Flow, KSP) están **implementados y consolidados en el repositorio**.

Además, cambios significativos que antes vivían en el working tree ya están consolidados: búsqueda con autocomplete, ecualizador real con AudioFX y persistencia, pantalla de Ajustes real con persistencia de tema, drawer con item activo, rediseño del reproductor y la capa visual con Material Icons Rounded.

En esta consolidación (2026-10-12) se sumaron: **nuevo ícono de app y logo in-app**, **versión visible en el Drawer**, **solicitud runtime de `POST_NOTIFICATIONS`** (Android 13+), **tests instrumentados** y un **workflow de CI con matriz de emuladores**.

> **v0.2.0 (2026-10-12)**: **escaneo automático por defecto + carpetas adicionales**. Auralis detecta la música del dispositivo al abrir; el usuario puede **sumar** carpetas con el explorador del sistema (SAF) desde *Canciones* o el estado vacío de Inicio, y gestionarlas también desde Ajustes. Ver la sección *Cambios de v0.2.0* más abajo.

---

## ✅ Funcionalidades Implementadas (Consolidadas en el repositorio)

### Reproducción Core
- Play / Pause / Next / Previous / Seek
- Shuffle on/off
- Repeat: OFF / ONE / ALL
- `MediaSessionService` con foreground service
- **Ecualizador real (AudioFX, 5 bandas)** integrado en `PlaybackService`, con presets (Normal / Bass Boost / Rock / Pop) y persistencia (`EqualizerPreferences`)
- **Temporizador de apagado (Sleep Timer)** real en `PlayerViewModel` (`startSleepTimer`/`cancelSleepTimer`), con diálogo de presets y pausa automática al agotarse
- Cola dinámica (swipe-to-queue) con overlay de feedback

### Biblioteca
- Escaneo automático vía MediaStore + `LibraryScanner` programado por `WorkManager`
- Escaneo de arranque cuando ya hay permiso y la biblioteca está vacía
- Vistas: Canciones, Álbumes, Artistas (lista + detalle)
- Playlists: crear, renombrar, eliminar, añadir/quitar canciones, reordenar (Mover arriba/abajo) y editar descripción (`PlaylistRepository`)
- **Ocultar canciones de la biblioteca** (reversible, no borra el archivo): long-press → `SongActionsSheet`; pantalla **Ajustes → Canciones ocultas** para restaurar.
- **Estadísticas de playlist** (`PlaylistSummary`: canciones visibles, duración total, portada) y **chip "Playlists" en Inicio**.

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
- `Drawer` muestra la **versión de la app** (`v0.2.0`) junto a EstalingradoCorp
- Diálogo "Detalles de la Pista" en `PlayerScreen`
- **Barra inferior unificada** (`MiniPlayerBar`): misma barra minimalista (carátula + metadatos + play/pausa + línea de progreso de 3dp **interactiva**) en **todas** las pantallas; se eliminó la `PlaybackBar` privada de Inicio. El progreso es un **scrubber** (pulsar/arrastrar para saltar; el tiempo se muestra solo al ajustar) y la posición se refresca cada 500 ms.
- **Aura de color animada y viva** en `PlayerScreen` (`VinylAura`), derivada del color dominante de la carátula con respaldo rojo vivo `#C62828`: disco de luz + halo con pico en el borde + **cuatro brumas** que orbitan, **respiran** y dan un **empuje angular** en cada golpe, **latiendo al ritmo real de la canción** con **movimiento fluido** (la energía se suaviza en el tiempo y se suma una deriva ambiental continua); el `RhythmAudioProcessor` separa graves y agudos y detecta los golpes con caída adaptada al tempo (sin permisos), con pulso sintético de respaldo. Halo del botón Play y visualizador tintados.

### Tema y Sistema de Diseño
- Material 3 con `ReproductorThemeTokens` (Color, Type, Shape, Spacing, Motion)
- Tema por defecto oscuro
- `AuralisArtwork` con fallback `Icons.Rounded.MusicNote` en color de acento
- Iconografía con `Icons.Rounded.*` / `Icons.AutoMirrored.Rounded.*` (`material-icons-extended`)
- **Nuevo logotipo**: ícono de app adaptativo (fondo negro) y logo circular in-app en Home

### Arquitectura
- Clean Architecture: Domain / Data / Feature / Core / DI
- Hilt para DI (DatabaseModule, RepositoryModule, DispatcherModule, etc.)
- Room con KSP (Room v6, migraciones `1→2`, `2→3`, `3→4`, `4→5`, `5→6`)
- Kotlin Coroutines + Flow reactivo
- ProGuard/R8 en release

### Calidad
- Tests unitarios en `app/src/test/` para datos y dominio (incluye `MusicFolderPreferencesTest`)
- **Tests instrumentados** en `app/src/androidTest/` (`LibraryDaoInstrumentedTest`, `AppLaunchSmokeTest`)
- **CI con matriz de emuladores** (`.github/workflows/android-ci.yml`)
- `assembleDebug` + `assembleRelease` + `testDebugUnitTest` + `lintDebug` + `assembleDebugAndroidTest` verificados localmente (**BUILD SUCCESSFUL**)

---

## ⚠️ Funcionalidades Parciales / En Desarrollo

- **Metadatos técnicos** (bitrate, sample rate, formato): El diálogo de detalles muestra título/artista/álbum/duración/origen, pero no los campos técnicos.
- **DataStore**: Las preferencias (tema, ecualizador, carpetas) aún usan `SharedPreferences`; la migración a DataStore está prevista en el Milestone 1.
- **Multi-idioma**: Solo español.
- **Robustez ante crashes**: no hay captura local de crashes; se diagnosticará con `logcat` cuando se reproduzca un cierre inesperado.

---

## 🐛 Bugs Conocidos

### Resueltos en esta consolidación
- **Hilo principal en `HomeViewModel.onSongClick`**: generación de UUIDs de `QueueItem` migrada a `Dispatchers.Default`.
- **Cover vacío en AlbumDetail**: se prioriza el primer `coverReference` no vacío.
- **Escaneo no detectaba música descargada [causa raíz]**: `MediaStore.Audio.Media.DATA` eliminado de la proyección (bloqueada en Android 11+ con `targetSdk >= 30`) y lecturas defensivas (`safeGetString`). El escaneo por defecto cubre toda la música (`IS_MUSIC`); el audio con `IS_MUSIC = 0` se incorpora sumando su carpeta manualmente.
- **Audio que no es música mezclado con las canciones**: nuevo `AudioPathFilter` descarta mensajería, grabaciones y sonidos del sistema.
- **Estado vacío en Inicio**: tras un escaneo sin resultados se muestra la guía ("Agregar carpetas") en lugar de la vista principal vacía.
- **Compilación de `PlaybackService`**: corregido el uso inexistente de `session.commands` → `DEFAULT_SESSION_COMMANDS`, `SessionError.ERROR_NOT_SUPPORTED`, `@OptIn`.
- **`mergeReleaseResources`**: renombrado `estalingradocorp_logo.png` → `.jpg` (era un JPEG mal nombrado).
- **Sin controles de reproducción en Android 13+**: se solicita `POST_NOTIFICATIONS` en runtime.

### Resueltos en la consolidación (2026-10-15)
- **La biblioteca ya no se vacía por un escaneo sin resultados**: `DefaultLibraryScanner` no marca `isAvailable=0` cuando el descubrimiento vuelve vacío. Así, canciones, favoritos y playlists dejan de "desaparecer" ante un escaneo transitorio (MediaStore aún no listo tras boot, volumen desmontado, SAF sin resolver o sin carpetas seleccionadas).
- **Inicio desacoplado del vacío de biblioteca**: la pantalla "Sin música" solo aparece si **no hay canciones, ni playlists, ni carpetas**; si hay playlists o carpetas aunque la biblioteca esté vacía, Inicio sigue visible.
- **Carpetas elegidas explícitamente se respetan**: `AudioPathFilter` solo actúa cuando la selección es amplia (todo el volumen); si la pista cae en una **subcarpeta concreta** elegida, no se excluye.
- **Copia de seguridad desactivada**: `android:allowBackup="false"` y exclusión de `database/` y `sharedpref/` en `backup_rules.xml` y `data_extraction_rules.xml` (evita que un restore revierta playlists/favoritos).
- **Trabajo pesado en el hilo principal durante la reproducción**: `HomeViewModel` recalculaba `buildFolders`/`mostPlayedSongs` (O(n)/O(n log n)) sobre todo el catálogo en **cada tick de reproducción (500 ms)** y en `Dispatchers.Main`. Ahora los derivados dependen solo del catálogo (`shareIn`/`distinctUntilChanged`) y se calculan en `Dispatchers.Default`; el mapping Room→Dominio de `RoomLibraryRepository` también se movió a `Default`. Prueba: `los ticks de reproduccion no recalculan los derivados de biblioteca`.

---

## 🧩 Cambios de v0.2.0 (2026-10-12)

**Escaneo automático + carpetas adicionales**:
- Dominio: `LibraryFolderRepository` (`observeSelectedFolders`/`selectedFolders`/`addFolder`/`removeFolder`).
- Data: `MusicFolderPreferences` (SharedPreferences `auralis_library_prefs`), `DefaultLibraryFolderRepository`; `MediaStoreSource.getAudioFiles(folderTreeUris)` escanea por defecto toda la música (`IS_MUSIC`) y **suma** el audio de las carpetas elegidas (tree URI de SAF, `DocumentsContract.getTreeDocumentId`) comprobando pertenencia por `VOLUME_NAME` + `RELATIVE_PATH` (API 29+, volumen SAF `primary` → `external_primary`) o por prefijo de `DATA` (API < 29), aunque `IS_MUSIC = 0`.
- Filtro de audio no-música: `AudioPathFilter` descarta mensajería (WhatsApp/Telegram/Signal), grabaciones de voz/llamadas y sonidos del sistema siempre, incluso dentro de una carpeta elegida.
- Escaneo: `DefaultLibraryScanner` inyecta `MusicFolderPreferences` y **escanea siempre** (aunque no haya carpetas); el escaneo por defecto ya cubre la música del dispositivo.
- UI: explorador del sistema (`ActivityResultContracts.OpenDocumentTree`) + hoja `MusicFoldersSheet` (`feature/folders/FolderSelection.kt`); acción de carpeta en *Canciones*, CTA en el estado vacío de Inicio y sección **Biblioteca** en `SettingsScreen`. La app **siempre arranca en `Routes.HOME`** (sin onboarding). El estado vacío de Inicio también aparece cuando un escaneo termina sin resultados.
- Tests: `LibraryScannerTest` ajustado a la nueva firma + caso "escanea por defecto aunque no haya carpetas"; `MusicFolderPreferencesTest` reescrito (tree URIs); `FolderLabelTest` (nuevo, `folderDisplayName`); `AudioPathFilterTest` (exclusión de audio no-música); `AppLaunchSmokeTest` sin seed de onboarding.
- Verificado localmente: `compileDebugKotlin`, `testDebugUnitTest`, `lintDebug`, `assembleDebug`, `assembleRelease` y `assembleDebugAndroidTest` **OK**. Re-verificado tras los fixes de esta sesión: `assembleDebug` + `testDebugUnitTest` + `lintDebug` **BUILD SUCCESSFUL**.

---

## 🎨 Cambios de la sesión (2026-10-13/14) — consolidados en `origin/main`

**Efectos visuales del reproductor** (`PlayerScreen`):
- Dominio: `ArtworkColorRepository` (`dominantColorFor(reference: String?): Int?`).
- Core: `core/common/color/DominantColor.kt` (`dominantColorFromPixels`, pura y sin Android).
- Data: `MediaArtworkColorRepository` (decode reducido a 64px, análisis en `@DefaultDispatcher`, caché `LruCache`); binding en `RepositoryModule`.
- UI: `core/ui/components/VinylAura.kt` (`ReactiveAura`/`StaticAura` con **disco de luz amplio + halo con pico en el borde + cuatro brumas** —base, aclarada y dos matices desviados— que orbitan, **respiran** y dan un **empuje angular** en cada golpe, en un único `drawWithCache`, sin `blur`); el color de portada se **satura y aclara en HSV** (`vividly`, saturación ≥0.64 y valor ≥0.68) y el respaldo `AuraFallbackColor` es un **rojo más vivo** (`#C62828`); `core/ui/components/PlaybackEnergy.kt` (`rememberPlaybackEnergy(isPlaying)`) combina el pulso sintético con el **ritmo real (graves + agudos, caída adaptada al tempo)** vía `PlaybackRhythm` (sin permisos); `PlayerScreen` usa el aura en el vinilo (carátula al 72%, contenedor al 98%), tintado del visualizador y halo del Play; controles minimalistas con `Icons.Rounded` + micro-animación + `SpaceEvenly`; barra de seek con track gradiente + **thumb de un solo color sólido** (`auraColor`).
- UI: barra inferior unificada — `core/ui/components/MiniPlayerBar.kt` es el único componente (carátula + metadatos + play/pausa + línea de progreso de 3dp **interactiva**) en todas las pantallas; se eliminó la `PlaybackBar` privada de `HomeScreen` y el `Slider`/etiquetas de tiempo del componente compartido. El progreso es un **scrubber** (`awaitEachGesture`; tiempo solo al arrastrar) y `MusicServiceConnection` refresca cada 500 ms. `HomeScreen` conserva el filtro seleccionado (`rememberSaveable`) y muestra el encabezado de sección según el chip activo ("CANCIONES"/"FAVORITOS"/"PLAYLISTS").
- Tokens: `AuraFallbackColor` (`#C62828`, rojo vivo) en `Color.kt`; `durationAmbient`/`durationRotation`/`durationAuroraDrift`/`durationAuroraSway` en `AppMotion`.
- `PlayerViewModel`: nuevo parámetro `artworkColorRepository` + `artworkAura: StateFlow<Color>`.
- Tests: `DominantColorTest` (nuevo) y `PlayerViewModelTest` ampliado (respaldo y color dominante).

**Gestión de biblioteca — ocultar canciones** (Room v5):
- Dominio: `Song.isHidden`; `LibraryRepository.observeHiddenSongs()` / `setSongHidden(songId, hidden)`.
- Data: `SongEntity.isHidden`; migración `MIGRATION_4_5` (`ALTER TABLE songs ADD COLUMN isHidden INTEGER NOT NULL DEFAULT 0`); `LibraryDao.setHidden`/`observeHiddenSongs`/`getHiddenSongs` y filtro `isHidden = 0` en canciones, álbumes, artistas, búsqueda y `FavoritesDao`/`PlaylistDao`; `DefaultLibraryScanner` preserva el flag; schema `5.json` exportado.
- UI: `core/ui/components/SongListItem.kt` y `SongActionsSheet.kt` (fila y hoja unificadas, en sustitución de las filas duplicadas de cada pantalla); acción "Ocultar de la biblioteca" (long-press) en Inicio, Canciones, Favoritos, Álbumes, Artistas y Playlists; pantalla `feature/hidden/HiddenSongsScreen.kt` + `HiddenSongsViewModel` (restaurar una/todas) accesible desde Ajustes.
- Tests: `LibraryDaoTest` (+2), `MappersTest` (round-trip `isHidden`), `HiddenSongsViewModelTest` (nuevo), fakes de `HomeViewModelTest`/`FavoritesViewModelTest` actualizados.

**Playlists** (estadísticas, Inicio y rediseño):
- Data: `PlaylistDao.observePlaylistSummaries()` (proyección `PlaylistSummaryRow` con subconsultas de canciones visibles/duración/portada) y `updateDescription()`.
- Dominio: `PlaylistSummary(id, name, description, songCount, totalDurationMs, coverReference)`; `PlaylistRepository.observePlaylistSummaries()` / `updateDescription()`.
- UI Inicio: chip "Playlists" en `FilterChipsRow` + listado inline (`HomeViewModel` inyecta `PlaylistRepository`, crear in-line); mantener pulsada una playlist **revela un botón de tacho de basura** en el extremo derecho de la fila, que abre una **confirmación minimalista** (`ConfirmDeleteDialog`, cancelable tocando fuera) — nueva acción `HomeViewModel.deletePlaylist`. El tacho **se oculta solo a los 3 s** si no se interactúa y **se cancela si se toca cualquier otra parte de la pantalla** (estado `revealedPlaylistId` en `MainContent` + `detectTapGestures` sobre el contenedor).
- UI Playlists: `PlaylistsScreen` rediseñada (portadas reales, menú Editar/Eliminar con confirmación); `PlaylistDetailScreen` rediseñada (portada, descripción, métricas, Reproducir/Aleatorio/Añadir, reordenar arriba/abajo); diálogos compartidos en `core/ui/components/PlaylistDialogs.kt` (`CreatePlaylistDialog`/`EditPlaylistDialog`/`ConfirmDeleteDialog`); helpers `formatTotalDuration`/`formatSongCount` en `TimeFormat.kt`.
- Tests: `RoomPlaylistRepositoryTest` (+4: resumen visible/duración/portada, resumen vacío, editar descripción), `PlaylistsViewModelTest` (nuevo), `TimeFormatTest` (nuevo).

- Verificado localmente: `assembleDebug` + `testDebugUnitTest` + `lintDebug` → **BUILD SUCCESSFUL**; APK de esta consolidación: 25.814.338 bytes, SHA-256 `09893ABEC9B8B2BA5EC9853827770B46FC3EC4AFB9771D68450E53267C483443` (histórico; `Auralis-debug.apk` ya fue regenerado con la versión en curso, ver más abajo).

---

## 🗂 Cambios de la sesión (2026-10-14/15) — consolidados en `origin/main` (2026-10-15)

**Catálogo por carpetas seleccionadas + Acceso rápido por más reproducidas** (consolidado):
- Dominio: `Song.sourceFolderUri` (tree URI de la carpeta seleccionada que contiene la canción; `null` si el escaneo no pudo asociarla).
- Data: `SongEntity.sourceFolderUri` + índice `index_songs_sourceFolderUri`; Room **v6** con migración `MIGRATION_5_6` (`ALTER TABLE songs ADD COLUMN sourceFolderUri TEXT DEFAULT NULL` + índice); `MediaStoreSource` indexa **solo** el audio dentro de las carpetas seleccionadas y asocia cada canción a la carpeta **más específica** (`matchedSelectedFolder`), descartando lo demás; `DefaultLibraryScanner` la persiste y `hasChanged` la compara; `Mappers` la mapea en ambas direcciones. Schema `6.json` exportado.
- Registro de reproducciones: `LibraryDao.registerPlay(id, playedAtEpochMs)` (`playCount + 1`, `lastPlayedAtEpochMs`); `LibraryRepository.registerPlay(...)`; `Media3PlaybackRepository` registra una reproducción por canción distinta al empezar a sonar.
- Core: `core/common/folder/SelectedFolders.kt` (`songsInSelectedFolder`, `libraryFolders` → `MusicLibraryFolder`) y `core/common/library/MostPlayedSongs.kt` (`mostPlayedSongs`). Se **elimina** `MusicFolderTree.kt`.
- UI: en Inicio → "Todas", **fila horizontal persistente** de carpetas (`FolderBrowser`/`FolderItem`: portada, nº canciones); la carpeta activa se **resalta** y se **cambia con un solo toque** (1→2→3→2→1), sin entrar/volver (se descarta la miga de pan y el botón "Volver"); **re-tocar la carpeta activa la colapsa** y la fila se **auto-desplaza** a la activa (`rememberLazyListState`). La selección **se mantiene al cambiar entre "Todas"/"Favoritos"/"Playlists"**. Debajo, la **lista plana recursiva** de la carpeta elegida; **sin carpeta seleccionada no se listan canciones** (pista "Elegí una carpeta para ver su música"). `selectedFolderUri` con `rememberSaveable`; botón "Añadir carpeta" y estado vacío si no hay carpetas. "Acceso rápido" usa `popularSongs` (`mostPlayedSongs`).
- Reproducción acotada: `HomeViewModel.onSongClick(song, scope)` / `playAll(scope)` fijan la **cola a la carpeta mostrada** (evita mezclar toda la biblioteca).
- Tests: `SelectedFoldersTest` (nuevo, 4 casos), `MostPlayedSongsTest` (nuevo, 5 casos), `LibraryDaoTest` (+1 `registerPlay`); `MappersTest`/`FavoritesViewModelTest`/`HiddenSongsViewModelTest` actualizados a `sourceFolderUri`/`registerPlay`.
- Verificado localmente (consolidación 2026-10-15): `assembleDebug` + `testDebugUnitTest` (**158 tests, 0 fallos**) + `lintDebug` → **BUILD SUCCESSFUL**. `Auralis-debug.apk` regenerado: **25.604.514 bytes**, SHA-256 `07720B4D6C330F606F981852FB031B6DAEE60019ECFBE7EEFC1645F3EF6948D5`.
- **Blindajes** (fixes de esta sesión): `DefaultLibraryScanner` no poda la biblioteca con descubrimiento vacío; Inicio solo muestra "Sin música" si no hay canciones, playlists ni carpetas; `MediaStoreSource` respeta las subcarpetas elegidas (no aplica `AudioPathFilter` cuando la selección es una subcarpeta concreta); backup desactivado (`allowBackup="false"` + reglas de exclusión).
- **Optimización de rendimiento (gama media-baja)**: `HomeViewModel` desacopla los *ticks* de reproducción (500 ms) de los derivados `folders`/`popularSongs` (solo se recalculan cuando cambia el catálogo, en `Dispatchers.Default`); `RoomLibraryRepository.observe*()` mueve el mapping Room→Dominio a `Dispatchers.Default`. Prueba nueva: `los ticks de reproduccion no recalculan los derivados de biblioteca`.

---

## 🚀 Próximos Pasos Recomendados (Roadmap)

Ver `docs/RELEASE_PLAN.md` para los hitos detallados hacia v1.0.0.

1. **Compatibilidad multi-dispositivo**: pruebas en equipos físicos y ajustes de optimización.
2. **Verificar en el remoto** la CI nueva (matriz de emuladores) y los tests instrumentados.
3. **Migrar a DataStore** para configuraciones de tema, audio y carpetas.
4. **Extracción de metadatos técnicos** (bitrate, sample rate, formato) en el diálogo de detalles.
5. **Diagnóstico del cierre puntual**: capturar `logcat` si se reproduce.

---

## 🛠 Estado de CI/CD e Infraestructura

- **Java**: Build local en JDK 21. CI configurado en JDK 21.
- **Build local verificado (2026-10-14)**: `assembleDebug` + `testDebugUnitTest` + `lintDebug` → **BUILD SUCCESSFUL**. (v0.2.0 verificada el 2026-10-12 con `assembleRelease` + `assembleDebugAndroidTest` incluidos.)
- **CI**: GitHub Actions. El workflow existente `CI - Build & Test` (`assembleDebug` + `testDebugUnitTest` + `lintDebug`) tuvo runs en estado **success** (#8, #9).
- **CI nuevo**: `.github/workflows/android-ci.yml` agrega un job de unit+lint y un job de **tests instrumentados en matriz de emuladores** (API 26 / 29 / 33 / 36). Aún **no verificado en el remoto** (recién consolidado).
- **Tests instrumentados locales**: requieren un dispositivo/emulador conectado; en este entorno no había ninguno disponible (`adb devices` vacío). Se puede usar `tools/run-instrumented-tests.ps1`.
- **Lint**: sin errores críticos.
- **Tests unitarios**: 25 archivos en `app/src/test/`; **tests instrumentados** en `app/src/androidTest/` (2 archivos). No se ha medido cobertura en este snapshot.

---

## 📌 Historial de Consolidación

| Fecha | Commit | Contenido |
|---|---|---|
| 2025-01-XX | `59403f7` | v0.1.0 inicial |
| 2026-09-05 | `8239939` | Base de funcionalidad (working tree previo) |
| 2026-10-12 | (consolidación) | Features core + nuevo logo/ícono + versión en drawer + POST_NOTIFICATIONS + tests instrumentados + CI matriz |
| 2026-10-12 | `8304e58` | v0.2.0: escaneo automático por defecto + carpetas adicionales (SAF); tag `v0.2.0` (subido a `origin`) |
| 2026-10-14 | (consolidación) | Pulido visual del reproductor (aura reactiva al ritmo, seek, mini-player scrub) + gestión de biblioteca/playlists (ocultar canciones, chip Playlists) — **subida a `origin/main`** |
| 2026-10-15 | `ea7035a` / `929ef6e` | Verificación Google Search Console (archivo correcto publicado, solicitud de indexación) |
| 2026-10-15 | `b119977` | Creación de `BITACORA.txt` (documentación viva, no publicada en sitio) |
| 2026-10-16 | (presente) | **Cambio de licencia**: de Apache 2.0 a **PolyForm Noncommercial 1.0.0** (source-available, uso no comercial); derechos reservados. Se agregan `CONTRIBUTING.md` (contribuciones solo con acuerdo previo/CLA), `THIRD_PARTY_NOTICES.md`, cabeceras de copyright en `.kt` y actualización del sitio (`docs/index.html`) con aviso de licencia/derechos. |
