# Checklist de Preparación para Google Play Store

Pasos técnicos y administrativos obligatorios para publicar Auralis en la tienda oficial.

**Estado del proyecto**: última release v0.2.0 (`8304e58` + tag `v0.2.0`); `versionName = "0.2.0"` / `versionCode = 2`. El pulido visual del reproductor, la gestión de biblioteca/playlists, el **catálogo por carpetas seleccionadas** (Room v6), el **Acceso rápido por más reproducidas**, los **blindajes anti-vaciado de la biblioteca** y la **optimización de Inicio** ya están **consolidados en `origin/main`** (2026-10-14/15). La mayoría de requisitos de Store siguen abiertos.

---

## 🛠 Preparación Técnica

- [ ] **Nombre del Paquete**: Confirmar que `com.auralis.player` es definitivo.
- [ ] **Versionado**: Definir `versionName` y `versionCode` para la release de Play Store. Actual: `0.2.0` / `2` (consolidado localmente en `8304e58` + tag `v0.2.0`).
- [ ] **Icono Adaptativo**: Verificar resolución y comportamiento de las capas (foreground/background) en distintos dispositivos. *Foreground actualizado (2026-10-12) en todas las densidades; background `#000000`.*
- [ ] **Ofuscación (R8)**: Probar el build de `release` para asegurar que las reglas de ProGuard no rompan la inyección de dependencias ni la serialización de Room. *`assembleRelease` verificado localmente en la consolidación 2026-10-12.*
- [ ] **Permisos**: Justificar el uso de permisos de almacenamiento si se solicita revisión de Google.
  - Permisos actualmente declarados:
    - `READ_MEDIA_AUDIO` (Android 13+)
    - `READ_EXTERNAL_STORAGE` (`maxSdkVersion=32`)
    - `FOREGROUND_SERVICE`
    - `FOREGROUND_SERVICE_MEDIA_PLAYBACK`
    - `POST_NOTIFICATIONS` (Android 13+, solicitado en runtime)
    - `MODIFY_AUDIO_SETTINGS` (AudioFX / ecualizador)
    - `WAKE_LOCK`
- [ ] **App Bundle**: Generar archivo `.aab` firmado con una clave de producción segura.

---

## 🎨 Activos Gráficos (Store Assets)

- [ ] **Capturas de Pantalla**: Al menos 4 capturas en resolución de 7" y 10" (Teléfono y Tablet).
- [ ] **Feature Graphic**: Imagen de 1024x500 que capture la esencia de Auralis.
- [ ] **Descripción Corta**: Texto atractivo de máximo 80 caracteres.
- [ ] **Descripción Larga**: Detallar funcionalidades, privacidad y soporte de formatos.

---

## ⚖️ Legal y Privacidad

- [ ] **Política de Privacidad**: URL alojada que detalle que la app no recolecta datos personales ni musicales.
- [ ] **Data Safety**: Completar el formulario de seguridad de datos en Play Console (Declarar que no se comparten datos con terceros).
- [ ] **Clasificación de Contenido**: Responder cuestionario IARC.

---

## ✅ Calidad Mínima Recomendada

Antes de subir a "Pruebas Internas", verificar:

- [ ] `./gradlew assembleDebug` sin errores. *(Verificado 2026-10-12 ✅)*
- [ ] `./gradlew lintDebug` sin errores críticos. *Verificado localmente.*
- [ ] `./gradlew testDebugUnitTest` 100% verde. *Verificado localmente.*
- [ ] Tests instrumentados (`connectedDebugAndroidTest`) en dispositivo/emulador.
- [ ] CI de GitHub Actions en verde (workflow de unit+lint; matriz de emuladores añadida 2026-10-12, pendiente de verificar en remoto).
- [ ] Probar en al menos 2 dispositivos físicos diferentes.
- [ ] Verificar el ciclo de vida del `MediaSessionService` tras bloqueo de pantalla prolongado.

---

## 🚦 Lanzamiento

- [ ] Crear ficha de Play Store.
- [ ] Subir App Bundle al canal de "Pruebas Internas" o "Beta Cerrada".
- [ ] Verificar reporte de pre-lanzamiento (Android Vitals y fallos en dispositivos de prueba de Google).
- [ ] Publicar a Producción.

---

## ⚠️ Bloqueos Conocidos

- La versión **v0.2.0** está consolidada (`8304e58` + tag `v0.2.0`) y subida a `origin/main`; el pulido visual del reproductor y la gestión de biblioteca/playlists también están en `origin/main` (2026-10-14).
- No se ha generado un build de release **firmado** todavía (el release actual se genera sin firmar).
- No existe captura de pantalla profesional.
- No existe Política de Privacidad publicada.
