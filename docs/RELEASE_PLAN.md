# Plan de Lanzamiento (Release Plan) - v1.0.0

**Última versión entregable (release)**: v0.1.0 (`59403f7`)
**Repositorio**: contenido de **v0.2.0** preparado en el working tree (2026-10-12); `versionName = "0.2.0"` / `versionCode = 2`. Commit y tag `v0.2.0` **pendientes** (por ahora no se toca `git`).

> El plan hacia v1.0.0 se mantiene, pero los hitos se han ajustado al estado real del repositorio.

---

## 🧱 Bloque previo al Milestone 1: Consolidación (v0.2.0)

Estos cambios están **implementados**. Solo resta **crear el commit y el tag `v0.2.0`** (el bump de versión ya está hecho) antes de iniciar el Milestone 1.

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
- [x] Mini-player rediseñado (`LinearProgressIndicator`)
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

1. Verificar que el working tree está limpio (o consolidar cambios pendientes).
2. Incrementar `versionCode` y `versionName` en `app/build.gradle.kts`.
3. Actualizar `CHANGELOG.md` moviendo `[Unreleased]` a la versión final fechada.
4. Ejecutar `./gradlew bundleRelease` para generar el AAB.
5. Subir a la rama `main` y etiquetar con `vX.Y.Z`.
6. GitHub Actions generará automáticamente el release con el binario adjunto.
7. Actualizar `docs/PROJECT_STATUS.md` y `docs/RELEASE_PLAN.md` con la nueva versión.
