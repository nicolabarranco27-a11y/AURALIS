# Documentación - Página web de Auralis

Documentación de la **página estática de descarga (landing)** de Auralis: qué es, qué secciones tiene, qué hace cada función de JavaScript, qué assets usa y cómo se mantiene y despliega.

> **Estado**: describe el estado real del archivo `docs/index.html` en `origin/main`, commit `522192c` (2026-10-16). Versión de la app mostrada en la página: **beta** (APK `Auralis.apk`, 24,4 MB).

---

## 1. Propósito y ubicación

- **Qué es**: sitio de una sola página (one-page) para presentar Auralis y ofrecer la descarga directa del APK para Android.
- **URL pública**: `https://nicolabarranco27-a11y.github.io/AURALIS/`
- **Repo / hosting**: repositorio `AURALIS` (público), servido con **GitHub Pages** desde la carpeta **`docs/`** de la rama **`main`**.
- **Archivo principal**: `docs/index.html` (autocontenido: HTML + `<style>` + `<script>` en el mismo archivo).
- **Descarga directa del APK**: `https://nicolabarranco27-a11y.github.io/AURALIS/Auralis.apk`

> Importante: la página vive dentro del repositorio de la app solo para aprovechar GitHub Pages. **No forma parte del código Android**; es un sitio estático independiente.

---

## 2. Tecnologías y dependencias (todas por CDN)

| Tecnología | Uso |
|---|---|
| **HTML5 estático** | Estructura de la página. Sin build ni frameworks. |
| **Tailwind CSS (Play CDN)** | Utilidades de estilo. Configuración inline (`tailwind.config`) para colores y `darkMode: 'class'`. |
| **FontAwesome 6.4 (CDN)** | Iconografía (`fa-solid`, `fa-regular`, `fa-brands`). |
| **Google Fonts - Inter** | Tipografía principal (300–700). |
| **JavaScript vanilla** | Interactividad (modales, menú móvil, copiar hash). Sin librerías. |

No requiere proceso de compilación: se edita `index.html` y se publica.

---

## 3. Identidad visual (tokens)

Definidos en `tailwind.config` y en el bloque `<style>`:

| Token | Valor | Uso |
|---|---|---|
| `darkBg` | `#0b0c10` | Fondo general de la página. |
| `cardBg` | `#12141c` | Tarjetas, header, superficies. |
| `cardHover` | `#1a1d28` | (Definido; hover de superficies). |
| `accentBlue` | `#3b82f6` | Acento azul (gradientes, iconos). |
| `accentPurple` | `#8b5cf6` | Acento violeta (gradientes, iconos). |

Clases utilitarias propias:

- **`.gradient-text`**: texto con degradado azul→violeta (`background-clip: text`).
- **`.gradient-btn`**: fondo degradado azul→violeta con hover (elevación + sombra violeta).
- **`.custom-scrollbar`**: scrollbar fina personalizada (definida; uso puntual).

Ajustes globales: `scroll-behavior: smooth` y `scroll-padding-top: 6rem` (para que los anclajes no queden bajo el header fijo), tipografía Inter y fondo `#0b0c10`.

---

## 4. Estructura de la página (secciones y anclas)

### 4.1 Header fijo (`<header>`, `sticky top-0 z-50`)
- Fondo translúcido con `backdrop-blur` y borde inferior.
- **Marca**: `logo.png` + "AURALIS" + "EstalingradoCorp".
- **Navegación de escritorio** (`hidden md:flex`): `#inicio`, `#caracteristicas`, `#demo` (Ecualizador), `#descarga`, `#empresa`.
- **CTA** "Descargar APK" (enlaza a `#descarga`).
- **Botón hamburguesa** (`md:hidden`) que abre el **menú móvil** (`#mobile-menu`).

### 4.2 `#inicio` — Portada / Hero
- Badge **"Versión Beta · En desarrollo"** con punto pulsante.
- Título `Tu música, organizada.` (parte con `.gradient-text`).
- Descripción y CTA **"Descargar para Android"** (lleva a `#descarga`).
- Sellos de confianza: *100% Seguro y Libre de Anuncios* y *Ultraligero*.
- **Visual**: 3 capturas reales dentro de marcos de teléfono (ver §6). El marco central lleva borde degradado; los laterales, borde gris. Cada pantalla incluye la "píldora" superior (notch simulado) y un **anillo interior sutil**.

