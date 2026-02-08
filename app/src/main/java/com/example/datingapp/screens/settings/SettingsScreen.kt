package com.example.datingapp.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.datingapp.components.forms.DatingTextField
import com.example.datingapp.R
import com.example.datingapp.ui.theme.LocalDatingAppSpacing
import com.example.datingapp.viewmodels.UserViewModel
import kotlinx.coroutines.launch

data class FieldData(
    val value: String,
    val label: String,
    val placeholder: String,
    val onValueChange: (String) -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val userViewModel: UserViewModel = viewModel()

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

    var showExitDialog by remember { mutableStateOf(false) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val spacing = LocalDatingAppSpacing.current

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { userViewModel.uploadProfileImage(it) }
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
        }
    }

    LaunchedEffect(uploadError) {
        uploadError?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar("Ошибка: $error")
            }
        }
    }

    fun checkForUnsavedChanges() {
        val originalData = userData ?: emptyMap()

        val hasChanges = name != (originalData["name"] as? String ?: "") ||
                username != (originalData["username"] as? String ?: "") ||
                telegram != (originalData["telegram"] as? String ?: "") ||
                email != (originalData["email"] as? String ?: "") ||
                age != ((originalData["age"] as? Long)?.toString() ?: "") ||
                university != (originalData["university"] as? String ?: "") ||
                favoritePlace != (originalData["favoritePlace"] as? String ?: "")

        hasUnsavedChanges = hasChanges

        if (hasChanges) {
            showExitDialog = true
        } else {
            navController.popBackStack()
        }
    }

    fun saveChanges() {
        val data = mapOf(
            "name" to name,
            "username" to username,
            "telegram" to telegram,
            "email" to email,
            "age" to age.toLongOrNull(),
            "university" to university,
            "favoritePlace" to favoritePlace
        ).filterValues { it != null && it.toString().isNotEmpty() }

        userViewModel.updateUserData(data)
        hasUnsavedChanges = false

        scope.launch {
            snackbarHostState.showSnackbar("Данные сохранены")
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Сохранить изменения?") },
            text = { Text("У вас есть несохраненные изменения. Сохранить перед выходом?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        saveChanges()
                        showExitDialog = false
                        navController.popBackStack()
                    }
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        navController.popBackStack()
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
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = { checkForUnsavedChanges() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_back),
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    if (isLoading || isUploadingImage) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(spacing.medium)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.large)
                        .padding(top = spacing.large),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Box(modifier = Modifier.size(120.dp)) {
                        if (isUploadingImage) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else {
                            Image(
                                painter = painterResource(id = R.mipmap.picture_defaullt_profile_foreground),
                                contentDescription = "Фото профиля",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable {
                                    pickImageLauncher.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                }
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Изменить фото",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(spacing.medium))
            }

            listOf(
                FieldData(name, "Имя", "Введите ваше имя") { name = it },
                FieldData(username, "Ник в приложении", "Введите никнейм") { username = it },
                FieldData(telegram, "Ник в Telegram", "@username") { telegram = it },
                FieldData(email, "Электронная почта", "pochta@gmail.com") { email = it },
                FieldData(age, "Возраст", "Ваш возраст") { age = it },
                FieldData(university, "ВУЗ", "Название учебного заведения") { university = it },
                FieldData(favoritePlace, "Любимое место", "Ваше любимое место в городе") { favoritePlace = it }
            ).forEach { fieldData ->
                item {
                    DatingTextField(
                        value = fieldData.value,
                        onValueChange = {
                            fieldData.onValueChange(it)
                            hasUnsavedChanges = true
                        },
                        label = fieldData.label,
                        placeholder = fieldData.placeholder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.large)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(spacing.large))
            }
        }
    }
}