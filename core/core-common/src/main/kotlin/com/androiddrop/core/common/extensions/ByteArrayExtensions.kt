package com.androiddrop.core.common.extensions

import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Extensiones para [ByteArray].
 *
 * POR QUÉ existen estas extensiones: Las operaciones criptográficas y de
 * representación de bytes son ubicuas en AndroidDrop (cifrado, hashing,
 * serialización de paquetes de red). Centralizar estas conversiones evita
 * duplicación de lógica y asegura consistencia en toda la aplicación.
 */

/**
 * Convierte un [ByteArray] a su representación hexadecimal en minúsculas.
 *
 * POR QUÉ hexadecimal: Es el formato más usado para depuración/logs de datos
 * binarios porque es legible, compacto y cada byte ocupa exactamente 2 caracteres.
 * Usamos minúsculas para consistencia con la salida estándar de MessageDigest.
 */
fun ByteArray.toHexString(): String = joinToString(separator = "") { byte ->
    String.format("%02x", byte)
}

/**
 * Convierte un [ByteArray] a su representación Base64.
 *
 * POR QUÉ Base64 vs hex: Base64 es ~33% más eficiente que hex en espacio
 * (4 caracteres por cada 3 bytes vs 2 caracteres por byte). Se usa para
 * serializar claves y firmas en los paquetes de red donde el overhead importa.
 */
fun ByteArray.toBase64String(): String = android.util.Base64.encodeToString(
    this,
    android.util.Base64.NO_WRAP
)

/**
 * Calcula el hash SHA-256 de este [ByteArray].
 *
 * POR QUÉ SHA-256: Es el estándar actual para verificación de integridad.
 * Es resistente a colisiones, ampliamente soportado, y su salida de 32 bytes
 * es el balance ideal entre seguridad y tamaño para checksums de chunks.
 */
fun ByteArray.sha256(): ByteArray = MessageDigest
    .getInstance("SHA-256")
    .digest(this)

/**
 * Calcula HMAC-SHA256 de este [ByteArray] usando la [key] proporcionada.
 *
 * POR QUÉ HMAC-SHA256: A diferencia del hash simple, HMAC requiere una clave
 * secreta, lo que lo hace seguro contra ataques de extensión de longitud.
 * Se utiliza en el protocolo de autenticación de paquetes para garantizar
 * que ningún tercero pueda modificar datos en tránsito.
 */
fun ByteArray.hmacSha256(key: ByteArray): ByteArray {
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(SecretKeySpec(key, "HmacSHA256"))
    return mac.doFinal(this)
}
