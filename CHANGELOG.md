# Changelog

Todas las versiones notables de este proyecto se documentan en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/),
y este proyecto adhiere a [Semantic Versioning](https://semver.org/lang/es/).

## [Unreleased]

### Added
- Playlists CRUD completo (crear, renombrar, eliminar)
- Pantalla AddToPlaylist con selección múltiple de canciones
- Reproducción integrada en detalle de playlist
- Navegación: Playlists → Detalle → Añadir canciones

## [0.1.0] - 2025-01-XX

### Added
- Reproducción de audio con ExoPlayer (Media3) + MediaSessionService
- Escaneo de biblioteca via MediaStore + WorkManager programado
- Vistas jerárquicas: Canciones, Álbumes, Artistas
- Pantalla Home con barra de reproducción persistente
- Pantalla Player completa con controles, seek, shuffle, repeat
- Navegación por Drawer (cajón lateral)
- Tema Material 3 personalizado (Color, Type, Shape, Spacing, Motion)
- Arquitectura Clean: Domain / Data / Feature / Core / DI
- Inyección de dependencias con Hilt
- Base de datos Room con Flow reactivo
- Kotlin Coroutines + Flow para async
- KSP para Room/Hilt
- ProGuard/R8 en release
- Tests unitarios (JUnit, Robolectric, Coroutines Test)

### Technical
- Min SDK 26 (Android 8.0)
- Target SDK 36 (Android 14)
- Compile SDK 37 (Android 15)
- Kotlin 2.3.21, AGP 9.3.2
- Compose BOM 2026.08.00

---

## Guía de Versiones

- **MAJOR**: Cambios incompatibles en API pública
- **MINOR**: Funcionalidad nueva compatible hacia atrás
- **PATCH**: Bugfixes compatibles hacia atrás

Ejemplo: `1.2.3` → `versionCode = 10203` (major*10000 + minor*100 + patch)