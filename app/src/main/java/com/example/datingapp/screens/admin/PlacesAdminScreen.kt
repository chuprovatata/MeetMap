// app/src/main/java/com/example/datingapp/screens/admin/PlacesAdminScreen.kt
package com.example.datingapp.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.datingapp.data.models.PlaceInfo
import com.example.datingapp.navigation.Screen
import com.example.datingapp.viewmodels.PlacesAdminViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesAdminScreen(
    navController: NavController,
    viewModel: PlacesAdminViewModel = hiltViewModel()
) {
    val db = Firebase.firestore
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var places by remember { mutableStateOf<List<PlaceInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var debugInfo by remember { mutableStateOf("") }

    // Загружаем места при первом открытии
    LaunchedEffect(Unit) {
        println("Начало загрузки мест...") // Отладка
        loadPlaces(db, snackbarHostState, scope) { loadedPlaces ->
            places = loadedPlaces
            isLoading = false
            debugInfo = "Загружено мест: ${loadedPlaces.size}"
            println("Загрузка завершена: ${loadedPlaces.size} мест") // Отладка
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Управление местами") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.ExcelImport.route) }) {
                        Icon(Icons.Default.Upload, contentDescription = "Импорт из Excel")
                    }
                    IconButton(onClick = {
                        scope.launch {
                            loadPlaces(db, snackbarHostState, scope) { loadedPlaces ->
                                places = loadedPlaces
                            }
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                    }
                    IconButton(onClick = {
                        scope.launch {
                            fixMissingIds(db, snackbarHostState) {
                                scope.launch {
                                    loadPlaces(db, snackbarHostState, scope) { loadedPlaces ->
                                        places = loadedPlaces
                                    }
                                }
                            }
                        }
                    }) {
                        Icon(Icons.Default.Build, contentDescription = "Исправить ID")
                    }
                    IconButton(onClick = { navController.navigate(Screen.CloudImages.route) }) {
                        Icon(Icons.Default.Cloud, contentDescription = "Облачные изображения")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.ExcelImport.route) },
                icon = { Icon(Icons.Default.Add, contentDescription = "Добавить") },
                text = { Text("Импорт Excel") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Поиск
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Поиск мест...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Поиск") },
                singleLine = true
            )

            // Статистика
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = places.size.toString(),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text("Всего мест", style = MaterialTheme.typography.bodySmall)
                    }

                    Divider(
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = places.count { it.place_ofday }.toString(),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text("Места дня", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Button(
                onClick = {
                    viewModel.sendPlacesOfDayNotification()
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("📢 Разослать уведомление о новых местах дня")
            }

            // Список мест
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filteredPlaces = if (searchQuery.isBlank()) {
                        places
                    } else {
                        places.filter {
                            it.name.contains(searchQuery, ignoreCase = true) ||
                                    it.address.contains(searchQuery, ignoreCase = true) ||
                                    it.categories.any { cat -> cat.contains(searchQuery, ignoreCase = true) }
                        }
                    }

                    items(filteredPlaces) { place ->
                        PlaceAdminCard(
                            place = place,
                            onTogglePlaceOfDay = { place_ofday ->
                                scope.launch {
                                    updatePlaceOfDay(
                                        place.id,
                                        place_ofday,
                                        db,
                                        snackbarHostState
                                    ) {
                                        scope.launch {
                                            loadPlaces(db, snackbarHostState, scope) { loadedPlaces ->
                                                places = loadedPlaces
                                            }
                                        }
                                    }
                                }
                            },
                            onToggleFireIcon = { hasFireIcon ->
                                scope.launch {
                                    updateFireIcon(
                                        place.id,
                                        hasFireIcon,
                                        db,
                                        snackbarHostState
                                    ) {
                                        scope.launch {
                                            loadPlaces(db, snackbarHostState, scope) { loadedPlaces ->
                                                places = loadedPlaces
                                            }
                                        }
                                    }
                                }
                            },
                            onDelete = {
                                scope.launch {
                                    deletePlace(
                                        place.id,
                                        db,
                                        snackbarHostState
                                    ) {
                                        scope.launch {
                                            loadPlaces(db, snackbarHostState, scope) { loadedPlaces ->
                                                places = loadedPlaces
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlaceAdminCard(
    place: PlaceInfo,
    onTogglePlaceOfDay: (Boolean) -> Unit,
    onToggleFireIcon: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )

                // Бейдж редкости
                Text(
                    text = PlaceInfo.rarityDisplayNames[place.rarity] ?: place.rarity,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .background(
                            when (place.rarity) {
                                PlaceInfo.RARITY_UNIQUE -> Color(0xFFFF9800) // Orange
                                PlaceInfo.RARITY_EPIC -> Color(0xFF9C27B0) // Purple
                                PlaceInfo.RARITY_RARE -> Color(0xFF2196F3) // Blue
                                PlaceInfo.RARITY_UNCOMMON -> Color(0xFF4CAF50) // Green
                                else -> Color(0xFF9E9E9E) // Gray
                            },
                            MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            Text(
                text = place.address,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Категории
            Text(
                text = place.categories.joinToString(", "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Кнопки управления
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Место дня
                FilterChip(
                    selected = place.place_ofday,
                    onClick = { onTogglePlaceOfDay(!place.place_ofday) },
                    label = { Text("Место дня") },
                    leadingIcon = if (place.place_ofday) {
                        { Icon(Icons.Default.Star, contentDescription = null) }
                    } else null
                )

                // Огонек
                FilterChip(
                    selected = place.hasFireIcon,
                    onClick = { onToggleFireIcon(!place.hasFireIcon) },
                    label = { Text("Огонек") },
                    leadingIcon = if (place.hasFireIcon) {
                        { Icon(Icons.Default.Whatshot, contentDescription = null) }
                    } else null
                )

                // Удалить
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// app/src/main/java/com/example/datingapp/screens/admin/PlacesAdminScreen.kt
private suspend fun loadPlaces(
    db: FirebaseFirestore,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    onSuccess: (List<PlaceInfo>) -> Unit
) {
    try {
        println("=== ПРОБУЕМ ЗАГРУЗИТЬ МЕСТА ===")

        // Простой запрос без сортировки
        val snapshot = db.collection("places_info")
            .limit(10) // Ограничим для теста
            .get()
            .await()

        println("Получено ${snapshot.documents.size} документов")

        val loadedPlaces = mutableListOf<PlaceInfo>()

        for (doc in snapshot.documents) {
            try {
                println("Документ ID: ${doc.id}")
                println("Данные: ${doc.data}")

                val data = doc.data ?: continue

                val place = PlaceInfo(
                    id = (data["id"] as? String) ?: doc.id,
                    name = (data["name"] as? String) ?: "Без названия",
                    address = (data["address"] as? String) ?: "",
                    latitude = (data["latitude"] as? Double) ?: 0.0,
                    longitude = (data["longitude"] as? Double) ?: 0.0,
                    metroStation = (data["metroStation"] as? String) ?: "",
                    photoUrl = (data["photo_url"] as? String) ?: "",
                    categories = (data["categories"] as? List<String>) ?: emptyList(),
                    likesCount = ((data["likes_count"] as? Number)?.toInt()) ?: 0,
                    hasFireIcon = (data["fire_icon"] as? Boolean) ?: false,
                    place_ofday = (data["place_ofday"] as? Boolean) ?: false,
                    uniqueId = (data["unique_id"] as? String) ?: "",
                    rarity = (data["rarity"] as? String) ?: "common",
                    createdAt = data["created_at"] as? com.google.firebase.Timestamp,
                    updatedAt = data["updated_at"] as? com.google.firebase.Timestamp
                )

                println("Создано место: ${place.name}")
                loadedPlaces.add(place)

            } catch (e: Exception) {
                println("Ошибка обработки документа ${doc.id}: ${e.message}")
            }
        }

        println("=== УСПЕШНО ЗАГРУЖЕНО ${loadedPlaces.size} МЕСТ ===")

        onSuccess(loadedPlaces)

    } catch (e: Exception) {
        println("!!! КРИТИЧЕСКАЯ ОШИБКА ЗАГРУЗКИ: ${e.message}")
        e.printStackTrace()
        scope.launch {
            snackbarHostState.showSnackbar(
                "Ошибка Firebase: ${e.message}",
                withDismissAction = true
            )
        }
    }
}

private suspend fun updatePlaceOfDay(
    placeId: String,
    place_ofday: Boolean,
    db: FirebaseFirestore,
    snackbarHostState: SnackbarHostState,
    onSuccess: () -> Unit
) {
    try {
        db.collection("places_info")
            .document(placeId)
            .update("place_ofday", place_ofday)
            .await()

        snackbarHostState.showSnackbar(
            if (place_ofday) "Добавлено в места дня" else "Убрано из мест дня"
        )

        onSuccess()
    } catch (e: Exception) {
        snackbarHostState.showSnackbar(
            "Ошибка обновления: ${e.message}",
            withDismissAction = true
        )
    }
}

private suspend fun updateFireIcon(
    placeId: String,
    hasFireIcon: Boolean,
    db: FirebaseFirestore,
    snackbarHostState: SnackbarHostState,
    onSuccess: () -> Unit
) {
    try {
        db.collection("places_info")
            .document(placeId)
            .update("fire_icon", hasFireIcon)
            .await()

        snackbarHostState.showSnackbar(
            if (hasFireIcon) "Огонек добавлен" else "Огонек убран"
        )

        onSuccess()
    } catch (e: Exception) {
        snackbarHostState.showSnackbar(
            "Ошибка обновления: ${e.message}",
            withDismissAction = true
        )
    }
}

private suspend fun deletePlace(
    placeId: String,
    db: FirebaseFirestore,
    snackbarHostState: SnackbarHostState,
    onSuccess: () -> Unit
) {
    try {
        db.collection("places_info")
            .document(placeId)
            .delete()
            .await()

        snackbarHostState.showSnackbar("Место удалено")

        onSuccess()
    } catch (e: Exception) {
        snackbarHostState.showSnackbar(
            "Ошибка удаления: ${e.message}",
            withDismissAction = true
        )
    }
}

@Composable
fun FixIdsButton(
    db: FirebaseFirestore,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    onFixComplete: () -> Unit
) {
    var fixing by remember { mutableStateOf(false) }

    Button(
        onClick = {
            scope.launch {
                fixing = true
                fixMissingIds(db, snackbarHostState) {
                    fixing = false
                    onFixComplete()
                }
            }
        },
        enabled = !fixing
    ) {
        if (fixing) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text("Исправить ID")
    }
}

private suspend fun fixMissingIds(
    db: FirebaseFirestore,
    snackbarHostState: SnackbarHostState,
    onComplete: () -> Unit
) {
    try {
        // Получаем все места где id пустое или совпадает с пустой строкой
        val snapshot = db.collection("places_info")
            .whereEqualTo("id", "")
            .get()
            .await()

        var fixedCount = 0

        snapshot.documents.forEach { doc ->
            val currentId = doc.data?.get("id") as? String
            if (currentId.isNullOrEmpty() || currentId == doc.id) {
                db.collection("places_info")
                    .document(doc.id)
                    .update("id", doc.id)
                    .await()
                fixedCount++
            }
        }

        snackbarHostState.showSnackbar(
            "Исправлено $fixedCount записей",
            withDismissAction = true
        )

    } catch (e: Exception) {
        snackbarHostState.showSnackbar(
            "Ошибка исправления: ${e.message}",
            withDismissAction = true
        )
    }

    onComplete()
}



