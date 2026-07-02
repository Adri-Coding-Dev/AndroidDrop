package com.androiddrop.animation.portal

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.opengles.GL10
import timber.log.Timber

/**
 * Renderizador del portal energético (grieta espacial) con OpenGL ES 3.0.
 *
 * POR QUÉ FBO para post-processing: El portal requiere efectos de bloom
 * (glow del borde) y distorsión de lente gravitacional que no pueden
 * lograrse en un solo pass. Usar un Framebuffer Object (FBO) permite
 * renderizar la escena a una textura intermedia y luego aplicar los
 * efectos de post-processing en un segundo pass.
 *
 * Componentes visuales del portal:
 *   - Anillo exterior giratorio: Textura procedural con patrón energético
 *     que rota continuamente a velocidad variable según la fase.
 *   - Vórtice interior: Remolino con profundidad que da la sensación
 *     de agujero espacial. Más profundo en fase ACTIVE.
 *   - Partículas emergentes: Pequeños puntos de luz que escapan del
 *     vórtice, simulando energía que fluye a través del portal.
 *   - Lente gravitacional: Distorsión del espacio alrededor del portal,
 *     simulando el efecto de un objeto masivo curvando la luz.
 *   - Iluminación dinámica del borde: Pulsos de luz que recorren el
 *     perímetro del anillo.
 */
class PortalRenderer : GLSurfaceView.Renderer {

    private companion object {
        const val FLOAT_SIZE = 4
        const val SHORT_SIZE = 2
        const val SEGMENTS = 64
        const val RING_SEGMENTS = 128
    }

    /** Estado actual del portal. */
    private var state: PortalRenderState = PortalRenderState()

    /** IDs de los shaders. */
    private var portalProgram: Int = 0
    private var postProcessProgram: Int = 0

    /** FBO para post-processing. */
    private var fboId: Int = 0
    private var fboTexture: Int = 0
    private var fboWidth: Int = 0
    private var fboHeight: Int = 0

    /** Buffers de geometría. */
    private var ringVertexBuffer: FloatBuffer? = null
    private var ringIndexBuffer: ShortBuffer? = null
    private var ringIndexCount: Int = 0

    private var vortexVertexBuffer: FloatBuffer? = null
    private var vortexIndexBuffer: ShortBuffer? = null
    private var vortexIndexCount: Int = 0

    /** Cuadrícula fullscreen para post-processing. */
    private var fullScreenQuadBuffer: FloatBuffer? = null

    /** Ubicaciones de uniforms del shader de portal. */
    private var portalURingRotation: Int = 0
    private var portalUVortexDepth: Int = 0
    private var portalUIntensity: Int = 0
    private var portalUGlowIntensity: Int = 0
    private var portalUDistortion: Int = 0
    private var portalUTime: Int = 0

    /** Ubicaciones de uniforms del shader de post-processing. */
    private var postUTexture: Int = 0
    private var postUDistortion: Int = 0
    private var postUGlow: Int = 0

    /**
     * Actualiza el estado del portal para el próximo frame.
     */
    fun updateState(newState: PortalRenderState) {
        state = newState
    }

    override fun onSurfaceCreated(gl: GL10, config: javax.microedition.khronos.egl.EGLConfig) {
        GLES30.glClearColor(0f, 0f, 0f, 0f)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)

        buildRingGeometry()
        buildVortexGeometry()
        buildFullScreenQuad()
        compileShaders()

        Timber.d("PortalRenderer: superficie creada")
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        fboWidth = width
        fboHeight = height
        GLES30.glViewport(0, 0, width, height)

