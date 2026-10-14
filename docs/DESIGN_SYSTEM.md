# Sistema de Diseño - Auralis

Auralis utiliza una personalización profunda de Material Design 3 para crear una experiencia visual coherente y "Premium".

> **Estado**: Este documento describe los tokens y componentes actualmente presentes en el repositorio (2026-10-14; versión actual `0.2.0`, consolidada en commit `8304e58` + tag `v0.2.0`). Los efectos visuales del reproductor (aura), la **fila unificada `SongListItem`**, la **hoja `SongActionsSheet`** y los **diálogos de playlist** están **consolidados en `origin/main`**.

## 🎨 Paleta de Colores (`Color.kt`)
La identidad se basa en un tema oscuro neutro con acentos tecnológicos vibrantes.
- **Fondo**: `Color(0xFF05070A)` (Negro profundo).
- **Superficie Alzada**: `Color(0xFF171B26)` (Gris azulado oscuro).
- **Acento Primario**: `Color(0xFF2E5BFF)` (Azul eléctrico).
- **Acento Secundario**: `Color(0xFF8A2BE2)` (Violeta/Púrpura).
- **Gradiente Primario**: Fusión de Azul y Violeta para elementos destacados (botón Play, progreso).
- **Aura de Respaldo**: `AuraFallbackColor = Color(0xFFC62828)` (rojo vivo). Se usa en el aura del vinilo y el halo del botón Play cuando la carátula actual no aporta un color dominante útil (ver `VinylAura`).

## Typography (`Type.kt`)
- **Display**: Tipografía ligera y espaciada para títulos de sección grandes.
- **Body**: Pesos medios para títulos de canciones para garantizar legibilidad.
- **Timer**: Monoespaciado sutil para tiempos de duración y progreso.

## 📐 Spacing y Formas
- **Spacing**: Escala de 4dp (xs: 4dp, s: 8dp, m: 16dp, l: 24dp, xl: 32dp).
- **Shapes**:
  - `Small`: 8dp (Carátulas de lista).
  - `Medium`: 16dp (Tarjetas, mini-player).
  - `Full`: CircleShape (Botones redondos, vinilo de carátula).

## 🧩 Componentes Personalizados Core

### `AuralisArtwork`
Componente inteligente para carga de imágenes:
- Soporta URIs de sistema y referencias opacas.
- **Fallback**: si no hay imagen, renderiza un icono `Icons.Rounded.MusicNote` centrado en color de acento (`alpha = 0.35f`).
- **Animación**: crossfade suave durante la carga.
- `ImageLoader` global con caché limitada al 25% de memoria y downsampling.

### `SongListItem`
Fila de canción **unificada** compartida por todas las listas (Inicio, Canciones, Favoritos, Álbumes, Artistas, Playlists), en sustitución de las filas duplicadas por pantalla.
- Parámetros: `song`, `isCurrent`, `onClick`, `onLongClick`, `onSwipeRight` (swipe-to-queue), `onToggleFavorite`, `modifier`, `showFavorite`, `trailingContent`.
- Tap reproduce, long-press abre `SongActionsSheet`, swipe a la derecha añade a la cola (umbral 150px) y el corazón alterna favoritos.
- `trailingContent` permite acciones contextuales (p. ej. "quitar de la playlist").

### `SongActionsSheet`
Hoja inferior (`ModalBottomSheet`) con las acciones de una canción, abierta con **long-press**.
- `SongAction(icon, label, destructive, enabled, onClick)` describe cada acción.
- Helper `songActions(song, onPlay, onAddToQueue, onToggleFavorite, onHide, extra)` compone las acciones estándar (Reproducir, Añadir a la cola, Favoritos) más las específicas de contexto (`extra`, p. ej. mover/arriba-abajo o quitar de playlist) y "Ocultar de la biblioteca".
- Iconografía `Icons.Rounded.*` / `Icons.AutoMirrored.Rounded.*`; las acciones deshabilitadas se atenúan.

