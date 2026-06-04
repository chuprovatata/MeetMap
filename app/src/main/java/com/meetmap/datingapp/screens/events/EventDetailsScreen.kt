package com.meetmap.datingapp.screens.events

import android.content.Intent
import android.net.Uri
import android.widget.ImageView
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.meetmap.datingapp.R
import com.meetmap.datingapp.components.headers.Heading_Arrow
import com.meetmap.datingapp.data.models.EventInfo
import com.meetmap.datingapp.data.models.EventParticipant
import com.meetmap.datingapp.data.models.EventStatus
import com.meetmap.datingapp.ui.theme.PurpleCard
import com.meetmap.datingapp.viewmodels.EventDetailsViewModel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.meetmap.datingapp.navigation.Screen

@Composable
fun EventDetailsScreen(
    navController: NavController,
    eventId: String,
    viewModel: EventDetailsViewModel = hiltViewModel()
) {
    val event by viewModel.event.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isGoing by viewModel.isGoing.collectAsState()
    val isOwner by viewModel.isOwner.collectAsState()
    val friendsGoing by viewModel.friendsGoing.collectAsState()
    val isActionLoading by viewModel.isActionLoading.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showEditWarning by remember { mutableStateOf(false) }

    LaunchedEffect(eventId) {
        viewModel.loadEvent(eventId)
    }

    LaunchedEffect(errorMessage) {
        val message = errorMessage ?: return@LaunchedEffect

        snackbarHostState.showSnackbar(message)
        viewModel.clearErrorMessage()
    }

    if (showEditWarning) {
        AlertDialog(
            onDismissRequest = {
                showEditWarning = false
            },
            title = {
                Text(
                    text = "Редактировать мероприятие?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Статус мероприятия будет изменён на черновик. После редактирования его нужно будет повторно отправить на модерацию. Если мероприятие уже было опубликовано, оно временно пропадёт из общих списков."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showEditWarning = false

                        viewModel.moveEventToDraft { eventId ->
                            navController.navigate(Screen.CreateEvent.edit(eventId))
                        }
                    }
                ) {
                    Text("Продолжить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEditWarning = false
                    }
                ) {
                    Text("Отменить")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                    .padding(horizontal = 9.dp)
            ) {
                Heading_Arrow(
                    heading = event?.title ?: "Мероприятие",
                    navController = navController
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        containerColor = Color.White
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            event == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Мероприятие не найдено",
                        color = Color.Gray,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            else -> {
                EventDetailsContent(
                    event = event!!,
                    isGoing = isGoing,
                    isOwner = isOwner,
                    friendsGoing = friendsGoing,
                    isActionLoading = isActionLoading,
                    onActionClick = {
                        if (isOwner) {
                            showEditWarning = true
                        } else {
                            viewModel.toggleGoing()
                        }
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun EventDetailsContent(
    event: EventInfo,
    isGoing: Boolean,
    isOwner: Boolean,
    friendsGoing: List<EventParticipant>,
    isActionLoading: Boolean,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 12.dp)
            .padding(
                bottom = WindowInsets.navigationBars.asPaddingValues()
                    .calculateBottomPadding() + 24.dp
            )
    ) {
        EventPhotoBlock(event = event)

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = event.title,
            color = Color.Black,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            lineHeight = 26.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        if (event.description.isNotBlank()) {
            Text(
                text = event.description,
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(18.dp))
        }

        EventInfoLine(
            title = "Время встречи:",
            value = event.getDatesText()
        )

        EventInfoLine(
            title = "Длительность:",
            value = event.duration.ifBlank { "Не указана" }
        )

        EventInfoLine(
            title = "Адрес:",
            value = event.address.ifBlank { "Не указан" }
        )

        if (event.sourceUrl.isNotBlank()) {
            EventInfoLine(
                title = "Источник:",
                value = event.sourceUrl.shortenUrl(),
                valueColor = PurpleCard,
                onClick = {
                    runCatching {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.sourceUrl))
                        context.startActivity(intent)
                    }
                }
            )
        } else {
            EventInfoLine(
                title = "Источник:",
                value = "Не указан"
            )
        }

        EventInfoLine(
            title = "Примечания:",
            value = event.participantNotes.ifBlank { "Нет" }
        )

        Spacer(modifier = Modifier.height(18.dp))

        FriendsBlock(friendsGoing = friendsGoing)

        Spacer(modifier = Modifier.height(22.dp))

        EventActionButton(
            event = event,
            isGoing = isGoing,
            isOwner = isOwner,
            isActionLoading = isActionLoading,
            onActionClick = onActionClick
        )
    }
}

@Composable
private fun EventPhotoBlock(
    event: EventInfo
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(255.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(238.dp)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFFD9D9D9))
        ) {
            if (event.photoUrl.isNotBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
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
                    .padding(end = 14.dp)
                    .size(58.dp)
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
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EventInfoLine(
    title: String,
    value: String,
    valueColor: Color = Color.Black,
    onClick: (() -> Unit)? = null
) {
    if (value.isBlank()) return

    Text(
        text = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(title)
                append(" ")
            }

            withStyle(
                style = SpanStyle(
                    color = valueColor,
                    fontWeight = if (valueColor == PurpleCard) {
                        FontWeight.Medium
                    } else {
                        FontWeight.Normal
                    }
                )
            ) {
                append(value)
            }
        },
        color = Color.Black,
        style = MaterialTheme.typography.bodyMedium,
        lineHeight = 20.sp,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
            .padding(bottom = 8.dp)
    )
}

@Composable
private fun FriendsBlock(
    friendsGoing: List<EventParticipant>
) {
    if (friendsGoing.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFF0E3F6))
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Text(
            text = "${friendsGoing.size} ${friendsGoing.size.friendWord()} отметили это мероприятие",
            color = Color.Black,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "посмотри, кто среди них",
            color = Color.Black,
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            friendsGoing.take(4).forEach { participant ->
                FriendAvatar(
                    name = participant.userName.ifBlank { "Без имени" },
                    photoUrl = participant.userPhotoUrl
                )
            }
        }
    }
}

@Composable
private fun FriendAvatar(
    name: String,
    photoUrl: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(58.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Color(0xFFD9D9D9)),
            contentAlignment = Alignment.Center
        ) {
            if (photoUrl.isNotBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                AndroidView(
                    factory = { context ->
                        ImageView(context).apply {
                            setImageResource(R.mipmap.picture_defaullt_profile_round)
                            scaleType = ImageView.ScaleType.CENTER_CROP
                            scaleX = 1.35f
                            scaleY = 1.35f
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = name,
            color = Color.Black,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EventActionButton(
    event: EventInfo,
    isGoing: Boolean,
    isOwner: Boolean,
    isActionLoading: Boolean,
    onActionClick: () -> Unit
) {
    val isArchived = event.status == EventStatus.ARCHIVE.value

    val buttonText = when {
        isOwner -> "Редактировать"
        isGoing -> "Не хочу идти"
        else -> "Хочу пойти"
    }

    val buttonColor = when {
        isOwner -> Color(0xFFD9D9D9)
        isGoing -> Color(0xFFD9D9D9)
        else -> PurpleCard
    }

    val textColor = when {
        isOwner -> Color.Black
        isGoing -> Color.Black
        else -> Color.White
    }

    Button(
        onClick = onActionClick,
        enabled = !isActionLoading && (!isArchived || isOwner),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = textColor,
            disabledContainerColor = Color(0xFFD9D9D9),
            disabledContentColor = Color.Gray
        ),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Text(
            text = if (isActionLoading) {
                "Сохраняем..."
            } else if (isArchived && !isOwner) {
                "Мероприятие в архиве"
            } else {
                buttonText
            },
            color = if (isArchived && !isOwner) Color.Gray else textColor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun EventInfo.getDatesText(): String {
    if (dates.isEmpty()) return "Не указано"

    return dates.joinToString(separator = "\n") { slot ->
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

private fun String.shortenUrl(maxLength: Int = 28): String {
    return if (length <= maxLength) {
        this
    } else {
        take(maxLength) + "..."
    }
}

private fun Int.friendWord(): String {
    val lastTwo = this % 100
    val last = this % 10

    return when {
        lastTwo in 11..14 -> "друзей"
        last == 1 -> "друг"
        last in 2..4 -> "друга"
        else -> "друзей"
    }
}