package com.meetmap.datingapp.screens.myplaces

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.meetmap.datingapp.R
import com.meetmap.datingapp.components.headers.Heading
import com.meetmap.datingapp.data.models.PlaceInfo
import com.meetmap.datingapp.navigation.Screen
import com.meetmap.datingapp.viewmodels.MyPlacesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPlacesScreen(
    navController: NavController,  // Глобальный контроллер
    localNavController: NavController, // Локальный контроллер (для совместимости)
    modifier: Modifier = Modifier,
    viewModel: MyPlacesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    val combinedPlaces by viewModel.combinedPlaces.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var searchText by remember { mutableStateOf("") }

    val filteredPlaces = if (searchText.isBlank()) {
        combinedPlaces
    } else {
        combinedPlaces.filter { (_, placeInfo) ->
            placeInfo?.name?.contains(searchText, ignoreCase = true) == true ||
                    placeInfo?.address?.contains(searchText, ignoreCase = true) == true
        }
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.refresh()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

        //PullToRefresh(
        //isRefreshing = isLoading,
        //onRefresh = { viewModel.refresh() },
        //hasItems = combinedPlaces.isNotEmpty()
    //) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .padding(top = 40.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 9.dp, end = 19.dp)
                    ) {
                        Heading(
                            heading = "Мои места",
                            showBackButton = false,
                            showSettings = false,
                            showProfile = true,
                            onBackClick = {
                                // Используем глобальный контроллер для возврата на главную
                                navController.navigate(Screen.Main.route) {
                                    popUpTo(0)
                                }
                            },
                            navController = navController // Передаем глобальный контроллер
                        )
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { },
                    containerColor = Color(0xFFA75CC6),
                    contentColor = Color.White,
                    modifier = Modifier
                        .size(56.dp)
                        .padding(bottom = 72.dp)
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    BasicTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        decorationBox = { innerTextField ->
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFF0E3F6), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Поиск",
                                    modifier = Modifier.size(20.dp),
                                    tint = Color(0xFF888888)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (searchText.isEmpty()) {
                                        Text(
                                            text = "Поиск мест...",
                                            color = Color(0xFF888888),
                                            fontSize = 14.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        }
                    )
                }

                if (isLoading && combinedPlaces.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (filteredPlaces.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.cloud),
                                contentDescription = "",
                                modifier = Modifier.size(200.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (searchText.isNotBlank()) "Ничего не найдено(" else "У тебя пока нет сохраненных мест(",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.Gray,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center
                            )

                            if (searchText.isBlank()) {
                                Text(
                                    text = buildAnnotatedString {
                                        append("Отмечай понравившиеся места в подборке ")
                                        withStyle(style = SpanStyle(
                                            color = Color(0xFFA75CC6),
                                            fontWeight = FontWeight.Bold
                                        )) {
                                            append("Места дня")
                                        }
                                        append(" — и они появятся здесь!")
                                    },
                                    color = Color.Gray,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                )
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredPlaces) { (userPlace, placeInfo) ->
                            if (placeInfo != null) {
                                MyPlaceGridItem(
                                    placeInfo = placeInfo,
                                    onClick = {
                                        // ИСПРАВЛЕНО: используем правильный маршрут и глобальный контроллер
                                        navController.navigate("myPlaceDetail/${placeInfo.id}")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
//}


@Composable
fun MyPlaceGridItem(
    placeInfo: PlaceInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(placeInfo.photoUrl.ifEmpty { null })
                    .crossfade(true)
                    .build(),
                contentDescription = placeInfo.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.picture_museum_background),
                placeholder = painterResource(id = R.drawable.picture_museum_background)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
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
                text = placeInfo.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            )

            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFA75CC6))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_star),
                    contentDescription = "Лайки",
                    modifier = Modifier.size(12.dp),
                    tint = Color.White
                )
                Text(
                    text = placeInfo.likesCount.toString(),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (placeInfo.hasFireIcon) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_fire),
                    contentDescription = "Популярное",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(20.dp),
                    tint = Color(0xFFFF5722)
                )
            }
        }
    }
}

@Composable
fun PullToRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    hasItems: Boolean,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        content()
        if (isRefreshing && hasItems) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.5f))
                    .clickable(enabled = false) { },
                contentAlignment = Alignment.TopCenter
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}