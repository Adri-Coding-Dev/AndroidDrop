## 12. Escalabilidad Futura

### 12.1 Estrategia Multiplataforma

La arquitectura permite anadir nuevas plataformas sin reescribir:

```
+-------------------------------------------+
|           Protocol Layer                   |
|  (Network Protocol Definition - agnostico) |
+----------+--------------------+----------+
           |                    |
+----------v----+     +---------v----------+
|  Android Drop  |     |  Desktop Client   |
|  (Android)     |     |  (KMP/Compose MP) |
|  - BLE         |     |  - mDNS/Bonjour   |
|  - WiFi Direct |     |  - TCP/IP         |
|  - Nearby API  |     |  - WebRTC         |
+----------------+     +-------------------+
```

### 12.2 Extensiones Previstas

| Plataforma | Descubrimiento | Transferencia |
|---|---|---|
| Android TV | BLE | Wi-Fi Direct |
| Wear OS | BLE | BLE (archivos pequenos) |
| Windows | mDNS | TCP/IP |
| macOS | Bonjour | TCP/IP |
| Linux | Avahi | TCP/IP |
| iOS | BLE | WebRTC/TCP |

### 12.3 Puntos de Extension Definidos

Cada modulo expone interfaces que permiten:

- **core-network**: Nuevos transportes (WebRTC, QUIC) sin cambiar capas superiores
- **security-crypto**: Nuevos algoritmos (post-quantum) intercambiando implementaciones
- **sync-protocol**: Nuevos formatos de sincronizacion versionados
- **animation-engine**: Nuevos efectos visuales como plugins
- **data-file-system**: Nuevas fuentes de almacenamiento (cloud, NAS)

---

## 13. Glosario

| Termino | Definicion |
|---|---|
| **BLE** | Bluetooth Low Energy |
| **Wi-Fi Direct** | Wi-Fi P2P, conexion directa sin router |
| **Nearby Connections** | API de Google que abstrae BLE + WiFi |
| **ECDH** | Elliptic Curve Diffie-Hellman |
| **HKDF** | HMAC-based Key Derivation Function |
| **AES-256-GCM** | Cifrado simetrico con autenticacion |
| **HMAC** | Hash-based Message Authentication Code |
| **FBO** | Frame Buffer Object, renderizado off-screen |
| **VBO** | Vertex Buffer Object, datos de vertices en GPU |
| **MVI** | Model-View-Intent, patron de UI con estado inmutable |
| **SAF** | Storage Access Framework, acceso a archivos |
| **RTT** | Round-Trip Time, latencia de red |
| **GATT** | Generic Attribute Profile, protocolo BLE |
| **SSOT** | Single Source of Truth |
| **MITM** | Man-In-The-Middle |
| **PFS** | Perfect Forward Secrecy |
