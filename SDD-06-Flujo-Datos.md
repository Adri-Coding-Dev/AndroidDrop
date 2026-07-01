## 6. Flujo de Datos y Protocolo

### 6.1 Ciclo de Vida de una Transferencia

```
EMISOR                                          RECEPTOR
  |                                                |
  +-- SelectFile                                  |
  |                                                |
  +-- AnimateFileToSphere ------------------------>+-- Mostrar Portal
  |                                                |
  +-- BLE Discovery ----> DeviceFound              |
  |       |                                        |
  |       +-- PairRequest ----------------------->+-- AcceptPair
  |                                                |
  +-- SphereIdle ----> GestureDetect               |
  |       |                                        |
  |       +-- Launched                             |
  |                                                |
  +-- AnimateEnterPortal ------------------------>+-- AnimateExitPortal
  |       |                                        |
  |       +-- TransferStart <---- Handshake ------>+-- TransferStart
  |       |                                        |
  +-- AnimateSphereDecay <---- Progress ---------->+-- AnimateSphereDecay
  |       |                                        |
  |       +-- Done ------------------------------->+-- FileReconstruct
  |                                                |
  +-- ShowSuccess <--------- sync --------------->+-- ShowSuccess
```

### 6.2 Canales de Comunicacion

| Fase | Canal | Proposito |
|---|---|---|
| Descubrimiento | BLE Advertising + Scan | Detectar dispositivos cercanos |
| Handshake | BLE GATT + L2CAP | Intercambiar capacidades, claves |
| Sincronizacion | BLE GATT (notifications) | Estado de animacion |
| Transferencia | Wi-Fi Direct TCP Socket | Archivo cifrado |
| Control | TCP Socket (keep-alive) | ACK, control de flujo, checksum |

### 6.3 Formato de Paquetes

```kotlin
@Serializable
data class ProtocolPacket(
    val version: Int = 1,
    val type: PacketType,
    val deviceId: String,
    val sessionId: String,
    val sequenceNumber: Long,
    val timestamp: Long,
    val payload: ByteArray,
    val signature: ByteArray  // HMAC-SHA256
)

@Serializable
enum class PacketType {
    DISCOVERY, HANDSHAKE,
    AUTH_CHALLENGE, AUTH_RESPONSE,
    FILE_METADATA, CHUNK_DATA,
    CHUNK_ACK, STATE_SYNC,
    PROGRESS_SYNC, KEEP_ALIVE,
    TRANSFER_COMPLETE, TRANSFER_CANCEL,
    ERROR
}
```

### 6.4 Capa de Descubrimiento (BLE)

```
Device A (Advertiser)                    Device B (Scanner)
       |                                        |
       |--- Advertising Packet ---------------->|
       |    Service UUID (AndroidDrop)          |
       |    Device Name (truncado)              |
       |    Random Token                        |
       |                                        |
       |<-- Scan Result ------------------------|
       |                                        |
       |--- Connection Request ---------------->|
       |<-- Connection Accepted ----------------|
       |                                        |
       |--- GATT: Exchange Capabilities ------>|
       |    Version protocolo                   |
       |    Clave publica efimera (ECDH)        |
       |    Wi-Fi Direct SSID/Passphrase        |
       |<-- GATT: Capabilities ACK -------------|
```

### 6.5 Capa de Transferencia (Wi-Fi Direct + TCP)

```
       |                                        |
       |--- Wi-Fi P2P Connect ----------------->|
       |<-- Wi-Fi P2P Connected ----------------|
       |                                        |
       |--- TCP: Auth Handshake -------------->|
       |    Nonce cifrado con clave compartida  |
       |<-- TCP: Auth Response -----------------|
       |    Nonce + 1 cifrado                   |
       |                                        |
       |--- TCP: File Metadata --------------->|
       |    Nombre, tamano, checksum SHA-256    |
       |    Fragmentos (size, count)            |
       |<-- TCP: Ready -------------------------|
       |                                        |
       |--- TCP: Encrypted Chunk [0..N] ------>|
       |    AES-256-GCM cifrado                 |
       |    IV + Tag + Ciphertext              |
       |<-- TCP: Chunk ACK ---------------------|
       |    Offset confirmado                   |
       |                                        |
       |--- TCP: Transfer Complete ----------->|
       |<-- TCP: SHA-256 Verification ----------|
```

### 6.6 Capa de Sincronizacion

Frecuencia de sincronizacion: **20-30 Hz** sobre BLE GATT Notify + TCP

```kotlin
data class SyncFrame(
    val phase: TransferPhase,
    val progress: Float,
    val sphereEnergy: Float,
    val sphereScale: Float,
    val spherePosition: Vector3,
    val portalIntensity: Float,
    val particleCount: Int,
    val masterTimestamp: Long,  // ns
    val localTimestamp: Long    // ns para calcular drift
)
```

Mecanismos:
1. **Clock maestro**: El emisor define el timeline
2. **Timestamp absoluto**: Cada evento lleva timestamp del clock maestro
3. **Latency compensation**: El receptor ajusta su timeline basado en RTT
4. **Interpolacion**: Si un paquete se pierde, se interpola hasta recibir el siguiente
5. **State machine compartida**: Ambos dispositivos tienen la misma maquina de estados
