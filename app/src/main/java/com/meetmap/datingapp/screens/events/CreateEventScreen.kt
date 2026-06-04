package com.meetmap.datingapp.screens.events

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.meetmap.datingapp.ui.theme.PurpleCard
import com.meetmap.datingapp.viewmodels.CreateEventDateUi
import com.meetmap.datingapp.viewmodels.CreateEventViewModel
import com.meetmap.datingapp.viewmodels.CreateEventViewModel.Companion.ADDRESS_MAX
import com.meetmap.datingapp.viewmodels.CreateEventViewModel.Companion.DESCRIPTION_MAX
import com.meetmap.datingapp.viewmodels.CreateEventViewModel.Companion.MODERATOR_NOTES_MAX
import com.meetmap.datingapp.viewmodels.CreateEventViewModel.Companion.PARTICIPANT_NOTES_MAX
import com.meetmap.datingapp.viewmodels.CreateEventViewModel.Companion.SOURCE_URL_MAX
import com.meetmap.datingapp.viewmodels.CreateEventViewModel.Companion.TITLE_MAX

@Composable
fun CreateEventScreen(
    navController: NavController,
    eventId: String = "",
    viewModel: CreateEventViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(eventId) {
        if (eventId.isNotBlank()) {
            viewModel.loadEventForEdit(eventId)
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.setPhotoUri(uri)
    }

    fun saveDraftAndExit() {
        if (state.isSubmitted) {
            navController.popBackStack()
            return
        }

        if (!viewModel.hasDraftContent()) {
            navController.popBackStack()
            return
        }

        viewModel.saveDraft(
            contentResolver = context.contentResolver,
            onSuccess = {
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("event_draft_saved", true)

                navController.popBackStack()
            }
        )
    }

    BackHandler {
        saveDraftAndExit()
    }

    LaunchedEffect(state.errorMessage) {
        val message = state.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearError()
    }

    if (state.submittedEventId != null) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(
                    text = "Мероприятие отправлено",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Мероприятие отправлено на модерацию. После успешного прохождения оно будет опубликовано на странице."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("event_submitted", true)

                        navController.popBackStack()
                    }
                ) {
                    Text("Понятно")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CreateEventHeader(
                onBackClick = {
                    saveDraftAndExit()
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 12.dp)
                    .padding(
                        bottom = WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding() + 24.dp
                    )
            ) {
                PhotoPickerBlock(
                    photoUri = state.photoUri,
                    photoUrl = state.photoUrl,
                    onClick = {
                        imagePicker.launch("image/*")
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))

                EventInput(
                    label = "Название",
                    value = state.title,
                    placeholder = "Максимум $TITLE_MAX символов",
                    required = true,
                    error = state.errors.title,
                    maxLength = TITLE_MAX,
                    onValueChange = viewModel::setTitle
                )

                state.dates.forEachIndexed { index, slot ->
                    EventDateBlock(
                        index = index,
                        slot = slot,
                        canRemove = state.dates.size > 1,
                        onDateFromChange = { viewModel.setDateFrom(index, it) },
                        onDateToChange = { viewModel.setDateTo(index, it) },
                        onStartTimeChange = { viewModel.setStartTime(index, it) },
                        onRemove = { viewModel.removeDateSlot(index) }
                    )
                }

                Text(
                    text = "+ Добавить еще дату",
                    color = PurpleCard,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(top = 2.dp, bottom = 14.dp)
                        .clickable {
                            viewModel.addDateSlot()
                        }
                )

                EventInput(
                    label = "Длительность мероприятия",
                    value = state.duration,
                    placeholder = "чч:мм",
                    error = state.errors.duration,
                    keyboardType = KeyboardType.Number,
                    onValueChange = viewModel::setDuration
                )

                EventInput(
                    label = "Адрес",
                    value = state.address,
                    placeholder = "Точный адрес мероприятия",
                    required = true,
                    error = state.errors.address,
                    maxLength = ADDRESS_MAX,
                    onValueChange = viewModel::setAddress
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = state.isOrganizer,
                        onCheckedChange = viewModel::setIsOrganizer
                    )

                    Text(
                        text = "Я являюсь организатором мероприятия",
                        color = Color.Black,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (state.isOrganizer) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFFF3CD))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Если вы являетесь организатором, ссылка на источник не обязательна. Настоятельно рекомендуем заполнить поле “Примечания для модераторов” и указать цель мероприятия. Модераторы свяжутся с вами для одобрения мероприятия.",
                            color = Color(0xFF6B4E00),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                EventInput(
                    label = "Ссылка на источник",
                    value = state.sourceUrl,
                    placeholder = "Ссылка на страницу мероприятия",
                    required = !state.isOrganizer,
                    error = state.errors.sourceUrl,
                    maxLength = SOURCE_URL_MAX,
                    onValueChange = viewModel::setSourceUrl
                )

                EventInput(
                    label = "Описание мероприятия",
                    value = state.description,
                    placeholder = "Расскажи, о чем мероприятие",
                    required = true,
                    error = state.errors.description,
                    maxLength = DESCRIPTION_MAX,
                    minHeight = 92.dp,
                    onValueChange = viewModel::setDescription
                )

                EventInput(
                    label = "Возрастное ограничение",
                    value = state.ageLimit,
                    placeholder = "16",
                    error = state.errors.ageLimit,
                    maxLength = 2,
                    keyboardType = KeyboardType.Number,
                    onValueChange = viewModel::setAgeLimit
                )

                EventInput(
                    label = "Примечания для участников",
                    value = state.participantNotes,
                    placeholder = "Например, необходимость предварительной регистрации",
                    error = state.errors.participantNotes,
                    maxLength = PARTICIPANT_NOTES_MAX,
                    minHeight = 92.dp,
                    onValueChange = viewModel::setParticipantNotes
                )

                EventInput(
                    label = "Примечания для модераторов",
                    value = state.moderatorNotes,
                    placeholder = "Комментарий, который увидит модератор",
                    error = state.errors.moderatorNotes,
                    maxLength = MODERATOR_NOTES_MAX,
                    minHeight = 78.dp,
                    onValueChange = viewModel::setModeratorNotes
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Я хочу получить уведомление о\nрезультатах модерации",
                        color = Color.Black,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )

                    Switch(
                        checked = state.notifyAboutModeration,
                        onCheckedChange = viewModel::setNotifyAboutModeration
                    )
                }

                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color.Red)) {
                            append("*")
                        }
                        withStyle(SpanStyle(color = Color.Gray)) {
                            append(" — обязательные поля для заполнения")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Button(
                    onClick = {
                        viewModel.submitForModeration(
                            contentResolver = context.contentResolver
                        )
                    },
                    enabled = !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PurpleCard,
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFD9D9D9),
                        disabledContentColor = Color.Gray
                    )
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Отправить",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Мероприятие будет отправлено на модерацию. После успешного прохождения оно будет опубликовано на странице.",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun CreateEventHeader(
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
            .padding(horizontal = 9.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = Color.Black
            )
        }

        Text(
            text = "Новое мероприятие",
            color = Color.Black,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PhotoPickerBlock(
    photoUri: Uri?,
    photoUrl: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFE7E7E7))
            .border(
                width = 1.dp,
                color = Color(0xFFD0D0D0),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        when {
            photoUri != null -> {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Фото мероприятия",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            photoUrl.isNotBlank() -> {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Фото мероприятия",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                Text(
                    text = "Загрузить фото",
                    color = Color.Black,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (photoUri != null || photoUrl.isNotBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Нажми, чтобы заменить фото",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EventDateBlock(
    index: Int,
    slot: CreateEventDateUi,
    canRemove: Boolean,
    onDateFromChange: (String) -> Unit,
    onDateToChange: (String) -> Unit,
    onStartTimeChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Column {
        Text(
            text = if (index == 0) {
                "Даты мероприятия"
            } else {
                "Дата мероприятия ${index + 1}"
            },
            color = Color.Black,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        EventInput(
            label = "Дата начала",
            value = slot.dateFrom,
            placeholder = "дд.мм.гггг",
            required = true,
            error = slot.dateFromError,
            keyboardType = KeyboardType.Number,
            onValueChange = onDateFromChange
        )

        EventInput(
            label = "Дата окончания",
            value = slot.dateTo,
            placeholder = "дд.мм.гггг, если несколько дней",
            error = slot.dateToError,
            keyboardType = KeyboardType.Number,
            onValueChange = onDateToChange
        )

        EventInput(
            label = "Время начала",
            value = slot.startTime,
            placeholder = "чч:мм",
            required = true,
            error = slot.startTimeError,
            keyboardType = KeyboardType.Number,
            onValueChange = onStartTimeChange
        )

        if (canRemove) {
            Text(
                text = "Удалить дату",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(bottom = 14.dp)
                    .clickable { onRemove() }
            )
        }
    }
}

@Composable
private fun EventInput(
    label: String,
    value: String,
    placeholder: String,
    required: Boolean = false,
    error: String? = null,
    maxLength: Int? = null,
    minHeight: Dp = 48.dp,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    var textFieldValue by remember(value) {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        )
    }

    LaunchedEffect(value) {
        if (value != textFieldValue.text) {
            textFieldValue = TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
    ) {
        if (required) {
            RequiredLabel(label)
        } else {
            Text(
                text = label,
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                val rawText = if (maxLength != null) {
                    newValue.text.take(maxLength)
                } else {
                    newValue.text
                }

                onValueChange(rawText)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(minHeight)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF3F3F3))
                .border(
                    width = if (error != null) 1.dp else 0.dp,
                    color = if (error != null) Color.Red else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            singleLine = minHeight <= 52.dp,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = Color.Black,
                fontSize = 14.sp
            ),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopStart
                ) {
                    if (value.isBlank()) {
                        Text(
                            text = placeholder,
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    innerTextField()
                }
            }
        )

        if (maxLength != null) {
            Text(
                text = "${value.length}/$maxLength",
                color = Color.Gray,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.End)
            )
        }

        if (error != null) {
            Text(
                text = error,
                color = Color.Red,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun RequiredLabel(text: String) {
    Row {
        Text(
            text = text,
            color = Color.Black,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "*",
            color = Color.Red,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}