### 4.3 `#caracteristicas` — Características
Tres tarjetas (grid 1/3 columnas):
1. **Organización Inteligente** (canciones, álbumes, artistas, playlists).
2. **Reproductor Flotante** (mini-player con portada circular).
3. **Estética Dark Mode** (paleta profunda + acentos azul/morado).

### 4.4 `#demo` — Ecualizador
- Captura única (`ecualizador.jpg`) en un marco de teléfono con borde degradado (ver §6).

### 4.5 `#descarga` — Descarga
- Tarjeta destacada con icono, título **"Descarga Auralis"** y texto.
- **Botón de descarga**: `<a id="apk-download" href="Auralis.apk" download="Auralis.apk" onclick="showDownloadModal()">`. Apunta al APK alojado en el mismo sitio.
- **Aviso beta** (texto ámbar).
- **Requisitos y tamaño**: "Requiere Android 8.0 o superior · APK 24,4 MB".
- **Verificación del archivo**: caja con el **SHA-256** del APK (`<code id="sha-hash">`) y botón **copiar** (`copyHash()`).
- **Pasos de instalación** (3 tarjetas): descargar → permitir instalación → abrir y disfrutar.

### 4.6 `#empresa` — Empresa
- Tarjeta con `empresa-logo.jpg`, nombre **EstalingradoCorp** y botón que abre `https://estalingradocorp.github.io/EstalingradoCorp/index.html` en pestaña nueva (`target="_blank" rel="noopener"`).

### 4.7 Modales (ocultos por defecto)
- `#download-modal`: confirmación "¡Descarga Iniciada!".
- `#info-modal`: textos legales (**Política de Privacidad** y **Términos de Servicio**).

### 4.8 Footer
- Marca + descripción + enlace a EstalingradoCorp.
- **Enlaces rápidos** (mismos anclajes que el header).
- **Legal & Soporte**: enlaces que abren los modales legales y `mailto:contacto@estalingradocorp.com`.
- Línea inferior: copyright, etiqueta "Auralis Beta · Android" y acceso de contacto.

---

## 5. Funcionalidad JavaScript

Todas las funciones son `vanilla JS` dentro de `<script>` al final del `<body>`.

| Función | Qué hace |
|---|---|
| `showDownloadModal()` | Muestra `#download-modal` (al pulsar el botón de descarga) y fija su texto. |
| `closeModal()` | Oculta `#download-modal`. También se cierra al hacer clic en el fondo (`onclick` con comprobación `event.target===this`). |
| `toggleMobileMenu()` | Abre/cierra `#mobile-menu`, alterna el icono (barras/equis) y actualiza `aria-expanded` en `#menu-btn`. |
| `closeMobileMenu()` | Cierra el menú móvil al elegir un enlace; restablece el icono y `aria-expanded`. |
| `copyHash()` | Copia el contenido de `#sha-hash` al portapapeles (`navigator.clipboard`) y muestra feedback (icono check verde por 1,5 s). |
| `openInfoModal(key)` | Abre `#info-modal` con el texto del objeto `INFO` (`'privacy'` o `'terms'`). Se cierra al clic en el fondo. |
| `closeInfoModal()` | Oculta `#info-modal`. |

Objeto `INFO`: contiene `privacy` y `terms` (título + cuerpo) usados por los modales legales.

---

## 6. Assets (imágenes y APK)

Todos los archivos viven junto a `index.html` en `docs/` y se referencian con rutas relativas.

| Archivo | Rol | Notas |
|---|---|---|
| `logo.png` | Logo en header y footer | `object-contain`. |
| `favicon.png` | Favicon | `<link rel="icon">`. |
| `empresa-logo.jpg` | Logo de EstalingradoCorp | Fondo blanco en la tarjeta. |
| `inicio.jpg` | Captura 1 del hero + imagen Open Graph/Twitter | **591×1280** |
| `reproduccion-1.jpg` | Captura 2 del hero (marco central) | **591×1280** |
| `reproduccion-2.jpg` | Captura 3 del hero | **591×1280** |
| `ecualizador.jpg` | Captura de la sección Ecualizador | **591×1280** |
| `Auralis.apk` | APK descargable | ~24,4 MB (build **debug**) |

