## 9. Seguridad

### 9.1 Cifrado Extremo a Extremo

```
Emisor                                      Receptor
  |                                            |
  |-- ECDH Key Pair (Curve25519)              |
  |-- Intercambio de claves publicas via BLE  |
  |                                            |
  |-- Shared Secret (ECDH) ------------------>|
  |                                            |
  |-- HKDF-SHA256                             |
  |   encKey (32 bytes AES-256)               |
  |   macKey (32 bytes HMAC-SHA256)           |
  |   ivSeed (16 bytes)                       |
  |                                            |
  |-- Cada chunk: IV = HMAC(ivSeed, chunkN)  |
  |   truncado a 12 bytes                     |
```

### 9.2 Intercambio de Claves

1. Cada sesion genera par ECDH efimero (Curve25519/X25519)
2. Intercambio de claves publicas durante handshake BLE
3. Derivacion de secreto compartido via `HKDF-SHA256`
4. Derivacion de:
   - `encKey`: AES-256-GCM key (32 bytes)
   - `macKey`: HMAC-SHA256 key (32 bytes)
   - `ivSeed`: Base para IVs (16 bytes)
5. Cada chunk usa IV = HMAC(ivSeed, chunkNumber) truncado a 12 bytes

### 9.3 Autenticacion Mutua

```
Emisor -> Receptor:  Nonce_A (16 bytes aleatorios)
Receptor -> Emisor:  AES-GCM(Nonce_A + 1, encKey)
                     + HMAC(Nonce_A + 1, macKey)
Emisor -> Receptor:  AES-GCM(Nonce_B + 1, encKey)
                     + HMAC(Nonce_B + 1, macKey)
```

### 9.4 Proteccion contra Replay

- Cada sesion tiene sessionId unico (UUIDv4)
- Cada paquete tiene sequenceNumber incremental
- Cada paquete lleva timestamp con tolerancia +/-500ms
- Nonce de 16 bytes en handshake
- IV unico por chunk

### 9.5 Verificacion de Integridad

- Checksum SHA-256 del archivo completo antes de transferir
- Checksum SHA-256 de cada chunk (para reanudacion)
- MAC de cada paquete del protocolo
- Al finalizar: receptor calcula SHA-256 del archivo recibido y lo compara

### 9.6 Principios de Seguridad

- Zero-trust: no asumir nada del otro dispositivo
- Perfect Forward Secrecy: claves efimeras por sesion
- Minimizacion de permisos: solo los estrictamente necesarios
- Sin datos a servidores externos: 100% local
- Rechazo de dispositivos no autenticados
- Timeout de sesion: 30 minutos maximo
