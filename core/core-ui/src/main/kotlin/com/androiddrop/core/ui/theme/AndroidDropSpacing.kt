package com.androiddrop.core.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Sistema de espaciado de AndroidDrop (SDD-07-Sistema-Diseno.md).
 *
 * POR QUÉ espaciado consistente: Un sistema de espaciado con valores fijos
 * garantiza que todos los elementos visuales mantengan relaciones armónicas
 * independientemente de dónde se usen. Esto elimina decisiones arbitrarias
 * ("¿2dp o 4dp?") y produce una UI visualmente coherente.
 *
 * La escala sigue una progresión de ~2x entre niveles:
 *   xxs(2) -> xs(4) -> sm(8) -> md(16) -> lg(24) -> xl(32) -> xxl(48) -> xxxl(64)
 *
 * POR QUÉ esta escala: 2^n en espaciado crea relaciones visuales predecibles.
 * El espaciado md(16dp) es el baseline; los demás son mitades o dobles.
 */
data class AndroidDropSpacing(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 48.dp,
    val xxxl: Dp = 64.dp
)

/**
 * CompositionLocal para proveer el sistema de espaciado.
 *
 * POR QUÉ CompositionLocal: Igual que los colores, el espaciado es un valor
 * de tema que muchos componentes necesitan. CompositionLocal permite acceso
 * sin acoplamiento explícito entre componentes padres e hijos.
 */
val LocalAndroidDropSpacing = staticCompositionLocalOf { AndroidDropSpacing() }
