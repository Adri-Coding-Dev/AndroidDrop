package com.androiddrop.core.common.extensions

import android.webkit.MimeTypeMap
import java.io.File

/**
 * Extensiones para [File] con utilidades específicas de AndroidDrop.
 *
 * POR QUÉ existen estas extensiones: La manipulación de archivos es el núcleo
 * de AndroidDrop. Estas extensiones encapsulan lógica repetitiva de formato
 * y tipo MIME que se usa en múltiples capas de la aplicación (UI, transferencia,
 * explorador de archivos).
 */

/**
 * Retorna el tamaño del archivo en formato legible por humanos.
 *
 * POR QUÉ formato legible: Mostrar bytes crudos es poco amigable. Esta
 * representación adaptativa (B, KB, MB, GB, TB) permite que la UI muestre
 * tamaños de manera consistente sin lógica de formateo dispersa.
 *
 * Ejemplos: "1.5 GB", "234 MB", "842 KB", "128 B"
 */
val File.formattedSize: String
    get() {
        if (!exists() || length() == 0L) return "0 B"

        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val base = 1024.0
        val size = length().toDouble()
        val unitIndex = (Math.log(size) / Math.log(base)).toInt().coerceAtMost(units.size - 1)
        val formattedSize = size / Math.pow(base, unitIndex.toDouble())

        return if (unitIndex == 0) {
            "${length()} B"
        } else {
            "%.1f %s".format(formattedSize, units[unitIndex])
        }
    }

/**
 * Retorna el tipo MIME del archivo basado en su extensión.
 *
 * POR QUÉ MIME: Android necesita el tipo MIME para many operaciones del sistema
 * (intents de compartición, previsualización de contenido). Usar MimeTypeMap
 * del SDK garantiza compatibilidad con el sistema sin dependencias externas.
 *
 * Fallback: Si la extensión no está registrada, retorna "application/octet-stream"
 * que es el tipo genérico para datos binarios desconocidos.
 */
val File.mimeType: String
    get() {
        val extension = extension.lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            ?: "application/octet-stream"
    }
