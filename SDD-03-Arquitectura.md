## 3. Arquitectura General

### 3.1 Diagrama de Capas

```
+-----------------------------------------------------------+
|                    Presentation Layer                      |
|  +----------+  +----------+  +------------------------+  |
|  |   UI      |  |   VM     |  |   Navigation           |  |
|  | (Compose) |  | (State)  |  |   (Decompose/Custom)   |  |
|  +----+-----+  +----+-----+  +----------+-----------+  |
|       |              |                   |              |
|  +----+--------------+-------------------+-----------+  |
|  |              Domain Layer                           |  |
|  |  +------------+ +----------+ +----------------+    |  |
|  |  |  UseCases  | |  Models  | |  Repository    |    |  |
|  |  |            | |  (Entity)| |  Interfaces    |    |  |
|  |  +------------+ +----------+ +----------------+    |  |
|  +----------------------------------------------------+  |
|                        |                                  |
|  +--------------------+--------------------------------+  |
|  |              Data Layer                              |  |
|  |  +----------+ +----------+ +----------------+      |  |
|  |  |  Local   | |  Remote  | |  Repository    |      |  |
|  |  | (Room/   | | (BLE/WiFi| |  Impl          |      |  |
|  |  |  FS)     | | /Sockets)| |                |      |  |
|  |  +----------+ +----------+ +----------------+      |  |
|  +----------------------------------------------------+  |
+-----------------------------------------------------------+
```

### 3.2 Patron MVVM + MVI

Cada pantalla sigue el patron **Model-View-Intent** con un **ViewModel** que expone un `StateFlow<UiState>` y recibe `Intent` sellados.

```
User Action -> Intent -> ViewModel -> Reduce State -> UI Render
                                                <=>
                                        Side Effects (OneShot)
```

El estado es inmutable y solo se modifica mediante funciones puras de reduccion.

### 3.3 Inyeccion de Dependencias

Decision: **Hilt** sobre Koin.

**Justificacion:**
- Hilt esta oficialmente soportado por Google para Android
- Integracion nativa con Jetpack Compose, ViewModel, Navigation
- Verificacion en tiempo de compilacion (Koin es runtime)
- Mejor rendimiento en startup (codigo generado vs. reflection)
- Scope management robusto (@Singleton, @ViewModelScoped, @ActivityRetainedScoped)

**Trade-off:** Hilt anade ~20s a builds iniciales. Aceptable para un proyecto de produccion.

### 3.4 Gestion de Estados

```kotlin
data class TransferUiState(
    val phase: TransferPhase = TransferPhase.Idle,
    val selectedFile: FileNode? = null,
    val discoveredDevices: List<NearbyDevice> = emptyList(),
    val pairedDevice: NearbyDevice? = null,
    val transferProgress: Float = 0f,
    val sphereState: SphereState = SphereState.Idle,
    val error: TransferError? = null
)

sealed interface TransferIntent {
    data class SelectFile(val file: FileNode) : TransferIntent
    data object StartDiscovery : TransferIntent
    data class ConnectToDevice(val device: NearbyDevice) : TransferIntent
    data class LaunchSphere(val velocity: Velocity) : TransferIntent
    data object CancelTransfer : TransferIntent
}
```

### 3.5 Reglas de Dependencias

```
feature/* -> domain/* -> core/*
                    -> data/*

domain/* NO depende de data/* ni de nada Android
core/* es puramente tecnico, sin logica de negocio
```
