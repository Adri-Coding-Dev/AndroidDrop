package com.androiddrop.feature.transfer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.androiddrop.core.ui.components.AnimatedBackground
import com.androiddrop.core.ui.components.EnergyButton
import com.androiddrop.core.ui.components.GlassCard
import com.androiddrop.core.ui.components.ProgressRing
import com.androiddrop.core.ui.theme.LocalAndroidDropColorScheme
import com.androiddrop.core.ui.theme.LocalAndroidDropSpacing
import com.androiddrop.domain.model.NearbyDevice
import com.androiddrop.domain.model.TransferPhase

@Composable
fun TransferScreen(
    viewModel: TransferViewModel,
    modifier: Modifier = Modifier,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToDiagnostics: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val colors = LocalAndroidDropColorScheme.current
    val spacing = LocalAndroidDropSpacing.current

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(spacing.md)
        ) {
            TopBar(
                onSettings = onNavigateToSettings,
                onDiagnostics = onNavigateToDiagnostics
            )

            Spacer(modifier = Modifier.height(spacing.md))

            SphereInteractionArea(
                state = state,
                onGesture = { gesture ->
                    when (gesture) {
                        is GestureEvent.Drag -> {
                            viewModel.onIntent(TransferIntent.UpdateSpherePosition(gesture.position))
                        }
                        is GestureEvent.Fling -> {
                            viewModel.onIntent(TransferIntent.LaunchSphere(gesture.velocity))
                        }
                        is GestureEvent.Tap -> {
                            if (state.phase == TransferPhase.IDLE) {
                                viewModel.onIntent(TransferIntent.StartDiscovery)
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Spacer(modifier = Modifier.height(spacing.md))

            TransferControls(
                state = state,
                onCancel = { viewModel.onIntent(TransferIntent.CancelTransfer) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        state.pairedDevice?.let { device ->
            DeviceFoundOverlay(
                device = device,
                signalStrength = 80,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        AnimatedVisibility(
            visible = state.phase == TransferPhase.TRANSFERRING,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            TransferProgressOverlay(
                progress = state.transferProgress,
                speedBps = 0L,
                modifier = Modifier.size(200.dp)
            )
        }

        AnimatedVisibility(
            visible = state.phase == TransferPhase.TRANSFER_COMPLETE,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            TransferCompleteAnimation(modifier = Modifier.size(200.dp))
        }
    }
}

@Composable
private fun TopBar(
    onSettings: () -> Unit,
    onDiagnostics: () -> Unit
) {
    val colors = LocalAndroidDropColorScheme.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "AndroidDrop",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colors.primary
        )
        Row {
            IconButton(onClick = onDiagnostics) {
                Icon(
                    imageVector = Icons.Filled.DeviceHub,
                    contentDescription = "Diagnósticos",
                    tint = colors.energyLow
                )
            }
            IconButton(onClick = onSettings) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Configuración",
                    tint = colors.energyLow
                )
            }
        }
    }
}

@Composable
fun SphereInteractionArea(
    state: TransferUiState,
    onGesture: (GestureEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAndroidDropColorScheme.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(colors.glassLight)
            .semantics { contentDescription = "Área de interacción con la esfera" },
        contentAlignment = Alignment.Center
    ) {
        when (state.phase) {
            TransferPhase.IDLE -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Wifi,
                        contentDescription = "Iniciar descubrimiento",
                        tint = colors.energyMedium,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Toca para buscar dispositivos",
                        color = colors.energyLow,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            TransferPhase.DISCOVERING -> {
                ScanningAnimation(modifier = Modifier.size(120.dp))
            }

            TransferPhase.FILE_SELECTED -> {
                Text(
                    text = "Archivo listo para enviar",
                    color = colors.energyMedium,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            else -> {
                val energy = state.sphereEnergy
                Box(
                    modifier = Modifier
                        .size(
                            width = (120 * state.sphereScale).dp,
                            height = (120 * state.sphereScale).dp
                        )
                        .clip(RoundedCornerShape(60.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    colors.energyHigh.copy(alpha = energy),
                                    colors.energyMedium.copy(alpha = energy * 0.6f),
                                    colors.primaryDark.copy(alpha = energy * 0.3f)
                                )
                            )
                        )
                        .semantics { contentDescription = "Esfera energética" }
                )
            }
        }
    }
}

@Composable
fun TransferControls(
    state: TransferUiState,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAndroidDropColorScheme.current

    GlassCard(modifier = modifier, alpha = 0.3f) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = when (state.phase) {
                        TransferPhase.IDLE -> "Listo"
                        TransferPhase.FILE_SELECTED -> "Archivo seleccionado"
                        TransferPhase.DISCOVERING -> "Buscando dispositivos..."
                        TransferPhase.DEVICE_FOUND,
                        TransferPhase.SPHERE_HELD -> "Dispositivo encontrado"
                        TransferPhase.SPHERE_LAUNCHED -> "Lanzando esfera..."
                        TransferPhase.ENTERING_PORTAL -> "Entrando al portal..."
                        TransferPhase.TRANSFERRING -> "Transfiriendo..."
                        TransferPhase.SPHERE_DECAYING -> "Finalizando..."
                        TransferPhase.SPHERE_EXPLODING,
                        TransferPhase.TRANSFER_COMPLETE -> "¡Completado!"
                        TransferPhase.ERROR -> "Error"
                        TransferPhase.SPHERE_TRANSFORM -> "Transformando..."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.energyMedium
                )

                state.selectedFile?.let { file ->
                    Text(
                        text = file.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.energyLow
                    )
                }
            }

            if (state.phase in listOf(
                    TransferPhase.DISCOVERING,
                    TransferPhase.TRANSFERRING,
                    TransferPhase.SPHERE_LAUNCHED,
                    TransferPhase.ENTERING_PORTAL,
                    TransferPhase.SPHERE_DECAYING
                )
            ) {
                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cancelar transferencia",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceFoundOverlay(
    device: NearbyDevice,
    signalStrength: Int,
    modifier: Modifier = Modifier
) {
    val colors = LocalAndroidDropColorScheme.current
    val spacing = LocalAndroidDropSpacing.current

    GlassCard(
        modifier = modifier
            .padding(spacing.md)
            .semantics { contentDescription = "Dispositivo encontrado: ${device.deviceName}" },
        alpha = 0.4f
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.DeviceHub,
                contentDescription = null,
                tint = colors.energyHigh,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(spacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.deviceName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.primary
                )
                Text(
                    text = "${device.deviceType.name} · Señal: $signalStrength%",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.energyLow
                )
            }
            SignalStrengthIcon(signalStrength, colors.energyMedium)
        }
    }
}

@Composable
private fun SignalStrengthIcon(signalStrength: Int, color: Color) {
    val bars = when {
        signalStrength > 75 -> 4
        signalStrength > 50 -> 3
        signalStrength > 25 -> 2
        else -> 1
    }

    Row(verticalAlignment = Alignment.Bottom) {
        repeat(4) { index ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(6.dp + (index * 4).dp)
                    .padding(horizontal = 1.dp)
                    .background(
                        if (index < bars) color else color.copy(alpha = 0.2f),
                        RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}

@Composable
fun TransferProgressOverlay(
    progress: Float,
    speedBps: Long,
    modifier: Modifier = Modifier
) {
    val colors = LocalAndroidDropColorScheme.current

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ProgressRing(
            progress = progress,
            modifier = Modifier.size(150.dp),
            strokeWidth = 8f
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colors.energyHigh
        )
        if (speedBps > 0) {
            val speedText = when {
                speedBps > 1_000_000 -> "${speedBps / 1_000_000} MB/s"
                speedBps > 1_000 -> "${speedBps / 1_000} KB/s"
                else -> "$speedBps B/s"
            }
            Text(
                text = speedText,
                style = MaterialTheme.typography.bodySmall,
                color = colors.energyLow
            )
        }
    }
}

@Composable
fun TransferCompleteAnimation(modifier: Modifier = Modifier) {
    val colors = LocalAndroidDropColorScheme.current

    val infiniteTransition = rememberInfiniteTransition(label = "completePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .alpha(pulseScale)
                .clip(RoundedCornerShape(50.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            colors.success.copy(alpha = 0.8f),
                            colors.success.copy(alpha = 0.2f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✓",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = colors.surfaceDark
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Transferencia completada",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.success
        )
    }
}

@Composable
private fun ScanningAnimation(modifier: Modifier = Modifier) {
    val colors = LocalAndroidDropColorScheme.current

    val infiniteTransition = rememberInfiniteTransition(label = "scanning")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 750, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .alpha(pulse)
                .clip(RoundedCornerShape(50.dp))
                .background(colors.energyMedium.copy(alpha = 0.15f))
        )
        Box(
            modifier = Modifier
                .size(70.dp)
                .alpha(pulse * 0.7f)
                .clip(RoundedCornerShape(35.dp))
                .background(colors.energyHigh.copy(alpha = 0.1f))
        )
        Icon(
            imageVector = Icons.Filled.Wifi,
            contentDescription = "Escaneando",
            tint = colors.energyMedium,
            modifier = Modifier.size(32.dp)
        )
    }
}
