package com.androiddrop.feature.fileexplorer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.androiddrop.core.ui.components.FileIcon
import com.androiddrop.core.ui.components.GlassCard
import com.androiddrop.core.ui.theme.LocalAndroidDropColorScheme
import com.androiddrop.core.ui.theme.LocalAndroidDropSpacing
import com.androiddrop.domain.model.FileCategory
import com.androiddrop.domain.model.FileNode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileExplorerScreen(
    viewModel: FileExplorerViewModel,
    onFileSelected: (FileNode) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentPath by viewModel.currentPath.collectAsState()
    val colors = LocalAndroidDropColorScheme.current
    val spacing = LocalAndroidDropSpacing.current

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = currentDisplayName(currentPath),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Volver atrás",
                        tint = colors.primary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colors.surfaceDark
            )
        )

        CategoryFilterBar(
            selectedCategory = (uiState as? FileExplorerUiState.Files)?.selectedCategory,
            onCategorySelected = { category ->
                viewModel.onIntent(FileExplorerIntent.FilterByCategory(category))
            }
        )

        when (val state = uiState) {
            is FileExplorerUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = colors.energyMedium,
                        modifier = Modifier.semantics { contentDescription = "Cargando archivos" }
                    )
                }
            }

            is FileExplorerUiState.Empty -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if ((uiState as? FileExplorerUiState.Files)?.selectedCategory != null)
                            "No hay archivos de este tipo" else "Esta carpeta está vacía",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.energyLow
                    )
                }
            }

            is FileExplorerUiState.Files -> {
                FileList(
                    files = state.files,
                    onFileClick = { file ->
                        if (file.isDirectory) {
                            viewModel.onIntent(FileExplorerIntent.NavigateTo(file.uri))
                        } else {
                            viewModel.onIntent(FileExplorerIntent.SelectFile(file))
                            onFileSelected(file)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            is FileExplorerUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryFilterBar(
    selectedCategory: FileCategory?,
    onCategorySelected: (FileCategory?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val categories = listOf(
            null to "Todo" to null,
            FileCategory.IMAGE to "Imágenes" to Icons.Filled.Image,
            FileCategory.VIDEO to "Videos" to Icons.Filled.Videocam,
            FileCategory.AUDIO to "Audio" to Icons.Filled.Audiotrack,
            FileCategory.DOCUMENT to "Documentos" to Icons.Filled.Description,
            FileCategory.APK to "APKs" to Icons.Filled.Description,
            FileCategory.ARCHIVE to "Archivos" to Icons.Filled.Folder
        )

        categories.forEach { (pair, icon) ->
            val (category, label) = pair
            FilterChip(
                selected = selectedCategory == category,
                onClick = {
                    onCategorySelected(if (selectedCategory == category) null else category)
                },
                label = { Text(label) },
                leadingIcon = if (icon != null) {
                    { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
            )
        }
    }
}

@Composable
fun FileList(
    files: List<FileNode>,
    onFileClick: (FileNode) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalAndroidDropSpacing.current

    LazyColumn(
        modifier = modifier.padding(horizontal = spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
        items(files, key = { it.uri }) { file ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FileListItem(
                    file = file,
                    onClick = { onFileClick(file) }
                )
            }
        }
    }
}

@Composable
fun FileListItem(
    file: FileNode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAndroidDropColorScheme.current
    val spacing = LocalAndroidDropSpacing.current
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = "${file.name}, " +
                    if (file.isDirectory) "carpeta" else "archivo, ${file.formattedSize}"
            },
        alpha = 0.25f
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (file.isDirectory) {
                Icon(
                    imageVector = Icons.Filled.Folder,
                    contentDescription = "Carpeta",
                    tint = colors.energyMedium,
                    modifier = Modifier.size(40.dp)
                )
            } else {
                FileIcon(
                    fileType = file.extension,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.width(spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = colors.primary
                )
                Spacer(modifier = Modifier.height(spacing.xxs))
                Row {
                    if (!file.isDirectory) {
                        Text(
                            text = file.formattedSize,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.energyLow
                        )
                        Text(
                            text = " · ",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.energyLow
                        )
                    }
                    Text(
                        text = dateFormat.format(Date(file.lastModified)),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.energyLow
                    )
                }
            }
        }
    }
}

private fun currentDisplayName(path: String): String {
    return when {
        path.startsWith("file://") -> {
            val p = path.removePrefix("file://")
            p.substringAfterLast("/").ifEmpty { "Almacenamiento" }
        }
        path.startsWith("/") -> {
            path.substringAfterLast("/").ifEmpty { "Almacenamiento" }
        }
        else -> "Almacenamiento"
    }
}