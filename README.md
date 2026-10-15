# Auralis - Reproductor de Música para Android

Auralis es un reproductor de música moderno, elegante y sin anuncios para Android. Diseñado con una arquitectura limpia, sigue las mejores prácticas de desarrollo Android y ofrece una experiencia de usuario fluida con Material Design 3.

> **Última release**: v0.2.0 (`8304e58` + tag `v0.2.0`). `versionName = "0.2.0"` / `versionCode = 2`. Sobre esa release se consolidaron y subieron a `origin/main` el pulido visual del reproductor —**aura que late al ritmo real de la canción** (sin permisos), controles con íconos, seek rediseñado y **barra inferior unificada con progreso interactivo**— y la **gestión de biblioteca/playlists** (ocultar canciones con `isHidden`/Room v5, chip "Playlists" en Inicio y rediseño de Playlists).
>
> **Consolidado en `origin/main` (2026-10-15)**: **catálogo por carpetas seleccionadas** (la app indexa **solo** el audio de las carpetas elegidas con SAF; `sourceFolderUri`/Room v6, vista de carpetas en Inicio → "Todas" con lista plana recursiva y cola acotada), **Acceso rápido = canciones más reproducidas**, **blindajes** para que la biblioteca no se vacíe por un escaneo sin resultados y para que las playlists no desaparezcan cuando la biblioteca está vacía, y **optimización de Inicio para gama media-baja** (los *ticks* de reproducción de 500 ms ya no recalculan `folders`/`popularSongs`; el trabajo pesado corre en `Dispatchers.Default`). Ver `CHANGELOG.md`, `docs/PROJECT_STATUS.md` y `docs/RELEASE_PLAN.md`.

## 📱 Capturas de Pantalla

*Próximamente*

## ✨ Características Principales (v0.2.0)

### Reproducción de Audio
- **Motor ExoPlayer (Media3)** - Reproducción robusta y eficiente
- **Servicio en segundo plano** - Reproducción continua con `MediaSessionService`
- **Controles de notificación** - Control desde la barra de notificaciones y pantalla de bloqueo
- **Modos de repetición** - Apagado, Repetir uno, Repetir todo
- **Aleatorio (Shuffle)** - Reproducción aleatoria
- **Control de velocidad** - Seek preciso con slider interactivo en `PlayerScreen`

### Organización de Biblioteca
- **Catálogo por carpetas** - Auralis cataloga **solo** el audio de las carpetas que el usuario elige con el explorador del sistema (SAF); las canciones fuera de ellas no aparecen
- **Carpetas de música (SAF)** - Añade/quita carpetas desde *Canciones* o Ajustes; en Inicio → "Todas" se listan las carpetas elegidas y, al abrir una, sus canciones (lista plana recursiva, cola acotada a esa carpeta)
- **WorkManager** - Escaneo en segundo plano programado
- **Metadatos completos** - Título, artista, álbum, duración, género, carátula
- **Vistas jerárquicas**:
  - 🎵 **Canciones** - Lista completa
  - 💿 **Álbumes** - Agrupados por álbum con detalle
  - 🎤 **Artistas** - Por artista con sus álbumes
  - 📋 **Playlists** - **CRUD completo**: crear, renombrar, eliminar, añadir/quitar canciones, reordenar y reproducción integrada
  - ❤️ **Favoritos** - Persistidos en Room con vista dedicada y toggle en las filas
  - 🙈 **Ocultar canciones** - Oculta (reversible, sin borrar el archivo) canciones de la biblioteca con long-press; restáuralas desde Ajustes → Canciones ocultas

### Interfaz de Usuario
- **Material Design 3** - Diseño moderno y consistente
- **Jetpack Compose** - UI declarativa y reactiva
- **Tema personalizado** - Colores, tipografía, formas y motion tokens (con selección de tema persistida)
- **Navegación por cajón (Drawer)** - Acceso rápido a todas las secciones, con item activo resaltado
- **Barra de reproducción persistente** - Mini-player con **línea de progreso interactiva** (pulsar/arrastrar para saltar; tiempo solo al ajustar)
- **Pantalla de reproducción completa** - Carátula tipo vinilo, controles, seek y visualizador
- **Aura reactiva** - El vinilo se rodea de un aura animada derivada del color dominante de la carátula que **late al ritmo real de la canción** (graves + agudos, sin permisos), con movimiento fluido y respaldo si no hay portada
- **Ecualizador de 5 bandas** - AudioFX con presets y persistencia

### Arquitectura y Calidad
- **Clean Architecture** - Separación Domain / Data / Presentation
- **Inyección de dependencias** - Hilt/Dagger para DI
- **Room Database** - Persistencia local tipada y reactiva (Flow)
- **Kotlin Coroutines + Flow** - Programación asíncrona reactiva
- **KSP** - Procesamiento de símbolos Kotlin para Room/Hilt
- **ProGuard/R8** - Optimización y ofuscación en release

