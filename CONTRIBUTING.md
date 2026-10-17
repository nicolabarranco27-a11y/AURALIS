# Contribuir a Auralis

¡Gracias por tu interés en Auralis!

> ⚠️ **Aviso de licencia — contribuciones solo con acuerdo previo.**
> Auralis es **software propietario** distribuido bajo la **PolyForm Noncommercial
> License 1.0.0** (ver [`LICENSE`](LICENSE)). El proyecto **es de código abierto
> solo para lectura (source-available)** y **no acepta contribuciones externas
> espontáneas**. Para enviar una contribución necesitás **contactar antes** a
> soporte@estalingradocorp.com y firmar un **acuerdo de cesión de derechos (CLA)**
> que transfiera la titularidad de tu aporte a EstalingradoCorp. Los Pull Requests
> sin ese acuerdo **no serán fusionados**.

## 🚀 Flujo para contribuciones autorizadas

1. **Contactá primero** a soporte@estalingradocorp.com y firmá el **CLA**.
2. **Fork** el repositorio (solo tras la autorización).
3. **Clona** tu fork: `git clone https://github.com/TU-USUARIO/AURALIS.git`
4. **Crea una rama**: `git checkout -b feature/mi-nueva-funcionalidad`
5. **Desarrolla** y testea tus cambios
6. **Commit** con mensajes convencionales
7. **Push** y abre un **Pull Request**

## 📋 Estándares de Código

### Kotlin
- Seguir [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Usar `ktlint` (configurado en el proyecto)
- Documentar APIs públicas con KDoc

### Commits (Conventional Commits)
```
<tipo>(<ámbito>): <descripción corta>

[cuerpo opcional]

[pie opcional]
```

| Tipo | Uso |
|------|-----|
| `feat` | Nueva funcionalidad |
| `fix` | Corrección de bug |
| `refactor` | Refactor sin cambio de comportamiento |
| `style` | Formato, imports, sin lógica |
| `docs` | Documentación |
| `test` | Tests |
| `chore` | Build, deps, mantenimiento |
| `perf` | Mejora de rendimiento |

**Ejemplos:**
```
feat(playlists): agregar drag-and-drop para reordenar canciones
fix(player): corregir seek en archivos VBR
refactor(domain): extraer UseCases para PlaybackRepository
```

### Pull Requests
- **Título**: sigue Conventional Commits
- **Descripción**: qué cambia, por qué, cómo testear
- **Relaciona issues**: `Closes #123`, `Fixes #456`
- **Screenshots** obligatorios para cambios de UI
- **Tests** requeridos para nueva lógica

## 🏗 Arquitectura

Respeta la **Clean Architecture** del proyecto:

```
domain/          # Reglas de negocio puras (sin Android)
data/            # Implementaciones (Room, MediaStore, Media3)
feature/         # Pantallas MVVM + Compose
core/            # Shared (theme, utils, result)
di/              # Hilt modules
```

**Reglas:**
- `domain` **no depende** de `data` ni `feature`
- `feature` depende de `domain` (interfaces) y `core`
- `data` implementa interfaces de `domain`
- Inyección via Hilt en `di/`

## 🧪 Testing

```bash
# Unit tests
./gradlew testDebugUnitTest

# Instrumented tests
./gradlew connectedDebugAndroidTest

# Lint
./gradlew lintDebug
```

- Cobertura mínima: **80%** en domain/data
- Tests unitarios para ViewModels, UseCases, Repositories
- Tests de UI para flujos críticos (Compose Testing)

## 🎨 UI / Compose

- **Material 3** + tema personalizado en `core/ui/theme/`
- **Tokens** (Color, Type, Shape, Spacing, Motion) - no hardcodees valores
- **Preview** en cada `@Composable` principal
- Accesibilidad: `contentDescription`, `semantics`, touch targets ≥ 48dp
- Soporte **Light/Dark** theme

## 📱 Permisos

- Solicita permisos **en runtime** con UI explicativa
- Maneja denegación graceful (estado vacío con botón "Conceder permiso")
- `AndroidManifest.xml` declara todos los permisos usados

## 🔒 Seguridad

- **No commits** secrets, keystores, API keys
- `local.properties` y `signing.properties` en `.gitignore`
- Usa `BuildConfig` o `secrets.properties` (no commiteado) para configs sensibles
- ProGuard/R8 activado en release

## 📦 Release

1. Actualiza `versionCode` y `versionName` en `app/build.gradle.kts`
2. Changelog en `CHANGELOG.md` (Keep a Changelog format)
3. Tag: `git tag -a v1.0.0 -m "Release v1.0.0"`
4. `./gradlew bundleRelease` → sube AAB a Play Console

## 🐛 Reportar Bugs

Usa la plantilla **Bug Report** (`.github/ISSUE_TEMPLATE/bug_report.yml`):
- Versión app / Android / Dispositivo
- Pasos para reproducir
- Comportamiento esperado vs actual
- Logs/capturas si aplica

## 💡 Sugerir Features

Usa la plantilla **Feature Request** (`.github/ISSUE_TEMPLATE/feature_request.yml`):
- Problema que resuelve
- Solución propuesta
- Alternativas consideradas
- Mockups si es UI

## 📄 Licencia

Este proyecto está bajo la **PolyForm Noncommercial License 1.0.0**. Al enviar
cualquier contribución autorizada, aceptás que tus aportes queden sujetos a
dicha licencia y cedés la titularidad a EstalingradoCorp según el CLA.

## 📞 Contacto

- Issues: [GitHub Issues](https://github.com/nicolabarranco27-a11y/AURALIS/issues)
- Email: soporte@estalingradocorp.com

---

**¡Gracias por tu interés en Auralis!** 🎵