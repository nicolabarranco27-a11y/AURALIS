name: Pull Request
description: Plantilla para Pull Requests
title: "[PR]: "
labels: []
body:
  - type: markdown
    attributes:
      value: |
        ## Descripción
        Qué cambios introduce este PR y por qué.

  - type: checkboxes
    id: type
    attributes:
      label: Tipo de Cambio
      options:
        - label: Nueva funcionalidad (feat)
        - label: Corrección de bug (fix)
        - label: Refactor (refactor)
        - label: Documentación (docs)
        - label: Tests (test)
        - label: Mantenimiento / Build / Deps (chore)
        - label: Mejora de rendimiento (perf)
        - label: Estilo / Formato (style)

  - type: textarea
    id: related
    attributes:
      label: Issues Relacionados
      description: Closes #123, Fixes #456, Relates to #789
      placeholder: "Closes #123"

  - type: textarea
    id: changes
    attributes:
      label: Cambios Principales
      description: Lista los archivos/modulos principales tocados y qué hacen
      placeholder: |
        - feature/playlists: AddToPlaylistScreen + ViewModel (nueva pantalla para añadir canciones)
        - data/repository: RoomPlaylistRepository.addSongsToPlaylist()
        - domain/model: PlaylistSong entity
        - navigation: Nueva ruta ADD_TO_PLAYLIST

  - type: textarea
    id: testing
    attributes:
      label: Cómo Testear
      description: Pasos manuales y/o tests automatizados añadidos
      placeholder: |
        1. Abrir app → Playlists → Crear "Test"
        2. Click FAB "+" → Abre AddToPlaylistScreen
        3. Seleccionar 3 canciones → Checkboxes funcionan
        4. Back → PlaylistDetail muestra las 3 canciones
        5. ./gradlew testDebugUnitTest pasa

  - type: textarea
    id: screenshots
    attributes:
      label: Capturas (obligatorio para cambios UI)
      description: Arrastra antes/después, grabaciones, o link a Figma

  - type: checkboxes
    id: checklist
    attributes:
      label: Checklist Pre-Merge
      options:
        - label: Código compila (`./gradlew assembleDebug`)
        - label: Tests pasan (`./gradlew testDebugUnitTest`)
        - label: Lint limpio (`./gradlew lintDebug`)
        - label: Ktlint/Format aplicado
        - label: Commits siguen Conventional Commits
        - label: Documentación actualizada (README, KDoc, CHANGELOG si aplica)
        - label: Sin secrets/keystores en diff
        - label: PR title sigue Conventional Commits