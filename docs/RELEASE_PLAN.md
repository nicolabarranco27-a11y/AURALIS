# Plan de Lanzamiento (Release Plan) - v1.0.0

**Versión actual entregable**: v0.1.0 (`59403f7`)
**Working tree**: contiene cambios listos para ser consolidados como **v0.2.0** (ver "Bloque previo al Milestone 1").

> El plan hacia v1.0.0 se mantiene, pero los hitos se han ajustado al estado real del repositorio. Los cambios que viven en el working tree deben consolidarse antes de iniciar el Milestone 1.

---

## 🧱 Bloque previo al Milestone 1: Consolidación del working tree (v0.2.0)

Estos cambios **ya están implementados en código** pero viven solo en el working tree. Deben commitease y bumpearse a `0.2.0` antes de iniciar el Milestone 1.

### Funcionalidad
- [ ] Búsqueda con Autocomplete (Home)
- [ ] Toggle de Favoritos en filas (Home, Songs, AlbumDetail, ArtistDetail, PlaylistDetail)
- [ ] Ecualizador real con AudioFX (5 bandas + presets + persistencia)
- [ ] Diálogo "Detalles de la Pista" en PlayerScreen
- [ ] Pantalla de Ajustes real (`SettingsScreen`)
- [ ] Drawer con item seleccionado
- [ ] Carrusel "Acceso Rápido" en Home
- [ ] Filtros por chips en Home

### UI/UX
- [ ] `AuralisArtwork` con fallback `MusicNote`
- [ ] PlayerScreen rediseñado (vinilo + glow + visualizador)
- [ ] Mini-player rediseñado (`LinearProgressIndicator`)
- [ ] Iconos Material Rounded

### Correcciones
- [ ] `HomeViewModel.onSongClick` a `Dispatchers.Default` (UUIDs)
- [ ] `AlbumDetail` cover con prioridad no vacío

### Infraestructura
- [ ] Permiso `MODIFY_AUDIO_SETTINGS` agregado al manifest (ya en working tree)
- [ ] `material-icons-extended` en catálogo (ya en working tree)
- [ ] Test `HomeViewModelTest` con `FakeFavoritesRepository`

---

## 🏁 Hitos Posteriores

### Milestone 1: Consolidación de Features Core (v0.5.0)
- [ ] Implementar la pantalla dedicada de "Favoritos" (ruta en Drawer + listado).
- [ ] Migrar configuraciones (tema, ecualizador, ajustes) a Jetpack DataStore.
- [ ] Mejora de extracción de metadatos (formato, bitrate, sample rate) en `TrackDetailsDialog`.
- [ ] Implementar el temporizador de apagado automático (Sleep Timer) real.
- [ ] Persistencia de preferencia de tema (claro/oscuro/seguir sistema).

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
