# Estado del Proyecto - Auralis

**Fecha de última auditoría**: 2026-09-05
**Versión entregable actual**: v0.1.0 (`59403f7`)
**Versión en build.gradle.kts**: `versionName = "0.1.0"`, `versionCode = 1`
**Working tree**: contiene cambios no commiteados (ver sección "Pendientes de consolidación")

---

## 🎯 Resumen Ejecutivo

Auralis se encuentra en una fase inicial consolidada. La versión 0.1.0 incluye el motor de reproducción Media3 funcional, las tres vistas principales de biblioteca (Canciones, Álbumes, Artistas), el sistema de favoritos persistido en Room y la arquitectura Clean completa con Hilt, Coroutines/Flow y KSP.

Existen además cambios significativos en el working tree local (no commiteados todavía) que mejoran la experiencia con búsqueda inteligente, ecualizador real con AudioFX, persistencia de ajustes de audio, drawer con selección activa y una capa visual más pulida. **Estos cambios no forman parte de ninguna versión entregable** y se describen en la sección correspondiente.

---

## ✅ Funcionalidades Implementadas (Versión 0.1.0 - Commiteadas)

### Reproducción Core
- Play / Pause / Next / Previous / Seek
- Shuffle on/off
- Repeat: OFF / ONE / ALL
- `MediaSessionService` con foreground service

### Biblioteca
- Escaneo automático via MediaStore + `LibraryScanner` programado por `WorkManager`
- Vistas: Canciones, Álbumes, Artistas (lista + detalle)
- Playlists: crear, renombrar, eliminar, añadir/quitar canciones (`PlaylistRepository`)

### Favoritos
- Persistencia en Room via `FavoritesDao`
- `FavoritesRepository` expone `toggleFavorite` / `isFavorite` / `observeFavorites`
- **Sin UI dedicada** (no hay ruta "Favoritos" en el Drawer)
- **Sin botón de toggle en las filas** (en la versión commiteada)

### Navegación
- Drawer lateral con secciones: Inicio, Canciones, Álbumes, Artistas, Playlists, Ajustes
- Ajustes: en 0.1.0 muestra "Próximamente" (placeholder)

### Tema y Sistema de Diseño
- Material 3 con `ReproductorThemeTokens` (Color, Type, Shape, Spacing, Motion)
- Tema oscuro personalizado
- `AuralisArtwork` con fallback de placeholder corporativo

### Arquitectura
- Clean Architecture: Domain / Data / Feature / Core / DI
- Hilt para DI (DatabaseModule, RepositoryModule, etc.)
- Room con KSP (incluye `FavoritesDao`, `PlaylistDao`, `LibraryDao`)
- Kotlin Coroutines + Flow reactivo
- ProGuard/R8 en release

### Calidad
- Tests unitarios en `app/src/test/` para datos y dominio
- Build `./gradlew assembleDebug` estable localmente

---

## ⚠️ Funcionalidades Parciales / En Desarrollo

- **Favoritos (UI)**: Implementados en modelo y BD; **falta la vista dedicada** y el botón de toggle en filas. Ambos están en working tree (ver abajo) pero no consolidados.
- **Detalles de la Pista**: Diálogo implementado en working tree, no en 0.1.0.
- **Temporizador (Sleep Timer)**: No implementado. En working tree, el ítem de menú muestra `Toast: "Próximamente"`.

---

## 🐛 Bugs Conocidos

### Commiteados en v0.1.0
- **Hilo principal en `HomeViewModel.onSongClick`**: La generación de UUIDs para `QueueItem` se ejecutaba en el hilo principal al reproducir desde listas grandes. **Corregido en working tree** (migrado a `Dispatchers.Default`).
- **Cover vacío en AlbumDetail**: Si la primera canción del álbum tenía `coverReference` vacío, se mostraba placeholder aunque otras canciones tuvieran cover válido. **Corregido en working tree**.

### Sin resolución
- Ningún bug crítico conocido en 0.1.0 sin atender.
- **Escaneo no detectaba música descargada** (`IS_MUSIC != 0` + columna `DATA`): corregido en working tree (ver "Pendientes de Consolidación → Correcciones").