### Encuadre de las capturas
- Todas las capturas son de **591×1280 px** (relación 0,4617).
- Los marcos usan `aspect-[591/1280]`, idéntico a la imagen → se muestra **completa, sin recorte ni deformación**.
- La imagen usa `object-cover object-top`. Además se le aplica el **mismo radio de esquina** que el marco y un **anillo interior** (`ring-1 ring-inset ring-white/10`) para que el redondeo se note sobre el fondo oscuro.
- Radios: hero `rounded-xl sm:rounded-[1.75rem]`; ecualizador `rounded-[1.75rem] sm:rounded-[2.5rem]`.

---

## 7. Responsive y accesibilidad

- **Breakpoints**: diseño a una columna en móvil y a varias columnas desde `sm`/`md`/`lg` (hero en 12 columnas; visual en 6).
- **Menú móvil**: la navegación se colapsa en un menú desplegable por debajo de `md`.
- **Accesibilidad**: `aria-label`, `aria-expanded`, `aria-controls` en el botón de menú; `alt` en todas las imágenes; botón de copiar con `aria-label`; cierre de modales clicando el fondo.
- **Rendimiento**: imágenes con `loading="lazy"` (excepto el logo).

---

## 8. SEO y metadatos

- `<title>`: "Auralis - Tu música, organizada".
- `meta description` + `<meta name="theme-color" content="#0b0c10">`.
- **Open Graph**: `og:type`, `og:title`, `og:description`, `og:image` (`inicio.jpg`).
- **Twitter Card**: `summary_large_image` con título, descripción e imagen (`inicio.jpg`).

---

## 9. Despliegue (GitHub Pages)

- Configuración: fuente **rama `main`, carpeta `/docs`**.
- Cada `push` a `main` que toque `docs/` dispara el redeploy automático de Pages (tarda ~1-2 min).
- Misma URL siempre; no se generan enlaces nuevos al actualizar.
- Nota: GitHub Pages puede cachear unos minutos; si no se ve el cambio, esperar o forzar recarga.

---

## 10. Cómo actualizar la página (mantenimiento)

### 10.1 Reemplazar el APK
1. Colocar el nuevo APK como `docs/Auralis.apk`.
2. Calcular su **SHA-256** y su **tamaño en MB** (÷ 1.048.576, redondeado a 1 decimal, con coma):
   - PowerShell: `Get-FileHash -Algorithm SHA256 .\Auralis.apk` y `(Get-Item .\Auralis.apk).Length / 1MB`.
3. En `index.html` actualizar:
   - el texto `Requiere Android 8.0 o superior · APK X MB`;
   - el contenido de `<code id="sha-hash">…</code>`.
4. Commit y push. El `href="Auralis.apk"` **no cambia** (mismo nombre de archivo).

> `.gitignore` del repo ignora `*.apk`, por lo que hay que forzar el alta: `git add -f docs/Auralis.apk`.

### 10.2 Reemplazar o agregar capturas
1. Exportar las capturas a **591×1280** (mismo aspecto del marco).
2. Copiarlas a `docs/` con un nombre sin espacios (p. ej. `inicio.jpg`, `reproduccion-1.jpg`, `reproduccion-2.jpg`, `ecualizador.jpg`).
3. Actualizar los `src` correspondientes en `index.html` (y `og:image`/`twitter:image` si aplica).

### 10.3 Publicar
```
git add -A docs/
# si corresponde, y por el .gitignore:
git add -f docs/Auralis.apk
git commit -m "docs: <descripción del cambio>"
git push origin main
```

---

## 11. Pendientes / notas

- El APK publicado es un **build debug**; para distribución final conviene generar un **release** firmado.
- Correos de contacto (`contacto@estalingradocorp.com`) y textos legales son placeholder editables directamente en `index.html`.
