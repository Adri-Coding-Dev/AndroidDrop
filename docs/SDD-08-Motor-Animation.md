## 8. Motor de Animaciones

### 8.1 Arquitectura del Motor

```
+---------------------------------------------------+
|                   AnimationEngine                   |
|                                                    |
|  +--------------+  +----------------------------+  |
|  |  SphereNode   |  |  ParticleSystem            |  |
|  |  - mesh       |  |  - emitters                |  |
|  |  - shader     |  |  - physics                 |  |
|  |  - transform  |  |  - lifetimes               |  |
|  +------+-------+  +----------+-----------------+  |
|         |                      |                    |
|  +------+----------------------+-----------------+  |
|  |              RenderPipeline                     |  |
|  |  - GLSurfaceView / Canvas                      |  |
|  |  - Hardware acceleration                       |  |
|  |  - Frame rate controller (60/120 FPS)          |  |
|  +------------------------------------------------+  |
|                                                    |
|  +------------------------------------------------+  |
|  |           GesturePhysicsEngine                  |  |
|  |  - Spring physics (mass, stiffness, damping)    |  |
|  |  - Inertia                                      |  |
|  |  - Boundary detection                           |  |
|  +------------------------------------------------+  |
+---------------------------------------------------+
```

### 8.2 Esfera Energetica

La esfera es el elemento central. Se renderiza en **OpenGL ES 3.0**.

**Caracteristicas:**
1. **Malla geodesica** ~2048 triangulos
2. **Shader personalizado** con:
   - Noise 3D (Simplex/Perlin) para animacion organica
   - Causticas internas simuladas
   - Fresnel effect para bordes brillantes
   - Multiples capas de color con blending aditivo
   - Pulse animation en sincronia con latido
3. **Particulas orbitales**: ~500 particulas orbitando
4. **Energy trails**: estelas al moverse
5. **Rayo interno**: hilos de luz rotando dentro

**Estados:**

| Estado | Descripcion | Transformacion |
|---|---|---|
| Idle | Flotando, respiracion | Escala 1.0 <-> 1.05 senoidal, 2s ciclo |
| Found | Dispositivo detectado | Escala -> 1.15, brillo +30%, pulso |
| Held | Agarrada por usuario | Sigue dedo, fisica spring |
| Launched | Lanzada hacia portal | Velocidad inicial, deceleracion |
| Entering | Entrando al portal | Escala -> 0, deformacion |
| Decaying | Perdiendo energia | Particulas escapan, escala y brillo decrecen |
| Exploding | Explosion final | Fragmentacion controlada |

### 8.3 Portal Energetico

El portal es una **grieta espacial** que conecta ambos dispositivos.

**Caracteristicas:**
1. Anillo exterior giratorio (textura procedural)
2. Vortice interior con profundidad
3. Particulas emergentes
4. Efecto de lente gravitacional (distorsion de fondo)
5. Iluminacion dinamica del borde de pantalla

**Renderizacion:** OpenGL ES 3.0 con FBO para post-processing.

### 8.4 Sistema de Particulas

```kotlin
data class Particle(
    var position: Vector3,
    var velocity: Vector3,
    var life: Float, val maxLife: Float,
    var size: Float, var color: Color,
    var alpha: Float, var rotation: Float,
    var angularVelocity: Float
)

class ParticleSystem(
    maxParticles: Int = 2000,
    emitterType: EmitterType = EmitterType.RADIAL
) {
    fun update(deltaTime: Float) {
        // Update physics (gravity, drag, turbulence)
        // Culling (off-screen particles)
        // Sorting (back-to-front for alpha blending)
    }
    fun render(gl: GL10) {
        // Rendering (point sprites or textured quads)
    }
}
```

### 8.5 Gesture Physics Engine

```kotlin
class GesturePhysicsEngine {
    // Spring physics for elastic feel
    data class SpringParams(
        val mass: Float = 1f,
        val stiffness: Float = 180f,  // Higher = more rigid
        val damping: Float = 15f       // Higher = less bounce
    )

    // Handles: drag, fling, throw, snap-to-edge
    // Boundary detection for portal placement
    // Velocity calculation for throw direction
    // Inertia simulation for smooth release
}
```

### 8.6 Sincronizacion de Animaciones

Frecuencia de sincronizacion: **20-30 Hz**

Mecanismos:
1. **Clock maestro**: El emisor define el timeline
2. **Timestamp absoluto**: Cada evento lleva timestamp del clock maestro
3. **Latency compensation**: El receptor ajusta su timeline basado en RTT
4. **Interpolacion**: Si un paquete se pierde, se interpola hasta recibir el siguiente
5. **State machine compartida**: Ambos dispositivos tienen la misma maquina de estados
