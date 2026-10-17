# Avisos de Terceros (Third-Party Notices)

Auralis (`com.auralis.player`) es **software propietario** distribuido bajo la
**PolyForm Noncommercial License 1.0.0** (ver [`LICENSE`](LICENSE)). Este archivo
lista las **dependencias de terceros** que utiliza el proyecto y sus licencias,
para cumplimiento. Las licencias de terceros **no se ven afectadas** por la
licencia de Auralis y debés respetarlas por separado.

> Estos avisos son informativos y no constituyen asesoramiento legal. Las
> versiones indicadas corresponden a `gradle/libs.versions.toml`.

## Dependencias de ejecución (runtime)

| Componente | Versión | Licencia |
|---|---|---|
| AndroidX Compose — UI, Material3, Tooling Preview, Material Icons (core/extended) | BOM 2026.08.00 | Apache License 2.0 |
| AndroidX Activity Compose | 1.13.0 | Apache License 2.0 |
| AndroidX Lifecycle (ViewModel / Runtime Compose) | 2.11.0 | Apache License 2.0 |
| AndroidX Navigation Compose | 2.9.8 | Apache License 2.0 |
| AndroidX Media3 (ExoPlayer, Session, Common) | 1.11.0 | Apache License 2.0 |
| AndroidX Room (Runtime, KTX) | 2.8.4 | Apache License 2.0 |
| AndroidX Work (work-runtime-ktx) | 2.11.2 | Apache License 2.0 |
| AndroidX Hilt (Navigation Compose, Work) | 1.4.0 | Apache License 2.0 |
| Dagger / Hilt (Hilt Android) | 2.60.1 | Apache License 2.0 |
| Kotlin Standard Library | 2.3.21 | Apache License 2.0 |
| Kotlinx Coroutines (Android) | 1.11.0 | Apache License 2.0 |
| Coil 3 (coil-compose) | 3.6.0 | Apache License 2.0 |
| AndroidX Test (core, ext-junit, runner, rules) | 1.7.0 / 1.3.0 | Apache License 2.0 |

## Herramientas de compilación (build)

| Componente | Versión | Licencia |
|---|---|---|
| Android Gradle Plugin (AGP) | 9.3.2 | Apache License 2.0 |
| Gradle / Gradle Wrapper | — | Apache License 2.0 |
| Kotlin Gradle Plugin | 2.3.21 | Apache License 2.0 |
| Kotlin Symbol Processing (KSP) | 2.3.11 | Apache License 2.0 |

## Dependencias de pruebas (test)

| Componente | Versión | Licencia |
|---|---|---|
| JUnit 4 | 4.13.2 | Eclipse Public License 1.0 |
| Robolectric | 4.16.1 | MIT License |
| Kotlinx Coroutines Test | 1.11.0 | Apache License 2.0 |
| AndroidX Compose UI Test (JUnit4) | BOM 2026.08.00 | Apache License 2.0 |
| AndroidX Work Testing | 2.11.2 | Apache License 2.0 |

## Recursos

- **Material Symbols / Material Icons** (`androidx.compose.material:material-icons-extended`):
  Apache License 2.0.
- **Fuentes tipográficas**: según lo declarado en `core/ui/theme` (si aplica),
  sujetas a sus propias licencias.

## Textos de las licencias

- **Apache License 2.0**: <https://www.apache.org/licenses/LICENSE-2.0>
- **Eclipse Public License 1.0**: <https://www.eclipse.org/legal/epl-v10.html>
- **MIT License**: <https://opensource.org/licenses/MIT>
