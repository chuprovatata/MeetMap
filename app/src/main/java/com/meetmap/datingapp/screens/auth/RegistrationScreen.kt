package com.meetmap.datingapp.screens.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.meetmap.datingapp.R
import com.meetmap.datingapp.components.buttons.PrimaryButton
import com.meetmap.datingapp.components.buttons.TextButtonWithUnderline
import com.meetmap.datingapp.components.buttons.WhiteButton
import com.meetmap.datingapp.components.forms.DatingTextField
import com.meetmap.datingapp.components.forms.TermsCheckbox
import com.meetmap.datingapp.components.progress.ProgressLine
import com.meetmap.datingapp.navigation.NavigationProgress
import com.meetmap.datingapp.navigation.Screen
import com.meetmap.datingapp.ui.theme.LocalDatingAppSpacing
import com.meetmap.datingapp.viewmodels.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun RegistrationScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var isAgreedWithTerms by rememberSaveable { mutableStateOf(false) }

    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    var showVerificationScreen by rememberSaveable { mutableStateOf(false) }
    var isContinueClicked by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val successMessage by authViewModel.successMessage.collectAsState()

    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }

    val spacing = LocalDatingAppSpacing.current

    val progress = NavigationProgress.getProgress(Screen.Registration)

    val canContinue = !isContinueClicked &&
            email.isNotEmpty() &&
            password.isNotEmpty() &&
            isValidEmail(email) &&
            isValidPassword(password) &&
            !isLoading

    val canRegister = isAgreedWithTerms && !isLoading

    val moveToPassword = {
        passwordFocusRequester.requestFocus()
        keyboardController?.show()
    }

    LaunchedEffect(email) {
        emailError = email.isNotEmpty() && !isValidEmail(email)
    }

    LaunchedEffect(password) {
        passwordError = password.isNotEmpty() && !isValidPassword(password)
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(error)
                authViewModel.clearErrors()
            }
        }
    }

    LaunchedEffect(successMessage) {
        successMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                authViewModel.clearSuccessMessage()
            }
            showVerificationScreen = true
            isContinueClicked = true
        }
    }

    fun registerWithFirebase() {
        if (!validateForm(email, password)) return

        authViewModel.register(
            email = email,
            password = password,
            onSuccess = {
            },
            onError = { error ->
                isContinueClicked = false
            }
        )
    }

    fun checkEmailVerification() {
        authViewModel.checkEmailVerification(
            onVerified = {
                navController.navigate("profileSetup") {
                    popUpTo("registration") { inclusive = true }
                }
            },
            onNotVerified = {
                scope.launch {
                    snackbarHostState.showSnackbar("Email еще не подтвержден. Проверьте вашу почту")
                }
            }
        )
    }

    fun resendVerificationEmail() {
        authViewModel.resendVerificationEmail(
            onSuccess = {
            },
            onError = { error ->
                scope.launch {
                    snackbarHostState.showSnackbar("Ошибка отправки письма: $error")
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
                            navController.navigate("profileSetup")
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Регистрация",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Это нужно для твоей безопасности",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(spacing.large))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DatingTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Электронная почта",
                    placeholder = "pochta@gmail.com",
                    isError = emailError,
                    errorMessage = if (emailError) "Введи корректный email" else null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { moveToPassword() }
                    ),
                    focusRequester = emailFocusRequester,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.large)
                )

                Spacer(modifier = Modifier.height(spacing.medium))

                DatingTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Пароль",
                    placeholder = "Придумай пароль для входа в аккаунт",
                    isError = passwordError,
                    errorMessage = if (passwordError) "Пароль должен быть не менее 6 символов" else null,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            if (validateForm(email, password) && !isContinueClicked) {
                                registerWithFirebase()
                            }
                        }
                    ),
                    focusRequester = passwordFocusRequester,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.large),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                painter = painterResource(
                                    id = if (passwordVisible) R.drawable.icon_eye_open
                                    else R.drawable.icon_eye_closed
                                ),
                                contentDescription = if (passwordVisible) "Скрыть пароль" else "Показать пароль",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(spacing.large))

                Box(
                    modifier = Modifier
                        .width(360.dp)
                        .height(57.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp)
                        )
                    } else {
                        PrimaryButton(
                            text = "Продолжить",
                            textSize = 20.sp,
                            onClick = {
                                keyboardController?.hide()
                                if (!isContinueClicked) {
                                    registerWithFirebase()
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                            enabled = canContinue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.large))

            AnimatedVisibility(
                visible = showVerificationScreen,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(spacing.medium))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.large)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spacing.medium),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp, horizontal = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Мы отправили письмо с подтверждением на:",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Text(
                                    text = email,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = TextDecoration.Underline
                                    ),
                                    textAlign = TextAlign.Left,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                Divider(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    NumberedItem(
                                        number = 1,
                                        text = "Проверь почту"
                                    )
                                    NumberedItem(
                                        number = 2,
                                        text = "Найди письмо от МитМап"
                                    )
                                    NumberedItem(
                                        number = 3,
                                        text = "Нажми на ссылку в письме"
                                    )
                                    NumberedItem(
                                        number = 4,
                                        text = "Возвращайся в приложение"
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(spacing.medium))

                        TextButton(
                            onClick = { resendVerificationEmail() },
                            enabled = !isLoading,
                            modifier = Modifier.padding(horizontal = spacing.medium)
                        ) {
                            Text(
                                text = "Не пришло письмо? Отправить еще раз",
                                textAlign = TextAlign.Left,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    textDecoration = TextDecoration.Underline
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing.large))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.large)
                    ) {
                        TermsCheckbox(
                            checked = isAgreedWithTerms,
                            onCheckedChange = { isAgreedWithTerms = it },
                            modifier = Modifier.fillMaxWidth(),
                            showDetailsLink = false
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = spacing.large),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            TextButtonWithUnderline(
                                text = "Читать подробнее",
                                onClick = {
                                    openPdfFile(
                                        context,
                                        "https://docs.google.com/document/d/1ZdU4hvSO9TTyQIQ3GvCkoeHl0wH_uUKb/export?format=pdf"
                                    )
                                },
                                modifier = Modifier,
                                textStyle = MaterialTheme.typography.bodyMedium,
                                showUnderline = true,
                                fontWeight = FontWeight.Normal,
                                textColor = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing.large))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.picture_registration_screen),
                            contentDescription = "Регистрация",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .width(360.dp)
                                .height(57.dp)
                                .padding(bottom = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(30.dp)
                                )
                            } else {
                                WhiteButton(
                                    text = "Зарегистрироваться",
                                    textSize = 18.sp,
                                    onClick = {
                                        keyboardController?.hide()
                                        checkEmailVerification()
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                    enabled = canRegister
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing.small))
                }
            }
        }
    }
}

@Composable
fun NumberedItem(number: Int, text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

fun openPdfFile(context: Context, pdfUrl: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.parse(pdfUrl), "application/pdf")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val packageManager = context.packageManager
        if (intent.resolveActivity(packageManager) != null) {
            context.startActivity(intent)
        } else {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl))
            context.startActivity(browserIntent)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

private fun isValidPassword(password: String): Boolean {
    return password.length >= 6
}

private fun validateForm(email: String, password: String): Boolean {
    return isValidEmail(email) && isValidPassword(password)
}