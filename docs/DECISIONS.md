# Registro de Decisiones Técnicas (ADR) - Auralis

Este documento registra las decisiones técnicas clave tomadas durante el desarrollo del proyecto y el razonamiento detrás de ellas.

---

## ADR 001: `SharedPreferences` para persistencia del Ecualizador
- **Fecha**: 2026-09-03
- **Estado**: Aceptado
- **Contexto**: Se necesitaba guardar 5 valores de ganancia (float) y un nombre de preset.
- **Decisión**: Utilizar `SharedPreferences` en lugar de Room o DataStore.
- **Razón**: Simplicidad y rapidez de implementación para una cantidad de datos mínima. Provee acceso síncrono necesario para la inicialización rápida del efecto de audio en el servicio.
- **Nota**: Migración futura a DataStore está prevista en el Milestone 1 del `docs/RELEASE_PLAN.md`.

---

## ADR 002: Normalización de búsqueda con `REPLACE` y `LOWER` en SQLite
- **Fecha**: 2026-08-29
- **Estado**: Aceptado
- **Contexto**: SQLite no soporta nativamente la comparación de caracteres acentuados (`á` vs `a`) en `LIKE`.
- **Decisión**: Cadena de funciones `REPLACE` y `LOWER` directamente en la consulta del DAO.
- **Razón**: Evita complejidad de funciones personalizadas en C y mantiene el esquema limpio.

---

## ADR 003: Gestión de `audioSessionId` dinámica
- **Fecha**: 2026-09-03
- **Estado**: Aceptado
- **Contexto**: ExoPlayer asigna el ID de sesión de audio solo después de iniciar la carga de la pista. Inicialmente es 0 (UNSET).
- **Decisión**: `Player.Listener.onAudioSessionIdChanged` en `PlaybackService` actualiza `MediaSession.extras` y reaplica el efecto.
- **Razón**: Asegura que el ecualizador se enganche a la sesión real, no a la sesión global 0 (restringida en Android moderno).

---

## ADR 004: `VerticalFader` nativo vs `Slider` rotado
- **Fecha**: 2026-09-03
- **Estado**: Aceptado
- **Contexto**: Los `Slider` de Material 3 son horizontales. Rotarlos 90° causa bugs visuales y zonas táctiles incorrectas.
- **Decisión**: Composable propio basado en `Box` + `detectVerticalDragGestures` + `detectTapGestures`.
- **Razón**: Control total del renderizado y comportamiento táctil coherente con un ecualizador real.

---

## ADR 005: Material Icons Rounded como estándar visual
- **Fecha**: 2026-09-05 (consolidado 2026-10-12)
- **Estado**: Aceptado (consolidado 2026-10-12)
- **Contexto**: En v0.1.0 se usaban glifos Unicode (`⌂`, `♪`, `⚙`, etc.) en botones, drawer y top bar. Esto generaba inconsistencias tipográficas entre dispositivos y familias de fuentes.
- **Decisión**: Reemplazar glifos Unicode por `androidx.compose.material.icons.Icons.Rounded` (vía `material-icons-extended`).
- **Razón**: Garantiza coherencia visual entre dispositivos, mejor accesibilidad (TalkBack lee descripciones reales) y consistencia con Material Design 3.
- **Migración**: requiere agregar `material-icons-extended` al catálogo.

---

