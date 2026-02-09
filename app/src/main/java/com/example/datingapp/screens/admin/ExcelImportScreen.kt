package com.example.datingapp.screens.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.datingapp.data.firebase.ExcelToFirestoreService
import kotlinx.coroutines.launch

@Composable
fun ExcelImportScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var importInProgress by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var totalItems by remember { mutableStateOf(0) }
    var currentItem by remember { mutableStateOf(0) }
    var resultMessage by remember { mutableStateOf<String?>(null) }

    val excelImportService = remember {
        ExcelToFirestoreService(context)
    }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                importInProgress = true
                progress = 0f
                resultMessage = null

                try {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        excelImportService.importCsvToFirestore(
                            inputStream = inputStream,
                            onProgress = { current, total ->
                                currentItem = current
                                totalItems = total
                                progress = if (total > 0) current.toFloat() / total else 0f
                            },
                            onComplete = { success, message ->
                                importInProgress = false
                                resultMessage = message

                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message ?: if (success) "Импорт завершен" else "Ошибка импорта"
                                    )
                                }
                            }
                        )
                    } ?: run {
                        importInProgress = false
                        resultMessage = "Не удалось открыть файл"
                    }
                } catch (e: Exception) {
                    importInProgress = false
                    resultMessage = "Ошибка: ${e.message}"
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Импорт мест из CSV",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (importInProgress) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Импортируется: $currentItem из $totalItems",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Button(
                    onClick = { filePicker.launch("text/*") },
                    enabled = !importInProgress,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Выбрать CSV файл")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Как подготовить файл:",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text("1. Откройте Excel файл")
                        Text("2. Файл → Сохранить как → CSV UTF-8")
                        Text("3. Выберите сохраненный CSV файл")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Формат CSV:",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("• Колонка B: Название места")
                        Text("• Колонка C: Адрес")
                        Text("• Колонка D: Категории (через запятую)")
                        Text("• Колонка E: Редкость (Базовое/Среднее/Редкое/Эпическое/Уникальное)")
                        Text("• Колонка F: Широта")
                        Text("• Колонка G: Долгота")
                    }
                }
            }

            resultMessage?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (message.contains("Успешно"))
                            MaterialTheme.colorScheme.tertiaryContainer
                        else
                            MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}