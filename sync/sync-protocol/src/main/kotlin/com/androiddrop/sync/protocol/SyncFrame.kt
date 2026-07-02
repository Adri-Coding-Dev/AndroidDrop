package com.androiddrop.sync.protocol

import com.androiddrop.core.common.Vector3
import kotlinx.serialization.Serializable

/**
 * Frame de sincronización que encapsula el estado completo del peer en un
 * instante dado.
 *
 * POR QUÉ un frame con timestamp absoluto: Para que ambos peers puedan
 * reconstruir la misma línea de tiempo, cada frame lleva el timestamp del
 * clock maestro (definido por el emisor). El receptor usa este timestamp
 * para aplicar latencia compensada e interpolar entre frames.
 *
 * @property timestamp    Timestamp absoluto del clock maestro (System.nanoTime).
 * @property position     Posición 3D de la esfera/portal en este frame.
 * @property rotation     Rotación en grados (X, Y, Z) de la esfera/portal.
 * @property scale        Escala actual de la esfera.
 * @property energy       Nivel de energía (0.0 - 1.0).
 * @property phase        Fase de animación (0 = IDLE, 1 = FOUND, etc.).
 * @property sequenceNumber Número de secuencia para detección de pérdidas.
 * @property isKeyFrame   Indica si es un frame clave (full state) o interpolado.
 */
@Serializable
data class SyncFrame(
    val timestamp: Long = 0L,
    val position: Vector3 = Vector3.zero,
    val rotation: Vector3 = Vector3.zero,
    val scale: Float = 1f,
    val energy: Float = 0f,
    val phase: Int = 0,
    val sequenceNumber: Int = 0,
    val isKeyFrame: Boolean = false
)
