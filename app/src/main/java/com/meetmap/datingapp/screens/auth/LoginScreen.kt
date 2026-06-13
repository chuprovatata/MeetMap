package com.meetmap.datingapp.screens.auth

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.meetmap.datingapp.R
import com.meetmap.datingapp.components.buttons.PrimaryButton
import com.meetmap.datingapp.components.buttons.TextButtonWithUnderline
import com.meetmap.datingapp.components.forms.DatingTextField
import com.meetmap.datingapp.ui.theme.LocalDatingAppSpacing
import com.meetmap.datingapp.viewmodels.AuthViewModel
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

    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val successMessage by authViewModel.successMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }

    val spacing = LocalDatingAppSpacing.current

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("GoogleSignIn", "===== GOOGLE SIGN-IN RESULT =====")
        Log.d("GoogleSignIn", "Result code: ${result.resultCode}")

        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken

                if (idToken != null) {
                    authViewModel.signInWithGoogle(
                        idToken = idToken,
                        onSuccess = {
                            navController.navigate("main") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onError = { error ->
                            scope.launch {
                                snackbarHostState.showSnackbar(error)
                            }
                        }
                    )
                } else {
                    scope.launch {
                        snackbarHostState.showSnackbar("Ошибка: не удалось получить токен")
                    }
                }
            } catch (e: ApiException) {
                scope.launch {
                    snackbarHostState.showSnackbar("Ошибка: ${e.message}")
                }
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Вход через Google отменён")
            }
        }
    }

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.large, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButtonWithUnderline(
                    text = "Пропустить",
                    onClick = {
                        navController.navigate("main")
                    },
                    showUnderline = false,
                    textColor = MaterialTheme.colorScheme.surfaceVariant,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

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
                        .padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

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

                Spacer(modifier = Modifier.height(12.dp))

                DatingTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Пароль",
                    placeholder = "Введи свой пароль",
                    isError = passwordError,
                    errorMessage = if (passwordError) "Пароль должен быть не менее 6 символов" else null,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { /* ... */ }
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

                Spacer(modifier = Modifier.height(4.dp))

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
                                    onSuccess = {},
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
                        fontSize = 12
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.large)
                        .height(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        PrimaryButton(
                            text = "Войти",
                            textSize = 18.sp,
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

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.large),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        text = "или",
                        modifier = Modifier.padding(horizontal = 12.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp
                    )
                    Divider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken("634266978778-e5qetn7v2lrolosd0tc18seruch2k9nm.apps.googleusercontent.com")
                            .requestEmail()
                            .build()
                        val googleSignInClient = GoogleSignIn.getClient(context, gso)
                        googleSignInLauncher.launch(googleSignInClient.signInIntent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.large)
                        .height(48.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = "Google Sign-In",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Войти через Google",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.large),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ещё нет аккаунта?",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 12.sp
                        )
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    TextButtonWithUnderline(
                        text = "Зарегистрироваться",
                        onClick = {
                            navController.navigate("registration")
                        },
                        showUnderline = true,
                        textColor = MaterialTheme.colorScheme.primary,
                        fontSize = 12
                    )
                }
            }

            Image(
                painter = painterResource(id = R.drawable.picture_registration_screen),
                contentDescription = "Вход в аккаунт",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
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