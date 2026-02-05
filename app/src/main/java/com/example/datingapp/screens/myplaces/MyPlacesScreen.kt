package com.example.datingapp.screens.myplaces

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.datingapp.R
import com.example.datingapp.components.places.PlaceGridItem
import com.example.datingapp.navigation.Screen

// Модель данных для сетки мест
data class GridPlace(
    val id: Int,
    val name: String,
    val imageRes: Int,
    val likes: Int,
    val hasFireIcon: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPlacesScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // Состояние для выбранной вкладки
    var selectedTab by remember { mutableStateOf(0) }

    // Данные для вкладок
    val tabs = listOf("Нравится", "В планах")

    // Тестовые данные
    val gridPlaces = listOf(
        GridPlace(1, "Artplay", R.drawable.picture_museum_background, 156, hasFireIcon = true),
        GridPlace(2, "Strelka", R.drawable.picture_bar_background, 89),
        GridPlace(3, "Винзавод", R.drawable.picture_creativity_background, 203),
        GridPlace(4, "Парк Горького", R.drawable.picture_park_background, 142),
        GridPlace(5, "ГЭС-2", R.drawable.picture_sport_background, 67),
        GridPlace(6, "Музей", R.drawable.picture_entertaiment_background, 94, hasFireIcon = true),
        GridPlace(7, "Флакон", R.drawable.picture_shop_background, 178),
        GridPlace(8, "Красный Октябрь", R.drawable.picture_museum_background, 121)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Мои места",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                actions = {
                    // Кнопка настроек (фиолетовый контур)
                    OutlinedIconButton(
                        onClick = { navController.navigate(Screen.Settings.route) },
                        modifier = Modifier.size(40.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp
                        ),
                        colors = IconButtonDefaults.outlinedIconButtonColors(
                            contentColor = Color(0xFFA75CC6),
                            containerColor = Color.Transparent
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Настройки",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Кнопка профиля (фиолетовый контур)
                    OutlinedIconButton(
                        onClick = { navController.navigate(Screen.Profile.route) },
                        modifier = Modifier.size(40.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp
                        ),
                        colors = IconButtonDefaults.outlinedIconButtonColors(
                            contentColor = Color(0xFFA75CC6),
                            containerColor = Color.Transparent
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Профиль",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )
        },
        bottomBar = {
            // Нижнее меню навигации
            NavigationBar(
                containerColor = Color.White,
                contentColor = Color(0xFFA75CC6)
            ) {
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Знакомства",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            "Знакомства",
                            fontSize = 10.sp
                        )
                    },
                    selected = false,
                    onClick = {
                        navController.navigate(Screen.Dating.route)
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFA75CC6),
                        selectedTextColor = Color(0xFFA75CC6),
                        unselectedIconColor = Color(0xFF888888),
                        unselectedTextColor = Color(0xFF888888),
                        indicatorColor = Color.Transparent
                    )
                )

                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Мои места",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            "Мои места",
                            fontSize = 10.sp
                        )
                    },
                    selected = true,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFA75CC6),
                        selectedTextColor = Color(0xFFA75CC6),
                        unselectedIconColor = Color(0xFF888888),
                        unselectedTextColor = Color(0xFF888888),
                        indicatorColor = Color.Transparent
                    )
                )

                NavigationBarItem(
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_friends),
                            contentDescription = "Друзья",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            "Друзья",
                            fontSize = 10.sp
                        )
                    },
                    selected = false,
                    onClick = {
                        navController.navigate(Screen.Friends.route)
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFA75CC6),
                        selectedTextColor = Color(0xFFA75CC6),
                        unselectedIconColor = Color(0xFF888888),
                        unselectedTextColor = Color(0xFF888888),
                        indicatorColor = Color.Transparent
                    )
                )
            }
        },
        floatingActionButton = {
            // Кнопка добавления (фиолетовый круг с плюсом)
            FloatingActionButton(
                onClick = {
                    // Добавить новое место
                },
                containerColor = Color(0xFFA75CC6),
                contentColor = Color.White,
                modifier = Modifier
                    .size(56.dp)
                    .padding(bottom = 72.dp) // Поднимаем над нижним меню
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_plus),
                    contentDescription = "Добавить место",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Поисковая строка (овал, бледно-фиолетовый)
            var searchText by remember { mutableStateOf("") }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                TextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    placeholder = { Text("") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Поиск",
                            tint = Color(0xFF888888)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF0E3F6),
                        unfocusedContainerColor = Color(0xFFF0E3F6),
                        disabledContainerColor = Color(0xFFF0E3F6),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )
            }

            // Вкладки "Нравится" / "В планах"
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.Transparent,
                contentColor = Color(0xFFA75CC6),
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        height = 3.dp,
                        color = Color(0xFFA75CC6)
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 16.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        }
                    )
                }
            }

            // Сетка мест (2 столбца)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(gridPlaces) { place ->
                    PlaceGridItem(
                        placeName = place.name,
                        imageRes = place.imageRes,
                        likesCount = place.likes,
                        onClick = {
                            // Навигация к деталям места
                        }
                    )
                }
            }
        }
    }
}