        // Crear FBO para post-processing
        setupFBO(width, height)
    }

    override fun onDrawFrame(gl: GL10) {
        // Renderizar portal al FBO
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fboId)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        renderPortalScene()

        // Renderizar post-processing a pantalla
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        GLES30.glDisable(GLES30.GL_DEPTH_TEST)

        renderPostProcessing()
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
    }

    /**
     * Renderiza la escena del portal (anillo + vórtice).
     */
    private fun renderPortalScene() {
        if (portalProgram == 0) return

        GLES30.glUseProgram(portalProgram)

        val time = System.nanoTime() / 1_000_000_000f
        GLES30.glUniform1f(portalUTime, time)
        GLES30.glUniform1f(portalURingRotation, state.ringRotation)
        GLES30.glUniform1f(portalUVortexDepth, state.vortexDepth)
        GLES30.glUniform1f(portalUIntensity, state.intensity)
        GLES30.glUniform1f(portalUGlowIntensity, state.glowIntensity)
        GLES30.glUniform1f(portalUDistortion, state.distortionAmount)

        // Renderizar anillo exterior
        renderRing()

        // Renderizar vórtice interior
        renderVortex()
    }

    /**
     * Renderiza el anillo exterior giratorio.
     */
    private fun renderRing() {
        ringVertexBuffer?.let { buffer ->
            buffer.position(0)
            GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 5 * FLOAT_SIZE, buffer)
            GLES30.glEnableVertexAttribArray(0)

            buffer.position(3)
            GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 5 * FLOAT_SIZE, buffer)
            GLES30.glEnableVertexAttribArray(1)
        }

        ringIndexBuffer?.let {
            it.position(0)
            GLES30.glDrawElements(GLES30.GL_TRIANGLES, ringIndexCount, GLES30.GL_UNSIGNED_SHORT, it)
        }

        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)
    }

    /**
     * Renderiza el vórtice interior con profundidad.
     */
    private fun renderVortex() {
        vortexVertexBuffer?.let { buffer ->
            buffer.position(0)
            GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 5 * FLOAT_SIZE, buffer)
            GLES30.glEnableVertexAttribArray(0)

            buffer.position(3)
            GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 5 * FLOAT_SIZE, buffer)
            GLES30.glEnableVertexAttribArray(1)
        }

        vortexIndexBuffer?.let {
            it.position(0)
            GLES30.glDrawElements(GLES30.GL_TRIANGLES, vortexIndexCount, GLES30.GL_UNSIGNED_SHORT, it)
        }

        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)
    }

    /**
     * Renderiza el post-processing (bloom + distorsión).
     */
    private fun renderPostProcessing() {
        if (postProcessProgram == 0) return

        GLES30.glUseProgram(postProcessProgram)

        // Bindear textura del FBO
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, fboTexture)
        GLES30.glUniform1i(postUTexture, 0)
        GLES30.glUniform1f(postUDistortion, state.distortionAmount)
        GLES30.glUniform1f(postUGlow, state.glowIntensity)

        fullScreenQuadBuffer?.let { buffer ->
            buffer.position(0)
            GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 4 * FLOAT_SIZE, buffer)
            GLES30.glEnableVertexAttribArray(0)

            buffer.position(2)
            GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 4 * FLOAT_SIZE, buffer)
            GLES30.glEnableVertexAttribArray(1)
        }

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)

        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)
    }

    /**
     * Construye la geometría del anillo exterior.
     */
    private fun buildRingGeometry() {
        val vertices = mutableListOf<Float>()
        val indices = mutableListOf<Short>()

        val innerRadius = 0.8f
        val outerRadius = 1.0f

        for (i in 0..RING_SEGMENTS) {
            val angle = i.toFloat() / RING_SEGMENTS * 2f * Math.PI.toFloat()
            val cosA = kotlin.math.cos(angle)
            val sinA = kotlin.math.sin(angle)

            // Vértice interior
            vertices.addAll(listOf(
                innerRadius * cosA, innerRadius * sinA, 0f,
                i.toFloat() / RING_SEGMENTS, 0f
            ))

            // Vértice exterior
            vertices.addAll(listOf(
                outerRadius * cosA, outerRadius * sinA, 0f,
                i.toFloat() / RING_SEGMENTS, 1f
            ))

            if (i < RING_SEGMENTS) {
                val base = (i * 2).toShort()
                indices.addAll(listOf(
                    base, (base + 1).toShort(), (base + 2).toShort(),
                    (base + 1).toShort(), (base + 3).toShort(), (base + 2).toShort()
                ))
            }
        }

        ringIndexCount = indices.size

        ringVertexBuffer = ByteBuffer
            .allocateDirect(vertices.size * FLOAT_SIZE)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertices.toFloatArray()) as FloatBuffer
        ringVertexBuffer?.position(0)

        ringIndexBuffer = ByteBuffer
            .allocateDirect(indices.size * SHORT_SIZE)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .put(indices.toShortArray()) as ShortBuffer
        ringIndexBuffer?.position(0)
    }

    /**
     * Construye la geometría del vórtice interior (espiral 3D).
     */
    private fun buildVortexGeometry() {
        val vertices = mutableListOf<Float>()
        val indices = mutableListOf<Short>()

        val spiralTurns = 4
        val totalPoints = SEGMENTS * spiralTurns

        for (i in 0..totalPoints) {
            val t = i.toFloat() / totalPoints
            val angle = t * spiralTurns * 2f * Math.PI.toFloat()
            val radius = (1f - t) * 0.7f
            val depth = t * 0.5f

            vertices.addAll(listOf(
                radius * kotlin.math.cos(angle),
                radius * kotlin.math.sin(angle),
                -depth,
                t, 0f
            ))

            if (i < totalPoints) {
                indices.add(i.toShort())
            }
        }

        // Convertir a triángulos (GL_TRIANGLE_STRIP)
        vortexIndexCount = indices.size
        val indicesArray = indices.toShortArray()
        // Crear strip: conectar cada punto al centro
        val stripIndices = mutableListOf<Short>()
        for (i in 0 until totalPoints) {
            stripIndices.add(indicesArray[i])
            stripIndices.add((i + 1).toShort())
        }
        vortexIndexCount = stripIndices.size

        vortexVertexBuffer = ByteBuffer
            .allocateDirect(vertices.size * FLOAT_SIZE)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertices.toFloatArray()) as FloatBuffer
        vortexVertexBuffer?.position(0)

        vortexIndexBuffer = ByteBuffer
            .allocateDirect(stripIndices.size * SHORT_SIZE)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .put(stripIndices.toShortArray()) as ShortBuffer
        vortexIndexBuffer?.position(0)
    }

    /**
     * Construye un quad fullscreen para post-processing.
     */
    private fun buildFullScreenQuad() {
        val quad = floatArrayOf(
            -1f, -1f, 0f, 0f,
             1f, -1f, 1f, 0f,
            -1f,  1f, 0f, 1f,
             1f,  1f, 1f, 1f
        )

        fullScreenQuadBuffer = ByteBuffer
            .allocateDirect(quad.size * FLOAT_SIZE)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(quad) as FloatBuffer
        fullScreenQuadBuffer?.position(0)
    }

    /**
     * Configura el FBO para renderizado fuera de pantalla.
     */
    private fun setupFBO(width: Int, height: Int) {
        // Generar FBO
        val fboIds = IntArray(1)
        GLES30.glGenFramebuffers(1, fboIds, 0)
        fboId = fboIds[0]

        // Generar textura del FBO
        val textures = IntArray(1)
        GLES30.glGenTextures(1, textures, 0)
        fboTexture = textures[0]

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, fboTexture)
        GLES30.glTexImage2D(
            GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA,
            width, height, 0,
            GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null
        )
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)

        // Adjuntar textura al FBO
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fboId)
        GLES30.glFramebufferTexture2D(
            GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0,
            GLES30.GL_TEXTURE_2D, fboTexture, 0
        )

        val status = GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER)
        if (status != GLES30.GL_FRAMEBUFFER_COMPLETE) {
            Timber.e("PortalRenderer: FBO incompleto, status = $status")
        }

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
    }

    /**
     * Compila los shaders del portal y de post-processing.
     */
    private fun compileShaders() {
        portalProgram = createProgram(PortalShader.vertexShader, PortalShader.fragmentShader)
        postProcessProgram = createProgram(PortalShader.postVertexShader, PortalShader.postFragmentShader)

        if (portalProgram != 0) {
            portalURingRotation = GLES30.glGetUniformLocation(portalProgram, "uRingRotation")
            portalUVortexDepth = GLES30.glGetUniformLocation(portalProgram, "uVortexDepth")
            portalUIntensity = GLES30.glGetUniformLocation(portalProgram, "uIntensity")
            portalUGlowIntensity = GLES30.glGetUniformLocation(portalProgram, "uGlowIntensity")
            portalUDistortion = GLES30.glGetUniformLocation(portalProgram, "uDistortion")
            portalUTime = GLES30.glGetUniformLocation(portalProgram, "uTime")
        }

        if (postProcessProgram != 0) {
            postUTexture = GLES30.glGetUniformLocation(postProcessProgram, "uTexture")
            postUDistortion = GLES30.glGetUniformLocation(postProcessProgram, "uDistortion")
            postUGlow = GLES30.glGetUniformLocation(postProcessProgram, "uGlow")
        }
    }

    /**
     * Crea y linkea un programa de shaders.
     */
    private fun createProgram(vertexSrc: String, fragmentSrc: String): Int {
        val vs = compileShader(GLES30.GL_VERTEX_SHADER, vertexSrc)
        val fs = compileShader(GLES30.GL_FRAGMENT_SHADER, fragmentSrc)

        if (vs == 0 || fs == 0) return 0

        val program = GLES30.glCreateProgram()
        GLES30.glAttachShader(program, vs)
        GLES30.glAttachShader(program, fs)
        GLES30.glLinkProgram(program)

        val linkStatus = IntArray(1)
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            Timber.e("PortalRenderer: error de linkeo: ${GLES30.glGetProgramInfoLog(program)}")
            GLES30.glDeleteProgram(program)
            GLES30.glDeleteShader(vs)
            GLES30.glDeleteShader(fs)
            return 0
        }

        GLES30.glDeleteShader(vs)
        GLES30.glDeleteShader(fs)
        return program
    }

    private fun compileShader(type: Int, source: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, source)
        GLES30.glCompileShader(shader)

        val status = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, status, 0)
        if (status[0] == 0) {
            Timber.e("PortalRenderer: error de compilación: ${GLES30.glGetShaderInfoLog(shader)}")
            GLES30.glDeleteShader(shader)
            return 0
        }

        return shader
    }
}