---

## 🚧 Pendientes de Consolidación (Working Tree, NO commiteados)

> Los siguientes cambios viven solo en el working tree local. **No forman parte de v0.1.0 ni de ninguna versión entregable.** Se listan aquí para evitar que la documentación describa como "estable" algo que aún no está consolidado en el repositorio remoto.

### Funcionalidad
- Búsqueda con Autocomplete (`HomeViewModel._searchQuery` + `SearchSuggestionsOverlay`)
- Toggle de Favoritos en filas (Home, Songs, AlbumDetail, ArtistDetail, PlaylistDetail)
- Ecualizador real con AudioFX (`PlaybackService.setupEqualizer` + UI en `PlayerScreen`)
- Persistencia de Ecualizador (`EqualizerPreferences` con SharedPreferences)
- Diálogo "Detalles de la Pista" en PlayerScreen
- Pantalla de Ajustes real (`SettingsScreen.kt` reemplaza al placeholder)
- Drawer con item seleccionado (pill visual)
- Carrusel "Acceso Rápido" en Home (LazyRow)
- Filtros por chips en Home ("Todas" / "Favoritos")

### UI/UX
- `AuralisArtwork`: fallback con icono `MusicNote` en color de acento
- `PlayerScreen` rediseñado: vinilo circular con shadow, halo glow, visualizador animado
- Mini-player rediseñado: `LinearProgressIndicator` 3dp reemplazando `Slider`
- Iconos Material Rounded reemplazando glifos Unicode
- `AlbumDetail` cover: prioriza cover no vacío

### Permisos
- `MODIFY_AUDIO_SETTINGS` agregado a `AndroidManifest.xml`

### Dependencias
- `material-icons-extended` + `material-icons-core` agregadas al catálogo

### Tests
- `HomeViewModelTest`: `FakeFavoritesRepository` añadido a la factory del VM

### Correcciones
- **Escaneo no detectaba música descargada [causa raíz]**: Se elimina `MediaStore.Audio.Media.DATA` de la proyección en `MediaStoreSource.queryAudio`. En Android 11+ con `targetSdk >= 30` esa columna está bloqueada y el proveedor rechaza la consulta completa (`SecurityException`), abortando todo el escaneo. Ahora `path` es `null` y las lecturas por fila son defensivas (`safeGetString`). Además se elimina el filtro `IS_MUSIC != 0` para incluir descargas con `IS_MUSIC = 0` (carpetas `Download/`). Verificado con `assembleDebug` + `testDebugUnitTest` + `lintDebug` (2026-10-11).
- **Escaneo automático al abrir la app**: `HomeViewModel.onScreenShown` lanza un escaneo cuando ya hay permiso y la biblioteca está vacía.

---

## 🚀 Próximos Pasos Recomendados (Roadmap)

Ver `docs/RELEASE_PLAN.md` para los hitos detallados hacia v1.0.0.

1. **Consolidar working tree** como v0.2.0 (commit + tag + bumpear `versionName`).
2. **Implementar vista dedicada de Favoritos** (ruta en Drawer).
3. **Implementar Sleep Timer** real (reemplazar el `Toast: "Próximamente"`).
4. **Migrar a DataStore** para configuraciones de tema y audio.
5. **Extracción de metadatos técnicos** (bitrate, sample rate, formato) en `TrackDetailsDialog`.

---

## 🛠 Estado de CI/CD e Infraestructura

- **Java**: Build local en JDK 21. CI configurado en JDK 21.
- **Build**: `./gradlew assembleDebug` estable localmente (verificado).
- **CI**: GitHub Actions `CI - Build & Test` ejecuta `assembleDebug` + `testDebugUnitTest` + `lintDebug`. Últimos runs (#8 y #9) en estado **success**.
- **Configuración Gradle CI**: `GRADLE_OPTS=-Xmx4096m`, `gradle.properties` trackeado, `gradle-daemon-jvm.properties` con `maxHeapSize=4096m`.
- **Lint**: Sin errores críticos detectados en los últimos runs.
- **Tests**: 13 archivos de tests en `app/src/test/`. No se ha verificado conteo exacto ni cobertura en este snapshot.
