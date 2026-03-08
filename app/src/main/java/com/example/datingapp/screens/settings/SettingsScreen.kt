package com.example.datingapp.screens.settings

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.datingapp.components.forms.DatingTextField
import com.example.datingapp.components.forms.TermsSwitch
import com.example.datingapp.R
import com.example.datingapp.screens.auth.openPdfFile
import com.example.datingapp.ui.theme.LocalDatingAppSpacing
import com.example.datingapp.viewmodels.UserViewModel
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.datingapp.navigation.Screen
import com.example.datingapp.ui.theme.boundedFamily

@Composable
fun SettingsHeadingArrow(
    heading: String,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp)
    ) {
        IconButton(
            onClick = onBackClick
        ) {
            Icon(
                painter = painterResource(R.drawable.arrow_left),
                contentDescription = "arrow_left",
                tint = Color.Black
            )
        }

        Text(
            text = heading,
            fontSize = 35.sp,
            fontFamily = boundedFamily,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

data class FieldData(
    val value: String,
    val label: String,
    val placeholder: String,
    val onValueChange: (String) -> Unit,
    val isEditable: Boolean = true
)

@RequiresApi(Build.VERSION_CODES.O)
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
    val isSaving by userViewModel.isSaving.collectAsState()
    val saveError by userViewModel.saveError.collectAsState()
    val saveSuccess by userViewModel.saveSuccess.collectAsState()
    val favoritePlacePhotoUrl by userViewModel.favoritePlacePhotoUrl.collectAsState()

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
    var showDeletePhotoDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }

    val isDataLoaded = remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val spacing = LocalDatingAppSpacing.current
    val context = LocalContext.current

    var isUploadingFavoritePlace by remember { mutableStateOf(false) }
    var showDeleteFavoritePhotoDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    var isForProfilePhoto by remember { mutableStateOf(true) }
    var showPermissionRationale by remember { mutableStateOf(false) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            userViewModel.uploadProfileImage(it, context.contentResolver)
            userViewModel.loadUserData()
        }
    }

    val pickFavoritePlaceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            isUploadingFavoritePlace = true
            scope.launch {
                try {
                    userViewModel.uploadFavoritePlacePhoto(it, context.contentResolver)
                    snackbarHostState.showSnackbar("Фото любимого места загружено")
                    userViewModel.loadUserData()
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Ошибка загрузки: ${e.message}")
                } finally {
                    isUploadingFavoritePlace = false
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (isForProfilePhoto) {
                pickImageLauncher.launch("image/*")
            } else {
                pickFavoritePlaceLauncher.launch("image/*")
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Нет разрешения на чтение файлов")
            }
        }
    }

    fun openGallery(isProfile: Boolean) {
        isForProfilePhoto = isProfile

        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        val permissionCheck = ContextCompat.checkSelfPermission(
            context,
            permission
        )

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            if (isProfile) {
                pickImageLauncher.launch("image/*")
            } else {
                pickFavoritePlaceLauncher.launch("image/*")
            }
        } else {
            // Всегда запрашиваем разрешение заново при нажатии
            permissionLauncher.launch(permission)
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
            hasUnsavedChanges = false
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

    LaunchedEffect(saveError) {
        saveError?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar("Ошибка сохранения: $error")
            }
            userViewModel.clearSaveError()
        }
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            scope.launch {
                snackbarHostState.showSnackbar("Изменения успешно сохранены")
            }
            userViewModel.clearSaveSuccess()
            hasUnsavedChanges = false
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

    fun deleteFavoritePlacePhoto() {
        scope.launch {
            try {
                userViewModel.deleteFavoritePlacePhoto()
                snackbarHostState.showSnackbar("Фото удалено")
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Ошибка удаления: ${e.message}")
            }
        }
    }

    fun saveChanges() {
        val data = mutableMapOf<String, Any?>(
            "name" to name,
            "username" to username,
            "telegram" to telegram,
            "age" to age.toLongOrNull(),
            "university" to university,
            "favoritePlace" to favoritePlace,
            "isPrivateAccount" to isPrivateAccount,
            "notificationsEnabled" to isNotificationsEnabled,
            "notificationSoundEnabled" to isNotificationSoundEnabled
        )

        val changedData = data.filterValues { it != null }
            .mapValues { (_, value) -> value!! }
            .toMutableMap()

        if (favoritePlace.isBlank() && favoritePlacePhotoUrl != null) {
            deleteFavoritePlacePhoto()
        }

        if (changedData.isNotEmpty()) {
            userViewModel.updateUserData(changedData)
            hasUnsavedChanges = false
        }
    }

    fun handleBackPressed() {
        checkForUnsavedChanges {
            navController.popBackStack()
        }
    }

    BackHandler(enabled = true) {
        handleBackPressed()
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = {
                showExitDialog = false
                pendingNavigation = false
            },
            title = { Text("Сохранить изменения?") },
            text = { Text("У тебя есть несохраненные изменения. Хочешь сохранить их перед выходом?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (isSaving) {
                            return@TextButton
                        }
                        saveChanges()
                        showExitDialog = false
                        if (pendingNavigation) {
                            navController.popBackStack()
                            pendingNavigation = false
                        }
                    },
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text("Сохранить")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        hasUnsavedChanges = false
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

    if (showContactDialog) {
        AlertDialog(
            onDismissRequest = { showContactDialog = false },
            title = {
                Text(
                    text = "Связаться с нами",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Есть вопросы или идеи? Пиши нам на почту:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "meetmap.team@gmail.com",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_SENDTO,
                                    android.net.Uri.parse("mailto:meetmap.team@gmail.com")
                                )
                                context.startActivity(intent)
                            }
                            .padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Подписывайся на Telegram-канал, чтобы следить за новостями и бета-тестами:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "t.me/meet_meet_map",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse("https://t.me/meet_meet_map")
                                )
                                context.startActivity(intent)
                            }
                            .padding(vertical = 4.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showContactDialog = false }
                ) {
                    Text("Закрыть")
                }
            }
        )
    }

    if (showDeletePhotoDialog) {
        AlertDialog(
            onDismissRequest = { showDeletePhotoDialog = false },
            title = { Text("Фото профиля") },
            text = { Text("Выбери действие") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeletePhotoDialog = false
                        openGallery(true)
                    }
                ) {
                    Text("Выбрать новое")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeletePhotoDialog = false
                        userViewModel.deleteProfilePhoto()
                    }
                ) {
                    Text("Удалить фото")
                }
            }
        )
    }

    if (showDeleteFavoritePhotoDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteFavoritePhotoDialog = false },
            title = { Text("Удалить фото") },
            text = { Text("Ты уверен, что хочешь удалить фото любимого места?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteFavoritePhotoDialog = false
                        deleteFavoritePlacePhoto()
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteFavoritePhotoDialog = false }
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Выход из профиля",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Ты уверен, что хочешь выйти?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        userViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0)
                        }
                    }
                ) {
                    Text(
                        text = "Выйти",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            if (hasUnsavedChanges) {
                saveChanges()
            }
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
                SettingsHeadingArrow(
                    heading = "Настройки",
                    onBackClick = { handleBackPressed() }
                )
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
                        color = Color.Gray.copy(alpha = 0.6f)
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
                                    if (profileImageUrl.isNullOrBlank() || profileImageUrl == com.example.datingapp.utils.CloudImageUtils.NO_PICTURE_URL) {
                                        R.drawable.picture_defaullt_profile
                                    } else {
                                        profileImageUrl!!.substringBefore("?X-Amz-")
                                    }
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
                                        showDeletePhotoDialog = true
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
                    FieldData(name, "Имя", "Введи свое имя", {
                        name = it
                        hasUnsavedChanges = true
                    }, true),
                    FieldData(username, "Ник в приложении", "Введи никнейм", {
                        username = it
                        hasUnsavedChanges = true
                    }, true),
                    FieldData(telegram, "Ник в Telegram", "@username", {
                        telegram = it
                        hasUnsavedChanges = true
                    }, true),
                    FieldData(email, "Электронная почта", "pochta@gmail.com", { }, false),
                    FieldData(age, "Возраст", "Твой возраст", {
                        age = it
                        hasUnsavedChanges = true
                    }, true),
                    FieldData(university, "ВУЗ", "Название учебного заведения", {
                        university = it
                        hasUnsavedChanges = true
                    }, true),
                ).forEach { fieldData ->
                    item {
                        DatingTextField(
                            value = fieldData.value,
                            onValueChange = fieldData.onValueChange,
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
                    Spacer(modifier = Modifier.height(spacing.medium))
                }

                item {
                    DatingTextField(
                        value = favoritePlace,
                        onValueChange = {
                            favoritePlace = it
                            hasUnsavedChanges = true
                        },
                        label = "Любимое место",
                        placeholder = "Твое любимое место",
                        enabled = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.large)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.large)
                    ) {
                        if (favoritePlacePhotoUrl != null &&
                            favoritePlacePhotoUrl != com.example.datingapp.utils.CloudImageUtils.NO_PICTURE_URL) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(favoritePlacePhotoUrl)
                                        .crossfade(true)
                                        .addHeader("User-Agent", "MeetMap-Android-App/1.0")
                                        .build(),
                                    contentDescription = "Фото любимого места",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(16.dp)
                                        ),
                                    contentScale = ContentScale.Crop,
                                    error = painterResource(id = R.drawable.picture_museum_background),
                                    placeholder = painterResource(id = R.drawable.picture_museum_background)
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 8.dp, y = (-8).dp)
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color.White)
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .clickable {
                                            showDeleteFavoritePhotoDialog = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isUploadingFavoritePlace) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Удалить фото",
                                            modifier = Modifier.size(20.dp),
                                            tint = Color.Black
                                        )
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable(enabled = favoritePlace.isNotBlank() && !isUploadingFavoritePlace) {
                                        if (favoritePlace.isNotBlank()) {
                                            openGallery(false)
                                        }
                                    }
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (isUploadingFavoritePlace) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(32.dp),
                                            strokeWidth = 3.dp
                                        )
                                    } else {
                                        Icon(
                                            painter = painterResource(id = R.drawable.icon_plus),
                                            contentDescription = "Добавить фото",
                                            modifier = Modifier.size(32.dp),
                                            tint = if (favoritePlace.isNotBlank())
                                                MaterialTheme.colorScheme.primary
                                            else
                                                Color.Gray.copy(alpha = 0.4f)
                                        )
                                        Text(
                                            text = if (favoritePlace.isNotBlank())
                                                "Нажми, чтобы добавить фото места"
                                            else
                                                "Сначала укажи название места",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = if (favoritePlace.isNotBlank())
                                                MaterialTheme.colorScheme.primary
                                            else
                                                Color.Gray.copy(alpha = 0.4f),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(spacing.medium))
                }

                item {
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.large),
                        thickness = 0.5.dp,
                        color = Color.Gray.copy(alpha = 0.3f)
                    )
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
                                showContactDialog = true
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
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Icon(
                            painter = painterResource(id = R.drawable.icon_help_circle),
                            contentDescription = "Помощь",
                            modifier = Modifier.size(24.dp),
                            tint = Color.Black
                        )
                    }
                }

                item {
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.large),
                        thickness = 0.5.dp,
                        color = Color.Gray.copy(alpha = 0.3f)
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
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Icon(
                            painter = painterResource(id = R.drawable.icon_shield),
                            contentDescription = "Политика конфиденциальности",
                            modifier = Modifier.size(24.dp),
                            tint = Color.Black
                        )
                    }
                }

                item {
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.large),
                        thickness = 0.5.dp,
                        color = Color.Gray.copy(alpha = 0.3f)
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
                                showLogoutDialog = true
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Выйти из профиля",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Normal
                                ),
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Icon(
                            painter = painterResource(id = R.drawable.icon_logout),
                            contentDescription = "Выйти",
                            modifier = Modifier.size(24.dp),
                            tint = Color.Black
                        )
                    }
                }

                item {
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.large),
                        thickness = 0.5.dp,
                        color = Color.Gray.copy(alpha = 0.3f)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(spacing.medium * 2))
                }
            }
        }
    }
}