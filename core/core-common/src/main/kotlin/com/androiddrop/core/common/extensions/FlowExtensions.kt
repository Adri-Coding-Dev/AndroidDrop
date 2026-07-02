package com.androiddrop.core.common.extensions

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

/**
 * Extensiones para [Flow] de Kotlin Coroutines.
 *
 * POR QUÉ existen estas extensiones: El flujo de datos en AndroidDrop involucra
 * muchos streams de alta frecuencia (progreso de transferencia, posición de
 * partículas, estado de conexión). Estas extensiones proporcionan operadores
 * específicos para manejar backpressure y agrupación temporal sin depender de
 * librerías externas o funciones experimentales de Kotlin.
 */

/**
 * Emite el último valor emitido dentro de cada ventana de [periodMillis].
 *
 * POR QUÉ throttleLatest vs sample: A diferencia de [sample] que toma valores
 * en intervalos fijos, throttleLatest reinicia el timer con cada emisión.
 * Esto asegura que SIEMPRE se emita el último valor disponible después del
 * período de silencio, crítico para sincronización de estado donde perder
 * la última actualización rompería la consistencia.
 *
 * Ejemplo de uso: Sincronización de progreso de transferencia a 30Hz.
 */
fun <T> Flow<T>.throttleLatest(periodMillis: Long): Flow<T> = callbackFlow {
    var latestValue: T? = null
    var job: Job? = null

    val scope = CoroutineScope(coroutineContext)

    collect { value ->
        latestValue = value
        if (job == null || job?.isCompleted == true) {
            job = scope.launch {
                delay(periodMillis)
                latestValue?.let { send(it) }
                latestValue = null
            }
        }
    }

    awaitClose {
        job?.cancel()
    }
}

/**
 * Acumula emisiones en un buffer y las emite como lote cada [timeoutMillis].
 *
 * POR QUÉ bufferWithTimeout: En operaciones de red como la recepción de chunks,
 * es más eficiente procesar lotes que elementos individuales. Este operador
 * agrupa emisiones durante una ventana de tiempo, emitiendo el lote acumulado
 * cuando el timeout expira o el canal se cierra.
 *
 * Ejemplo de uso: Agrupar chunks de archivos para escritura en disco por lotes.
 */
fun <T> Flow<T>.bufferWithTimeout(timeoutMillis: Long): Flow<List<T>> = callbackFlow {
    val buffer = mutableListOf<T>()
    var job: Job? = null

    val scope = CoroutineScope(coroutineContext)

    collect { value ->
        buffer.add(value)
        if (job == null || job?.isCompleted == true) {
            job = scope.launch {
                delay(timeoutMillis)
                if (buffer.isNotEmpty()) {
                    send(buffer.toList())
                    buffer.clear()
                }
            }
        }
    }

    awaitClose {
        job?.cancel()
        if (buffer.isNotEmpty()) {
            trySend(buffer.toList())
        }
    }
}
