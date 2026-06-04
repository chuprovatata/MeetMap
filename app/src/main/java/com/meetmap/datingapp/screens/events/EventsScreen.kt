package com.meetmap.datingapp.screens.events

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.meetmap.datingapp.R
import com.meetmap.datingapp.components.headers.Heading_Arrow
import com.meetmap.datingapp.components.segmentedButton.CustomTabsComponent
import com.meetmap.datingapp.data.models.EventDateSlot
import com.meetmap.datingapp.data.models.EventInfo
import com.meetmap.datingapp.data.models.EventStatus
import com.meetmap.datingapp.navigation.Screen
import com.meetmap.datingapp.ui.theme.PurpleCard
import com.meetmap.datingapp.ui.theme.PurpleMedium
import com.meetmap.datingapp.viewmodels.EventsViewModel

@Composable
fun EventsScreen(
    navController: NavController,
    viewModel: EventsViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchText by remember { mutableStateOf("") }
    var showUnpublished by remember { mutableStateOf(false) }

    val approvedEvents by viewModel.approvedEvents.collectAsState()
    val myEvents by viewModel.myEvents.collectAsState()
    val myUnpublishedEvents by viewModel.myUnpublishedEvents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val draftSavedState = savedStateHandle
        ?.getStateFlow("event_draft_saved", false)
        ?.collectAsState()
    val eventSubmittedState = savedStateHandle
        ?.getStateFlow("event_submitted", false)
        ?.collectAsState()

    val draftSaved = draftSavedState?.value == true
    val eventSubmitted = eventSubmittedState?.value == true

    LaunchedEffect(Unit) {
        viewModel.loadEvents()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadEvents()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(errorMessage) {
        val message = errorMessage ?: return@LaunchedEffect

        snackbarHostState.showSnackbar(message)
        viewModel.clearErrorMessage()
    }

    LaunchedEffect(successMessage) {
        val message = successMessage ?: return@LaunchedEffect

        snackbarHostState.showSnackbar(message)
        viewModel.clearSuccessMessage()
    }

    LaunchedEffect(draftSaved) {
        if (draftSaved) {
            snackbarHostState.showSnackbar("Мероприятие сохранено в черновики")

            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("event_draft_saved", false)

            viewModel.loadEvents()
        }
    }

    LaunchedEffect(eventSubmitted) {
        if (eventSubmitted) {
            snackbarHostState.showSnackbar("Мероприятие отправлено на модерацию")

            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("event_submitted", false)

            viewModel.loadEvents()
        }
    }

    val eventsForSelectedTab = when {
        selectedTab == 0 -> approvedEvents
        selectedTab == 1 && showUnpublished -> myUnpublishedEvents
        selectedTab == 1 -> myEvents
        else -> approvedEvents
    }

    val filteredEvents = if (searchText.isBlank()) {
        eventsForSelectedTab
    } else {
        eventsForSelectedTab.filter { event ->
            event.title.contains(searchText, ignoreCase = true) ||
                    event.description.contains(searchText, ignoreCase = true) ||
                    event.address.contains(searchText, ignoreCase = true) ||
                    event.dates.any { slot ->
                        slot.dateFrom.contains(searchText, ignoreCase = true) ||
                                slot.dateTo.contains(searchText, ignoreCase = true) ||
                                slot.startTime.contains(searchText, ignoreCase = true)
                    }
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 9.dp, end = 8.dp)
                ) {
                    Heading_Arrow(
                        heading = "Мероприятия",
                        navController = navController
                    )
                }

                EventSearchField(
                    searchText = searchText,
                    onSearchTextChange = { searchText = it }
                )

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CustomTabsComponent(
                        title1 = "Все",
                        title2 = "Мои",
                        icon1 = R.drawable.icon_location,
                        icon2 = R.drawable.icon_star_outline,
                        selectedTab = selectedTab,
                        onTabSelected = { tabIndex ->
                            selectedTab = tabIndex
                            showUnpublished = false
                        }
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.CreateEvent.route)
                },
                containerColor = PurpleCard,
                contentColor = Color.White,
                modifier = Modifier.size(66.dp),
                shape = CircleShape
            ) {
                Text(
                    text = "+",
                    color = Color.White,
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White),
            contentPadding = PaddingValues(
                start = 12.dp,
                end = 12.dp,
                top = 10.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues()
                    .calculateBottomPadding() + 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                EventsHeaderCard(selectedTab = selectedTab)
            }

            if (selectedTab == 1) {
                item {
                    UnpublishedEventsButton(
                        showUnpublished = showUnpublished,
                        count = myUnpublishedEvents.size,
                        onClick = {
                            showUnpublished = !showUnpublished
                        }
                    )
                }
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (filteredEvents.isEmpty()) {
                item {
                    EmptyEvents(
                        selectedTab = selectedTab,
                        searchText = searchText
                    )
                }
            } else {
                items(
                    items = filteredEvents,
                    key = { it.id }
                ) { event ->
                    EventListItem(
                        event = event,
                        showStatus = selectedTab == 1 && event.createdByUserId == currentUserId,
                        onClick = {
                            navController.navigate(
                                Screen.EventDetails.passEventId(event.id)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EventSearchField(
    searchText: String,
    onSearchTextChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        BasicTextField(
            value = searchText,
            onValueChange = onSearchTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = Color.Black,
                fontSize = 14.sp
            ),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = Color(0xFFF0E3F6),
                            shape = RoundedCornerShape(20.dp)
                        )
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
                                text = "Поиск мероприятий...",
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
}

@Composable
private fun EventsHeaderCard(
    selectedTab: Int
) {
    val title = if (selectedTab == 0) {
        "Список\nмероприятий"
    } else {
        "Мои\nмероприятия"
    }

    val subtitle = if (selectedTab == 0) {
        "Всегда найдешь, куда сходить.\nА если нет, то создай\nмероприятие сам!"
    } else {
        "Здесь будут мероприятия,\nкоторые ты создал или\nотметил."
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(154.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(PurpleMedium)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 8.dp, top = 9.dp, end = 30.dp, bottom = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.width(220.dp)
            ) {
                Text(
                    text = title,
                    color = Color.Black,
                    style = MaterialTheme.typography.headlineLarge
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier.width(190.dp)
                ) {
                    Text(
                        text = subtitle,
                        color = Color.Black,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Image(
                painter = painterResource(R.drawable.person_on_board),
                contentDescription = null,
                modifier = Modifier
                    .size(118.dp)
                    .padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun EventListItem(
    event: EventInfo,
    showStatus: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val dateText = event.dates.toDisplayDatesText()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(145.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .width(153.dp)
                .height(136.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 141.dp, height = 128.dp)
                    .align(Alignment.BottomStart)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFB7B7B7))
            ) {
                if (event.photoUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(event.photoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = event.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            if (event.ageLimit != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(
                            width = 1.dp,
                            color = PurpleCard,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${event.ageLimit}+",
                        color = PurpleCard,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 8.dp)
        ) {
            Text(
                text = event.title,
                color = Color.Black,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = event.description,
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (showStatus) 3 else 4,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = dateText,
                color = PurpleCard,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (showStatus) {
                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = event.status.toEventStatusText(),
                    color = event.status.toEventStatusColor(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun EmptyEvents(
    selectedTab: Int,
    searchText: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.cloud),
                contentDescription = "",
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when {
                    searchText.isNotBlank() -> "Ничего не найдено("
                    selectedTab == 1 -> "У тебя пока нет мероприятий("
                    else -> "Мероприятий пока нет("
                },
                color = Color.Gray,
                style = MaterialTheme.typography.titleSmall,
                fontSize = 20.sp
            )
        }
    }
}

private fun List<EventDateSlot>.toDisplayDatesText(): String {
    if (isEmpty()) return "Дата не указана"

    return joinToString(", ") { slot ->
        val date = when {
            slot.dateFrom.isNotBlank() && slot.dateTo.isNotBlank() -> {
                "${slot.dateFrom}-${slot.dateTo}"
            }

            slot.dateFrom.isNotBlank() -> {
                slot.dateFrom
            }

            else -> {
                "Дата не указана"
            }
        }

        if (slot.startTime.isNotBlank()) {
            "$date, с ${slot.startTime}"
        } else {
            date
        }
    }
}

private fun String.toEventStatusText(): String {
    return when (this) {
        EventStatus.DRAFT.value -> "Черновик"
        EventStatus.CREATED.value -> "На модерации"
        EventStatus.APPROVED.value -> "Опубликовано"
        EventStatus.ARCHIVE.value -> "Архив"
        else -> this
    }
}

private fun String.toEventStatusColor(): Color {
    return when (this) {
        EventStatus.DRAFT.value -> Color.Gray
        EventStatus.CREATED.value -> Color(0xFFFF9800)
        EventStatus.APPROVED.value -> Color(0xFF4CAF50)
        EventStatus.ARCHIVE.value -> Color.Gray
        else -> Color.Gray
    }
}

@Composable
private fun UnpublishedEventsButton(
    showUnpublished: Boolean,
    count: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (showUnpublished) PurpleCard else Color(0xFFF0E3F6))
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (showUnpublished) {
                "Показать опубликованные и архив"
            } else {
                "Черновики и модерация" + if (count > 0) " ($count)" else ""
            },
            color = if (showUnpublished) Color.White else Color.Black,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}