## ADR 006: `LinearProgressIndicator` 3dp en mini-player
- **Fecha**: 2026-09-05 (consolidado 2026-10-12)
- **Estado**: Aceptado (consolidado 2026-10-12)
- **Contexto**: El mini-player en v0.1.0 usaba `Slider` para mostrar progreso, lo que ocupaba ~32dp verticales y mostraba el thumb.
- **Decisión**: Reemplazarlo por `LinearProgressIndicator` pasivo de 3dp sin interactividad.
- **Razón**: El seek se realiza en `PlayerScreen` (pantalla completa); el mini-player solo refleja estado. Reduce ruido visual y mantiene la mini-player compacta.
- **Nota (2026-10-13)**: el componente compartido `MiniPlayerBar` había vuelto a usar un `Slider` con etiquetas de tiempo, por lo que Inicio (con su `PlaybackBar` privada) y el resto de pantallas divergían. Se consolidó el diseño mínimo en una única `MiniPlayerBar` (ver ADR 016).
- **Nota (2026-10-13, rev.)**: el mini-player recupera el seek como **scrubber** integrado en su propia línea de progreso (ver ADR 018). El valor predeterminado deja de ser "seek solo en `PlayerScreen`", pero se mantiene el objetivo de bajo ruido (sin `Slider` ni thumb persistentes; el tiempo se muestra solo al ajustar).

---

## ADR 007: Indicador de sección activa en Drawer con píldora (pilled highlight)
- **Fecha**: 2026-09-05 (consolidado 2026-10-12)
- **Estado**: Aceptado (consolidado 2026-10-12)
- **Contexto**: El Drawer en v0.1.0 no indicaba la sección actual; el usuario perdía contexto al navegar.
- **Decisión**: Usar `currentBackStackEntryAsState` para conocer la ruta y resaltar el item correspondiente con `secondaryContainer` + texto en negrita + color de acento, dentro de un `RoundedCornerShape(percent=50)`.
- **Razón**: Patrón estándar de Material 3 (NavigationRail/DrawerItem). Aporta orientación sin agregar peso visual significativo.

---

## ADR 008: `AuralisArtwork` fallback con `MusicNote`
- **Fecha**: 2026-09-05 (consolidado 2026-10-12)
- **Estado**: Aceptado (consolidado 2026-10-12)
- **Contexto**: En v0.1.0, `AuralisArtwork` mostraba el logo corporativo como placeholder cuando no había cover. Esto vinculaba la marca corporativa con la app.
- **Decisión**: Reemplazar el placeholder corporativo por un icono `Icons.Rounded.MusicNote` centrado, con `tint = colors.accent.copy(alpha = 0.35f)`.
- **Razón**: Refuerza la identidad visual de Auralis (no de EstalingradoCorp) y mantiene coherencia con el tema.

---

## ADR 009: Solicitud runtime de `POST_NOTIFICATIONS` (Android 13+)
- **Fecha**: 2026-10-12
- **Estado**: Aceptado
- **Contexto**: En Android 13 (API 33)+ el permiso `POST_NOTIFICATIONS` debe concederse en tiempo de ejecución. Sin él, el `MediaSessionService` no puede mostrar los controles de reproducción en la notificación ni en la pantalla de bloqueo, degradando la experiencia y la estabilidad del foreground service (riesgo de `SecurityException` al iniciar la reproducción).
- **Decisión**: Solicitar el permiso desde `MainActivity` con `registerForActivityResult(RequestPermission)` + `ContextCompat.checkSelfPermission`, y declararlo en el manifest con `maxSdkVersion` no aplicable (`POST_NOTIFICATIONS`).
- **Razón**: Requisito obligatorio de la plataforma para una app de reproducción con controles multimedia.

---

## ADR 010: Nuevo logotipo e ícono de app de Auralis
- **Fecha**: 2026-10-12
- **Estado**: Aceptado
- **Contexto**: El ícono previo usaba `ic_launcher_foreground.webp` de baja fidelidad y el header de Inicio usaba un placeholder corporativo.
- **Decisión**:
  - Regenerar el **foreground** del ícono adaptativo como PNG en todas las densidades (`mdpi`→`xxxhdpi`), con el logotipo dentro de la zona segura (~61%) sobre fondo negro (`ic_launcher_background = #000000`).
  - Usar `drawable/logo_auralis.png` como logo circular in-app en el header de Inicio.
