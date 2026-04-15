package com.meetmap.datingapp.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.meetmap.datingapp.utils.CloudImageUtils  // ← ИЗМЕНЕНО
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudImagesScreen(
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var cloudImages by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var debugInfo by remember { mutableStateOf("") }

    // Функция загрузки изображений
    fun loadImages() {
        scope.launch {
            isLoading = true
            loadCloudImages { images, info ->
                cloudImages = images
                debugInfo = info
                isLoading = false
            }
        }
    }

    // Загружаем изображения при открытии
    LaunchedEffect(Unit) {
        loadImages()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Изображения в облаке") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { loadImages() },
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Отладочная информация
            if (debugInfo.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = debugInfo,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (cloudImages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Нет изображений в облаке")
                        Text(
                            text = "Загрузите изображения в бакет 'meetmap' на Яндекс.Облаке",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(cloudImages) { imageUrl ->
                        CloudImageItem(imageUrl = imageUrl)
                    }
                }
            }
        }
    }
}

@Composable
fun CloudImageItem(imageUrl: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Изображение из облака",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )

            Text(
                text = imageUrl,
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2
            )
        }
    }
}

// Функция должна быть suspend
private suspend fun loadCloudImages(
    onComplete: (List<String>, String) -> Unit
) {
    try {
        val debugInfo = StringBuilder()
        debugInfo.appendLine("=== ПРОВЕРКА ИЗОБРАЖЕНИЙ В ОБЛАКЕ ===")

        // 1. Получаем список файлов
        val files = CloudImageUtils.listFiles()  // ← ИЗМЕНЕНО
        debugInfo.appendLine("Всего файлов в облаке: ${files.size}")
        debugInfo.appendLine("Файлы: $files")

        // 2. Фильтруем изображения
        val imageFiles = files.filter { fileName ->
            fileName.endsWith(".jpg", ignoreCase = true) ||
                    fileName.endsWith(".jpeg", ignoreCase = true) ||
                    fileName.endsWith(".png", ignoreCase = true) ||
                    fileName.endsWith(".webp", ignoreCase = true)
        }

        debugInfo.appendLine("Изображений найдено: ${imageFiles.size}")

        // 3. Получаем публичные URL
        val images = imageFiles.map { fileName ->
            CloudImageUtils.getPublicUrl(fileName)  // ← ИЗМЕНЕНО
        }

        debugInfo.appendLine("=== ДОСТУПНО ИЗОБРАЖЕНИЙ: ${images.size} ===")

        onComplete(images, debugInfo.toString())
    } catch (e: Exception) {
        val errorInfo = """
            === ОШИБКА ===
            ${e.message}
            ${e.stackTraceToString()}
        """.trimIndent()
        onComplete(emptyList(), errorInfo)
    }
}