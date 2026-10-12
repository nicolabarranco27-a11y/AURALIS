# Sistema de Diseño - Auralis

Auralis utiliza una personalización profunda de Material Design 3 para crear una experiencia visual coherente y "Premium".

> **Estado**: Este documento describe los tokens y componentes actualmente presentes en el repositorio (consolidados el 2026-10-12 sobre la base de v0.1.0).

## 🎨 Paleta de Colores (`Color.kt`)
La identidad se basa en un tema oscuro neutro con acentos tecnológicos vibrantes.
- **Fondo**: `Color(0xFF05070A)` (Negro profundo).
- **Superficie Alzada**: `Color(0xFF171B26)` (Gris azulado oscuro).
- **Acento Primario**: `Color(0xFF2E5BFF)` (Azul eléctrico).
- **Acento Secundario**: `Color(0xFF8A2BE2)` (Violeta/Púrpura).
- **Gradiente Primario**: Fusión de Azul y Violeta para elementos destacados (botón Play, progreso).

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

### Marca (logo e íconos de aplicación)
- **Logo in-app**: `drawable/logo_auralis.png` (512×512). Se usa como logo circular en el header de Inicio (`HomeScreen`) con `CircleShape` y `ContentScale.Fit`.
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
Visualizador animado de 4 barras en el top bar de `PlayerScreen`. Altura animada con `rememberInfiniteTransition` + `tween` + `RepeatMode.Reverse`. Se atenúa cuando la reproducción está pausada.

### `PlayerScreen` (reproductor rediseñado)
- Carátula tipo **vinilo circular** con shadow y halo glow en el botón Play.
- Visualizador animado en el top bar.
- Diálogo "Detalles de la Pista".

## 🎯 Mini-Player

Usa `LinearProgressIndicator` pasivo de 3dp (sin interactividad). El seek se realiza en `PlayerScreen`. Reduce ruido visual y mantiene la barra compacta.

## 🎯 Drawer

Cada item se resalta con `secondaryContainer` + texto en negrita + color de acento cuando coincide con la ruta actual (vía `currentBackStackEntryAsState`), dentro de un `RoundedCornerShape(percent = 50)`. Además, la sección inferior muestra la **versión de la app** (`v0.1.0`) junto a EstalingradoCorp.

## 📚 Iconografía

Se usan `androidx.compose.material.icons.Icons.Rounded.*` / `Icons.AutoMirrored.Rounded.*` (vía `material-icons-extended`) — por ejemplo `Search`, `Shuffle`, `PlayArrow`, `Pause`, `MoreVert`, `Tune`, `Timer`, `Info`, `MusicNote`, `Favorite`, `FavoriteBorder` — en lugar de glifos Unicode.