- **Razón**: Mejora la nitidez y consistencia del ícono en todas las densidades y separa la identidad visual de Auralis de la marca corporativa (EstalingradoCorp). Se eliminaron los `.webp` legacy.

---

## ADR 010b: Versión de la app visible en el Drawer
- **Fecha**: 2026-10-12
- **Estado**: Aceptado
- **Contexto**: No había forma de conocer la versión instalada desde la UI.
- **Decisión**: Mostrar `v${BuildConfig.VERSION_NAME}` junto a EstalingradoCorp en la sección inferior del Drawer, alineado a la derecha (el nombre usa `Modifier.weight(1f)`) y en color de acento.
- **Razón**: Facilita el soporte y la verificación de builds sin costo visual.

---

## ADR 011: Tests instrumentados + CI con matriz de emuladores
- **Fecha**: 2026-10-12
- **Estado**: Aceptado
- **Contexto**: El proyecto tenía tests unitarios en JVM pero ninguna verificación en dispositivo, pese a que la causa raíz de varios bugs (p. ej. el escaneo) solo se manifestaba en Android real. Tampoco había verificación de compatibilidad entre versiones de API.
- **Decisión**:
  - Añadir `app/src/androidTest/` con tests instrumentados (`LibraryDaoInstrumentedTest` para el DAO en SQLite real y `AppLaunchSmokeTest` para el arranque y el Drawer).
  - Configurar `testInstrumentationRunner` y las dependencias `androidx.test` en `app/build.gradle.kts`.
  - Añadir `.github/workflows/android-ci.yml` con un job de unit+lint y un job de instrumentados en una matriz de emuladores (API 26 / 29 / 33 / 36).
  - Proveer `tools/run-instrumented-tests.ps1` para ejecución local en un dispositivo/emulador conectado.
- **Razón**: Detecta regresiones dependientes de la plataforma (permisos, MediaStore, Room) y valida la compatibilidad mínima (`minSdk 26`) hasta la última API (`targetSdk 36`).
- **Nota**: En el entorno de desarrollo no había dispositivo/emulador disponible; los tests quedan listos para ejecutarse localmente y en CI.

---

## ADR 012: Escaneo automático por defecto + carpetas adicionales de música
- **Fecha**: 2026-10-12
- **Estado**: Aceptado (v0.2.0)
- **Contexto**: El escaneo inicial mezclaba todo el audio del dispositivo (música, descargas, notas de voz, WhatsApp...). Restringir el escaneo **solo** a carpetas elegidas dejaba la biblioteca vacía en el primer arranque y exigía configuración previa; se quería que Auralis organizara la música del usuario sin intervención y, a la vez, permitir sumar ubicaciones concretas.
- **Decisión**:
  - **Sin onboarding**: la app siempre arranca en `Routes.HOME`; la gestión de carpetas es una acción dentro de la app (vista **Canciones** y Ajustes).
  - **Escaneo por defecto**: `MediaStoreSource.getAudioFiles(folderTreeUris)` escanea toda la música del dispositivo (`IS_MUSIC != 0`) sin necesidad de configuración.
  - **Carpetas que se suman**: el usuario elige carpetas con el **explorador de documentos de Android** (`ActivityResultContracts.OpenDocumentTree`, SAF); su audio se **añade** a la biblioteca aunque el sistema marque `IS_MUSIC = 0` (p. ej. descargas). Una hoja inferior (`MusicFoldersSheet`) lista y permite quitar las elegidas.
  - Persistir cada carpeta como **tree URI** en `MusicFolderPreferences` (`SharedPreferences`, `auralis_library_prefs`) expuesta como `StateFlow`; `LibraryFolderRepository` expone `addFolder`/`removeFolder`.
  - La pertenencia a una carpeta se comprueba por `VOLUME_NAME` + `RELATIVE_PATH` (API 29+) o por prefijo de `DATA` (API < 29). `DefaultLibraryScanner` **escanea siempre**; `AudioPathFilter` descarta siempre el audio que no es música.
