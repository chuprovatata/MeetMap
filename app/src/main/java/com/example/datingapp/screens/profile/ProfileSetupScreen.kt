package com.example.datingapp.screens.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.datingapp.components.buttons.PrimaryButton
import com.example.datingapp.components.buttons.TextButtonWithUnderline
import com.example.datingapp.viewmodels.ProfileSetupViewModel
import com.example.datingapp.components.forms.DatingTextField
import com.example.datingapp.components.progress.ProgressLine
import com.example.datingapp.navigation.NavigationProgress
import com.example.datingapp.navigation.Screen
import com.example.datingapp.ui.theme.LocalDatingAppSpacing
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    navController: NavController,
    viewModel: ProfileSetupViewModel = viewModel()
) {
    var name by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var university by rememberSaveable { mutableStateOf("") }
    var bio by rememberSaveable { mutableStateOf("") }
    var selectedGender by rememberSaveable { mutableStateOf<Gender?>(null) }
    var selectedYear by rememberSaveable { mutableStateOf<Int?>(null) }
    var telegram by rememberSaveable { mutableStateOf("") }

    var showYearDropdown by rememberSaveable { mutableStateOf(false) }
    var showGenderError by rememberSaveable { mutableStateOf(false) }
    var showYearError by rememberSaveable { mutableStateOf(false) }

    var nameError by remember { mutableStateOf(false) }
    var usernameError by remember { mutableStateOf(false) }
    var universityError by remember { mutableStateOf(false) }
    var telegramError by remember { mutableStateOf(false) } // Добавлено
    var nameErrorMessage by remember { mutableStateOf("") }
    var usernameErrorMessage by remember { mutableStateOf("") }
    var universityErrorMessage by remember { mutableStateOf("") }
    var telegramErrorMessage by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val spacing = LocalDatingAppSpacing.current

    val nameFocusRequester = remember { FocusRequester() }
    val usernameFocusRequester = remember { FocusRequester() }
    val universityFocusRequester = remember { FocusRequester() }
    val telegramFocusRequester = remember { FocusRequester() } // Добавлено
    val bioFocusRequester = remember { FocusRequester() }

    val progress = NavigationProgress.getProgress(Screen.ProfileSetup)

    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    val birthYears = remember {
        (currentYear - 35..currentYear - 18).reversed().toList()
    }

    val isLoading by viewModel.isLoading.collectAsState()

    val isFormValid = remember(name, username, selectedGender, selectedYear) {
        name.isNotBlank() &&
                username.isNotBlank() &&
                selectedGender != null &&
                selectedYear != null &&
                !nameError &&
                !usernameError &&
                !universityError &&
                !telegramError
    }

    LaunchedEffect(name) {
        if (name.isNotEmpty()) {
            val (isValid, message) = validateName(name)
            nameError = !isValid
            nameErrorMessage = message
        } else {
            nameError = false
            nameErrorMessage = ""
        }
    }

    LaunchedEffect(username) {
        if (username.isNotEmpty()) {
            val (isValid, message) = validateUsername(username)
            usernameError = !isValid
            usernameErrorMessage = message
        } else {
            usernameError = false
            usernameErrorMessage = ""
        }
    }

    LaunchedEffect(university) {
        if (university.isNotEmpty()) {
            val (isValid, message) = validateUniversity(university)
            universityError = !isValid
            universityErrorMessage = message
        } else {
            universityError = false
            universityErrorMessage = ""
        }
    }

    LaunchedEffect(telegram) {
        if (telegram.isNotEmpty()) {
            val (isValid, message) = validateTelegram(telegram)
            telegramError = !isValid
            telegramErrorMessage = message
        } else {
            telegramError = false
            telegramErrorMessage = ""
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ProfileSetupViewModel.SetupEvent.NavigateToTargets -> {
                    navController.navigate("targetSelection") {
                        popUpTo("profileSetup") { inclusive = false }
                    }
                }
                is ProfileSetupViewModel.SetupEvent.NavigateToCategories -> {
                }
                is ProfileSetupViewModel.SetupEvent.NavigateToTutorial -> {
                    navController.navigate(Screen.PlacesTutorial.route) {
                        popUpTo(0)
                    }
                }
                is ProfileSetupViewModel.SetupEvent.NavigateToMain -> {
                }
                is ProfileSetupViewModel.SetupEvent.RegistrationStarted -> {
                    println("Registration started in ProfileSetupScreen")
                }
                is ProfileSetupViewModel.SetupEvent.ShowError -> {
                    scope.launch {
                        snackbarHostState.showSnackbar("Ошибка: ${event.message}")
                    }
                }
                is ProfileSetupViewModel.SetupEvent.ShowSuccessMessage -> {
                    scope.launch {
                        snackbarHostState.showSnackbar("Профиль успешно сохранен!")
                    }
                }
            }
        }
    }

    fun saveProfile() {
        showGenderError = selectedGender == null
        showYearError = selectedYear == null

        if (!isFormValid || selectedGender == null || selectedYear == null) {
            scope.launch {
                snackbarHostState.showSnackbar("Заполните все обязательные поля")
            }
            return
        }

        val age = currentYear - selectedYear!!

        val userProfile = mapOf(
            "name" to name.trim(),
            "username" to username.trim(),
            "university" to university.trim().takeIf { it.isNotBlank() },
            "bio" to bio.trim().takeIf { it.isNotBlank() },
            "gender" to selectedGender!!.value,
            "birthYear" to selectedYear,
            "age" to age,
            "telegram" to telegram.trim()
        )

        viewModel.saveUserProfileWithCallback(
            userProfile = userProfile,
            onSuccess = {
                scope.launch {
                    snackbarHostState.showSnackbar("Профиль сохранен!")
                    navController.navigate("targetSelection") {
                        popUpTo("profileSetup") { inclusive = false }
                    }
                }
            },
            onError = { errorMessage ->
                scope.launch {
                    snackbarHostState.showSnackbar("Ошибка: $errorMessage")
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
                    .padding(vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.large),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProgressLine(
                        progress = progress,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(spacing.medium))

                    TextButtonWithUnderline(
                        text = "Пропустить",
                        onClick = {
                            // Пропускаем к выбору целей
                            navController.navigate("targetSelection") {
                                popUpTo("profileSetup") { inclusive = false }
                            }
                        },
                        showUnderline = false,
                        textColor = MaterialTheme.colorScheme.surfaceVariant,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Заполни профиль",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Давай узнаем друг друга лучше",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(spacing.large))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = spacing.medium)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.Start
                ) {
                    DatingTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Имя",
                        placeholder = "Как к тебе можно обращаться?",
                        isError = nameError,
                        errorMessage = if (nameError) nameErrorMessage else null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { usernameFocusRequester.requestFocus() }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(nameFocusRequester)
                    )

                    Spacer(modifier = Modifier.height(spacing.medium))

                    DatingTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = "Ник",
                        placeholder = "Придумай никнейм для приложения",
                        isError = usernameError,
                        errorMessage = if (usernameError) usernameErrorMessage else null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { universityFocusRequester.requestFocus() }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(usernameFocusRequester)
                    )

                    Spacer(modifier = Modifier.height(spacing.medium))

                    DatingTextField(
                        value = university,
                        onValueChange = { university = it },
                        label = "ВУЗ",
                        placeholder = "Наше приложение для студентов",
                        isError = universityError,
                        errorMessage = if (universityError) universityErrorMessage else null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { telegramFocusRequester.requestFocus() }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(universityFocusRequester)
                    )

                    Spacer(modifier = Modifier.height(spacing.medium))

                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DatingTextField(
                            value = telegram,
                            onValueChange = { telegram = it },
                            label = "Социальная сеть для общения",
                            placeholder = "Укажи, где тебе будет удобно общаться",
                            isError = telegramError,
                            errorMessage = if (telegramError) telegramErrorMessage else null,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { bioFocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(telegramFocusRequester)
                        )
                    }

                    Spacer(modifier = Modifier.height(spacing.medium))

                    Text(
                        text = "Пол",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (showGenderError) {
                        Text(
                            text = "Выберите пол",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.medium)
                    ) {
                        GenderChip(
                            gender = Gender.MALE,
                            isSelected = selectedGender == Gender.MALE,
                            onClick = {
                                selectedGender = Gender.MALE
                                showGenderError = false
                            },
                            modifier = Modifier.weight(1f)
                        )

                        GenderChip(
                            gender = Gender.FEMALE,
                            isSelected = selectedGender == Gender.FEMALE,
                            onClick = {
                                selectedGender = Gender.FEMALE
                                showGenderError = false
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(spacing.medium))

                    DatingTextField(
                        value = bio,
                        onValueChange = { newText -> bio = newText },
                        label = "Описание профиля",
                        placeholder = "Напиши пару слов о себе и своих увлечениях",
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { keyboardController?.hide() }
                        ),
                        singleLine = false,
                        maxLines = 4,
                        maxCharacters = 200,
                        showCharacterCounter = true,
                        focusRequester = bioFocusRequester
                    )

                    Spacer(modifier = Modifier.height(spacing.medium))

                    Text(
                        text = "Год рождения",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (showYearError) {
                        Text(
                            text = "Выберите год рождения",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable { showYearDropdown = !showYearDropdown },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedYear?.toString() ?: "Выбрать",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (selectedYear == null)
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = if (showYearDropdown) "▲" else "▼",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (showYearDropdown) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(8.dp)
                                ),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column(
                                modifier = Modifier.verticalScroll(rememberScrollState())
                            ) {
                                birthYears.forEach { year ->
                                    Text(
                                        text = year.toString(),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedYear = year
                                                showYearDropdown = false
                                                showYearError = false
                                            }
                                            .padding(vertical = 12.dp, horizontal = 16.dp)
                                    )

                                    if (year != birthYears.last()) {
                                        Divider(
                                            modifier = Modifier.fillMaxWidth(),
                                            thickness = 0.5.dp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    selectedYear?.let { year ->
                        Text(
                            text = "Возраст: ${currentYear - year}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(
                        start = spacing.medium,
                        end = spacing.medium,
                        top = 8.dp,
                        bottom = 12.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(30.dp))
                } else {
                    PrimaryButton(
                        text = "Зарегистрироваться",
                        textSize = 20.sp,
                        onClick = {
                            keyboardController?.hide()
                            saveProfile()
                        },
                        modifier = Modifier.fillMaxSize(),
                        enabled = isFormValid && !isLoading
                    )
                }
            }
        }
    }
}

@Composable
fun GenderChip(
    gender: Gender,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(128.dp)
            .height(37.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            width = if (isSelected) 0.dp else 1.dp,
            color = if (isSelected) Color.Transparent
            else MaterialTheme.colorScheme.outline
        ),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = gender.displayName,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

enum class Gender(val value: String, val displayName: String) {
    MALE("M", "мужской"),
    FEMALE("F", "женский")
}

private fun validateName(name: String): Pair<Boolean, String> {
    return when {
        name.length < 2 -> Pair(false, "Имя должно быть не менее 2 символов")
        name.length > 50 -> Pair(false, "Имя должно быть не более 50 символов")
        !name.matches(Regex("^[a-zA-Zа-яА-ЯёЁ\\s\\-.'’]+$")) ->
            Pair(false, "Имя может содержать только буквы, пробелы, дефисы и апострофы")
        else -> Pair(true, "")
    }
}

private fun validateUsername(username: String): Pair<Boolean, String> {
    return when {
        username.length < 3 -> Pair(false, "Никнейм должен быть не менее 3 символов")
        username.length > 20 -> Pair(false, "Никнейм должен быть не более 20 символов")
        !username.matches(Regex("^[a-zA-Zа-яА-ЯёЁ0-9_.]+$")) ->
            Pair(false, "Можно использовать только буквы, цифры, _ и .")
        else -> Pair(true, "")
    }
}

private fun validateUniversity(university: String): Pair<Boolean, String> {
    return when {
        university.length > 100 -> Pair(false, "Название вуза должно быть не более 100 символов")
        university.contains(Regex("[<>{}]")) ->
            Pair(false, "Название содержит недопустимые символы")
        else -> Pair(true, "")
    }
}

private fun validateTelegram(telegram: String): Pair<Boolean, String> {
    return when {
        telegram.length > 50 -> Pair(false, "Слишком длинное название")
        telegram.contains(Regex("[<>{}]")) ->
            Pair(false, "Содержит недопустимые символы")
        else -> Pair(true, "")
    }
}