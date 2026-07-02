package com.androiddrop.feature.discovery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.androiddrop.core.ui.components.AnimatedBackground
import com.androiddrop.core.ui.components.EnergyButton
import com.androiddrop.core.ui.components.GlassCard
import com.androiddrop.core.ui.theme.LocalAndroidDropColorScheme
import com.androiddrop.core.ui.theme.LocalAndroidDropSpacing
import com.androiddrop.domain.model.DeviceType
import com.androiddrop.domain.model.NearbyDevice

@Composable
fun DiscoveryScreen(
    viewModel: DiscoveryViewModel,
    onDeviceConnected: (NearbyDevice) -> Unit,
    modifier: Modifier = Modifier
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
            Text(
                text = "Descubrir dispositivos",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colors.primary,
                modifier = Modifier.padding(bottom = spacing.md)
            )

            when (val currentState = state) {
                is DiscoveryUiState.Idle -> {
                    IdleContent(
                        onStartScanning = {
                            viewModel.onIntent(DiscoveryIntent.StartScanning)
                        }
                    )
                }

                is DiscoveryUiState.Scanning -> {
                    ScanningContent(
                        onStopScanning = {
                            viewModel.onIntent(DiscoveryIntent.StopScanning)
                        }
                    )
                }

                is DiscoveryUiState.DevicesFound -> {
                    DevicesFoundContent(
                        devices = currentState.devices,
                        onDeviceClick = { device ->
                            viewModel.onIntent(DiscoveryIntent.SelectDevice(device))
                        },
                        onStopScanning = {
                            viewModel.onIntent(DiscoveryIntent.StopScanning)
                        }
                    )
                }

                is DiscoveryUiState.Connecting -> {
                    ConnectingContent(device = currentState.device)
                }

                is DiscoveryUiState.Connected -> {
                    ConnectedContent(
                        device = currentState.device,
                        onDisconnect = {
                            viewModel.onIntent(DiscoveryIntent.Disconnect)
                        },
                        onContinue = {
                            onDeviceConnected(currentState.device)
                        }
                    )
                }

                is DiscoveryUiState.Error -> {
                    ErrorContent(
                        message = currentState.message,
                        onRetry = {
                            viewModel.onIntent(DiscoveryIntent.StartScanning)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun IdleContent(onStartScanning: () -> Unit) {
    val colors = LocalAndroidDropColorScheme.current

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Devices,
            contentDescription = null,
            tint = colors.energyLow,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Busca dispositivos cercanos",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.energyLow
        )
        Spacer(modifier = Modifier.height(24.dp))
        EnergyButton(
            onClick = onStartScanning,
            text = "Buscar dispositivos",
            modifier = Modifier.semantics { contentDescription = "Iniciar búsqueda de dispositivos" }
        )
    }
}

@Composable
private fun ScanningContent(onStopScanning: () -> Unit) {
    val colors = LocalAndroidDropColorScheme.current

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ScanningAnimation(modifier = Modifier.size(150.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Buscando dispositivos...",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.energyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onStopScanning,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.glassHeavy
            )
        ) {
            Text("Detener búsqueda", color = colors.primary)
        }
    }
}

@Composable
private fun DevicesFoundContent(
    devices: List<NearbyDevice>,
    onDeviceClick: (NearbyDevice) -> Unit,
    onStopScanning: () -> Unit
) {
    val colors = LocalAndroidDropColorScheme.current
    val spacing = LocalAndroidDropSpacing.current

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
        items(devices, key = { it.deviceId }) { device ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                DeviceCard(
                    device = device,
                    onConnect = { onDeviceClick(device) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(spacing.md))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onStopScanning,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.glassHeavy
                    )
                ) {
                    Text(
                        text = "Detener búsqueda",
                        color = colors.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(spacing.lg))
        }
    }
}

@Composable
fun DeviceCard(
    device: NearbyDevice,
    onConnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAndroidDropColorScheme.current
    val spacing = LocalAndroidDropSpacing.current

    val deviceIcon: ImageVector = when (device.deviceType) {
        DeviceType.PHONE -> Icons.Filled.PhoneAndroid
        DeviceType.TABLET -> Icons.Filled.Tablet
        DeviceType.TV -> Icons.Filled.Computer
        DeviceType.WEARABLE -> Icons.Filled.Watch
        DeviceType.DESKTOP -> Icons.Filled.Computer
    }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "${device.deviceName}, tipo ${device.deviceType.name}"
            },
        alpha = 0.3f
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = deviceIcon,
                contentDescription = null,
                tint = colors.energyMedium,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.deviceName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = colors.primary
                )
                Spacer(modifier = Modifier.height(spacing.xxs))
                Text(
                    text = "${device.deviceType.name} · ${transportLabel(device.connectionInfo.transportType)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.energyLow
                )
            }

            Spacer(modifier = Modifier.width(spacing.sm))

            SignalStrengthIndicator(signalStrength = device.signalStrength)

            Spacer(modifier = Modifier.width(spacing.sm))

            ElevatedButton(
                onClick = onConnect,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = colors.energyMedium.copy(alpha = 0.2f),
                    contentColor = colors.energyMedium
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Conectar",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun SignalStrengthIndicator(signalStrength: Int) {
    val colors = LocalAndroidDropColorScheme.current
    val bars = when {
        signalStrength > 75 -> 4
        signalStrength > 50 -> 3
        signalStrength > 25 -> 2
        else -> 1
    }

    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.semantics { contentDescription = "Intensidad de señal: $signalStrength%" }
    ) {
        repeat(4) { index ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(4.dp + (index * 3).dp)
                    .padding(horizontal = 1.dp)
                    .background(
                        if (index < bars) colors.energyHigh else colors.glassHeavy,
                        RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}

@Composable
private fun ConnectingContent(device: NearbyDevice) {
    val colors = LocalAndroidDropColorScheme.current

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = colors.energyMedium,
            modifier = Modifier.size(60.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Conectando a ${device.deviceName}...",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.energyMedium
        )
    }
}

@Composable
private fun ConnectedContent(
    device: NearbyDevice,
    onDisconnect: () -> Unit,
    onContinue: () -> Unit
) {
    val colors = LocalAndroidDropColorScheme.current
    val spacing = LocalAndroidDropSpacing.current

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            colors.success.copy(alpha = 0.6f),
                            colors.success.copy(alpha = 0.1f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.PhoneAndroid,
                contentDescription = null,
                tint = colors.success,
                modifier = Modifier.size(50.dp)
            )
        }
        Spacer(modifier = Modifier.height(spacing.md))
        Text(
            text = "Conectado a ${device.deviceName}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = colors.success
        )
        Spacer(modifier = Modifier.height(spacing.lg))
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
            Button(
                onClick = onDisconnect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.glassHeavy
                )
            ) {
                Text("Desconectar", color = colors.primary)
            }
            EnergyButton(
                onClick = onContinue,
                text = "Continuar"
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    val colors = LocalAndroidDropColorScheme.current

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.energyLow
        )
        Spacer(modifier = Modifier.height(24.dp))
        EnergyButton(
            onClick = onRetry,
            text = "Reintentar"
        )
    }
}

@Composable
fun ScanningAnimation(modifier: Modifier = Modifier) {
    val colors = LocalAndroidDropColorScheme.current

    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val outerPulse by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "outerPulse"
    )
    val innerPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "innerPulse"
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(colors.energyMedium.copy(alpha = outerPulse * 0.15f))
        )
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(colors.energyHigh.copy(alpha = innerPulse * 0.1f))
        )
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(colors.energyMedium.copy(alpha = 0.2f))
        )
        Icon(
            imageVector = Icons.Filled.Devices,
            contentDescription = "Escaneando dispositivos",
            tint = colors.energyMedium,
            modifier = Modifier.size(28.dp)
        )
    }
}

private fun transportLabel(transportType: com.androiddrop.domain.model.TransportType): String {
    return when (transportType) {
        com.androiddrop.domain.model.TransportType.BLE -> "Bluetooth LE"
        com.androiddrop.domain.model.TransportType.WIFI_DIRECT -> "WiFi Direct"
        com.androiddrop.domain.model.TransportType.NEARBY -> "Nearby"
        com.androiddrop.domain.model.TransportType.TCP -> "TCP/IP"
    }
}
