package com.androiddrop.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Sistema de colores de AndroidDrop (SDD-07-Sistema-Diseno.md).
 *
 * POR QUÉ un sistema de colores propio vs Material You: AndroidDrop necesita
 * una identidad visual consistente basada en el concepto de "energía contenida".
 * Los colores neón/energy (verde, cian, amarillo) sobre fondos oscuros crean
 * la sensación de tecnología avanzada y poder controlado. Material You cambiaría
 * estos colores dinámicamente según el wallpaper, rompiendo la identidad de marca.
 *
 * POR QUÉ variantes Light/Medium/Heavy: El glass morphism requiere múltiples
 * niveles de transparencia para crear profundidad. Cada nivel corresponde a
 * un "plano" visual diferente: Light para fondos lejanos, Medium para tarjetas,
 * Heavy para elementos interactivos.
 */

// Colores base del sistema ------------------------------------------------

/** Color primario: verde neón. Es el color de acción principal, botones, enlaces. */
val Primary = Color(0xFF00E676)

/** Variante del primario: verde más oscuro para estados hover/pressed. */
val PrimaryVariant = Color(0xFF00C853)

/** Versión oscura del primario, usada en fondos de botones o elementos decorativos. */
val PrimaryDark = Color(0xFF009624)

/** Color secundario: cian vibrante. Contrasta con el primario, usado en acciones secundarias. */
val Secondary = Color(0xFF00BCD4)

/** Variante del secundario para estados de interacción. */
val SecondaryVariant = Color(0xFF0097A7)

/** Color terciario: ámbar/energía, usado para acentos y warnings. */
val Tertiary = Color(0xFFFFAB00)

/** Variante del terciario. */
val TertiaryVariant = Color(0xFFFF8F00)

// Fondos Glass -----------------------------------------------------------

/** Fondo glass más transparente, para capas profundas (background principal). */
val GlassLight = Color(0x1AFFFFFF)

/** Fondo glass medio, para tarjetas y superficies elevadas. */
val GlassMedium = Color(0x33FFFFFF)

/** Fondo glass más opaco, para elementos interactivos (botones, inputs). */
val GlassHeavy = Color(0x4DFFFFFF)

// Fondos sólidos ---------------------------------------------------------

/** Fondo oscuro principal (casi negro con leve tono azulado). */
val SurfaceDark = Color(0xFF0D1117)

/** Fondo claro (para modo claro, aunque el diseño prioriza modo oscuro). */
val SurfaceLight = Color(0xFFF5F5F5)

/** Fondo variante para superficies elevadas en modo claro. */
val SurfaceVariant = Color(0xFFE0E0E0)

// Colores funcionales ----------------------------------------------------

/** Verde energía (bajo): estado inactivo, carga completa, progreso bajo. */
val EnergyLow = Color(0xFF66BB6A)

/** Verde-azul energía (medio): progreso en curso, estado activo. */
val EnergyMedium = Color(0xFF00E676)

/** Cian-verde energía (alto): máxima actividad, transferencia en marcha. */
val EnergyHigh = Color(0xFF00E5FF)

/** Verde de éxito: operación completada correctamente. */
val Success = Color(0xFF4CAF50)

/** Rojo de error: operación fallida, alerta crítica. */
val Error = Color(0xFFEF5350)

/** Amarillo de advertencia: operación pendiente, alerta no crítica. */
val Warning = Color(0xFFFFCA28)

/**
 * Esquema de colores completo de AndroidDrop.
 *
 * Agrupa todos los colores del sistema en una estructura que puede ser
 * provista mediante CompositionLocal para acceso consistente desde cualquier
 * componente de la UI.
 */
data class AndroidDropColorScheme(
    val primary: Color = Primary,
    val primaryVariant: Color = PrimaryVariant,
    val primaryDark: Color = PrimaryDark,
    val secondary: Color = Secondary,
    val secondaryVariant: Color = SecondaryVariant,
    val tertiary: Color = Tertiary,
    val tertiaryVariant: Color = TertiaryVariant,
    val surfaceDark: Color = SurfaceDark,
    val surfaceLight: Color = SurfaceLight,
    val surfaceVariant: Color = SurfaceVariant,
    val glassLight: Color = GlassLight,
    val glassMedium: Color = GlassMedium,
    val glassHeavy: Color = GlassHeavy,
    val energyLow: Color = EnergyLow,
    val energyMedium: Color = EnergyMedium,
    val energyHigh: Color = EnergyHigh,
    val success: Color = Success,
    val error: Color = Error,
    val warning: Color = Warning
) {
    companion object
}

/** Esquema de colores por defecto para modo oscuro. */
fun androidDropDarkColorScheme() = AndroidDropColorScheme(
    primary = Primary,
    primaryVariant = PrimaryVariant,
    primaryDark = PrimaryDark,
    secondary = Secondary,
    secondaryVariant = SecondaryVariant,
    tertiary = Tertiary,
    tertiaryVariant = TertiaryVariant,
    surfaceDark = SurfaceDark,
    surfaceLight = SurfaceLight,
    surfaceVariant = SurfaceVariant,
    glassLight = GlassLight,
    glassMedium = GlassMedium,
    glassHeavy = GlassHeavy,
    energyLow = EnergyLow,
    energyMedium = EnergyMedium,
    energyHigh = EnergyHigh,
    success = Success,
    error = Error,
    warning = Warning
)

/** Esquema de colores para modo claro. Los glass se mantienen semi-transparentes. */
fun androidDropLightColorScheme() = AndroidDropColorScheme(
    primary = Primary,
    primaryVariant = PrimaryVariant,
    primaryDark = PrimaryDark,
    secondary = Secondary,
    secondaryVariant = SecondaryVariant,
    tertiary = Tertiary,
    tertiaryVariant = TertiaryVariant,
    surfaceDark = SurfaceLight,
    surfaceLight = SurfaceDark,
    surfaceVariant = SurfaceVariant,
    glassLight = GlassLight,
    glassMedium = GlassMedium,
    glassHeavy = GlassHeavy,
    energyLow = EnergyLow,
    energyMedium = EnergyMedium,
    energyHigh = EnergyHigh,
    success = Success,
    error = Error,
    warning = Warning
)

/**
 * CompositionLocal para acceder al esquema de colores desde cualquier composable.
 *
 * POR QUÉ CompositionLocal vs pasar colores como parámetros: AndroidDrop tiene
 * muchos componentes anidados que necesitan acceso a colores. Pasarlos como
 * parámetros explícitos crearía ruido en las firmas. CompositionLocal permite
 * que cualquier componente hijo acceda al tema sin acoplamiento explícito.
 */
val LocalAndroidDropColorScheme = staticCompositionLocalOf { androidDropDarkColorScheme() }

/**
 * Acceso directo al esquema de colores desde cualquier @Composable.
 */
val AndroidDropColorScheme.Companion.current: AndroidDropColorScheme
    @Composable get() = LocalAndroidDropColorScheme.current
