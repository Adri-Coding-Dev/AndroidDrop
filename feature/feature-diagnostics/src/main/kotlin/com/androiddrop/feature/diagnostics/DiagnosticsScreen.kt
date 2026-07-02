package com.androiddrop.feature.diagnostics

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    viewModel: DiagnosticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    val filteredLogs by remember {
        derivedStateOf {
            val logs = uiState.logs
            if (uiState.filterLevel != null) {
                logs.filter { it.level == uiState.filterLevel }
            } else {
                logs
            }
        }
    }

    LaunchedEffect(filteredLogs.size) {
        if (filteredLogs.isNotEmpty()) {
            listState.animateScrollToItem(filteredLogs.size - 1)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Diagnóstico") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            DeviceInfoCard(
                info = uiState.deviceInfo,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.onIntent(DiagnosticsIntent.StartLogCapture) },
                    enabled = !uiState.isMonitoring,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.BugReport, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Capturar")
                }
                Button(
                    onClick = { viewModel.onIntent(DiagnosticsIntent.StopLogCapture) },
                    enabled = uiState.isMonitoring,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.BugReport, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Detener")
                }
                IconButton(
                    onClick = { viewModel.onIntent(DiagnosticsIntent.ClearLogs) }
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Limpiar logs")
                }
                IconButton(
                    onClick = {
                        val logContent = viewModel.exportLogs()
                        val file = File(context.cacheDir, "diagnostics_log.txt")
                        file.writeText(logContent)
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Exportar logs"))
                    }
                ) {
                    Icon(Icons.Default.SaveAlt, contentDescription = "Exportar logs")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.filterLevel == null,
                    onClick = { viewModel.onIntent(DiagnosticsIntent.SetFilterLevel(null)) },
                    label = { Text("Todos") }
                )
                LogLevel.entries.forEach { level ->
                    val levelLabel = when (level) {
                        LogLevel.DEBUG -> "DEBUG"
                        LogLevel.INFO -> "INFO"
                        LogLevel.WARNING -> "WARNING"
                        LogLevel.ERROR -> "ERROR"
                    }
                    FilterChip(
                        selected = uiState.filterLevel == level,
                        onClick = { viewModel.onIntent(DiagnosticsIntent.SetFilterLevel(level)) },
                        label = { Text(levelLabel) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = logColor(level).copy(alpha = 0.2f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (filteredLogs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.isMonitoring) "Esperando logs..."
                                   else "Inicia la captura para ver logs",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LogViewer(
                        logs = filteredLogs,
                        modifier = Modifier.fillMaxSize(),
                        listState = listState
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceInfoCard(
    info: DeviceInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Información del dispositivo",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            InfoRow(icon = Icons.Default.PhoneAndroid, label = "Modelo", value = info.deviceModel)
            InfoRow(icon = Icons.Default.Info, label = "Android", value = "${info.androidVersion} (API ${info.apiLevel})")
            InfoRow(icon = Icons.Default.SdStorage, label = "Almacenamiento disponible", value = info.availableStorageFormatted)
            InfoRow(icon = Icons.Default.ScreenRotation, label = "Frecuencia de pantalla", value = "${info.screenRefreshRate} Hz")
            InfoRow(
                icon = Icons.Default.Bluetooth,
                label = "BLE",
                value = if (info.bleSupported) "Soportado" else "No soportado"
            )
            InfoRow(
                icon = Icons.Default.Wifi,
                label = "Wi-Fi Direct",
                value = if (info.wifiDirectSupported) "Soportado" else "No soportado"
            )
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun LogViewer(
    logs: List<LogEntry>,
    modifier: Modifier = Modifier,
    listState: androidx.compose.foundation.lazy.LazyListState = rememberLazyListState()
) {
    LazyColumn(
        modifier = modifier
            .semantics { contentDescription = "Visor de logs" },
        state = listState
    ) {
        items(logs, key = { "${it.timestamp}_${it.message.hashCode()}" }) { entry ->
            LogEntryItem(entry = entry)
        }
    }
}

@Composable
fun LogEntryItem(
    entry: LogEntry,
    modifier: Modifier = Modifier
) {
    val color = logColor(entry.level)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.08f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = entry.formattedTimestamp,
            color = color.copy(alpha = 0.7f),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = "[${entry.level.name.first()}]",
            color = color,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(40.dp)
        )
        Text(
            text = "[${entry.tag}]",
            color = color.copy(alpha = 0.8f),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = entry.message,
            color = color,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

private fun logColor(level: LogLevel): Color {
    return when (level) {
        LogLevel.DEBUG -> Color.Gray
        LogLevel.INFO -> Color(0xFF42A5F5)
        LogLevel.WARNING -> Color(0xFFFFCA28)
        LogLevel.ERROR -> Color(0xFFEF5350)
    }
}
