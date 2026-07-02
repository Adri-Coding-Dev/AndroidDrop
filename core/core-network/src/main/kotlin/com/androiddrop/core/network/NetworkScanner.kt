package com.androiddrop.core.network

import kotlinx.coroutines.flow.Flow

/**
 * Escáner de servicios en la red local.
 *
 * POR QUÉ un escáner: Complemento del [NetworkAdvertiser]. Mientras el
 * anunciante emite presencia, el escáner busca activamente otros dispositivos
 * AndroidDrop en la red. El escáner funciona como "cliente": detecta los
 * broadcasts de los anunciantes y reporta peers disponibles.
 *
 * POR QUÉ retorna Flow<ConnectionInfo>: El escáner puede encontrar múltiples
 * peers a lo largo del tiempo. Flow es el mecanismo natural de Kotlin para
 * emitir una secuencia de valores a medida que se descubren. El llamante
 * puede recolectar el flow y actualizar la UI en tiempo real.
 */
interface NetworkScanner {

    /**
     * Comienza la búsqueda de peers en la red.
     *
     * @param timeoutMillis Tiempo máximo de escaneo antes de cancelar automáticamente.
     *   Si es 0, escanea indefinidamente hasta llamar a [stopScanning].
     * @return Flow que emite [ConnectionInfo] por cada peer descubierto.
     */
    fun startScanning(timeoutMillis: Long = 0L): Flow<ConnectionInfo>

    /**
     * Detiene el escaneo de red activo.
     *
     * POR QUÉ método explícito: Similar a [NetworkAdvertiser.stopAdvertising],
     * detener el escaneo libera recursos de red (sockets, receptores BLE, etc.).
     * Siempre debe llamarse cuando el usuario sale de la vista de descubrimiento.
     */
    fun stopScanning()
}
