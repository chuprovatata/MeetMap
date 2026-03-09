package com.example.datingapp.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.datingapp.R
import com.example.datingapp.components.buttons.PrimaryButton
import com.example.datingapp.components.buttons.TextButtonWithUnderline
import com.example.datingapp.components.cards.SelectableCard
import com.example.datingapp.components.progress.ProgressLine
import com.example.datingapp.navigation.NavigationProgress
import com.example.datingapp.navigation.Screen
import com.example.datingapp.viewmodels.ProfileSetupViewModel
import com.example.datingapp.ui.theme.LocalDatingAppSpacing
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class Target(
    val id: Int,
    val title: String,
    val imageRes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TargetSelectionScreen(
    navController: NavController,
    viewModel: ProfileSetupViewModel = viewModel()
) {
    val targets = remember {
        listOf(
            Target(1, "Найти новых друзей", R.mipmap.ic_launcher_foreground),
            Target(2, "Узнать новые места", R.mipmap.picture_target_2_foreground),
            Target(3, "Общаться с друзьями", R.mipmap.picture_target_3_foreground),
            Target(4, "Найти компанию для прогулки", R.mipmap.picture_target_4_foreground)
        )
    }

    var selectedTargets by rememberSaveable { mutableStateOf<Set<Int>>(emptySet()) }
    var showTargetError by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val spacing = LocalDatingAppSpacing.current

    val progress = NavigationProgress.getProgress(Screen.TargetSelection)

    val isLoading by viewModel.isLoading.collectAsState()
    val isFormValid = remember(selectedTargets) { selectedTargets.isNotEmpty() }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ProfileSetupViewModel.SetupEvent.NavigateToCategories -> {
                    navController.navigate("categorySelection") {
                        popUpTo("targetSelection") { inclusive = false }
                    }
                }
                is ProfileSetupViewModel.SetupEvent.ShowError -> {
                    scope.launch { snackbarHostState.showSnackbar("Ошибка: ${event.message}") }
                }
                is ProfileSetupViewModel.SetupEvent.ShowSuccessMessage -> {
                    scope.launch { snackbarHostState.showSnackbar("Цели успешно сохранены!") }
                }
                else -> {}
            }
        }
    }

    fun saveTargets() {
        if (!isFormValid) {
            showTargetError = true
            scope.launch { snackbarHostState.showSnackbar("Выберите хотя бы одну цель") }
            return
        }
        showTargetError = false
        viewModel.saveUserTargets(selectedTargets.toList())
    }

    fun onTargetSelected(targetId: Int) {
        selectedTargets = if (selectedTargets.contains(targetId)) {
            selectedTargets - targetId
        } else {
            selectedTargets + targetId
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
                text = "Какие у тебя цели?",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Выбери все подходящие варианты",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(spacing.large))

            if (showTargetError) {
                Text(
                    text = "Выберите хотя бы одну цель",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.medium)
                )
                Spacer(modifier = Modifier.height(spacing.small))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.75f)
                    .padding(horizontal = spacing.medium)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(spacing.medium),
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                    contentPadding = PaddingValues(bottom = 40.dp)
                ) {
                    items(targets) { target ->
                        SelectableCard(
                            imagePainter = painterResource(id = target.imageRes),
                            title = target.title,
                            isSelected = selectedTargets.contains(target.id),
                            onClick = { onTargetSelected(target.id) }
                        )
                    }
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
                        text = "Продолжить",
                        textSize = 20.sp,
                        onClick = {
                            keyboardController?.hide()
                            saveTargets()
                        },
                        modifier = Modifier.fillMaxSize(),
                        enabled = isFormValid && !isLoading
                    )
                }
            }
        }
    }
}