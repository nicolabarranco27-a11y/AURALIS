# Guía para Agentes de IA - Auralis Project

Este documento contiene las reglas, principios y guías de estilo obligatorias para cualquier agente de IA que trabaje en el repositorio de Auralis.

## 🏗 Arquitectura y Estructura

Auralis sigue estrictamente **Clean Architecture** junto con el patrón **MVVM** y **Jetpack Compose**.

1. **Domain Layer**: Contiene las entidades de negocio (`domain/model`) e interfaces de repositorio (`domain/repository`). No debe tener dependencias de Android ni de bibliotecas de terceros (excepto Coroutines/Flow).
2. **Data Layer**: Implementaciones de repositorios, DAOs de Room, fuentes de datos (MediaStore) y servicios de audio (Media3).
3. **UI Layer**: Componibles de Compose y ViewModels organizados por features (`feature/`).

## 🎨 Guía de Estilo UI (Jetpack Compose)

- **Tokens de Diseño**: Utilizar siempre `ReproductorThemeTokens` para acceder a colores, espaciados y tipografías. Nunca hardcodear valores.
- **Componentes Core**: Reutilizar componentes en `core/ui/components/` (ej. `AuralisArtwork`).
- **Paddings**: Mantener la consistencia con los tokens de `AppSpacing`.
- **Iconos**: Preferir `androidx.compose.material.icons.Icons.Rounded` (disponibles vía `material-icons-extended`).

## 🛠 Guardrails Técnicos

- **Media3/ExoPlayer**: No modificar la configuración del motor de audio a menos que sea estrictamente necesario para AudioFX o estabilidad.
- **SQLite/Room**: Las búsquedas deben usar `LOWER` y `REPLACE` para ser insensibles a acentos y mayúsculas.
- **Hilos**: Las operaciones pesadas (mapeos, UUIDs, DB) deben ejecutarse en `Dispatchers.Default` o `Dispatchers.IO`, devolviendo el resultado al Hilo Principal para llamadas de Media3 o actualizaciones de UI.
- **AudioFX**: Instanciar siempre dentro de bloques `try-catch` para evitar fallos en dispositivos sin soporte de hardware.

## 📚 Sistema de Documentación Continua

Este repositorio mantiene una documentación viva que **debe reflejar siempre el estado real del código**, no planes hipotéticos ni versiones futuras.

### Reglas Generales

1. **Antes** de implementar cambios importantes, revisar la documentación actual para conocer el estado real del proyecto.
2. **Después** de cada funcionalidad, corrección o cambio relevante, actualizar la documentación correspondiente.
3. La documentación describe el estado real del código. Nunca inventar funcionalidades, resultados, tests ni estados.
4. Mantener la documentación consolidada y evitar duplicaciones innecesarias.
5. Mantener un registro claro de:
   - funcionalidades terminadas;
   - funcionalidades en progreso;
   - bugs conocidos;
   - bugs corregidos;
   - decisiones técnicas;
   - cambios de arquitectura;
   - estado de CI/CD;
   - estado de preparación para GitHub;
   - estado de preparación para Google Play Store.
6. Al terminar una tarea importante: revisar qué archivos cambiaron, actualizar la documentación correspondiente, ejecutar las verificaciones pertinentes y dejar el repositorio consistente.

### Documentos y Cuándo Actualizarlos

| Documento | Cuándo actualizarlo |
|---|---|
| `README.md` | Cambios en stack, build variants, permisos, novedades |
| `CHANGELOG.md` | Cualquier funcionalidad nueva, fix o cambio visible para el usuario. Formato [Keep a Changelog] |
| `docs/PROJECT_STATUS.md` | Fotografía del estado real. Marcar items como "pendientes/no consolidados" si aún no están commiteados |
| `docs/FEATURES.md` | Si se agrega, modifica o elimina una funcionalidad del usuario |
| `docs/ARCHITECTURE.md` | Cambios en capas, DI, threading, flujo de audio |
| `docs/DESIGN_SYSTEM.md` | Cambios en tokens, paleta, componentes core |
| `docs/DECISIONS.md` | Cuando exista una decisión arquitectónica o técnica relevante (ADR) |
| `docs/RELEASE_PLAN.md` | Cuando cambie el estado de un hito del plan de release |
| `docs/PLAY_STORE_CHECKLIST.md` | Cuando se complete algún requisito de release en Play Store |
| `docs/PRODUCT.md` | Cambios en visión, pilares, público objetivo o roadmap |

### Reglas Específicas

- **No inventar contenido**: Si una funcionalidad no está implementada en código, no documentarla como completada.
- **Marcar lo pendiente**: Lo que vive solo en el working tree (sin commit) debe identificarse como "pendiente de consolidación".
- **No duplicar**: Si un dato aparece en varios documentos, mantenerlo sincronizado.
- **CHANGELOG.md no incluye planes**: Solo cambios efectivamente entregados o en desarrollo confirmado.
- **PROJECT_STATUS.md es la fuente principal del estado**: si hay discrepancias con otros docs, PROJECT_STATUS.md gana.

## 🧪 Calidad

- Cada nueva lógica en `ViewModel` o `Repository` debe venir acompañada de sus correspondientes tests unitarios en `app/src/test/`.
- El build debe pasar siempre `./gradlew lintDebug`.
