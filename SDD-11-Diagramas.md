## 11. Diagramas

### 11.1 Diagrama de Estados (Transferencia)

```
                    +----------+
                    |   IDLE   |
                    +----+-----+
                         | Select File
                         v
              +----------------------+
              |  FILE_PREVIEW        |
              |  (Archivo centrado)  |
              +----------+-----------+
                         | Animate
                         v
              +----------------------+
              |  SPHERE_TRANSFORM    |
              |  (Archivo -> Esfera) |
              +----------+-----------+
                         | Complete
                         v
              +----------------------+
         +--->|  DISCOVERING         |<-----------+
         |    |  "Buscando..."       |            |
         |    +----------+-----------+            |
         |               | Device Found           |
         |               v                        |
         |    +----------------------+            |
         |    |  DEVICE_FOUND        |            |
         |    |  "Dispositivo X"    |            |
         |    +----------+-----------+            |
         |               | Gesture (drag)         |
         |               v                        |
         |    +----------------------+            |
         |    |  SPHERE_HELD         |            |
         |    |  (Sigue dedo, gel)   |            |
         |    +----------+-----------+            |
         |               | Release (throw)        |
         |               v                        |
         |    +----------------------+            |
         |    |  SPHERE_LAUNCHED     |            |
         |    |  (Hacia portal)     |            |
         |    +----------+-----------+            |
         |               | Enter portal           |
         |               v                        |
         |    +----------------------+            |
         |    |  ENTERING_PORTAL     |            |
         |    |  (Desaparece)       |            |
         |    +----------+-----------+            |
         |               | Transfer start         |
         |               v                        |
         |    +----------------------+            |
         |    |  TRANSFERRING        |            |
         |    |  (Esfera decayendo)  |            |
         |    +----------+-----------+            |
         |               | Progress 100%          |
         |               v                        |
         |    +----------------------+            |
         |    |  SPHERE_EXPLODING    |            |
         |    |  (Explosion suave)  |            |
         |    +----------+-----------+            |
         |               | File reconstruct       |
         |               v                        |
         |    +----------------------+            |
         |    |  TRANSFER_COMPLETE   |            |
         |    |  (Exito)            |            |
         |    +----------+-----------+            |
         |               | New transfer           |
         +---------------v                        |
                                                  |
         ERROR -----------------------------------+
```

### 11.2 Diagrama de Componentes del Modulo de Transferencia

```
+-----------------------------------------------------------+
|                    TransferViewModel                       |
|  - StateFlow<TransferUiState>                             |
|  - accept(intent: TransferIntent)                         |
|  - collectSideEffects(): Flow<SideEffect>                 |
+----------+----------------------------+------------------+
           |                            |
           v                            v
+------------------------+  +----------------------------+
|  GetDeviceUseCase      |  |  StartTransferUseCase     |
|  - discover()          |  |  - prepareFile()          |
|  - pair()              |  |  - negotiateSession()     |
|  - authenticate()      |  |  - sendChunks()           |
+-----------+-----------+  |  - monitorProgress()       |
           |               +------------+---------------+
           v                            v
+------------------------+  +----------------------------+
|  DeviceRepository      |  |  TransferRepository       |
|  (Interface)           |  |  (Interface)              |
+-----------+-----------+  +------------+---------------+
           |                            |
+----------+-----------+  +-------------+----------------+
|  BLEDeviceRepo       |  |  WiFiDirectTransferRepo     |
|  - BluetoothAdapter  |  |  - WifiP2pManager           |
|  - BLEDevice         |  |  - ServerSocket             |
|  - GATT Server/Client|  |  - Socket channels          |
+----------------------+  +-----------------------------+
```

### 11.3 Diagrama de Modulos y sus Dependencias

```
feature/file-explorer     feature/transfer      feature/discovery
        |                       |                      |
        +----------+------------+----------+-----------+
                   |                       |
              domain/usecase          domain/repository
                   |                       |
              domain/model           data/ble,
                   |                  data/wifi-direct,
              core/common,           data/nearby,
              core/crypto,           data/transfer
              core/network
                   |
              animation/engine,
              animation/sphere,
              animation/portal,
              animation/particles,
              animation/gesture,
              sync/protocol,
              security/crypto,
              security/key-exchange
```
