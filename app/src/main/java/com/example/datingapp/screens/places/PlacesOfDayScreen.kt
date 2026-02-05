package com.example.datingapp.screens.places

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.datingapp.R
import com.example.datingapp.navigation.Screen
import kotlinx.coroutines.launch

// Модель данных для мест
data class Place(
    val id: Int,
    val name: String,
    val imageRes: Int,
    val address: String,
    val metro: String,
    val tags: List<String>
)

// Список тестовых данных
val samplePlaces = listOf(
    Place(
        id = 1,
        name = "Artplay",
        imageRes = R.drawable.picture_museum_background,
        address = "Нижняя Сыромятническая улица, 10",
        metro = "Курская",
        tags = listOf("art", "выставка", "дизайн")
    ),
    Place(
        id = 2,
        name = "Винзавод",
        imageRes = R.drawable.picture_creativity_background,
        address = "4-й Сыромятнический пер., 1/8",
        metro = "Курская",
        tags = listOf("искусство", "галерея", "арт-пространство")
    ),
    Place(
        id = 3,
        name = "Strelka",
        imageRes = R.drawable.picture_bar_background,
        address = "Берсеневская наб., 14",
        metro = "Кропоткинская",
        tags = listOf("бар", "лекции", "образование")
    ),
    Place(
        id = 4,
        name = "ГЭС-2",
        imageRes = R.drawable.picture_sport_background,
        address = "Болотная наб., 15",
        metro = "Полянка",
        tags = listOf("культура", "искусство", "архитектура")
    ),
    Place(
        id = 5,
        name = "Флакон",
        imageRes = R.drawable.picture_shop_background,
        address = "ул. Б. Новодмитровская, 36",
        metro = "Дмитровская",
        tags = listOf("шопинг", "дизайн", "кафе")
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesOfDayScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // Состояние пейджера
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f
    ) {
        samplePlaces.size // количество страниц
    }

    // Корутина для анимированного перелистывания
    val coroutineScope = rememberCoroutineScope()

    // Функция для циклического перелистывания
    fun navigateToNextPage() {
        coroutineScope.launch {
            val nextPage = (pagerState.currentPage + 1) % samplePlaces.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    fun navigateToPreviousPage() {
        coroutineScope.launch {
            val prevPage = if (pagerState.currentPage - 1 < 0) {
                samplePlaces.size - 1
            } else {
                pagerState.currentPage - 1
            }
            pagerState.animateScrollToPage(prevPage)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Места дня",
                        style = MaterialTheme.typography.displaySmall
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.navigate(Screen.Main.route) {
                                popUpTo(0)
                            }
                        }) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_back),
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Горизонтальный пейджер для пролистывания
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            ) { page ->
                val place = samplePlaces[page]

                // Изображение места
                Image(
                    painter = painterResource(id = place.imageRes),
                    contentDescription = place.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
            }

            // Слайдер (стрелки и индикатор)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Стрелка влево - всегда активна благодаря циклическому перелистыванию
                IconButton(
                    onClick = { navigateToPreviousPage() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_chevron_left),
                        contentDescription = "Предыдущее место",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Индикатор слайдера
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(samplePlaces.size) { index ->
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

                // Стрелка вправо - всегда активна благодаря циклическому перелистыванию
                IconButton(
                    onClick = { navigateToNextPage() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_chevron_right),
                        contentDescription = "Следующее место",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
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
                val currentPlace = samplePlaces[pagerState.currentPage]

                Text(
                    text = currentPlace.name,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Теги места
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    currentPlace.tags.forEach { tag ->
                        FilterChip(
                            selected = false,
                            onClick = { /* фильтр */ },
                            label = {
                                Text(
                                    text = tag,
                                    fontSize = 12.sp,
                                )
                            }
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

                // Метро
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
                        text = currentPlace.metro,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                shape = MaterialTheme.shapes.medium
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