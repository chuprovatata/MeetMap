package com.example.datingapp.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.datingapp.components.forms.DatingTextField
import com.example.datingapp.components.forms.TermsSwitch
import com.example.datingapp.components.headers.Heading_Arrow
import com.example.datingapp.R
import com.example.datingapp.screens.auth.openPdfFile
import com.example.datingapp.ui.theme.LocalDatingAppSpacing
import com.example.datingapp.viewmodels.UserViewModel
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel

data class FieldData(
    val value: String,
    val label: String,
    val placeholder: String,
    val onValueChange: (String) -> Unit,
    val isEditable: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val userViewModel: UserViewModel = hiltViewModel()

    val isUploadingImage by userViewModel.isUploadingImage.collectAsState()
    val profileImageUrl by userViewModel.profileImageUrl.collectAsState()
    val userData by userViewModel.userData.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()
    val uploadError by userViewModel.uploadError.collectAsState()

    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var telegram by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var university by remember { mutableStateOf("") }
    var favoritePlace by remember { mutableStateOf("") }

    var isPrivateAccount by remember { mutableStateOf(false) }
    var isNotificationsEnabled by remember { mutableStateOf(true) }
    var isNotificationSoundEnabled by remember { mutableStateOf(true) }

    var showExitDialog by remember { mutableStateOf(false) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }
    var pendingNavigation by remember { mutableStateOf(false) }

    val isDataLoaded = remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val spacing = LocalDatingAppSpacing.current
    val context = LocalContext.current

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            userViewModel.uploadProfileImage(it, context.contentResolver)
        }
    }

    LaunchedEffect(Unit) {
        userViewModel.loadUserData()
    }

    LaunchedEffect(userData) {
        userData?.let { data ->
            name = data["name"] as? String ?: ""
            username = data["username"] as? String ?: ""
            telegram = data["telegram"] as? String ?: ""
            email = data["email"] as? String ?: ""
            age = (data["age"] as? Long)?.toString() ?: ""
            university = data["university"] as? String ?: ""
            favoritePlace = data["favoritePlace"] as? String ?: ""
            isPrivateAccount = data["isPrivateAccount"] as? Boolean ?: false
            isNotificationsEnabled = data["notificationsEnabled"] as? Boolean ?: true
            isNotificationSoundEnabled = data["notificationSoundEnabled"] as? Boolean ?: true

            isDataLoaded.value = true
        }
    }

    LaunchedEffect(uploadError) {
        uploadError?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar("Ошибка загрузки фото: $error")
            }
            userViewModel.clearUploadError()
        }
    }

    fun checkForUnsavedChanges(onComplete: () -> Unit) {
        val originalData = userData ?: emptyMap()

        val hasChanges = name != (originalData["name"] as? String ?: "") ||
                username != (originalData["username"] as? String ?: "") ||
                telegram != (originalData["telegram"] as? String ?: "") ||
                age != ((originalData["age"] as? Long)?.toString() ?: "") ||
                university != (originalData["university"] as? String ?: "") ||
                favoritePlace != (originalData["favoritePlace"] as? String ?: "") ||
                isPrivateAccount != (originalData["isPrivateAccount"] as? Boolean ?: false) ||
                isNotificationsEnabled != (originalData["notificationsEnabled"] as? Boolean ?: true) ||
                isNotificationSoundEnabled != (originalData["notificationSoundEnabled"] as? Boolean ?: true)

        hasUnsavedChanges = hasChanges

        if (hasChanges) {
            showExitDialog = true
            pendingNavigation = true
        } else {
            onComplete()
        }
    }

    fun saveChanges() {
        val data = mapOf(
            "name" to name,
            "username" to username,
            "telegram" to telegram,
            "age" to age.toLongOrNull(),
            "university" to university,
            "favoritePlace" to favoritePlace,
            "isPrivateAccount" to isPrivateAccount,
            "notificationsEnabled" to isNotificationsEnabled,
            "notificationSoundEnabled" to isNotificationSoundEnabled
        ).filterValues { it != null && it.toString().isNotEmpty() }

        userViewModel.updateUserData(data)
        hasUnsavedChanges = false

        scope.launch {
            snackbarHostState.showSnackbar("Данные сохранены")
        }
    }

    fun handleBackPressed() {
        checkForUnsavedChanges {
            navController.popBackStack()
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = {
                showExitDialog = false
                pendingNavigation = false
            },
            title = { Text("Сохранить изменения?") },
            text = { Text("У вас есть несохраненные изменения. Сохранить перед выходом?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        saveChanges()
                        showExitDialog = false
                        if (pendingNavigation) {
                            navController.popBackStack()
                            pendingNavigation = false
                        }
                    }
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        if (pendingNavigation) {
                            navController.popBackStack()
                            pendingNavigation = false
                        }
                    }
                ) {
                    Text("Не сохранять")
                }
            }
        )
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
                Heading_Arrow(
                    heading = "Настройки",
                    navController = navController)
            }
        }
    ) { paddingValues ->
        if (isLoading || !isDataLoaded.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Загрузка данных...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White),
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.large)
                    ) {
                        Box(modifier = Modifier.size(110.dp)) {
                            if (isUploadingImage) {
                                Box(
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            } else {
                                val imageModel = remember(profileImageUrl) {
                                    profileImageUrl?.let {
                                        runCatching {
                                            scope.launch {
                                            }
                                        }
                                        it.substringBefore("?X-Amz-")
                                    } ?: R.drawable.picture_defaullt_profile
                                }

                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(imageModel)
                                        .crossfade(true)
                                        .addHeader("User-Agent", "MeetMap-Android-App/1.0")
                                        .build(),
                                    contentDescription = "Фото профиля",
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clip(CircleShape)
                                        .align(Alignment.CenterStart),
                                    contentScale = ContentScale.Crop,
                                    error = painterResource(id = R.drawable.picture_defaullt_profile),
                                    placeholder = painterResource(id = R.drawable.picture_defaullt_profile)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = (-10).dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        pickImageLauncher.launch(
                                            PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    }
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.icon_edit_photo),
                                    contentDescription = "Изменить фото",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(spacing.medium))
                }

                listOf(
                    FieldData(name, "Имя", "Введите ваше имя", { name = it }, true),
                    FieldData(username, "Ник в приложении", "Введите никнейм", { username = it }, true),
                    FieldData(telegram, "Ник в Telegram", "@username", { telegram = it }, true),
                    FieldData(email, "Электронная почта", "pochta@gmail.com", { }, false),
                    FieldData(age, "Возраст", "Ваш возраст", { age = it }, true),
                    FieldData(university, "ВУЗ", "Название учебного заведения", { university = it }, true),
                    FieldData(favoritePlace, "Любимое место", "Ваше любимое место в городе", { favoritePlace = it }, true)
                ).forEach { fieldData ->
                    item {
                        DatingTextField(
                            value = fieldData.value,
                            onValueChange = {
                                if (fieldData.isEditable) {
                                    fieldData.onValueChange(it)
                                    hasUnsavedChanges = true
                                }
                            },
                            label = fieldData.label,
                            placeholder = fieldData.placeholder,
                            enabled = fieldData.isEditable,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spacing.large)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(spacing.medium))
                    }
                }

                item {
                    TermsSwitch(
                        checked = isPrivateAccount,
                        onCheckedChange = {
                            isPrivateAccount = it
                            hasUnsavedChanges = true
                        },
                        text = "Закрытый аккаунт",
                        subtitle = "Только твои друзья могут просматривать твой профиль.",
                        showDetailsLink = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.large)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(spacing.medium))
                }

                item {
                    TermsSwitch(
                        checked = isNotificationsEnabled,
                        onCheckedChange = {
                            isNotificationsEnabled = it
                            hasUnsavedChanges = true
                        },
                        text = "Уведомления",
                        showDetailsLink = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.large)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(spacing.medium))
                }

                item {
                    TermsSwitch(
                        checked = isNotificationSoundEnabled,
                        onCheckedChange = {
                            isNotificationSoundEnabled = it
                            hasUnsavedChanges = true
                        },
                        text = "Звук уведомлений",
                        showDetailsLink = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.large)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(spacing.medium))
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.large)
                            .clickable {
                                navController.navigate("main")
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Связаться с нами",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Normal
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Icon(
                            painter = painterResource(id = R.drawable.icon_help_circle),
                            contentDescription = "Помощь",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                item {
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.large),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(spacing.small))
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.large)
                            .clickable {
                                openPdfFile(
                                    context = context,
                                    pdfUrl = "https://docs.google.com/document/d/1ZdU4hvSO9TTyQIQ3GvCkoeHl0wH_uUKb/export?format=pdf"
                                )
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Политика конфиденциальности",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Normal
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Icon(
                            painter = painterResource(id = R.drawable.icon_shield),
                            contentDescription = "Политика конфиденциальности",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                item {
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.large),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(spacing.medium * 2))
                }
            }
        }
    }
}