### Escaneo de biblioteca (v0.2.0, 2026-10-12)
- **Escaneo automático por defecto**: la app detecta y organiza la música del dispositivo sin configuración previa
- **Carpetas adicionales (SAF)**: el usuario puede sumar una o más carpetas con `OpenDocumentTree`; su audio se añade a la biblioteca y puede gestionarse desde *Canciones* o Ajustes
- **Filtro de audio que no es música**: se descartan mensajería (WhatsApp/Telegram/Signal), grabaciones y sonidos del sistema
- **Inicio sin bloqueo**: la app siempre abre en Inicio; no hay onboarding obligatorio

### Otras novedades de v0.2.0 (consolidadas 2026-10-12)
- Ecualizador de 5 bandas con AudioFX real y persistencia
- Búsqueda inteligente con Autocomplete
- Toggle de Favoritos en filas y vista dedicada de Favoritos
- Drawer con sección activa resaltada
- Pantalla de Ajustes real con persistencia de tema
- Carrusel "Acceso Rápido" y filtros por chips en Home
- Detalles de Pista
- Nuevo **logo e ícono de app**
- **Versión visible en el Drawer** (`v0.2.0`)
- **Permiso runtime `POST_NOTIFICATIONS`** (Android 13+)
- **Tests instrumentados** y **CI con matriz de emuladores** (API 26/29/33/36)
- Refinamiento visual (Material Icons Rounded, vinilo, glow, visualizador)

Consultá `docs/FEATURES.md` y `docs/RELEASE_PLAN.md` para el detalle completo.

## 🛠 Stack Tecnológico

| Capa | Tecnología | Versión |
|------|-----------|---------|
| **Lenguaje** | Kotlin | 2.3.21 |
| **UI** | Jetpack Compose + Material3 | 2026.08.00 |
| **Navegación** | Navigation Compose | 2.9.8 |
| **DI** | Hilt | 2.60.1 |
| **Base de Datos** | Room + KSP | 2.8.4 |
| **Reproducción** | Media3 / ExoPlayer | 1.11.0 |
| **Background Work** | WorkManager + Hilt | 2.11.2 |
| **Async** | Coroutines + Flow | 1.11.0 |
| **Testing** | JUnit, Robolectric, Coroutines Test, AndroidX Test (instrumentados) | - |
| **Build** | Gradle KTS (Kotlin DSL) | AGP 9.3.2 |
| **JDK** | 21 | |
| **Min SDK** | Android 8.0 (API 26) | |
| **Target SDK** | API 36 | |
| **Compile SDK** | API 37 | |
| **VersionName** | 0.2.0 | |
| **VersionCode** | 2 | |

## 🏗 Estructura del Proyecto

```
app/src/main/java/com/auralis/player/
├── core/                          # Núcleo compartido
│   ├── common/                    # Utilidades genéricas (Result, Dispatchers)
│   └── ui/theme/                  # Sistema de diseño (Tokens, Color, Type, Shape, Spacing, Motion)
│   └── ui/components/             # Componentes core (AuralisArtwork, QueueFeedbackOverlay)
├── data/                          # Capa de datos
│   ├── database/                  # Room: Entities, DAOs, Database, Mappers
│   ├── mediastore/                # MediaStoreSource - Acceso a archivos del sistema
│   ├── metadata/                  # MetadataExtractor - Lectura de tags ID3
│   ├── repository/                # Implementaciones: RoomLibraryRepository, RoomPlaylistRepository, etc.
│   └── scanner/                   # LibraryScanner, Worker, Scheduler, Monitor
├── di/                            # Módulos Hilt (AppModule, DatabaseModule, RepositoryModule)
├── domain/                        # Capa de dominio (Clean Architecture)
│   ├── model/                     # Entidades de negocio: Song, Album, Artist, Playlist, PlaybackState, etc.
│   └── repository/                # Interfaces: LibraryRepository, PlaybackRepository, PlaylistRepository, FavoritesRepository
├── feature/                       # Features por pantalla (MVVM + Compose)
│   ├── home/                      # HomeScreen, HomeViewModel
│   ├── player/                    # PlayerScreen, PlayerViewModel
│   ├── playback/                  # PlaybackService (MediaSessionService), MusicServiceConnection
│   ├── songs/                     # SongsScreen, SongsViewModel
│   ├── albums/                    # AlbumsScreen, AlbumDetailScreen, ViewModels
│   ├── artists/                   # ArtistsScreen, ArtistDetailScreen, ViewModels
│   ├── playlists/                 # PlaylistsScreen, PlaylistDetailScreen, AddToPlaylistScreen + ViewModels
│   ├── settings/                  # SettingsScreen, ThemeController
│   └── navigation/                # ReproductorNavHost, Rutas, Drawer
└── App.kt / MainActivity.kt       # Entry points
```

