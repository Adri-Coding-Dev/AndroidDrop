## 5. Decisiones Tecnicas

### 5.1 Stack Tecnologico

| Componente | Tecnologia | Justificacion |
|---|---|---|
| Lenguaje | Kotlin 2.x + KSP | Estandar Android moderno. KSP sobre KAPT por rendimiento |
| UI | Jetpack Compose + Material3 | Declarativo, moderno, first-party, interoperable con View |
| Animaciones | Canvas + OpenGL ES 3.0 | Canvas para particulas 2D, GLES para esfera 3D |
| DI | Hilt | Google oficial, compile-time, integracion nativa Compose |
| Async | Coroutines + Flow | First-class en Kotlin, integracion con Compose |
| Serializacion | Kotlinx Serialization | Multiplataforma, mas rapido que Gson/Moshi, KSP |
| Almacenamiento | Room (metadata) + SAF (archivos) | Room para cache de indices, SAF para acceso seguro |
| Cifrado | Conscrypt + Bouncy Castle | Conscrypt (Google), BC como fallback |
| Red | OkHttp (sockets) + Ktor (opcional) | OkHttp probado en produccion |
| BLE | Android BLE API | API nativa, madura, sin librerias externas |
| Wi-Fi Direct | WifiP2pManager | API nativa |
| Nearby | Nearby Connections API | Wrapper util como fallback |
| Logging | Timber | Estandar, configurable, tree-based |
| Tests | JUnit 5 + MockK + Turbine | MockK sobre Mockito por soporte Kotlin nativo |

### 5.2 Minimum SDK

**minSdk = 29** (Android 10)

Justificacion:
- BLE Advertising Extensions (mejor rango)
- Wi-Fi Direct maduro
- Seguridad (Scoped Storage, mejor cifrado)
- Penetracion de mercado: >92% de dispositivos activos
- Evita mantener backward compatibility para APIs obsoletas

### 5.3 Target SDK

**targetSdk = 35** (Android 15)

### 5.4 Build System

- Gradle Kotlin DSL (build.gradle.kts)
- Version Catalog (libs.versions.toml) para dependencias centralizadas
- Convention plugins en `build-logic` para evitar duplicacion entre modulos
- KSP sobre KAPT por rendimiento en compilacion

### 5.5 Testing Strategy

| Nivel | Framework | Cobertura |
|---|---|---|
| Unit | JUnit 5 + MockK | Domain, Core |
| Integration | Turbine (Flow testing) | ViewModel + UseCase |
| UI | Compose UI Test | Pantallas principales |
| E2E | Custom (mock network) | Flujo completo transferencia |
| Performance | Macrobenchmark | Animaciones 60/120fps |
