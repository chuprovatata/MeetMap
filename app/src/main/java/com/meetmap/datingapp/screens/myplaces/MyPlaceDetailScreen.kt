package com.meetmap.datingapp.screens.myplaces

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

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
import coil.decode.SvgDecoder
import com.meetmap.datingapp.R
import com.meetmap.datingapp.components.map.OnePointMap
import com.meetmap.datingapp.data.models.AppUser
import com.meetmap.datingapp.data.models.PlaceInfo
import com.meetmap.datingapp.navigation.Screen
import com.meetmap.datingapp.screens.feedback.FeedbackAfterPlaceDeleted
import com.meetmap.datingapp.viewmodels.FeedbackViewModel
import com.meetmap.datingapp.viewmodels.MyPlaceDetailViewModel
import com.meetmap.datingapp.viewmodels.UserViewModel

import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPlaceDetailScreen(
    placeId: String,
    navController: NavController,
    viewModel: MyPlaceDetailViewModel = hiltViewModel(),
    userViewModel: UserViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val placeInfo by viewModel.placeInfo.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLiking by viewModel.isLiking.collectAsState()
    val isLiked by viewModel.isLiked.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Feedback ViewModel
    val feedbackViewModel: FeedbackViewModel = hiltViewModel()

    // Данные о пользователях
    val usersCount by viewModel.usersCount.collectAsState()
    val users by viewModel.users.collectAsState()
    val isLoadingUsers by viewModel.isLoadingUsers.collectAsState()
    val hasMoreUsers by viewModel.hasMoreUsers.collectAsState()

    // Состояния для диалога фидбека при удалении
    var showDeleteFeedbackDialog by remember { mutableStateOf(false) }
    var currentPlaceForDeleteFeedback by remember { mutableStateOf<PlaceInfo?>(null) }

    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    //для карты
    var isMapInteracting by remember { mutableStateOf(false) }

    LaunchedEffect(placeId) {
        viewModel.loadPlaceDetails(placeId)
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    fun onToggleLike(place: PlaceInfo) {
        viewModel.toggleLike(place.id)

        // Если место было в избранном и мы его удаляем (unlike)
        if (isLiked) {
            currentPlaceForDeleteFeedback = place
            showDeleteFeedbackDialog = true
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = placeInfo?.name ?: "Детали места",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(Screen.MyPlaces.route) {
                            popUpTo(0) { inclusive = false }
                            launchSingleTop = true
                        }

                    }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (placeInfo == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_location),
                        contentDescription = "Место не найдено",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Место не найдено",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        navController.navigate(Screen.MyPlaces.route) {
                            popUpTo(0) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                    ) {
                        Text("Вернуться к списку")
                    }
                }
            }
        } else {
            val place = placeInfo!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState, enabled = !isMapInteracting)
            ) {
                // Фото места
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(place.photoUrl.ifEmpty { null })
                            .crossfade(true)
                            .build(),
                        contentDescription = place.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.picture_museum_background),
                        placeholder = painterResource(id = R.drawable.picture_museum_background)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.7f)
                                    )
                                )
                            )
                    )

                    Text(
                        text = place.name,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    )

                    if (place.rarity !in listOf("common", "uncommon")) {
                        val rarityColor = when (place.rarity) {
                            "rare" -> Color(0xFF4CAF50)
                            "epic" -> Color(0xFF9C27B0)
                            "unique" -> Color(0xFFFFC107)
                            else -> MaterialTheme.colorScheme.primary
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(rarityColor.copy(alpha = 0.9f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = when (place.rarity) {
                                    "rare" -> "Редкое"
                                    "epic" -> "Эпическое"
                                    "unique" -> "Уникальное"
                                    else -> place.rarity
                                },
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Адрес
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_location),
                            contentDescription = "Адрес",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = place.address,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Метро (с SVG иконкой)
                    if (place.metroStation.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            if (place.metroLine.isNotBlank()) {
                                val metroLineImageUrl =
                                    "https://storage.yandexcloud.net/meetmap/metrostation/Moskwa_Metro_Line_${place.metroLine}.svg"
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(metroLineImageUrl)
                                        .decoderFactory(SvgDecoder.Factory())
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Линия метро ${place.metroLine}",
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
                                    append(place.metroStation)
                                    if (place.distanceToMetro > 0) {
                                        append(" • ${formatDistance(place.distanceToMetro)}")
                                    }
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Категории
                    if (place.categories.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                        ) {
                            place.categories.forEach { category ->
                                AssistChip(
                                    onClick = { },
                                    label = { Text(category, fontSize = 12.sp) }
                                )
                            }
                        }
                    }

                    // Описание
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        )
                    ) {
                        Text(
                            text = if (!place.description.isNullOrBlank()) {
                                place.description
                            } else {
                                "Описание временно отсутствует. Скоро мы добавим информацию об этом месте."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    // карта
                    OnePointMap(
                        text = place.name,
                        latitude = place.latitude,
                        longitude = place.longitude,
                        onInteractionChange = { isMapInteracting = it }
                    )

                    // Блок с количеством пользователей
                    UsersCountCard(count = usersCount)

                    // Список пользователей, добавивших это место
                    if (users.isNotEmpty()) {
                        Text(
                            text = "Кто добавил это место",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            users.forEach { user ->
                                UserItem(
                                    user = user,
                                    onClick = { /* можно оставить пустым или удалить */ },
                                    navController = navController,  // передаем навигацию
                                    userViewModel = userViewModel // получаем UserViewModel
                                )
                            }
                        }

                        // Кнопка "Показать еще"
                        if (hasMoreUsers) {
                            Button(
                                onClick = { viewModel.loadMoreUsers(place.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                enabled = !isLoadingUsers,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                if (isLoadingUsers) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Text("Показать еще")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

            // Кнопка внизу
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Button(
                    onClick = { onToggleLike(place) },  // Используем обновленную функцию
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    enabled = !isLiking,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLiked) {
                            Color(0xFFE0E0E0)
                        } else {
                            Color(0xFFA75CC6)
                        },
                        contentColor = if (isLiked) {
                            Color(0xFF666666)
                        } else {
                            Color.White
                        },
                        disabledContainerColor = Color(0xFFE0E0E0),
                        disabledContentColor = Color(0xFF666666)
                    )
                ) {
                    if (isLiking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = if (isLiked) {
                                Color(0xFF666666)
                            } else {
                                Color.White
                            }
                        )
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (isLiked) R.drawable.icon_star_filled
                                    else R.drawable.icon_star
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = if (isLiked) {
                                    Color(0xFF666666)
                                } else {
                                    Color.White
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isLiked) "В ИЗБРАННОМ" else "МНЕ НРАВИТСЯ",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (isLiked) {
                                    Color(0xFF666666)
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

    // Диалог фидбека при удалении места
    FeedbackAfterPlaceDeleted(
        isOpen = showDeleteFeedbackDialog,
        onDismiss = {
            showDeleteFeedbackDialog = false
            currentPlaceForDeleteFeedback = null
        },
        onSubmit = { selectedOption ->
            currentPlaceForDeleteFeedback?.let { place ->
                feedbackViewModel.savePlaceDeletedFeedback(
                    placeId = place.id,
                    placeName = place.name,
                    deletedReasonOption = selectedOption
                )
            }
            showDeleteFeedbackDialog = false
            currentPlaceForDeleteFeedback = null
        },
        place = currentPlaceForDeleteFeedback
    )
}

// Функция форматирования расстояния
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

@Composable
fun UsersCountCard(
    count: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F0FA)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = count.toString(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7A4FCB),
                modifier = Modifier
                    .width(48.dp)
                    .padding(start = 15.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = "раз пользователи отмечали это место за последние 30 дней",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "посмотри, кто среди них",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun UserItem(
    user: AppUser,
    onClick: () -> Unit,
    navController: NavController,
    userViewModel: UserViewModel
) {
    // Состояние для отслеживания загрузки
    var isLoading by remember { mutableStateOf(false) }
    var isNavigating by remember { mutableStateOf(false) }

    // LaunchedEffect для навигации после загрузки
    LaunchedEffect(isNavigating) {
        if (isNavigating) {
            // Небольшая задержка для завершения загрузки
            kotlinx.coroutines.delay(300)

            // Получаем актуальные данные
            val currentUser = userViewModel.myUser.value
            val friendInfo = currentUser?.friends?.get(user.id)
            val status = friendInfo?.status

            isLoading = false
            isNavigating = false

            Log.d("MyPlaceDetail", "🔍 Статус дружбы с ${user.name}: $status")

            when (status) {
                "friend" -> {
                    navController.navigate("cur_friend/${user.id}")
                }

                else -> {
                    val pageTitle = when (status) {
                        "request" -> "Входящая заявка"
                        "my_application" -> "Исходящая заявка"
                        "deny" -> "Отклоненная заявка"
                        else -> "Профиль"
                    }
                    navController.navigate("req_friend/${user.id}/$pageTitle")
                }
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = !isLoading,
                onClick = {
                    isLoading = true
                    // Принудительно загружаем свежие данные о пользователе
                    userViewModel.forceLoadUserData(user.id)
                    // Запускаем навигацию
                    isNavigating = true
                }
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Аватар
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            if (!user.profileImageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(user.profileImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.icon_person)
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.icon_person),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Индикатор загрузки
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = user.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            if (user.username.isNotBlank()) {
                Text(
                    text = "@${user.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun getLikesWord(count: Int): String {
    return when {
        count % 10 == 1 && count % 100 != 11 -> "лайк"
        count % 10 in 2..4 && (count % 100 !in 12..14) -> "лайка"
        else -> "лайков"
    }
}