### `PlaylistDialogs`
Diálogos compartidos de gestión de playlists (`core/ui/components/PlaylistDialogs.kt`):
- `CreatePlaylistDialog` (nombre + descripción), `EditPlaylistDialog` (renombrar/descripción) y `ConfirmDeleteDialog` (confirmación destructiva).
- Reutilizados por Inicio (creación rápida), `PlaylistsScreen` y `PlaylistDetailScreen`.

### Marca (logo e íconos de aplicación)- **Logo in-app**: `drawable/logo_auralis.png` (512×512). Se usa como logo circular en el header de Inicio (`HomeScreen`) con `CircleShape` y `ContentScale.Fit`.
- **Ícono de app (launcher)**: ícono adaptativo con foreground PNG en todas las densidades (`mipmap-*/ic_launcher_foreground.png`) sobre fondo `ic_launcher_background = #000000`. El logotipo ocupa la zona segura (~61%).
- El logo corporativo del Drawer (`estalingradocorp_logo.jpg`) es **independiente** del logo de Auralis.

### `VerticalFader`
Composable propio para el ecualizador (evita los bugs de rotación de los `Slider` estándar):
- Implementado con `Box` + `detectVerticalDragGestures` + `detectTapGestures`.
- Track de 4dp con fill dinámico proporcional al valor (`-15dB..+15dB`).
- Tirador circular de 20dp con shadow + borde en color de acento.
- Cobertura: `PlayerScreen` (contenido del ecualizador).

### `QueueFeedbackOverlay`
Overlay efímero que aparece al hacer swipe-to-queue, confirmando que la canción fue añadida a la cola.

### `AudioEqualizerVisualizer`
Visualizador animado de 4 barras en el top bar de `PlayerScreen`. Altura animada con `rememberInfiniteTransition` + `tween` + `RepeatMode.Reverse`. Se atenúa cuando la reproducción está pausada. Acepta un `color` opcional (por defecto el acento) y el `PlayerScreen` le pasa el aura actual.

### `VinylAura`
Aura de color animada alrededor del vinilo de `PlayerScreen`, derivada del **color dominante de la carátula**:
- `VinylAura(isPlaying, auraColor, modifier, content)`: cuando hay reproducción activa dibuja `ReactiveAura` (disco de luz + halo que **respiran con una escala global**, más **cuatro brumas** que orbitan a velocidades distintas, laten y dan un **empuje angular** en cada golpe); en pausa dibuja `StaticAura` (mismas capas, atenuadas y estáticas).
- El color de portada se **satura y aclara en HSV** (`vividly`, saturación ≥0.64 y valor ≥0.68) para que el aura **se note de verdad** aunque la carátula sea oscura o apagada. Las cuatro brumas se derivan de ese color vivido: **base**, **aclarada** (`lighten`) y **dos matices desviados** (`rotateHue` ~+42° y ~−48°), cada una con su órbita/velocidad. La energía (`PlaybackEnergy`) modula la escala global, el alpha, el radio y el empuje de cada bruma: la señal rítmica (`value`) marca el golpe y la señal ambiental (`ambient`) mantiene una deriva continua, ambas suavizadas en el tiempo.
- Render en un solo `drawWithCache`: **disco de luz amplio** + **halo con el pico en el borde de la carátula** (se lee como un resplandor que emana del vinilo) más las **brumas** girando vía `rotate(...)` (con empuje reactivo) y **respirando** vía `scale(...)` sobre el canvas. **Sin `Modifier.blur`** ni escalado fuera de los límites, evitando recortes rectangulares y formas nítidas. Los valores de las animaciones se leen en fase de dibujo, sin recomposición por fotograma.
- La carátula se dibuja al **72%** del contenedor del aura (contenedor al **98%** de ancho) para que la luz tenga más espacio alrededor del vinilo. **No rota la carátula**, solo el aura.
- Pureza: la extracción del color vive en `core/common/color/DominantColor.kt` (`dominantColorFromPixels`), sin dependencias de Android; el cálculo pesado ocurre fuera del hilo principal (ver `MediaArtworkColorRepository`).

