package com.androiddrop.core.common

/**
 * Constantes globales de AndroidDrop.
 *
 * POR QUÉ existe este objeto: Centralizar todos los valores de configuración
 * de la aplicación en un solo lugar garantiza consistencia entre módulos,
 * facilita el mantenimiento y evita "magic numbers" dispersos en el código.
 * Cualquier cambio en estos valores (tamaño de chunk, versión de protocolo, etc.)
 * debe hacerse exclusivamente aquí para mantener una única fuente de verdad.
 */
object Constants {

    /** Nombre oficial de la aplicación, usado en descubrimiento de red y UI */
    const val APP_NAME = "AndroidDrop"

    /**
     * Tamaño de fragmento (chunk) para transferencia de archivos: 1 MB.
     * Es el balance óptimo entre overhead de red y capacidad de reanudación.
     * Fragmentos más pequeños permiten reanudar con mayor granularidad;
     * fragmentos más grandes reducen el overhead de cabeceras TCP/UDP.
     */
    const val CHUNK_SIZE = 1_048_576 // 1 MB

    /**
     * Duración máxima de una sesión de transferencia en minutos (30 min).
     * Previene sesiones huérfanas que consuman recursos en ambos extremos.
     * Después de este tiempo, la sesión se cancela automáticamente.
     */
    const val MAX_SESSION_TIME_MINUTES = 30

    /**
     * Frecuencia de sincronización de estado entre pares (30 Hz).
     * 30 actualizaciones por segundo proporcionan una experiencia visual fluida
     * sin sobrecargar el canal de control durante la transferencia.
     */
    const val SYNC_FREQUENCY_HZ = 30

    /**
     * Número máximo de partículas en la animación de fondo (~2000).
     * Límite de seguridad para mantener rendimiento en dispositivos de gama baja.
     * El motor de partículas nunca excede este valor.
     */
    const val MAX_PARTICLES = 2000

    /**
     * Versión actual del protocolo de transferencia.
     * Se incrementa cuando hay cambios incompatibles en el formato de paquetes.
     * Durante el handshake, ambos peers negocian la versión más alta compatible.
     */
    const val PROTOCOL_VERSION = 1

    /**
     * Tamaño de clave AES en bits (256 bits).
     * AES-256-GCM es el estándar de cifrado simétrico más seguro actualmente,
     * utilizado para el cifrado de datos en tránsito entre pares.
     */
    const val AES_KEY_SIZE_BITS = 256

    /**
     * Longitud del checksum SHA-256 en bytes (32 bytes = 256 bits).
     * Se usa para verificar la integridad de cada chunk transferido.
     */
    const val SHA256_CHECKSUM_LENGTH = 32
}
