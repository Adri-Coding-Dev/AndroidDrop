package com.androiddrop.feature.fileexplorer

import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androiddrop.domain.model.FileCategory
import com.androiddrop.domain.model.FileNode
import com.androiddrop.domain.usecase.SelectFileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed interface FileExplorerUiState {
    data object Loading : FileExplorerUiState
    data object Empty : FileExplorerUiState
    data class Files(
        val files: List<FileNode>,
        val currentPath: String,
        val selectedCategory: FileCategory? = null,
        val allFiles: List<FileNode> = files
    ) : FileExplorerUiState
    data class Error(val message: String) : FileExplorerUiState
}

sealed interface FileExplorerIntent {
    data class LoadDirectory(val uri: String) : FileExplorerIntent
    data class NavigateTo(val path: String) : FileExplorerIntent
    data object NavigateUp : FileExplorerIntent
    data class SelectFile(val file: FileNode) : FileExplorerIntent
    data class FilterByCategory(val category: FileCategory?) : FileExplorerIntent
}

sealed interface FileExplorerSideEffect {
    data class ShowError(val message: String) : FileExplorerSideEffect
    data object FileSelected : FileExplorerSideEffect
}

@HiltViewModel
class FileExplorerViewModel @Inject constructor(
    private val selectFileUseCase: SelectFileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<FileExplorerUiState>(FileExplorerUiState.Loading)
    val uiState: StateFlow<FileExplorerUiState> = _uiState.asStateFlow()

    private val _selectedFile = MutableStateFlow<FileNode?>(null)
    val selectedFile: StateFlow<FileNode?> = _selectedFile.asStateFlow()

    private val _currentPath = MutableStateFlow("")
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()

    private val _sideEffects = MutableSharedFlow<FileExplorerSideEffect>()
    val sideEffects: SharedFlow<FileExplorerSideEffect> = _sideEffects.asSharedFlow()

    private val backStack = mutableListOf<String>()
    private var allFiles = mutableListOf<FileNode>()
    private val rootUri = Environment.getExternalStorageDirectory().absolutePath

    init {
        loadDirectory(rootUri)
    }

    fun onIntent(intent: FileExplorerIntent) {
        when (intent) {
            is FileExplorerIntent.LoadDirectory -> loadDirectory(intent.uri)
            is FileExplorerIntent.NavigateTo -> navigateTo(intent.path)
            is FileExplorerIntent.NavigateUp -> navigateUp()
            is FileExplorerIntent.SelectFile -> selectFile(intent.file)
            is FileExplorerIntent.FilterByCategory -> filterByCategory(intent.category)
        }
    }

    private fun loadDirectory(uri: String) {
        viewModelScope.launch {
            _uiState.value = FileExplorerUiState.Loading
            _currentPath.value = uri

            selectFileUseCase.getFiles(uri)
                .catch { e ->
                    Timber.e(e, "Error al cargar directorio $uri")
                    _uiState.value = FileExplorerUiState.Error(
                        message = "No se pudieron cargar los archivos: ${e.localizedMessage ?: "Error desconocido"}"
                    )
                }
                .collect { files ->
                    allFiles = files.sortedWith(
                        compareByDescending<FileNode> { it.isDirectory }
                            .thenBy { it.name.lowercase() }
                    ).toMutableList()

                    val currentCategory = (_uiState.value as? FileExplorerUiState.Files)?.selectedCategory
                    applyCategoryFilter(currentCategory, uri)
                }
        }
    }

    private fun navigateTo(path: String) {
        backStack.add(_currentPath.value)
        loadDirectory(path)
    }

    private fun navigateUp() {
        if (backStack.isNotEmpty()) {
            val previousPath = backStack.removeLast()
            loadDirectory(previousPath)
        } else if (_currentPath.value != rootUri) {
            loadDirectory(rootUri)
        }
    }

    private fun filterByCategory(category: FileCategory?) {
        applyCategoryFilter(category, _currentPath.value)
    }

    private fun applyCategoryFilter(category: FileCategory?, uri: String) {
        val filtered = if (category == null || category == FileCategory.FOLDER) {
            allFiles
        } else {
            allFiles.filter { it.category == category }
        }

        _uiState.value = if (filtered.isEmpty()) {
            FileExplorerUiState.Empty
        } else {
            FileExplorerUiState.Files(
                files = filtered,
                currentPath = uri,
                selectedCategory = category,
                allFiles = allFiles
            )
        }
    }

    private fun selectFile(file: FileNode) {
        _selectedFile.value = file
        viewModelScope.launch {
            _sideEffects.emit(FileExplorerSideEffect.FileSelected)
        }
        Timber.d("Archivo seleccionado: ${file.name} en ${file.path}")
    }
}