- **Alternativas descartadas**:
  - Onboarding obligatorio con lista in-app de carpetas detectadas: bloqueaba el primer arranque y alejaba la selección del momento en que el usuario la necesita.
  - Indexar con `DocumentFile`/SAF en lugar de MediaStore: perdería `mediaStoreId`, carátula y la organización ya provista por MediaStore.
  - Escanear **solo** las carpetas elegidas: dejaba la biblioteca vacía en el primer arranque y exigía configuración previa.
- **Razón**: mantiene a MediaStore (y Room) como fuente de verdad del catálogo, no requiere migración de Room y combina un escaneo automático listo desde el primer arranque con control explícito para incluir ubicaciones adicionales.

---

## ADR 013: Extracción de color dominante propia (sin Palette) para el aura del reproductor
- **Fecha**: 2026-10-13
- **Estado**: Aceptado (consolidado en `origin/main`, 2026-10-14)
- **Contexto**: Se quería un aura de color alrededor del vinilo de `PlayerScreen` derivada de la carátula. `androidx.palette` añade una dependencia completa (y su carga de bitmap) solo para obtener un color; además el color debe calcularse fuera del hilo principal y ser testeable en JVM.
- **Decisión**:
  - Implementar la extracción propia en `core/common/color/DominantColor.kt`: `dominantColorFromPixels(pixels: IntArray): RgbColor?` es una **función pura** (sin Android) que cuantiza cada canal a 4 bits, agrupa por cubo y puntúa por frecuencia ponderada por saturación; descarta píxeles transparentes (alpha < 128), muy oscuros (max < 40) y prácticamente grises (saturación < 0.12), devolviendo `null` en esos casos.
  - `ArtworkColorRepository` (dominio) expone `suspend fun dominantColorFor(reference: String?): Int?`; `MediaArtworkColorRepository` decodifica la carátula reducida (`inSampleSize`→64px), ejecuta el análisis en `@DefaultDispatcher` (con `@IoDispatcher` para el decode) y cachea en un `LruCache` en memoria (64 entradas, sentinel `0` = "sin color").
  - `PlayerViewModel` expone `artworkAura: StateFlow<Color>` (inicial `AuraFallbackColor = #C62828`), recoloreando al cambiar `currentSong.coverReference`.
  - Render: `VinylAura` dibuja en un único `drawWithCache` un halo radial suave que respira y dos brillos tenues que orbitan lentamente; el halo del botón Play y el visualizador se tiñen con el aura. Sin `Modifier.blur` ni escalado fuera de los límites (evita recortes rectangulares y anillos duros). La carátula **no** rota.
- **Alternativas descartadas**:
  - `androidx.palette`: dependencia y carga extra solo para un color; menos control sobre el filtrado (grises/oscuros) y difícil de testear en JVM.
  - Rotar la carátula además del aura: descartado por el usuario (solo debe animarse el aura).
  - Servicio/scheduler dedicado para el color: innecesario para un cálculo de milisegundos con caché en memoria.
  - Aura con `Modifier.blur` y anillos `sweepGradient` de gran alpha: descartado tras feedback del usuario (se veían "círculos toscos" y un "cuadrado girando" por el recorte rectangular del blur y el escalado fuera de límites).
- **Razón**: mantiene el dominio libre de Android, no añade dependencias y permite validar la heurística de color con tests unitarios puros. El color de respaldo garantiza un aura coherente cuando la portada no aporta información cromática.

