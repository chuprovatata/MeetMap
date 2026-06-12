package com.meetmap.datingapp.screens.events

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.meetmap.datingapp.R
import com.meetmap.datingapp.components.buttons.PrimaryButton
import com.meetmap.datingapp.components.forms.DatingTextField
import com.meetmap.datingapp.components.headers.Heading_Arrow
import com.meetmap.datingapp.data.models.Event
import com.meetmap.datingapp.ui.theme.PurpleCard
import com.meetmap.datingapp.viewmodels.EventViewModel
import com.meetmap.datingapp.viewmodels.UserViewModel
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext







@Composable
fun EventEdit(
    eventId: String,
    navController: NavController,
    userViewModel: UserViewModel,
    eventViewModel: EventViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val event by eventViewModel.currentEvent.collectAsState()
    val isLoading by eventViewModel.isLoading.collectAsState()
    val currentUser = userViewModel.myUser.value

    // Поля формы
    var title by remember { mutableStateOf("") }
    var place by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var university by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var isForAll by remember { mutableStateOf(true) }

    // Ошибки валидации
    var dateError by remember { mutableStateOf<String?>(null) }
    var timeError by remember { mutableStateOf<String?>(null) }

    // Фото
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploadingImage by remember { mutableStateOf(false) }

    // PhotoPicker для выбора фото
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
            }
        }
    )

    // Загрузка данных мероприятия
    LaunchedEffect(eventId) {
        eventViewModel.loadEventById(eventId)
    }

    // Заполнение полей после загрузки
    LaunchedEffect(event) {
        event?.let {
            title = it.title
            place = it.place
            description = it.description
            university = it.university
            date = it.date
            time = it.time
            isForAll = it.isForAll
        }
    }




    if (event != null && currentUser?.uid != event?.organizerId) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    // Валидация
    fun validateDate(dateStr: String) {
        dateError = when {
            dateStr.isBlank() -> "Введи дату"
            !isValidDate(dateStr) -> "Некорректная дата. Используй формат ДД.ММ.ГГГГ и помни, что дата не может быть в прошлом"
            else -> null
        }
    }

    fun validateTime(timeStr: String) {
        timeError = when {
            timeStr.isBlank() -> "Введи время"
            !isValidTime(timeStr) -> "Некорректное время. Используй формат ЧЧ:ММ"
            else -> null
        }
    }

    val isDateValid = date.isNotBlank() && isValidDate(date)
    val isTimeValid = time.isNotBlank() && isValidTime(time)
    val isDateTimeValid = isDateValid && isTimeValid && isDateTimeValid(date, time)

    val isFormValid = title.isNotBlank() &&
            place.isNotBlank() &&
            isDateValid &&
            isTimeValid &&
            isDateTimeValid

    // Функция сохранения
    fun updateEventData(imageUrl: String? = null) {
        val finalImageUrl = imageUrl ?: event?.imageUrl

        val updatedEvent = Event(
            id = eventId,
            title = title,
            place = place,
            description = description,
            date = date,
            time = time,
            university = university,
            isForAll = isForAll,
            organizerId = event?.organizerId ?: "",
            organizerUsername = event?.organizerUsername ?: "",
            organizerAvatarUrl = event?.organizerAvatarUrl,
            imageUrl = finalImageUrl,
            maxParticipants = 50,
            currentParticipants = event?.currentParticipants ?: 0,
            participantsList = event?.participantsList ?: emptyList()
        )
        eventViewModel.updateEvent(updatedEvent) { success ->
            if (success) {
                navController.popBackStack()
            }
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 6.dp)
                    .padding(
                        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                        bottom = 20.dp
                    )
            ) {
                Heading_Arrow("Редактирование", navController, textSize = 30.sp)
            }
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
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Редактируй мероприятие",
                        style = MaterialTheme.typography.displayLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // БЛОК С ФОТО (как в профиле пользователя)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE0E0E0))
                            .clickable {
                                photoPickerLauncher.launch("image/*")
                            }
                    ) {
                        when {
                            isUploadingImage -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                            selectedImageUri != null -> {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = "Preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            event?.imageUrl != null -> {
                                AsyncImage(
                                    model = event?.imageUrl,
                                    contentDescription = "Event image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    error = painterResource(R.drawable.place1)
                                )
                            }
                            else -> {
                                Image(
                                    painter = painterResource(R.drawable.place1),
                                    contentDescription = "Добавить фото",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        // Иконка камеры для добавления/изменения фото
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .clickable {
                                    photoPickerLauncher.launch("image/*")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.icon_camera),
                                contentDescription = "Изменить фото",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    DatingTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = "Название мероприятия"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DatingTextField(
                            value = date,
                            onValueChange = {
                                date = it
                                validateDate(it)
                            },
                            label = "Дата (дд.мм.гггг)",
                            modifier = Modifier.weight(1f),
                            isError = dateError != null,
                            errorMessage = dateError
                        )
                        DatingTextField(
                            value = time,
                            onValueChange = {
                                time = it
                                validateTime(it)
                            },
                            label = "Время (чч:мм)",
                            modifier = Modifier.weight(1f),
                            isError = timeError != null,
                            errorMessage = timeError
                        )
                    }

                    if (isDateValid && isTimeValid && !isDateTimeValid(date, time)) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Дата и время не могут быть в прошлом",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    DatingTextField(
                        value = place,
                        onValueChange = { place = it },
                        label = "Место проведения"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DatingTextField(
                        value = university,
                        onValueChange = { university = it },
                        label = "Название ВУЗа"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Мероприятие для всех желающих",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isForAll) PurpleCard else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Checkbox(
                            checked = isForAll,
                            onCheckedChange = { isForAll = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = PurpleCard,
                                uncheckedColor = Color.Gray
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    DatingTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = "Описание мероприятия",
                        singleLine = false,
                        maxLines = 5,
                        maxCharacters = 500,
                        showCharacterCounter = true
                    )

                    Spacer(modifier = Modifier.height(80.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    PrimaryButton(
                        text = "Сохранить изменения",
                        onClick = {
                            Log.d("EventEdit", "=== onClick СРАБОТАЛ ===")
                            Log.d("EventEdit", "selectedImageUri = $selectedImageUri")

                            if (selectedImageUri != null) {
                                Log.d("EventEdit", "Ветка с фото")
                                isUploadingImage = true
                                eventViewModel.uploadEventImage(
                                    selectedImageUri!!,
                                    context.contentResolver,
                                    eventId
                                ) { imageUrl ->
                                    Log.d("EventEdit", "uploadEventImage callback: $imageUrl")
                                    isUploadingImage = false
                                    if (imageUrl != null) {
                                        updateEventData(imageUrl)
                                    }
                                }
                            } else {
                                Log.d("EventEdit", "Ветка без фото")
                                updateEventData()
                            }
                        },
                        enabled = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}