### `PlaybackEnergy`
`rememberPlaybackEnergy(isPlaying)` expone dos señales (0f..1f):
- `value` (rítmica): mezcla un pulso sintético con el **ritmo real de la música** extraído del PCM de 16 bits del audio sink de Media3 (passthrough, sin `RECORD_AUDIO`) mediante `RhythmAudioProcessor` + `PlaybackRhythm`. El procesador separa **graves** (filtro paso bajo ~150 Hz) y **agudos** (resta), sigue una **envolvente de pico** por banda y detecta **ataques** contra una media adaptativa; el nivel combina el volumen normalizado (AGC) con un "pulso" cuya caída se **adapta al tempo**. La señal se **suaviza en el tiempo** con constantes distintas de subida (rápida) y bajada (fluida), para que el aura acelere/desacelere sin saltos entre buffers de audio.
- `ambient` (continua): oscilación lenta e independiente del ritmo, para que el aura **nunca quede quieta** y respire con fluidez.

El dato de ritmo se considera fresco durante 500 ms y, en ausencia de señal, se usa solo el pulso sintético. En pausa el valor baja a 0.12. Ambas señales se leen en fase de dibujo, sin recomposición por fotograma.

### `PlayerScreen` (reproductor rediseñado)
- Carátula tipo **vinilo circular** con shadow, envuelta por `VinylAura`, y halo glow del botón Play **tintado con el color del aura**.
- Visualizador animado en el top bar (tintado con el aura).
- Controles de transporte minimalistas con `Icons.Rounded` (`Shuffle`, `SkipPrevious`, `PlayArrow`/`Pause`, `SkipNext`, `Repeat`/`RepeatOne`): botones sin relleno salvo el estado activo (fondo acento tenue), micro-animación de presión (`scale`) y distribución `SpaceEvenly`. El botón Play es el protagonista (círculo con gradiente + halo tintado con el aura).
- Barra de progreso (seek) con track en gradiente y thumb con glow.
- Diálogo "Detalles de la Pista".

## 🎯 Mini-Player

Componente único y compartido `core/ui/components/MiniPlayerBar.kt` en **todas** las pantallas (Inicio, Canciones, Favoritos, Álbumes, Artistas, Playlists, Canciones ocultas), colocado con `Modifier.align(BottomCenter).padding(spacing.m)`. Diseño minimalista: carátula (42dp), metadatos, play/pausa y una **línea de progreso fina (3dp) interactiva**: se puede **pulsar o arrastrar para saltar** (área táctil de 22dp vía `awaitEachGesture`), la línea se engrosa a 5dp mientras se ajusta y el tiempo (`mm:ss / mm:ss`) se muestra **solo durante el gesto** (reemplazando la línea del artista, en color de acento). Mantiene el swipe-to-skip y el tap para abrir `PlayerScreen`. La posición se refresca cada 500 ms durante la reproducción (`MusicServiceConnection`).

## 🎯 Drawer

Cada item se resalta con `secondaryContainer` + texto en negrita + color de acento cuando coincide con la ruta actual (vía `currentBackStackEntryAsState`), dentro de un `RoundedCornerShape(percent = 50)`. Además, la sección inferior muestra la **versión de la app** (`v0.2.0`) junto a EstalingradoCorp.

## 📚 Iconografía

Se usan `androidx.compose.material.icons.Icons.Rounded.*` / `Icons.AutoMirrored.Rounded.*` (vía `material-icons-extended`) — por ejemplo `Search`, `Shuffle`, `PlayArrow`, `Pause`, `MoreVert`, `Tune`, `Timer`, `Info`, `MusicNote`, `Favorite`, `FavoriteBorder` — en lugar de glifos Unicode.