## ADR 014: Ocultar canciones de la biblioteca en vez de eliminarlas
- **Fecha**: 2026-10-13
- **Estado**: Aceptado (consolidado en `origin/main`, 2026-10-14)
- **Contexto**: El usuario pidió poder "eliminar" canciones no deseadas de la biblioteca. Borrar el archivo del dispositivo es destructivo e irreversible y requiere permisos/acciones sobre MediaStore.
- **Decisión**:
  - "Eliminar" = **ocultar**: nueva columna booleana `SongEntity.isHidden` (Room **v5**, migración `MIGRATION_4_5` con `DEFAULT 0`). **No** se borra el archivo.
  - Todas las consultas de biblioteca (canciones, álbumes, artistas, búsqueda, favoritos y canciones de playlist) filtran `isHidden = 0`; `DefaultLibraryScanner` **preserva** el flag en el upsert.
  - UI: **long-press** abre `SongActionsSheet` con "Ocultar de la biblioteca"; una pantalla **Ajustes → Canciones ocultas** (`HiddenSongsScreen`) permite restaurar una o todas.
- **Alternativas descartadas**:
  - Borrado físico del archivo: destructivo, irreversible y con permisos adicionales; descartado por el usuario.
  - Solo "excluir de la vista" sin persistencia: se perdería el efecto en el siguiente re-escaneo.
- **Razón**: es reversible, seguro y no requiere permisos nuevos; el flag sobrevive a los re-escaneos.

## ADR 015: Estadísticas de playlist calculadas en SQL (`PlaylistSummary`)
- **Fecha**: 2026-10-13
- **Estado**: Aceptado (consolidado en `origin/main`, 2026-10-14)
- **Contexto**: La lista de playlists necesitaba portada, número de canciones y duración. Calcularlo en Kotlin obligaría a observar `playlist_songs` + `songs` de todas las playlists y agregar en memoria.
- **Decisión**:
  - `PlaylistDao.observePlaylistSummaries()` devuelve una proyección `PlaylistSummaryRow` con **subconsultas correlacionadas** (`COUNT`, `COALESCE(SUM(durationMs),0)` y primera `coverUri` por `position`), todas excluyendo canciones ocultas (`s.isHidden = 0`).
  - `PlaylistRepository` la expone como `List<PlaylistSummary>` (dominio). Como la consulta referencia `songs`, el Flow se reemite al cambiar la biblioteca.
- **Razón**: una sola consulta reactiva, sin bucles N+1 ni agregación en memoria; coherente con el patrón de filtrado `isHidden` del resto de la biblioteca.

## ADR 016: Mini-player único y minimalista en toda la app
- **Fecha**: 2026-10-13
- **Estado**: Aceptado (consolidado en `origin/main`, 2026-10-14)
- **Contexto**: La barra inferior se veía distinta según la pantalla. Inicio tenía una `PlaybackBar` privada (carátula + metadatos + play/pausa + `LinearProgressIndicator` de 3dp) mientras que el componente compartido `MiniPlayerBar` usaba un `Slider` con etiquetas de tiempo (más alto y con más ruido visual). El usuario pidió una barra limpia y minimalista, de proporción idéntica en todos los apartados.
- **Decisión**:
  - `core/ui/components/MiniPlayerBar.kt` pasa a ser el **único** componente de barra inferior, con el diseño mínimo de Inicio (carátula 42dp, metadatos, play/pausa y `LinearProgressIndicator` de 3dp sobre el que se conservan el swipe-to-skip y el tap para abrir `PlayerScreen`).
  - Se elimina la `PlaybackBar` privada de `HomeScreen` y el `Slider`/etiquetas de tiempo del componente compartido; el parámetro `onSeek` desaparece (el seek vive solo en `PlayerScreen`, según ADR 006).
  - Todas las pantallas (Inicio, Canciones, Favoritos, Álbumes, Artistas, Playlists, Canciones ocultas) colocan el componente con el mismo `Modifier.align(BottomCenter).padding(spacing.m)`.
- **Alternativas descartadas**:
  - Mantener dos implementaciones visualmente parecidas: duplicaba código y volvía a divergir con facilidad.
  - Conservar el `Slider` de seek en la barra: contradecía ADR 006 y el objetivo minimalista.
- **Razón**: una sola fuente de verdad para la barra inferior, apariencia uniforme y menos código.

