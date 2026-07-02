package com.androiddrop.core.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Icono dinámico que representa el tipo de archivo.
 *
 * POR QUÉ iconos por tipo de archivo: En la UI de exploración y transferencia,
 * el usuario necesita identificar rápidamente qué tipo de archivo está viendo.
 * Los iconos visuales son más rápidos de procesar que el texto de la extensión.
 * Mapeamos las extensiones más comunes a iconos de Material Icons.
 *
 * @param fileType Extensión o tipo del archivo (ej: "pdf", "jpg", "mp4").
 * @param modifier Modificador para tamaño y posicionamiento.
 */
@Composable
fun FileIcon(
    fileType: String,
    modifier: Modifier = Modifier
) {
    val icon: ImageVector = when (fileType.lowercase()) {
        "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg", "heic" -> Icons.Filled.Image
        "mp4", "mkv", "avi", "mov", "wmv", "flv", "webm" -> Icons.Filled.VideoFile
        "mp3", "wav", "flac", "aac", "ogg", "wma", "m4a" -> Icons.Filled.Audiotrack
        "pdf" -> Icons.Filled.Description
        "zip", "rar", "tar", "gz", "7z" -> Icons.Filled.FolderZip
        "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt" -> Icons.Filled.Description
        "apk" -> Icons.Filled.PhoneAndroid
        else -> Icons.Filled.InsertDriveFile
    }

    Icon(
        imageVector = icon,
        contentDescription = "Tipo de archivo: $fileType",
        modifier = modifier
    )
}
