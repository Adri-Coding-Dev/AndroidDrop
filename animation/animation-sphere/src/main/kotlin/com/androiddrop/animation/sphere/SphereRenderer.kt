package com.androiddrop.animation.sphere

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.opengles.GL10
import timber.log.Timber

/**
 * Renderizador de la esfera energética con OpenGL ES 3.0.
 *
 * POR QUÉ OpenGL ES 3.0 vs Canvas/Vulkan: La esfera energética requiere
 * efectos visuales complejos (noise 3D, cáusticas internas, efecto Fresnel,
 * blending aditivo) que solo un pipeline shader-based puede proporcionar en
 * tiempo real. OpenGL ES 3.0 es el estándar más ampliamente soportado en
 * Android (desde API 18+) que soporta shaders programables.
 *
 * POR QUÉ malla geodésica vs UV-sphere: Una UV-sphere tiene más densidad
 * de vértices en los polos que en el ecuador, desperdiciando triángulos.
 * La malla geodésica (~2048 triángulos) distribuye uniformemente los
 * triángulos sobre la superficie, dando una calidad visual consistente
 * desde cualquier ángulo.
 *
 * La esfera tiene 6 estados de animación:
 *   - IDLE:    Flotando estable, respiración senoidal con ciclo de 2s.
 *   - FOUND:   Escala aumenta a 1.15, brillo +30%, pulso de detección.
 *   - HELD:    Sigue la posición del dedo/usuario (drag).
 *   - LAUNCHED: Deceleración progresiva hasta detenerse.
 *   - ENTERING: Escala -> 0 con deformación radial, desvanece.
 *   - DECAYING: Partículas escapan de la superficie, brillo decrece.
 */
class SphereRenderer : GLSurfaceView.Renderer {

    /** Número de subdivisiones para la malla geodésica. */
    private companion object {
        const val SUBDIVISIONS = 4
        const val FLOAT_SIZE = 4
        const val SHORT_SIZE = 2
        const val POSITION_SIZE = 3
        const val NORMAL_SIZE = 3
        const val TEXCOORD_SIZE = 2
        const val STRIDE = (POSITION_SIZE + NORMAL_SIZE + TEXCOORD_SIZE) * FLOAT_SIZE
    }

    /** Estado actual de renderizado de la esfera. */
    private var state: SphereRenderState = SphereRenderState()

    /** Buffers de la malla geodésica. */
    private var vertexBuffer: FloatBuffer? = null
    private var indexBuffer: ShortBuffer? = null
    private var indexCount: Int = 0

    /** IDs de los shaders de OpenGL. */
    private var programId: Int = 0

    /** Ubicaciones de uniforms. */
    private var uMatrixLocation: Int = 0
    private var uEnergyLocation: Int = 0
    private var uScaleLocation: Int = 0
    private var uPulsePhaseLocation: Int = 0
    private var uBrightnessLocation: Int = 0
    private var uTimeLocation: Int = 0

    /**
     * Actualiza el estado de renderizado desde el exterior.
     * Se llama desde el hilo del GLSurfaceView.
     */
    fun updateState(newState: SphereRenderState) {
        state = newState
    }

    override fun onSurfaceCreated(gl: GL10, config: javax.microedition.khronos.egl.EGLConfig) {
        GLES30.glClearColor(0f, 0f, 0f, 0f)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)

        buildGeodesicSphere()
        compileShaders()

        Timber.d("SphereRenderer: superficie creada, malla = ${indexCount} triángulos")
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        if (programId == 0) return

        GLES30.glUseProgram(programId)

        // Configurar uniforms desde el estado actual
        GLES30.glUniform1f(uEnergyLocation, state.energy)
        GLES30.glUniform1f(uScaleLocation, state.scale)
        GLES30.glUniform1f(uPulsePhaseLocation, state.pulsePhase)
        GLES30.glUniform1f(uBrightnessLocation, state.brightness)
        GLES30.glUniform1f(uTimeLocation, System.nanoTime() / 1_000_000_000f)

        // Bindear buffers de vértices
        vertexBuffer?.let { buffer ->
            buffer.position(0)
            GLES30.glVertexAttribPointer(0, POSITION_SIZE, GLES30.GL_FLOAT, false, STRIDE, buffer)
            GLES30.glEnableVertexAttribArray(0)

            buffer.position(POSITION_SIZE)
            GLES30.glVertexAttribPointer(1, NORMAL_SIZE, GLES30.GL_FLOAT, false, STRIDE, buffer)
            GLES30.glEnableVertexAttribArray(1)

            buffer.position(POSITION_SIZE + NORMAL_SIZE)
            GLES30.glVertexAttribPointer(2, TEXCOORD_SIZE, GLES30.GL_FLOAT, false, STRIDE, buffer)
            GLES30.glEnableVertexAttribArray(2)
        }

