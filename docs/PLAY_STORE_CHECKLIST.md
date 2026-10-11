# Checklist de Preparación para Google Play Store

Pasos técnicos y administrativos obligatorios para publicar Auralis en la tienda oficial.

**Estado del proyecto**: v0.1.0 entregable. La mayoría de requisitos siguen abiertos.

---

## 🛠 Preparación Técnica

- [ ] **Nombre del Paquete**: Confirmar que `com.auralis.player` es definitivo.
- [ ] **Versionado**: Definir `versionName` y `versionCode` para la release de Play Store. Actual: `0.1.0` / `1`.
- [ ] **Icono Adaptativo**: Verificar resolución y comportamiento de las capas (foreground/background) en distintos dispositivos.
- [ ] **Ofuscación (R8)**: Probar el build de `release` para asegurar que las reglas de ProGuard no rompan la inyección de dependencias ni la serialización de Room.
- [ ] **Permisos**: Justificar el uso de permisos de almacenamiento si se solicita revisión de Google.
  - Permisos actualmente declarados:
    - `READ_MEDIA_AUDIO` (Android 13+)
    - `READ_EXTERNAL_STORAGE` (`maxSdkVersion=32`)
    - `FOREGROUND_SERVICE`
    - `FOREGROUND_SERVICE_MEDIA_PLAYBACK`
    - `POST_NOTIFICATIONS` (Android 13+)
    - `MODIFY_AUDIO_SETTINGS` (en working tree, para AudioFX)
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

- [ ] `./gradlew assembleDebug` sin errores.
- [ ] `./gradlew lintDebug` sin errores críticos.
- [ ] `./gradlew testDebugUnitTest` 100% verde.
- [ ] CI de GitHub Actions en verde (último run: **success** ✅).
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

- El proyecto se encuentra en **v0.1.0** con cambios significativos en el working tree que aún no se han consolidado como versión.
- No se ha generado un build de release firmado todavía.
- No existe captura de pantalla profesional.
- No existe Política de Privacidad publicada.
