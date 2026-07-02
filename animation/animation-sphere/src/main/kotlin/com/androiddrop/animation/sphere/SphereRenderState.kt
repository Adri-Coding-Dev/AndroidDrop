package com.androiddrop.animation.sphere

import com.androiddrop.core.common.Vector3

/**
 * Estado de renderizado de la esfera energética.
 *
 * POR QUÉ un data class inmutable: El estado de la esfera se actualiza desde
 * el gestor de dispositivos y se consume en el hilo de renderizado. Un data
 * class inmutable evita race conditions y permite diffing eficiente para
 * detectar qué cambió entre frames.
 *
 * @property energy     Nivel de energía actual (0.0 - 1.0). Controla intensidad
 *                      de shader, brillo y tasa de partículas.
 * @property scale      Escala uniforme de la esfera (1.0 = tamaño normal).
 * @property position   Posición 3D del centro de la esfera.
 * @property rotation   Rotación en grados alrededor de cada eje (X, Y, Z).
 * @property pulsePhase Fase de la respiración senoidal (0.0 - 2*PI).
 *                      Avanza ~2*PI cada 2 segundos en estado IDLE.
 * @property brightness Brillo adicional aplicado al render (1.0 = normal).
 */
data class SphereRenderState(
    val energy: Float = 0f,
    val scale: Float = 1f,
    val position: Vector3 = Vector3.zero,
    val rotation: Vector3 = Vector3.zero,
    val pulsePhase: Float = 0f,
    val brightness: Float = 1f
)
