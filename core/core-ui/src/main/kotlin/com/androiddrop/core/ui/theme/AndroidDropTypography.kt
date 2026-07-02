package com.androiddrop.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Sistema tipográfico de AndroidDrop (SDD-07-Sistema-Diseno.md).
 *
 * POR QUÉ tipografía personalizada vs Material3 default: La tipografía de
 * AndroidDrop comunica modernidad y tecnología. Usamos SansSerif (Roboto en
 * Android) con pesos bold y medium para títulos, creando contraste con el
 * body regular. Los tamaños siguen la escala tipográfica de Material3 pero
 * con ajustes específicos de nuestra identidad.
 *
 * POR QUÉ FontFamily.SansSerif vs una fuente personalizada: SansSerif es la
 * familia del sistema Android, garantizando renderizado óptimo sin descargar
 * fuentes adicionales (reduce APK size). La variante Roboto que Android usa
 * por defecto es moderna y altamente legible en pantallas pequeñas.
 */
object AndroidDropTypography {

    /** Título principal de pantallas: 57sp, bold. Ej: "AndroidDrop", "Transferencias". */
    val displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    )

    /** Título secundario: 45sp, bold. Ej: nombres de secciones grandes. */
    val displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp
    )

    /** Título terciario: 36sp, bold. Ej: títulos de diálogos modales. */
    val displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp
    )

    /** Encabezado de sección: 32sp, bold. Ej: "Archivos recientes". */
    val headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    )

    /** Texto corrido principal: 16sp, normal. El cuerpo de la mayoría de la UI. */
    val bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )

    /** Texto secundario: 14sp, medium. Subtítulos, descripciones, metadatos. */
    val bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    )

    /** Etiquetas y botones: 14sp, bold. Texto en botones y badges. */
    val labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )

    /**
     * Mapa completo de estilos para Material3 Typography.
     *
     * Mapeamos nuestros estilos a los roles semánticos de Material3 para
     * que componentes como TopAppBar, NavigationBar y Button usen nuestros
     * estilos automáticamente.
     */
    fun toMaterialTypography(): Typography = Typography(
        displayLarge = displayLarge,
        displayMedium = displayMedium,
        displaySmall = displaySmall,
        headlineLarge = headlineLarge,
        headlineMedium = headlineLarge.copy(fontSize = 28.sp, lineHeight = 36.sp),
        headlineSmall = headlineLarge.copy(fontSize = 24.sp, lineHeight = 32.sp),
        bodyLarge = bodyLarge,
        bodyMedium = bodyMedium,
        bodySmall = bodyMedium.copy(fontSize = 12.sp, lineHeight = 16.sp),
        labelLarge = labelLarge,
        labelMedium = labelLarge.copy(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
        labelSmall = labelLarge.copy(fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium)
    )
}
