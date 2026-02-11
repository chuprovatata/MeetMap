package com.example.datingapp.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.datingapp.R
import com.example.datingapp.components.buttons.PrimaryButton
import com.example.datingapp.components.buttons.TextButtonWithUnderline
import com.example.datingapp.components.forms.DatingTextField
import com.example.datingapp.ui.theme.LocalDatingAppSpacing
import com.example.datingapp.viewmodels.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val successMessage by authViewModel.successMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }

    val spacing = LocalDatingAppSpacing.current

    val canLogin = email.isNotEmpty() &&
            password.isNotEmpty() &&
            isValidEmail(email) &&
            isValidPassword(password) &&
            !isLoading

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
        }
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
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButtonWithUnderline(
                        text = "Пропустить",
                        onClick = {
                            navController.navigate("main")
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
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.large),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Вход",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Войди в свой аккаунт",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(spacing.large * 2))

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
                        placeholder = "Введи свой пароль",
                        isError = passwordError,
                        errorMessage = if (passwordError) "Пароль должен быть не менее 6 символов" else null,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                if (validateForm(email, password) && !isLoading) {
                                    authViewModel.login(
                                        email = email,
                                        password = password,
                                        onSuccess = {
                                            navController.navigate("main") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        }
                                    )
                                }
                            }
                        ),
                        focusRequester = passwordFocusRequester,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.large)
                    )

                    Spacer(modifier = Modifier.height(spacing.small))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.large),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButtonWithUnderline(
                            text = "Забыли пароль?",
                            onClick = {
                                if (isValidEmail(email)) {
                                    authViewModel.sendPasswordResetEmail(
                                        email = email,
                                        onSuccess = {
                                        },
                                        onError = { error ->
                                            scope.launch {
                                                snackbarHostState.showSnackbar(error)
                                            }
                                        }
                                    )
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Введи корректный email для восстановления пароля")
                                    }
                                }
                            },
                            showUnderline = true,
                            fontWeight = FontWeight.Normal,
                            textColor = MaterialTheme.colorScheme.primary,
                            fontSize = 14
                        )
                    }

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
                                text = "Войти",
                                textSize = 20.sp,
                                onClick = {
                                    keyboardController?.hide()
                                    authViewModel.login(
                                        email = email,
                                        password = password,
                                        onSuccess = {
                                            navController.navigate("main") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        }
                                    )
                                },
                                modifier = Modifier.fillMaxSize(),
                                enabled = canLogin
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing.medium))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.large),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ещё нет аккаунта?",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        TextButtonWithUnderline(
                            text = "Зарегистрироваться",
                            onClick = {
                                navController.navigate("registration")
                            },
                            showUnderline = true,
                            textColor = MaterialTheme.colorScheme.primary,
                            fontSize = 16
                        )
                    }
                }

                Spacer(modifier = Modifier.height(spacing.large))
            }

            Image(
                painter = painterResource(id = R.drawable.picture_registration_screen),
                contentDescription = "Вход в аккаунт",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            )
        }
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