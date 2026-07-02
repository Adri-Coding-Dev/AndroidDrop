## 4. Estructura de Modulos

### 4.1 Modulos Gradle

```
AndroidDrop/
+-- app/                          # Aplicacion principal (wrapper)
+-- build-logic/                  # Convenciones de build compartidas
+-- core/
|   +-- core-ui/                  # Componentes UI reutilizables
|   +-- core-network/             # Abstracciones de red
|   +-- core-crypto/              # Cifrado E2E, hashing, checksum
|   +-- core-common/              # Extensiones, utils, constantes
|   +-- core-testing/             # Mocks, test doubles, rules
+-- data/
|   +-- data-file-system/         # Acceso a sistema de archivos
|   +-- data-ble/                 # BLE advertising + scan
|   +-- data-wifi-direct/         # Wi-Fi P2P
|   +-- data-nearby/              # Nearby Connections API wrapper
|   +-- data-transfer/            # Transferencia (sockets cifrados)
+-- domain/
|   +-- domain-model/             # Entidades puras de dominio
|   +-- domain-repository/        # Interfaces de repositorio
|   +-- domain-usecase/           # Casos de uso
+-- feature/
|   +-- feature-file-explorer/    # Explorador de archivos
|   +-- feature-transfer/         # Pantalla de transferencia (esfera)
|   +-- feature-discovery/        # Descubrimiento de dispositivos
|   +-- feature-settings/         # Configuracion
|   +-- feature-diagnostics/      # Diagnostico y logs
+-- service/
|   +-- service-discovery/        # Servicio foreground de descubrimiento
|   +-- service-transfer/         # Servicio foreground de transferencia
+-- animation/
|   +-- animation-engine/         # Motor de animaciones GL/Canvas
|   +-- animation-sphere/         # Renderizado de esfera energetica
|   +-- animation-portal/         # Renderizado de portal
|   +-- animation-particles/      # Sistema de particulas
|   +-- animation-gesture/        # Fisica de gestos
+-- sync/
|   +-- sync-protocol/            # Protocolo de sincronizacion
+-- security/
|   +-- security-crypto/          # Operaciones criptograficas
|   +-- security-key-exchange/    # Intercambio de claves ECDH
+-- docs/                         # Documentacion tecnica
```

### 4.2 Reglas de Dependencias entre Modulos

- `feature/*` puede depender de `domain/*`, `core/*`
- `data/*` implementa interfaces de `domain/*`
- `domain/*` NO depende de `data/*` ni de `core/android`
- `feature/*` se comunica con otros `feature/*` solo via interfaces compartidas en `core/*`
- `animation/*` es independiente, solo depende de `core/common`
- `sync/*` depende de `domain/model` y `core/network`
- `security/*` depende de `core/common`

### 4.3 Convenciones de Nomenclatura

- **Modulos core:** `core-{nombre}` - librerias base sin logica de negocio
- **Modulos data:** `data-{fuente}` - implementaciones de repositorios
- **Modulos domain:** `domain-{categoria}` - logica de negocio pura
- **Modulos feature:** `feature-{nombre}` - pantallas y flujos de UI
- **Modulos service:** `service-{nombre}` - servicios Android foreground
- **Modulos animation:** `animation-{nombre}` - renderizado y fisica
- **Modulos sync:** `sync-{nombre}` - sincronizacion entre dispositivos
- **Modulos security:** `security-{nombre}` - cifrado y autenticacion
