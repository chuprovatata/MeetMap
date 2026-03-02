package com.example.datingapp.screens.places

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.datingapp.R
import com.example.datingapp.data.models.PlaceInfo
import com.example.datingapp.navigation.Screen
import com.example.datingapp.viewmodels.UserPlacesViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.background
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import kotlinx.coroutines.flow.collectLatest
import coil.decode.SvgDecoder
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import kotlin.math.abs
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesOfDayScreen(
    navController: NavController,
    isFirstEntry: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = Firebase.firestore
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val userPlacesViewModel: UserPlacesViewModel = hiltViewModel()

    val isLiking by userPlacesViewModel.isLiking.collectAsState()
    val likeResult by userPlacesViewModel.likeResult.collectAsState()
    val errorMessage by userPlacesViewModel.errorMessage.collectAsState()
    val likedPlaces by userPlacesViewModel.likedPlaces.collectAsState()

    var placesOfDay by remember { mutableStateOf<List<PlaceInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessageLoad by remember { mutableStateOf<String?>(null) }

    val hasShownFeedback = remember { mutableStateOf(false) }

    val likedPlaceIds = remember(likedPlaces) {
        likedPlaces.map { it.placeId }.toSet()
    }

    LaunchedEffect(Unit) {
        loadPlacesOfDay(db) { loadedPlaces, error ->
            placesOfDay = loadedPlaces
            isLoading = false
            errorMessageLoad = error
            userPlacesViewModel.loadLikedPlaces()
        }
    }

    LaunchedEffect(likeResult) {
        likeResult?.onSuccess { userPlace ->
            scope.launch {
                val message = if (userPlace.status == "liked") {
                    "Место добавлено в избранное"
                } else {
                    "Место удалено из избранного"
                }
                snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
            }
            userPlacesViewModel.clearLikeResult()
        }?.onFailure { error ->
            scope.launch {
                snackbarHostState.showSnackbar("Ошибка: ${error.message}", duration = SnackbarDuration.Short)
            }
            userPlacesViewModel.clearLikeResult()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            }
            userPlacesViewModel.clearError()
        }
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f
    ) {
        placesOfDay.size
    }

    var lastDragDirection by remember { mutableStateOf(0f) }
    var lastDragTime by remember { mutableStateOf(0L) }

    var photoHeight by remember { mutableStateOf(380.dp) }
    val minPhotoHeight = 200.dp
    val maxPhotoHeight = 380.dp

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y

                val newHeight = (photoHeight + delta.dp * 0.3f).coerceIn(minPhotoHeight, maxPhotoHeight)

                return if (newHeight != photoHeight) {
                    photoHeight = newHeight
                    Offset(0f, delta)
                } else {
                    Offset.Zero
                }
            }
        }
    }

    fun navigateToNextPage() {
        scope.launch {
            if (pagerState.currentPage == placesOfDay.size - 1) {
                if (!hasShownFeedback.value) {
                    hasShownFeedback.value = true
                    navController.navigate("feedback_after_places_of_day/${isFirstEntry}")
                }
            } else {
                val nextPage = pagerState.currentPage + 1
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    fun navigateToPreviousPage() {
        scope.launch {
            if (pagerState.currentPage > 0) {
                val prevPage = pagerState.currentPage - 1
                pagerState.animateScrollToPage(prevPage)
            }
        }
    }

    val globalHorizontalDragModifier = Modifier.pointerInput(Unit) {
        detectHorizontalDragGestures(
            onDragStart = {
                lastDragDirection = 0f
            },
            onDragEnd = {
                if (abs(lastDragDirection) > 30f) {
                    if (lastDragDirection > 0) {
                        navigateToPreviousPage()
                    } else {
                        navigateToNextPage()
                    }
                }
                lastDragDirection = 0f
            },
            onDragCancel = {
                lastDragDirection = 0f
            },
            onHorizontalDrag = { change, dragAmount ->
                change.consume()
                lastDragDirection += dragAmount
            }
        )
    }

    suspend fun updatePlaceLikesCount(placeId: String, increment: Boolean) {
        try {
            val placeRef = db.collection("places_info").document(placeId)

            if (increment) {
                placeRef.update("likesCount", FieldValue.increment(1)).await()
                Log.d("PLACES_DAY", "Incremented likesCount for place $placeId")
            } else {
                placeRef.update("likesCount", FieldValue.increment(-1)).await()
                Log.d("PLACES_DAY", "Decremented likesCount for place $placeId")
            }

            placesOfDay = placesOfDay.map { place ->
                if (place.id == placeId) {
                    place.copy(likesCount = place.likesCount + (if (increment) 1 else -1))
                } else {
                    place
                }
            }

        } catch (e: Exception) {
            Log.e("PLACES_DAY", "Error updating likesCount", e)
        }
    }

    fun onLikeButtonClick(placeId: String) {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            scope.launch {
                snackbarHostState.showSnackbar("Необходимо авторизоваться", duration = SnackbarDuration.Short)
            }
            return
        }

        val wasLiked = placeId in likedPlaceIds

        scope.launch {
            if (wasLiked) {
                userPlacesViewModel.unlikePlace(placeId)
                updatePlaceLikesCount(placeId, false)
            } else {
                userPlacesViewModel.likePlace(placeId, "places_of_day")
                updatePlaceLikesCount(placeId, true)
            }
        }
    }

    fun onBackClick() {
        navController.popBackStack()
    }

    fun getRarityInfo(rarity: String): Pair<Color, String> {
        return when (rarity) {
            PlaceInfo.RARITY_RARE -> Pair(Color(0xFF4CAF50), "Редкое")
            PlaceInfo.RARITY_EPIC -> Pair(Color(0xFF9C27B0), "Эпическое")
            PlaceInfo.RARITY_UNIQUE -> Pair(Color(0xFFFFC107), "Уникальное")
            else -> Pair(Color.Transparent, "")
        }
    }

    fun formatDistance(distanceInKm: Double): String {
        return if (distanceInKm >= 1.0) {
            val formatter = DecimalFormat("#.#")
            val formatted = formatter.format(distanceInKm)
            val cleanNumber = if (formatted.endsWith(".0")) {
                formatted.substring(0, formatted.length - 2)
            } else {
                formatted
            }
            "$cleanNumber км"
        } else {
            val meters = (distanceInKm * 1000).toInt()
            "$meters м"
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 6.dp)
                    .padding(top = 40.dp, bottom = 20.dp)
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Места дня",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { onBackClick() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Назад"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                    modifier = Modifier.height(56.dp)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .nestedScroll(nestedScrollConnection)
                .then(globalHorizontalDragModifier)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (errorMessageLoad != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = "Ошибка",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(text = "Ошибка загрузки", style = MaterialTheme.typography.titleLarge)
                        Text(text = errorMessageLoad ?: "Неизвестная ошибка", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(
                            onClick = {
                                isLoading = true
                                scope.launch {
                                    loadPlacesOfDay(db) { loadedPlaces, error ->
                                        placesOfDay = loadedPlaces
                                        isLoading = false
                                        errorMessageLoad = error
                                    }
                                }
                            }
                        ) {
                            Text("Повторить")
                        }
                    }
                }
            } else if (placesOfDay.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_location),
                            contentDescription = "Нет мест",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(text = "Нет мест дня", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "Вернитесь позже",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    HorizontalPager(
                        state = pagerState,
                        userScrollEnabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(photoHeight)
                    ) { page ->
                        val place = placesOfDay[page]
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(MaterialTheme.shapes.medium)
                        ) {
                            AsyncImage(
                                model = place.photoUrl,
                                contentDescription = place.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        IconButton(
                            onClick = { navigateToPreviousPage() },
                            modifier = Modifier.size(48.dp),
                            enabled = pagerState.currentPage > 0
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_chevron_left),
                                contentDescription = "Предыдущее место",
                                modifier = Modifier.size(24.dp),
                                tint = if (pagerState.currentPage > 0)
                                    MaterialTheme.colorScheme.primary
                                else
                                    Color.Gray
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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

                        IconButton(
                            onClick = { navigateToNextPage() },
                            modifier = Modifier.size(48.dp),
                            enabled = true
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_chevron_right),
                                contentDescription = "Следующее место",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (placesOfDay.isNotEmpty()) {
                        val currentPlace = placesOfDay[pagerState.currentPage]
                        val isCurrentPlaceLiked = currentPlace.id in likedPlaceIds
                        val (rarityColor, rarityText) = getRarityInfo(currentPlace.rarity)
                        val shouldShowRarity = currentPlace.rarity !in listOf(PlaceInfo.RARITY_COMMON, PlaceInfo.RARITY_UNCOMMON)

                        val scrollState = rememberScrollState()

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(scrollState)
                                .padding(horizontal = 16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = currentPlace.name,
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.weight(1f)
                                )

                                if (shouldShowRarity) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(rarityColor.copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = rarityText,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = rarityColor
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            androidx.compose.foundation.layout.FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                currentPlace.categories.forEach { category ->
                                    FilterChip(
                                        selected = false,
                                        onClick = { },
                                        label = { Text(text = category, fontSize = 12.sp) }
                                    )
                                }
                            }

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

                            if (currentPlace.metroStation.isNotBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                ) {
                                    if (currentPlace.metroLine.isNotBlank()) {
                                        val metroLineImageUrl = "https://storage.yandexcloud.net/meetmap/metrostation/Moskwa_Metro_Line_${currentPlace.metroLine}.svg"
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(metroLineImageUrl)
                                                .decoderFactory(SvgDecoder.Factory())
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "Линия метро ${currentPlace.metroLine}",
                                            modifier = Modifier.size(20.dp),
                                            error = painterResource(id = R.drawable.icon_subway)
                                        )
                                    } else {
                                        Icon(
                                            painter = painterResource(id = R.drawable.icon_subway),
                                            contentDescription = "Метро",
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = buildString {
                                            append(currentPlace.metroStation)
                                            if (currentPlace.distanceToMetro > 0) {
                                                append(" • ${formatDistance(currentPlace.distanceToMetro)}")
                                            }
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                )
                            ) {
                                Text(
                                    text = if (!currentPlace.description.isNullOrBlank()) {
                                        currentPlace.description
                                    } else {
                                        "Описание временно отсутствует. Скоро мы добавим информацию об этом месте."
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }

                if (placesOfDay.isNotEmpty()) {
                    val currentPlace = placesOfDay[pagerState.currentPage]
                    val isCurrentPlaceLiked = currentPlace.id in likedPlaceIds

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(
                                Color.White.copy(alpha = 0.95f),
                                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Button(
                            onClick = { onLikeButtonClick(currentPlace.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = MaterialTheme.shapes.medium,
                            enabled = !isLiking,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCurrentPlaceLiked) {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                contentColor = if (isCurrentPlaceLiked) {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                } else {
                                    Color.White
                                },
                                disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        ) {
                            if (isLiking) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = if (isCurrentPlaceLiked) {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    } else {
                                        Color.White
                                    }
                                )
                            } else {
                                Text(
                                    text = if (isCurrentPlaceLiked) "В ИЗБРАННОМ" else "МНЕ НРАВИТСЯ",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = if (isCurrentPlaceLiked) {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    } else {
                                        Color.White
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun loadPlacesOfDay(
    db: FirebaseFirestore,
    onComplete: (List<PlaceInfo>, String?) -> Unit
) {
    try {
        val snapshot = db.collection("places_info")
            .whereEqualTo("place_ofday", true)
            .get()
            .await()

        val places = snapshot.documents.mapNotNull { doc ->
            try {
                val data = doc.data ?: return@mapNotNull null

                val photoUrl = when {
                    data.containsKey("photoUrl") -> data["photoUrl"] as? String ?: ""
                    data.containsKey("photo_url") -> data["photo_url"] as? String ?: ""
                    else -> ""
                }

                PlaceInfo(
                    id = (data["id"] as? String ?: doc.id),
                    name = data["name"] as? String ?: "",
                    address = data["address"] as? String ?: "",
                    latitude = (data["latitude"] as? Double) ?: 0.0,
                    longitude = (data["longitude"] as? Double) ?: 0.0,
                    metroStation = data["metroStation"] as? String ?: "",
                    metroLine = data["metroLine"] as? String ?: "",
                    distanceToMetro = (data["distanceToMetro"] as? Double) ?: 0.0,
                    photoUrl = photoUrl,
                    categories = (data["categories"] as? List<String>) ?: emptyList(),
                    likesCount = (data["likesCount"] as? Long)?.toInt() ?: (data["likes_count"] as? Long)?.toInt() ?: 0,
                    hasFireIcon = data["hasFireIcon"] as? Boolean ?: data["fire_icon"] as? Boolean ?: false,
                    place_ofday = data["place_ofday"] as? Boolean ?: false,
                    uniqueId = data["unique_id"] as? String ?: "",
                    rarity = data["rarity"] as? String ?: "common",
                    description = data["description"] as? String ?: "",
                    createdAt = data["created_at"] as? com.google.firebase.Timestamp,
                    updatedAt = data["updated_at"] as? com.google.firebase.Timestamp
                )
            } catch (e: Exception) {
                null
            }
        }

        onComplete(places, null)
    } catch (e: Exception) {
        onComplete(emptyList(), "Ошибка загрузки: ${e.message}")
    }
}