# Plan de Lanzamiento (Release Plan) - v1.0.0

**Última versión entregable (release)**: v0.2.0 (`8304e58` + tag `v0.2.0`)
**Repositorio**: v0.2.0 está consolidada en el commit `8304e58` con tag anotado `v0.2.0`. El pulido visual del reproductor (**aura reactiva al ritmo**, controles con íconos, seek) y la **gestión de biblioteca/playlists** (ocultar canciones Room v5, chip/rediseño de Playlists) se **consolidaron y subieron a `origin/main`** (2026-10-14). El **catálogo por carpetas seleccionadas** (`sourceFolderUri`/Room v6), el **Acceso rápido por más reproducidas**, los **blindajes anti-vaciado de la biblioteca** y la **optimización de Inicio para gama media-baja** se **consolidaron y subieron a `origin/main`** (2026-10-15).

> El plan hacia v1.0.0 se mantiene, pero los hitos se han ajustado al estado real del repositorio.

---

## 🧱 Bloque previo al Milestone 1: Consolidación (v0.2.0)

La consolidación de v0.2.0 está **hecha** (commit `8304e58` + tag `v0.2.0`) y **subida a `origin/main`**, junto con los efectos visuales del reproductor y la gestión de biblioteca/playlists (2026-10-14). Bloque cerrado.

### Funcionalidad
- [x] Búsqueda con Autocomplete (Home)
- [x] Toggle de Favoritos en filas (Home, Songs, AlbumDetail, ArtistDetail, PlaylistDetail)
- [x] Vista dedicada de Favoritos (ruta en Drawer)
- [x] Ecualizador real con AudioFX (5 bandas + presets + persistencia)
- [x] Diálogo "Detalles de la Pista" en PlayerScreen
- [x] Pantalla de Ajustes real (`SettingsScreen`) con persistencia de tema
- [x] Drawer con item seleccionado
- [x] Carrusel "Acceso Rápido" en Home
- [x] Filtros por chips en Home
- [x] Escaneo automático de la música del dispositivo + carpetas adicionales (explorador del sistema / SAF, gestión desde Canciones y Ajustes)

### UI/UX
- [x] `AuralisArtwork` con fallback `MusicNote`
- [x] PlayerScreen rediseñado (vinilo + glow + visualizador)
- [x] Mini-player rediseñado con progreso interactivo (scrub) + barra inferior unificada
- [x] Aura reactiva al ritmo real (graves + agudos, sin permisos)
- [x] Fila de canción unificada (`SongListItem`) + `SongActionsSheet`
- [x] Ocultar canciones (reversible, Room v5) + pantalla "Canciones ocultas"
- [x] Playlists: estadísticas, edición con descripción y eliminar desde Inicio
- [x] Iconos Material Rounded
- [x] Nuevo logo/ícono de app + versión en el Drawer

### Correcciones
- [x] `HomeViewModel.onSongClick` a `Dispatchers.Default` (UUIDs)
- [x] `AlbumDetail` cover con prioridad no vacío
- [x] Escaneo de música descargada (proyección MediaStore compatible + `IS_MUSIC`)

### Infraestructura
- [x] Permiso `MODIFY_AUDIO_SETTINGS` agregado al manifest
- [x] Permiso `WAKE_LOCK` agregado al manifest
- [x] Solicitud runtime de `POST_NOTIFICATIONS` (Android 13+)
- [x] `material-icons-extended` en catálogo
- [x] Test `HomeViewModelTest` con `FakeFavoritesRepository`
- [x] Tests instrumentados (`app/src/androidTest/`) + script local
- [x] CI con matriz de emuladores (`.github/workflows/android-ci.yml`)

### Consolidación 2026-10-15 (en `origin/main`)
- [x] Catálogo por carpetas seleccionadas: la app indexa **solo** el audio de las carpetas elegidas con SAF (`sourceFolderUri`/Room v6)
- [x] Navegación persistente de carpetas en Inicio (fila persistente, colapso de la activa, auto-scroll, cola acotada a la carpeta)
- [x] Acceso rápido = canciones más reproducidas (`playCount`, desempate por última reproducción)
- [x] Blindaje: la biblioteca no se vacía por un escaneo sin resultados (`DefaultLibraryScanner`)
- [x] Blindaje: Inicio no oculta playlists/carpetas cuando la biblioteca está vacía
- [x] Blindaje: las subcarpetas elegidas explícitamente se respetan (`AudioPathFilter`)
- [x] Copia de seguridad desactivada (`allowBackup="false"` + reglas de exclusión)
- [x] Rendimiento (gama media-baja): los *ticks* de reproducción (500 ms) no recalculan los derivados de biblioteca; trabajo pesado en `Dispatchers.Default`

---

## 🏁 Hitos Posteriores

### Milestone 1: Consolidación de Features Core (v0.5.0)
- [x] Pantalla dedicada de "Favoritos" (ruta en Drawer + listado). *(hecho)*
- [x] Implementar el temporizador de apagado automático (Sleep Timer) real. *(hecho en `PlayerViewModel`)*
- [x] Persistencia de preferencia de tema (claro/oscuro/seguir sistema). *(hecho vía `ThemeController`)*
- [ ] Migrar configuraciones (tema, ecualizador, ajustes, carpetas) a Jetpack DataStore.
- [ ] Mejora de extracción de metadatos (formato, bitrate, sample rate) en `TrackDetailsDialog`.

### Milestone 2: Pulido y Localización (v0.8.0)
- [ ] Soporte multi-idioma (Español e Inglés).
- [ ] Implementar edición de metadatos (ID3 Tags Editor).
- [ ] Optimizar el rendimiento del scanner para bibliotecas de más de 5,000 canciones.
- [ ] Asegurar accesibilidad completa (TalkBack y contrastes).

### Milestone 3: Compatibilidad y Ecosistema (v0.9.0)
- [ ] Integración con Android Auto.
- [ ] Soporte para Media Buttons en auriculares Bluetooth externos.
- [ ] Widgets de escritorio (pequeño y mediano).

### Milestone 4: Estabilidad y Store (v1.0.0)
- [ ] Auditoría de seguridad y Data Safety.
- [ ] Generación de capturas de pantalla profesionales para la Store.
- [ ] Preparación de la Política de Privacidad oficial.
- [ ] Lanzamiento en Google Play Store.

---

## 🚀 Proceso de Release

1. Verificar que el working tree está limpio (`git status`).
2. Incrementar `versionCode` y `versionName` en `app/build.gradle.kts`.
3. Actualizar `CHANGELOG.md` moviendo `[Unreleased]` a la versión final fechada.
4. Ejecutar `./gradlew bundleRelease` para generar el AAB.
5. Subir a la rama `main` y etiquetar con `vX.Y.Z`.
6. GitHub Actions generará automáticamente el release con el binario adjunto.
7. Actualizar `docs/PROJECT_STATUS.md` y `docs/RELEASE_PLAN.md` con la nueva versión.
