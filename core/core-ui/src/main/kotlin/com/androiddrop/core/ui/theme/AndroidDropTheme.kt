package com.androiddrop.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

/**
 * Tema principal de AndroidDrop.
 *
 * POR QUÉ un tema unificado: Envolver la aplicación en AndroidDropTheme
 * asegura que todos los componentes hijos tengan acceso consistente a
 * colores, tipografía y espaciado. Sin este wrapper, cada componente
 * tendría que importar valores individualmente, perdiendo coherencia.
 *
 * POR QUÉ darkTheme = true por defecto: El diseño de AndroidDrop está
 * concebido para fondo oscuro. Los colores neón/energy brillan sobre
 * fondos oscuros, creando el efecto de "energía contenida". El modo
 * claro es secundario y existe principalmente para accesibilidad.
 *
 * @param darkTheme Si usar esquema oscuro (true) o claro (false).
 * @param content Contenido de la aplicación que consume el tema.
 */
@Composable
fun AndroidDropTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        androidDropDarkColorScheme()
    } else {
        androidDropLightColorScheme()
    }

    val typography = AndroidDropTypography.toMaterialTypography()
    val spacing = AndroidDropSpacing()

    CompositionLocalProvider(
        LocalAndroidDropColorScheme provides colorScheme,
        LocalAndroidDropSpacing provides spacing
    ) {
        MaterialTheme(
            colorScheme = androidx.compose.material3.darkColorScheme(
                primary = colorScheme.primary,
                secondary = colorScheme.secondary,
                tertiary = colorScheme.tertiary,
                background = colorScheme.surfaceDark,
                surface = colorScheme.glassMedium,
                error = colorScheme.error,
                onPrimary = colorScheme.surfaceDark,
                onSecondary = colorScheme.surfaceDark,
                onTertiary = colorScheme.surfaceDark,
                onBackground = Color.White,
                onSurface = Color.White,
                onError = Color.White
            ),
            typography = typography,
            content = content
        )
    }
}