Tests instrumentados en `app/src/androidTest/` (DAO en dispositivo + smoke de UI) y script `tools/run-instrumented-tests.ps1`.

## 🚀 Primeros Pasos

### Requisitos Previos
- **Android Studio** Ladybug (2024.2.1) o superior
- **JDK 21** (configurado en `gradle.properties`)
- **Android SDK** con API 37 (compileSdk) y API 26+ (minSdk)

### Clonar y Construir

```bash
git clone https://github.com/tu-usuario/auralis.git
cd auralis

# Dar permisos al wrapper (Linux/macOS)
chmod +x gradlew

# Compilar debug
./gradlew assembleDebug

# Instalar en dispositivo conectado
./gradlew installDebug
```

### Configuración de Firma (Release)

Crea un archivo `local.properties` en la raíz (ya está en `.gitignore`):

```properties
storeFile=../keystore/release.keystore
storePassword=tu_password
keyAlias=tu_alias
keyPassword=tu_key_password
```

## 📦 Build Variants

| Variant | Descripción |
|---------|-------------|
| `debug` | Desarrollo, sin ofuscación, logs habilitados |
| `release` | Producción, ProGuard/R8 activado, shrinkResources |

```bash
./gradlew assembleRelease
./gradlew bundleRelease
```

## 🧪 Testing

```bash
# Tests unitarios (JVM)
./gradlew testDebugUnitTest

# Tests instrumentados (requieren dispositivo/emulador conectado)
./gradlew connectedDebugAndroidTest
# o en Windows:
powershell -File tools/run-instrumented-tests.ps1

# Lint
./gradlew lintDebug
```

La integración continua (`.github/workflows/android-ci.yml`) corre **unit + lint** y, además, **tests instrumentados** en una matriz de emuladores (API 26 / 29 / 33 / 36).

## 📋 Permisos Requeridos

| Permiso | Uso | API |
|---------|-----|-----|
| `READ_MEDIA_AUDIO` | Leer archivos de audio (Android 13+) | 33+ |
| `READ_EXTERNAL_STORAGE` | Leer almacenamiento (Android 12 y anteriores) | ≤32 |
| `POST_NOTIFICATIONS` | Mostrar controles de reproducción en la notificación y pantalla de bloqueo (Android 13+) | 33+ |
| `FOREGROUND_SERVICE` | Servicio en primer plano | 26+ |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Servicio de reproducción en foreground | 29+ |
| `WAKE_LOCK` | Mantener la reproducción con la pantalla apagada | todo |
| `MODIFY_AUDIO_SETTINGS` | Aplicar efectos de AudioFX (Ecualizador) | todo |

> **Nota**: La app maneja automáticamente la solicitud de permisos en tiempo de ejecución con UI guiada.

## 🎨 Personalización del Tema

El sistema de diseño vive en `core/ui/theme/`:

- `Color.kt` - Paleta Light/Dark, gradientes, semántica
- `Type.kt` - Escalas tipográficas (Display, Title, Body, Label, Timer)
- `Shape.kt` - Formas (Small, Medium, Large, Full)
- `Spacing.kt` - Espaciado consistente (xs, s, m, l, xl, xxl)
- `Motion.kt` - Duraciones y easings estándar
- `ReproductorThemeTokens.kt` - Agregador principal

Ver `docs/DESIGN_SYSTEM.md` para el detalle.

## 🤝 Contribución

1. Fork del repositorio
2. Crear rama feature: `git checkout -b feature/nueva-funcionalidad`
3. Commit cambios: `git commit -m 'feat: agregar nueva funcionalidad'`
4. Push a la rama: `git push origin feature/nueva-funcionalidad`
5. Abrir Pull Request

### Convenciones de Commit (Conventional Commits)

| Tipo | Descripción |
|------|-------------|
| `feat` | Nueva funcionalidad |
| `fix` | Corrección de bug |
| `refactor` | Refactorización sin cambio de comportamiento |
| `style` | Formato, lint, sin lógica |
| `docs` | Documentación |
| `test` | Tests |
| `chore` | Mantenimiento, build, deps |

## 📄 Licencia

```
Copyright 2025 EstalingradoCorp

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## 🙋 Soporte y Contacto

- **Issues**: GitHub Issues del repositorio
- **Web**: EstalingradoCorp
- **Email**: soporte@estalingradocorp.com

---

**Desarrollado con ❤️ por EstalingradoCorp**
