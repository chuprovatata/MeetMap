// app/src/main/java/com/example/datingapp/screens/places/PlacesOfDayScreen.kt
package com.example.datingapp.screens.places

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.datingapp.R
import com.example.datingapp.components.headers.Heading_Arrow
import com.example.datingapp.data.models.PlaceInfo
import com.example.datingapp.navigation.Screen
import com.example.datingapp.utils.CloudImageUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log
import androidx.compose.foundation.background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesOfDayScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = Firebase.firestore
    val scope = rememberCoroutineScope()

    // Состояние для мест дня
    var placesOfDay by remember { mutableStateOf<List<PlaceInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Загружаем места дня из Firestore
    LaunchedEffect(Unit) {
        loadPlacesOfDay(db) { loadedPlaces, error ->
            placesOfDay = loadedPlaces
            isLoading = false
            errorMessage = error

            // Логируем информацию о фотографиях
            loadedPlaces.forEachIndexed { index, place ->
                Log.d("PLACES_DEBUG", "Место $index: ${place.name}")
                Log.d("PLACES_DEBUG", "URL фото: ${place.photoUrl}")
                Log.d("PLACES_DEBUG", "Длина URL: ${place.photoUrl.length}")
            }
        }
    }

    // Состояние пейджера
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f
    ) {
        placesOfDay.size // количество страниц
    }

    // Функция для циклического перелистывания
    fun navigateToNextPage() {
        scope.launch {
            val nextPage = (pagerState.currentPage + 1) % placesOfDay.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    fun navigateToPreviousPage() {
        scope.launch {
            val prevPage = if (pagerState.currentPage - 1 < 0) {
                placesOfDay.size - 1
            } else {
                pagerState.currentPage - 1
            }
            pagerState.animateScrollToPage(prevPage)
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 6.dp)
                    .padding(top = 40.dp, bottom = 20.dp)
            ) {
                Heading_Arrow("Места дня", navController)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                // Индикатор загрузки
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                // Ошибка загрузки
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = "Ошибка",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Ошибка загрузки",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = errorMessage ?: "Неизвестная ошибка",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = {
                                isLoading = true
                                scope.launch {
                                    loadPlacesOfDay(db) { loadedPlaces, error ->
                                        placesOfDay = loadedPlaces
                                        isLoading = false
                                        errorMessage = error
                                    }
                                }
                            }
                        ) {
                            Text("Повторить")
                        }
                    }
                }
            } else if (placesOfDay.isEmpty()) {
                // Нет мест дня
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_location),
                            contentDescription = "Нет мест",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Нет мест дня",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Вернитесь позже или выберите места дня в админке",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                // Горизонтальный пейджер для пролистывания
                // В HorizontalPager исправляем:
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                ) { page ->
                    val place = placesOfDay[page]

                    Log.d("IMAGE_DEBUG", "Загружаем фото для: ${place.name}")
                    Log.d("IMAGE_DEBUG", "URL: ${place.photoUrl}")

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(MaterialTheme.shapes.medium)
                    ) {
                        AsyncImage(
                            model = place.photoUrl,
                            contentDescription = place.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            onLoading = {
                                Log.d("IMAGE_DEBUG", "Загрузка фото для: ${place.name}")
                            },
                            onSuccess = {
                                Log.d("IMAGE_DEBUG", "✅ Успешно загружено фото для: ${place.name}")
                            },
                            onError = { errorState ->
                                Log.e("IMAGE_DEBUG", "❌ Ошибка загрузки фото для: ${place.name}")
                                Log.e("IMAGE_DEBUG", "Ошибка: ${errorState.result.throwable?.message}")
                                errorState.result.throwable?.printStackTrace()
                            }
                        )
                    }
                }

                // Слайдер (стрелки и индикатор)
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Стрелка влево
                    IconButton(
                        onClick = { navigateToPreviousPage() },
                        modifier = Modifier.size(48.dp),
                        enabled = placesOfDay.isNotEmpty()
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_chevron_left),
                            contentDescription = "Предыдущее место",
                            modifier = Modifier.size(24.dp),
                            tint = if (placesOfDay.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }

                    // Индикатор слайдера
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(placesOfDay.size) { index ->
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (index == pagerState.currentPage)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                    )
                            )
                        }
                    }

                    // Стрелка вправо
                    IconButton(
                        onClick = { navigateToNextPage() },
                        modifier = Modifier.size(48.dp),
                        enabled = placesOfDay.isNotEmpty()
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_chevron_right),
                            contentDescription = "Следующее место",
                            modifier = Modifier.size(24.dp),
                            tint = if (placesOfDay.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                }

                // Информация о текущем месте
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    // Название места (обновляется при пролистывании)
                    if (placesOfDay.isNotEmpty()) {
                        val currentPlace = placesOfDay[pagerState.currentPage]

                        Text(
                            text = currentPlace.name,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Теги места (категории)
                        androidx.compose.foundation.layout.FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            currentPlace.categories.forEach { category ->
                                FilterChip(
                                    selected = false,
                                    onClick = { /* фильтр */ },
                                    label = {
                                        Text(
                                            text = category,
                                            fontSize = 12.sp,
                                        )
                                    }
                                )
                            }

                            // Бейдж редкости
                            currentPlace.rarity.takeIf { it != PlaceInfo.RARITY_COMMON }?.let { rarity ->
                                FilterChip(
                                    selected = false,
                                    onClick = { /* фильтр по редкости */ },
                                    label = {
                                        Text(
                                            text = PlaceInfo.rarityDisplayNames[rarity] ?: rarity,
                                            fontSize = 12.sp,
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = when (rarity) {
                                            PlaceInfo.RARITY_UNIQUE -> Color(0xFFFF9800)
                                            PlaceInfo.RARITY_EPIC -> Color(0xFF9C27B0)
                                            PlaceInfo.RARITY_RARE -> Color(0xFF2196F3)
                                            PlaceInfo.RARITY_UNCOMMON -> Color(0xFF4CAF50)
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    )
                                )
                            }
                        }

                        // Адрес
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_location),
                                contentDescription = "Адрес",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentPlace.address,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Метро (если есть)
                        if (currentPlace.metroStation.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 24.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.icon_subway),
                                    contentDescription = "Метро",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = currentPlace.metroStation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }

                // Кнопка «Мне нравится»
                Button(
                    onClick = {
                        // Навигация к экрану "Понравилось место"
                        navController.navigate(Screen.PlaceLiked.route)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    enabled = placesOfDay.isNotEmpty()
                ) {
                    Text(
                        text = "МНЕ НРАВИТСЯ",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

/**
 * Компонент для отображения изображения из облака
 */
// В CloudImageDisplay функции исправляем условие:

@Composable
fun CloudImageDisplay(
    imageUrl: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Log.d("CLOUD_IMAGE", "Загрузка: $imageUrl")

    // Простая проверка
    val finalUrl = if (imageUrl.isNotBlank() && imageUrl.startsWith("http")) {
        imageUrl
    } else {
        null
    }

    if (finalUrl == null) {
        Log.d("CLOUD_IMAGE", "Используем заглушку")
        Image(
            painter = painterResource(R.drawable.picture_museum_background),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
        return
    }

    Log.d("CLOUD_IMAGE", "Пробуем загрузить: $finalUrl")

    AsyncImage(
        model = finalUrl,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        onLoading = {
            Log.d("CLOUD_IMAGE", "Загрузка...")
        },
        onSuccess = {
            Log.d("CLOUD_IMAGE", "✅ Успешно загружено!")
        },
        onError = { errorState ->
            Log.e("CLOUD_IMAGE", "❌ Ошибка загрузки")
            Log.e("CLOUD_IMAGE", "Ошибка: ${errorState.result.throwable?.message}")
        },
        error = painterResource(R.drawable.picture_museum_background),
        placeholder = painterResource(R.drawable.picture_museum_background)
    )
}

// Функция загрузки мест дня из Firestore
// app/src/main/java/com/example/datingapp/screens/places/PlacesOfDayScreen.kt
// Обновляем функцию loadPlacesOfDay:

private suspend fun loadPlacesOfDay(
    db: FirebaseFirestore,
    onComplete: (List<PlaceInfo>, String?) -> Unit
) {
    try {
        Log.d("PLACES_LOAD", "Начало загрузки мест дня...")

        val snapshot = db.collection("places_info")
            .whereEqualTo("place_ofday", true)
            .get()
            .await()

        Log.d("PLACES_LOAD", "Получено документов: ${snapshot.documents.size}")

        val places = snapshot.documents.mapNotNull { doc ->
            try {
                val data = doc.data ?: return@mapNotNull null

                Log.d("PLACES_LOAD", "Документ ${doc.id} данные: ${data.keys}")

                // Ищем photoUrl с большой U (как в базе)
                val photoUrl = when {
                    data.containsKey("photoUrl") -> {
                        val url = data["photoUrl"] as? String ?: ""
                        Log.d("PLACES_LOAD", "Найден photoUrl: $url")
                        url
                    }
                    data.containsKey("photo_url") -> {
                        val url = data["photo_url"] as? String ?: ""
                        Log.d("PLACES_LOAD", "Найден photo_url: $url")
                        url
                    }
                    else -> {
                        Log.d("PLACES_LOAD", "Нет поля photoUrl или photo_url")
                        ""
                    }
                }

                val place = PlaceInfo(
                    id = (data["id"] as? String ?: doc.id),
                    name = data["name"] as? String ?: "",
                    address = data["address"] as? String ?: "",
                    latitude = (data["latitude"] as? Double) ?: 0.0,
                    longitude = (data["longitude"] as? Double) ?: 0.0,
                    metroStation = data["metroStation"] as? String ?: "",
                    photoUrl = photoUrl, // Используем найденный URL
                    categories = (data["categories"] as? List<String>) ?: emptyList(),
                    likesCount = (data["likes_count"] as? Long)?.toInt() ?: 0,
                    hasFireIcon = data["fire_icon"] as? Boolean ?: false,
                    isPlaceOfDay = data["place_ofday"] as? Boolean ?: false,
                    uniqueId = data["unique_id"] as? String ?: "",
                    rarity = data["rarity"] as? String ?: "common",
                    createdAt = data["created_at"] as? com.google.firebase.Timestamp,
                    updatedAt = data["updated_at"] as? com.google.firebase.Timestamp
                )

                Log.d("PLACES_LOAD", "Загружено место: ${place.name}, фото: ${place.photoUrl}")
                Log.d("PLACES_LOAD", "Длина URL фото: ${place.photoUrl.length}")
                Log.d("PLACES_LOAD", "Содержит NO Picture: ${place.photoUrl.contains("NO%20Picture")}")

                place
            } catch (e: Exception) {
                Log.e("PLACES_LOAD", "Ошибка парсинга документа ${doc.id}", e)
                null
            }
        }

        Log.d("PLACES_LOAD", "Успешно загружено мест: ${places.size}")

        onComplete(places, null)
    } catch (e: Exception) {
        Log.e("PLACES_LOAD", "Ошибка загрузки мест дня", e)
        onComplete(emptyList(), "Ошибка загрузки: ${e.message}")
    }
}