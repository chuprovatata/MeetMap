package com.example.datingapp.screens.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.datingapp.data.firebase.ExcelToFirestoreService
import com.example.datingapp.data.firebase.ImportStatistics
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
    var showStatistics by remember { mutableStateOf(false) }
    var statistics by remember { mutableStateOf<ImportStatistics?>(null) }

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
                showStatistics = false
                statistics = null

                try {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        excelImportService.importCsvToFirestore(
                            inputStream = inputStream,
                            onProgress = { current, total ->
                                currentItem = current
                                totalItems = total
                                progress = if (total > 0) current.toFloat() / total else 0f
                            },
                            onComplete = { success, message, stats ->
                                importInProgress = false
                                statistics = stats
                                showStatistics = true

                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message ?: if (success) "Импорт завершен" else "Ошибка импорта"
                                    )
                                }
                            }
                        )
                    } ?: run {
                        importInProgress = false
                        scope.launch {
                            snackbarHostState.showSnackbar("Не удалось открыть файл")
                        }
                    }
                } catch (e: Exception) {
                    importInProgress = false
                    scope.launch {
                        snackbarHostState.showSnackbar("Ошибка: ${e.message}")
                    }
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Импорт мест из CSV",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (importInProgress) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
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
                        Text("• Колонка A: ID (для существующих мест)")
                        Text("• Колонка C: Название места")
                        Text("• Колонка D: Адрес")
                        Text("• Колонка E: Категория")
                        Text("• Колонка F: Редкость")
                        Text("• Колонка G: Широта")
                        Text("• Колонка H: Долгота")
                        Text("• Колонка I: Описание")
                        Text("• Колонка L: Ближайшее метро")
                        Text("• Колонка M: Расстояние")
                        Text("• Колонка N: Линия метро")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (showStatistics && statistics != null) {
                    StatisticsCard(statistics = statistics!!)
                }
            }
        }
    }
}

@Composable
fun StatisticsCard(statistics: ImportStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (statistics.added > 0 || statistics.updated > 0)
                MaterialTheme.colorScheme.tertiaryContainer
            else
                MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Результаты импорта",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Основная статистика
            StatisticRow("Всего в Firebase:", statistics.totalInFirebase.toString())
            StatisticRow("Найдено в файле:", statistics.totalInFile.toString())
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            StatisticRow("✅ Добавлено:", statistics.added.toString(), color = MaterialTheme.colorScheme.primary)
            StatisticRow("✏️ Обновлено:", statistics.updated.toString(), color = MaterialTheme.colorScheme.primary)
            StatisticRow("⏭️ Пропущено:", statistics.skipped.size.toString(), color = MaterialTheme.colorScheme.error)

            // Список пропущенных мест
            if (statistics.skipped.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Пропущенные места:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(statistics.skipped) { skipped ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "Строка ${skipped.rowNumber}: ${skipped.placeName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = skipped.reason,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatisticRow(label: String, value: String, color: androidx.compose.ui.graphics.Color? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = color ?: MaterialTheme.colorScheme.onSurface
        )
    }
}