        // Dibujar la malla
        indexBuffer?.let {
            it.position(0)
            GLES30.glDrawElements(GLES30.GL_TRIANGLES, indexCount, GLES30.GL_UNSIGNED_SHORT, it)
        }

        // Deshabilitar atributos
        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)
        GLES30.glDisableVertexAttribArray(2)
    }

    /**
     * Construye una malla geodésica mediante subdivisión de un icosaedro.
     *
     * El icosaedro base tiene 20 triángulos y 12 vértices. Con 4 subdivisiones
     * (SUBDIVISIONS = 4), obtenemos ~20 * 4^4 = 5120 triángulos, de los cuales
     * nos quedamos con ~2048 para la esfera energética.
     */
    private fun buildGeodesicSphere() {
        val vertices = mutableListOf<Float>()
        val indices = mutableListOf<Short>()

        // Icosaedro base: 12 vértices
        val t = (1f + kotlin.math.sqrt(5f)) / 2f
        val baseVerts = listOf(
            -1f, t, 0f, 1f, t, 0f, -1f, -t, 0f, 1f, -t, 0f,
            0f, -1f, t, 0f, 1f, t, 0f, -1f, -t, 0f, 1f, -t,
            t, 0f, -1f, t, 0f, 1f, -t, 0f, -1f, -t, 0f, 1f
        )

        val baseIndices = listOf(
            0, 11, 5, 0, 5, 1, 0, 1, 7, 0, 7, 10, 0, 10, 11,
            1, 5, 9, 5, 11, 4, 11, 10, 2, 10, 7, 6, 7, 1, 8,
            3, 9, 4, 3, 4, 2, 3, 2, 6, 3, 6, 8, 3, 8, 9,
            4, 9, 5, 2, 4, 11, 6, 2, 10, 8, 6, 7, 9, 8, 1
        )

        // Normalizar vértices a la esfera unitaria
        val normalized = baseVerts.toMutableList()
        for (i in normalized.indices step 3) {
            val x = normalized[i]
            val y = normalized[i + 1]
            val z = normalized[i + 2]
            val len = kotlin.math.sqrt(x * x + y * y + z * z)
            normalized[i] = x / len
            normalized[i + 1] = y / len
            normalized[i + 2] = z / len
        }

        // Subdividir cada triángulo
        val midCache = mutableMapOf<Long, Short>()
        var nextIndex: Short = 12

        fun getMidPoint(i1: Short, i2: Short, verts: MutableList<Float>): Short {
            val key = if (i1 < i2) (i1.toLong() shl 16) or i2.toLong()
                      else (i2.toLong() shl 16) or i1.toLong()
            return midCache.getOrPut(key) {
                val idx = nextIndex++
                val i1i = i1.toInt() * 3
                val i2i = i2.toInt() * 3
                val mx = (verts[i1i] + verts[i2i]) / 2f
                val my = (verts[i1i + 1] + verts[i2i + 1]) / 2f
                val mz = (verts[i1i + 2] + verts[i2i + 2]) / 2f
                val len = kotlin.math.sqrt(mx * mx + my * my + mz * mz)
                verts.add(mx / len)
                verts.add(my / len)
                verts.add(mz / len)
                idx
            }
        }

        // Cola de triángulos a subdividir
        data class Tri(val a: Short, val b: Short, val c: Short)
        val queue = ArrayDeque<Tri>()

        for (i in baseIndices.indices step 3) {
            queue.add(
                Tri(
                    baseIndices[i].toShort(),
                    baseIndices[i + 1].toShort(),
                    baseIndices[i + 2].toShort()
                )
            )
        }

        var currentSub = 0
        while (queue.isNotEmpty()) {
            val size = queue.size
            repeat(size) {
                val tri = queue.removeFirst()
                if (currentSub >= SUBDIVISIONS) {
                    // Nivel máximo: agregar triángulo a la malla final
                    indices.add(tri.a)
                    indices.add(tri.b)
                    indices.add(tri.c)
                } else {
                    val ab = getMidPoint(tri.a, tri.b, normalized)
                    val bc = getMidPoint(tri.b, tri.c, normalized)
                    val ca = getMidPoint(tri.c, tri.a, normalized)
                    queue.add(Tri(tri.a, ab, ca))
                    queue.add(Tri(tri.b, bc, ab))
                    queue.add(Tri(tri.c, ca, bc))
                    queue.add(Tri(ab, bc, ca))
                }
            }
            currentSub++
        }

        indexCount = indices.size

        // Construir buffer de vértices con posiciones, normales y UVs
        val vertexData = FloatArray(normalized.size / 3 * (POSITION_SIZE + NORMAL_SIZE + TEXCOORD_SIZE))
        for (i in 0 until normalized.size / 3) {
            val x = normalized[i * 3]
            val y = normalized[i * 3 + 1]
            val z = normalized[i * 3 + 2]

            // Posición
            vertexData[i * (POSITION_SIZE + NORMAL_SIZE + TEXCOORD_SIZE)] = x
            vertexData[i * (POSITION_SIZE + NORMAL_SIZE + TEXCOORD_SIZE) + 1] = y
            vertexData[i * (POSITION_SIZE + NORMAL_SIZE + TEXCOORD_SIZE) + 2] = z

            // Normal (en una esfera unitaria, normal = posición)
            vertexData[i * (POSITION_SIZE + NORMAL_SIZE + TEXCOORD_SIZE) + 3] = x
            vertexData[i * (POSITION_SIZE + NORMAL_SIZE + TEXCOORD_SIZE) + 4] = y
            vertexData[i * (POSITION_SIZE + NORMAL_SIZE + TEXCOORD_SIZE) + 5] = z

            // Coordenada UV (proyección esférica)
            val u = 0.5f + kotlin.math.atan2(z, x) / (2f * Math.PI.toFloat())
            val v = 0.5f - kotlin.math.asin(y) / Math.PI.toFloat()
            vertexData[i * (POSITION_SIZE + NORMAL_SIZE + TEXCOORD_SIZE) + 6] = u
            vertexData[i * (POSITION_SIZE + NORMAL_SIZE + TEXCOORD_SIZE) + 7] = v
        }

        vertexBuffer = ByteBuffer
            .allocateDirect(vertexData.size * FLOAT_SIZE)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexData) as FloatBuffer
        vertexBuffer?.position(0)

        val indexData = ShortArray(indices.size) { indices[it] }
        indexBuffer = ByteBuffer
            .allocateDirect(indexData.size * SHORT_SIZE)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .put(indexData) as ShortBuffer
        indexBuffer?.position(0)

        Timber.d("SphereRenderer: malla generada con ${normalized.size / 3} vértices, $indexCount índices")
    }

    /**
     * Compila los shaders de la esfera desde [SphereShader].
     */
    private fun compileShaders() {
        val vertexShader = compileShader(GLES30.GL_VERTEX_SHADER, SphereShader.vertexShader)
        val fragmentShader = compileShader(GLES30.GL_FRAGMENT_SHADER, SphereShader.fragmentShader)

        if (vertexShader == 0 || fragmentShader == 0) return

        programId = GLES30.glCreateProgram()
        GLES30.glAttachShader(programId, vertexShader)
        GLES30.glAttachShader(programId, fragmentShader)
        GLES30.glLinkProgram(programId)

        val linkStatus = IntArray(1)
        GLES30.glGetProgramiv(programId, GLES30.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            val log = GLES30.glGetProgramInfoLog(programId)
            Timber.e("SphereRenderer: error de linkeo: $log")
            GLES30.glDeleteProgram(programId)
            programId = 0
            return
        }

        // Obtener ubicaciones de uniforms
        uMatrixLocation = GLES30.glGetUniformLocation(programId, "uMatrix")
        uEnergyLocation = GLES30.glGetUniformLocation(programId, "uEnergy")
        uScaleLocation = GLES30.glGetUniformLocation(programId, "uScale")
        uPulsePhaseLocation = GLES30.glGetUniformLocation(programId, "uPulsePhase")
        uBrightnessLocation = GLES30.glGetUniformLocation(programId, "uBrightness")
        uTimeLocation = GLES30.glGetUniformLocation(programId, "uTime")

        GLES30.glDeleteShader(vertexShader)
        GLES30.glDeleteShader(fragmentShader)

        Timber.d("SphereRenderer: shaders compilados y linkeados")
    }

    /**
     * Compila un shader individual y retorna su ID.
     */
    private fun compileShader(type: Int, source: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, source)
        GLES30.glCompileShader(shader)

        val compileStatus = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            val log = GLES30.glGetShaderInfoLog(shader)
            val typeName = if (type == GLES30.GL_VERTEX_SHADER) "vertex" else "fragment"
            Timber.e("SphereRenderer: error de compilación ($typeName): $log")
            GLES30.glDeleteShader(shader)
            return 0
        }

        return shader
    }
}