- **Nota (2026-10-13, rev.)**: se **reintroduce** el parámetro `onSeek` en `MiniPlayerBar` como **scrubber** integrado en la línea de progreso (ver ADR 018), invirtiendo el punto "el parámetro `onSeek` desaparece" de esta decisión. La barra sigue siendo única y minimalista.

## ADR 017: Rediseño del aura a luz ambiental fluida
- **Fecha**: 2026-10-13
- **Estado**: Aceptado (consolidado en `origin/main`, 2026-10-14)
- **Contexto**: Tras el primer `VinylAura` (halo con banda visible en 0.86–0.95 y dos orbes pequeños), el usuario seguía percibiéndolo "pobre/genérico": el brillo quedaba como un anillo fino pegado al borde de la carátula y los orbes se leían como puntos nítidos.
- **Decisión**:
  - `VinylAura` dibuja una **luz ambiental** con degradado continuo (alpha alto en el centro que decae suavemente hasta transparente, sin banda ni anillo duro) más **brumas amplias** (radio ~0.62 del contenedor, alpha bajo) que derivan lentamente al reproducir; en pausa, un halo estático tenue.
  - Se amplía el espacio del vinilo en `PlayerScreen` (carátula al 78% del contenedor, contenedor al 94%) para que el aura rodee la portada en lugar de esconderse tras ella.
  - Se mantiene sin `Modifier.blur` ni escalado fuera de límites (solo `drawWithCache`), evitando recortes rectangulares.
- **Alternativas descartadas**:
  - Reducir la carátula mucho más (p. ej. 0.7): perdía protagonismo sin ganar mucho aura.
  - Usar `Modifier.blur`/`RenderEffect`: ya descartado en ADR 013 por el "cuadrado girando".
- **Razón**: un brillo ancho, suave y en movimiento lento se percibe como luz ambiental agradable y no como formas geométricas; ampliar el margen del vinilo hace visible esa luz alrededor de la portada.

---

## ADR 018: Progreso (scrub) interactivo en el mini-player
- **Fecha**: 2026-10-13
- **Estado**: Aceptado (consolidado en `origin/main`, 2026-10-14)
- **Contexto**: Con la barra mínima (ADR 016) el mini-player mostraba el progreso pero no permitía saltar: había que abrir `PlayerScreen`. El usuario pidió poder **deslizar/correr la barra** en la propia ventana del mini-player, viendo el tiempo mientras se ajusta, sin perder el look minimalista.
- **Decisión**:
  - La línea de progreso de 3dp de `MiniPlayerBar` pasa a ser un **scrubber**: se puede pulsar (tap-to-seek) o arrastrar (arrastre horizontal) para saltar a cualquier punto, usando un único gesto con `awaitEachGesture` (`awaitFirstDown` + seguimiento del puntero).
  - El área táctil es de 22dp (invisible) centrada sobre la línea; la línea visible se engrosa a 5dp **mientras se ajusta**.
  - El **tiempo** (`mm:ss / mm:ss`) se muestra **solo durante el gesto**, reemplazando la línea del artista en color de acento. `isScrubbing`/`scrubMs` son estado local; al soltar se llama a `onSeek`.
  - Se reintroduce el parámetro `onSeek: (Long) -> Unit` y se enhebra por las siete pantallas (Inicio, Canciones, Favoritos, Álbumes, Artistas, Playlists, Canciones ocultas), cada una conectándolo a su `ViewModel.seekTo`.
  - Complementariamente, `MusicServiceConnection` refresca la posición cada **500 ms** (antes 1000 ms) para que la barra avance con fluidez.
- **Alternativas descartadas**:
  - Volver al `Slider` con thumb: contradecía ADR 006/016 (más alto, más ruido).
  - Seek solo por tap: insuficiente para ajustes finos.
  - Mostrar el tiempo siempre: el usuario pidió verlo **solo al arrastrar** para mantener la barra limpia.
