# Plan de Implementación: Sección de Playlists

Este plan detalla la implementación de la gestión de Playlists en Auralis, permitiendo a los usuarios crear, organizar y reproducir sus propias listas de canciones con persistencia real.

## Cambios Propuestos

### Capa de Datos (Room & Repository)

#### [MODIFY] [PlaylistDao.kt](file:///C:/Users/nbmn8/Downloads/Reproductor de musica/app/src/main/java/com/auralis/player/data/database/dao/PlaylistDao.kt)
- Añadir consulta para observar los objetos `SongEntity` pertenecientes a una playlist:
  ```kotlin
  @Query("""
      SELECT s.* FROM songs s
      JOIN playlist_songs ps ON s.id = ps.songId
      WHERE ps.playlistId = :playlistId
      ORDER BY ps.position
  """)
  fun observePlaylistSongs(playlistId: String): Flow<List<SongEntity>>
  ```

#### [MODIFY] [PlaylistRepository.kt](file:///C:/Users/nbmn8/Downloads/Reproductor de musica/app/src/main/java/com/auralis/player/domain/repository/PlaylistRepository.kt)
- Añadir `fun observeSongs(id: PlaylistId): Flow<List<Song>>`.

#### [MODIFY] [RoomPlaylistRepository.kt](file:///C:/Users/nbmn8/Downloads/Reproductor de musica/app/src/main/java/com/auralis/player/data/repository/RoomPlaylistRepository.kt)
- Implementar `observeSongs(id: PlaylistId)` utilizando el nuevo método del DAO.

### Feature: Playlists

#### [NEW] `PlaylistsViewModel.kt` & `PlaylistsScreen.kt`
- **Lista de Playlists**: Mostrar todas las playlists creadas.
- **Creación**: Botón "+" que abre un diálogo minimalista para ingresar el nombre de la nueva playlist.
- **Navegación**: Al tocar una playlist, navegar al detalle.

#### [NEW] `PlaylistDetailViewModel.kt` & `PlaylistDetailScreen.kt`
- **Gestión de Contenido**: Mostrar canciones de la playlist.
- **Acciones**:
    - Renombrar playlist (diálogo).
    - Eliminar playlist (confirmación).
    - Quitar canciones individualmente.
    - Botón "Añadir canciones" que navega al selector.
- **Reproducción**: Integración con el sistema global. Al tocar una canción, se carga la playlist en la cola.

#### [NEW] `AddToPlaylistViewModel.kt` & `AddToPlaylistScreen.kt`
- **Selector**: Mostrar todas las canciones de la biblioteca (`LibraryRepository.observeSongs()`).
- **Interacción**: Permitir añadir canciones a la playlist actual con un simple toque o checkbox.

### Navegación

#### [MODIFY] [ReproductorNavHost.kt](file:///C:/Users/nbmn8/Downloads/Reproductor de musica/app/src/main/java/com/auralis/player/feature/navigation/ReproductorNavHost.kt)
- Añadir rutas: `PLAYLISTS`, `PLAYLIST_DETAIL/{playlistId}`, `ADD_TO_PLAYLIST/{playlistId}`.
- Conectar el Navigation Drawer con la pantalla principal de Playlists.

## Diseño Visual
- **Estética**: Fondo negro absoluto, acentos azul eléctrico y violeta.
- **Consistencia**: Uso de tokens `AppType` y `AppSpacing`.
- **Interactividad**: Feedback visual (ripple) y diálogos modernos con el tema de la app.

## Plan de Verificación

### Pruebas Automatizadas
- Ejecutar tests existentes para asegurar integridad.
- Añadir test básico para `RoomPlaylistRepository` si es necesario (verificar adición/remoción).

### Verificación Manual
1. Crear una playlist llamada "Favoritos".
2. Abrir la playlist y añadir 3 canciones desde la biblioteca.
3. Reproducir una canción y verificar sincronización con el mini-player.
4. Quitar una canción y verificar que ya no aparezca en la lista.
5. Renombrar la playlist.
6. Eliminar la playlist y verificar que las canciones sigan en la sección "Canciones".

### Calidad
- `./gradlew test`
- `./gradlew assembleDebug`
- `./gradlew lint`
