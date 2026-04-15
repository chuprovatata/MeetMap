package com.meetmap.datingapp.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
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
import com.meetmap.datingapp.R
import com.meetmap.datingapp.components.buttons.PrimaryButton
import com.meetmap.datingapp.components.buttons.TextButtonWithUnderline
import com.meetmap.datingapp.components.cards.SelectableCard
import com.meetmap.datingapp.components.progress.ProgressLine
import com.meetmap.datingapp.viewmodels.ProfileSetupViewModel
import com.meetmap.datingapp.navigation.NavigationProgress
import com.meetmap.datingapp.navigation.Screen
import com.meetmap.datingapp.ui.theme.LocalDatingAppSpacing
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class Category(
    val id: Int,
    val title: String,
    val imageRes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectionScreen(
    navController: NavController,
    viewModel: ProfileSetupViewModel = viewModel()
) {
    val categories = remember {
        listOf(
            Category(
                id = 1,
                title = "Кафе, рестораны",
                imageRes = R.mipmap.picture_cafe_foreground
            ),
            Category(
                id = 2,
                title = "Городские парки",
                imageRes = R.mipmap.picture_park_foreground
            ),
            Category(
                id = 3,
                title = "Спортивные центры",
                imageRes = R.mipmap.picture_sport_foreground
            ),
            Category(
                id = 4,
                title = "Музеи, театры",
                imageRes = R.mipmap.picture_museum_foreground
            ),
            Category(
                id = 5,
                title = "Бары, клубы",
                imageRes = R.mipmap.picture_bar_foreground
            ),
            Category(
                id = 6,
                title = "Магазины, торговые центры",
                imageRes = R.mipmap.picture_shop_foreground
            ),
            Category(
                id = 7,
                title = "Развлекательные комплексы",
                imageRes = R.mipmap.picture_entertaiment_foreground
            ),
            Category(
                id = 8,
                title = "Креативные пространства",
                imageRes = R.mipmap.picture_creativity_foreground
            )
        )
    }

    var selectedCategories by rememberSaveable { mutableStateOf<Set<Int>>(emptySet()) }

    var showCategoryError by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val spacing = LocalDatingAppSpacing.current

    val progress = NavigationProgress.getProgress(Screen.CategorySelection)


    val isLoading by viewModel.isLoading.collectAsState()

    val isFormValid = remember(selectedCategories) {
        selectedCategories.isNotEmpty()
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ProfileSetupViewModel.SetupEvent.NavigateToMain -> {
                    navController.navigate("main") {
                        popUpTo("categorySelection") { inclusive = true }
                    }
                }
                is ProfileSetupViewModel.SetupEvent.ShowError -> {
                    scope.launch {
                        snackbarHostState.showSnackbar("Ошибка: ${event.message}")
                    }
                }
                is ProfileSetupViewModel.SetupEvent.ShowSuccessMessage -> {
                    scope.launch {
                        snackbarHostState.showSnackbar("Категории успешно сохранены!")
                    }
                }
                else -> {}
            }
        }
    }

    fun saveCategories() {
        if (!isFormValid) {
            showCategoryError = true
            scope.launch {
                snackbarHostState.showSnackbar(
                    "Выберите хотя бы одну категорию"
                )
            }
            return
        }

        showCategoryError = false
        viewModel.saveUserCategories(selectedCategories.toList())
    }

    fun onCategorySelected(categoryId: Int) {
        selectedCategories = if (selectedCategories.contains(categoryId)) {
            selectedCategories - categoryId
        } else {
            selectedCategories + categoryId
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
                text = "Какие места ты любишь посещать?",
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

            if (showCategoryError) {
                Text(
                    text = "Выберите хотя бы одну категорию",
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
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(spacing.medium),
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                    contentPadding = PaddingValues(bottom = 40.dp)
                ) {
                    items(categories) { category ->
                        SelectableCard(
                            imagePainter = painterResource(id = category.imageRes),
                            title = category.title,
                            isSelected = selectedCategories.contains(category.id),
                            onClick = { onCategorySelected(category.id) }
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
                        text = "Завершить",
                        textSize = 20.sp,
                        onClick = {
                            keyboardController?.hide()
                            saveCategories()
                        },
                        modifier = Modifier.fillMaxSize(),
                        enabled = isFormValid && !isLoading
                    )
                }
            }
        }
    }
}