- **Razón**: recupera la utilidad de saltar sin salir de la lista, manteniendo la estética minimalista y una única fuente de verdad para la barra inferior.

---

## ADR 019: Aura reactiva a la reproducción mediante energía sintética (sin permisos)
- **Fecha**: 2026-10-13
- **Estado**: Aceptado (consolidado en `origin/main`, 2026-10-14)
- **Contexto**: Tras el rediseño a "luz ambiental" (ADR 017), el usuario pidió que el aura y el reproductor "se vieran más vivos", y que el aura **se moviera y reaccionara según la reproducción**. La reactividad real (visualizador con `android.media.audiofx.Visualizer`) exige el permiso `RECORD_AUDIO`, que no está declarado y sería intrusivo para una app que solo reproduce.
- **Decisión**:
  - El aura se vuelve **reactiva sin permisos**: `VinylAura` dibuja **tres brumas** (color base, aclarado y matiz desviado ~42° de la portada) que orbitan a velocidades distintas y modulan su alpha/pulso con una energía sintética.
  - Se introduce `PlaybackEnergy` (`rememberPlaybackEnergy(isPlaying)`): señal 0f..1f que combina un pulso sintético con el **ritmo real del audio** extraído del pipeline de Media3 vía `RhythmAudioProcessor` + `PlaybackRhythm`. El procesador es passthrough, analiza PCM de 16 bits, aísla la banda de graves y detecta ataques (golpes de bombo/bajo) contra una media adaptativa, publicando un nivel con pico; el dato se considera "fresco" solo durante 500 ms y, cuando no hay señal fresca, se usa solo el pulso sintético. Todo esto **sin el permiso `RECORD_AUDIO`**; el análisis se hace sobre el audio que ya procesa el propio reproductor. Se lee en fase de dibujo (`drawWithCache`), sin recomposición por fotograma.
  - Se mantiene la ausencia total de `RECORD_AUDIO` y de `androidx.palette`; el color sigue derivándose del **color dominante de la carátula** con respaldo `#C62828`.
- **Alternativas descartadas**:
  - `Visualizer` con `RECORD_AUDIO`: requeriría un permiso sensible para una animación estética.
  - `Modifier.blur`/`RenderEffect`: ya descartado en ADR 013/017 por el "cuadrado girando".
  - Rotar la carátula además del aura: el usuario prefiere que solo se mueva el aura.
- **Razón**: logra un efecto vivo y "reactivo a la reproducción" con coste cero de permisos, buen rendimiento y coherencia con el resto del sistema visual.
- **Nota (2026-10-14, rev.)**: para que el aura se mueva realmente "al ritmo de la canción", el `RhythmAudioProcessor` deja de ser un simple RMS y pasa a separar **graves** (paso bajo ~150 Hz) y **agudos** (resta), siguiendo una envolvente de pico por banda y **detectando ataques** contra una media adaptativa; publica en `PlaybackRhythm` un nivel que combina el volumen con un "pulso" cuya **caída se adapta al tempo** estimado entre ataques. `PlaybackEnergy` da más peso a esa señal real. En lo visual, `VinylAura` pasa a **cuatro brumas**, añade una **respiración global** (`scale`) y un **empuje angular** de las brumas en cada golpe, con un **movimiento fluido** (la energía se suaviza en el tiempo con subida rápida y bajada lenta, y se le suma una **deriva ambiental continua** independiente del ritmo), y acelera las derivas (`durationRotation` 13 s, `durationAuroraDrift` 21 s, `durationAuroraSway` 32 s). También se refuerza el tono rojo del respaldo (`#C62828`) y el `vividly` (saturación ≥0.64, valor ≥0.68), y el thumb de la barra de seek pasa a ser de un solo color sólido (`auraColor`). Se usa `DefaultRenderersFactory` personalizado para inyectar el procesador en `DefaultAudioSink` (passthrough; no usa `Visualizer`).
