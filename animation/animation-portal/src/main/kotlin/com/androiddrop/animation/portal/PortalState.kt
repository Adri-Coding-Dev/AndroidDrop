package com.androiddrop.animation.portal

/**
 * Fase del ciclo de vida del portal.
 *
 * POR QUÉ una enum vs boolean flags: El portal tiene transiciones discretas
 * entre estados (IDLE -> OPENING -> ACTIVE -> CLOSING -> IDLE). Un enum
 * asegura que solo un estado está activo a la vez y permite manejar
 * transiciones con when() exhaustivo.
 */
enum class PortalPhase {
    /** Portal cerrado, esperando activación. */
    IDLE,

    /** Animación de apertura: el anillo se expande, el vórtice emerge. */
    OPENING,

    /** Portal completamente abierto: transferencia de datos activa. */
    ACTIVE,

    /** Animación de cierre: el vórtice colapsa, el anillo se contrae. */
    CLOSING
}

/**
 * Estado de renderizado del portal energético (grieta espacial).
 *
 * @property phase            Fase actual del ciclo de vida.
 * @property ringRotation     Rotación del anillo exterior en grados.
 * @property vortexDepth      Profundidad aparente del vórtice (0.0 - 1.0).
 * @property intensity        Intensidad general del efecto visual.
 * @property glowIntensity    Intensidad del brillo/glow del borde.
 * @property distortionAmount Cantidad de distorsión de lente gravitacional.
 */
data class PortalRenderState(
    val phase: PortalPhase = PortalPhase.IDLE,
    val ringRotation: Float = 0f,
    val vortexDepth: Float = 0f,
    val intensity: Float = 0f,
    val glowIntensity: Float = 0f,
    val distortionAmount: Float = 0f
)
