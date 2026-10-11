# Sistema de Diseño - Auralis

Auralis utiliza una personalización profunda de Material Design 3 para crear una experiencia visual coherente y "Premium".

> **Estado**: Este documento describe los tokens y componentes de la versión entregable v0.1.0. Los cambios visuales que viven en el working tree se marcan explícitamente como **(pendiente de consolidación)**.

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
Componente inteligente para carga de imágenes (v0.1.0):
- Soporta URIs de sistema y referencias opacas.
- **Fallback v0.1.0**: Si no hay imagen, renderiza el logo corporativo (`placeholder_ec`).
- **Fallback pendiente de consolidación**: Si no hay imagen, renderiza un contenedor oscuro con un icono `Icons.Rounded.MusicNote` en color de acento (`alpha = 0.35f`) ocupando el 50% del contenedor.
- **Animación**: Crossfade suave durante la carga.

### `VerticalFader` (pendiente de consolidación)
Componente nativo para el ecualizador:
- Diseñado para evitar los bugs de rotación de los Sliders estándar.
- Implementado con `Box` + `detectVerticalDragGestures` + `detectTapGestures`.
- Track de 4dp con fill dinámico proporcional al valor (`-15dB..+15dB`).
- Tirador circular de 20dp con shadow + borde en color de acento.
- Cobertura: solo en `PlayerScreen.EqualizerContent` (working tree).

### `QueueFeedbackOverlay`
Overlay efímero que aparece al hacer swipe-to-queue, confirmando que la canción fue añadida a la cola.

### `AudioEqualizerVisualizer` (pendiente de consolidación)
Visualizador animado de 4 barras en el top bar del `PlayerScreen`. Altura animada con `rememberInfiniteTransition` + `tween` + `RepeatMode.Reverse`. Se atenúa cuando la reproducción está pausada.

## 🎯 Mini-Player (v0.1.0 vs pendiente de consolidación)

- **v0.1.0**: usa `Slider` para reflejar progreso, ocupando ~32dp verticales con thumb visible.
- **Pendiente de consolidación**: reemplazado por `LinearProgressIndicator` de 3dp sin interactividad. Reduce ruido visual; el seek se realiza en `PlayerScreen`.

## 🎯 Drawer (v0.1.0 vs pendiente de consolidación)

- **v0.1.0**: los items del Drawer no indican la sección activa.
- **Pendiente de consolidación**: cada item se resalta con `secondaryContainer` + texto en negrita + color de acento cuando coincide con `currentRoute` (vía `currentBackStackEntryAsState`).

## 📚 Iconografía

- **v0.1.0**: glifos Unicode (`⌂`, `♪`, `⚙`, etc.) en botones, drawer y top bar.
- **Pendiente de consolidación**: reemplazo por `androidx.compose.material.icons.Icons.Rounded` (`Search`, `Shuffle`, `PlayArrow`, `Pause`, `MoreVert`, `Tune`, `Timer`, `Info`, `MusicNote`, `Favorite`, `FavoriteBorder`).
