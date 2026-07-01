## 10. Rendimiento y Accesibilidad

### 10.1 Estrategias de Optimizacion

| Area | Estrategia |
|---|---|
| Lectura de archivos | FileChannel + direct ByteBuffer + memory-mapped para archivos grandes |
| Escritura de archivos | AsynchronousFileChannel para no bloquear |
| Red | Chunks de 1MB, 4 sockets paralelos para Wi-Fi Direct |
| Cifrado | AES-NI, pre-generacion de IVs |
| Animaciones | OpenGL ES 3.0, VBOs estaticos, shaders optimizados |
| Particulas | Compute shaders (GLES 3.1+) o CPU con pooling |
| UI Compose | derivedStateOf, remember, lazy lists, stable types |
| BLE | Batch scans, intervalos adaptativos segun cercania |
| Memoria | LRU cache para thumbnails, pool de buffers directos |
| Battery | JobScheduler + WorkManager para operaciones largas |

### 10.2 Thresholds de Rendimiento

| Operacion | Limite Aceptable | Target |
|---|---|---|
| App cold start | < 2s | < 1s |
| File explorer (1000 items) | < 500ms | < 200ms |
| Sphere animation | 60 FPS | 120 FPS |
| Encryption throughput | 100 MB/s | 500 MB/s (AES-NI) |
| Transfer (100MB file) | < 30s | < 15s |
| Battery impact (1GB transfer) | < 5% | < 2% |

### 10.3 Manejo de Archivos Grandes

```kotlin
// Pipeline con backpressure
fun transferFlow(file: File): Flow<TransferProgress> = flow {
    val channel = FileChannel.open(file.toPath(), StandardOpenOption.READ)
    val buffer = ByteBuffer.allocateDirect(CHUNK_SIZE)
    var bytesRead: Long; var totalRead = 0L
    val fileSize = file.length()

    while (channel.read(buffer).also { bytesRead = it.toLong() } > 0) {
        buffer.flip()
        val chunk = ByteArray(buffer.remaining())
        buffer.get(chunk)
        emit(TransferProgress.Chunk(
            data = encryptChunk(chunk, sessionKey),
            offset = totalRead, size = chunk.size
        ))
        totalRead += bytesRead
        emit(TransferProgress.Progress(totalRead.toFloat() / fileSize))
        buffer.clear()
    }
}.flowOn(Dispatchers.IO)
```

### 10.4 Accesibilidad

**Principios (WCAG 2.1 AA):**
- Contraste de color minimo 4.5:1 para texto normal
- Soporte completo para TalkBack
- Todas las animaciones desactivables (reduced motion)
- Tamano de texto dinamico (dp)
- Descripciones de contenido en todos los elementos interactivos

```kotlin
@Composable
fun TransferSphereButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .semantics {
                contentDescription = "Iniciar transferencia de archivo"
                role = Role.Button
                stateDescription = when {
                    isTransferring -> "Transferencia en progreso"
                    isComplete -> "Transferencia completada"
                    else -> "Archivo listo para enviar"
                }
            }
            .clickable(onClick = onClick)
    ) {
        SphereAnimation(
            state = sphereState,
            reducedMotion = LocalReducedMotion.current
        )
    }
}
```
