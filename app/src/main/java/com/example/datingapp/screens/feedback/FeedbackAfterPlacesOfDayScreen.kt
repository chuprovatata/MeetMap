package com.example.datingapp.screens.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.datingapp.R
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.datingapp.components.buttons.PrimaryButton
import com.example.datingapp.navigation.Screen
import com.example.datingapp.ui.theme.LocalDatingAppSpacing
import com.example.datingapp.viewmodels.FeedbackViewModel
import kotlinx.coroutines.launch

@Composable
fun FeedbackAfterPlacesOfDayScreen(
    navController: NavController,
    viewModel: FeedbackViewModel = hiltViewModel()
) {
    var rating by remember { mutableStateOf(0) }
    val spacing = LocalDatingAppSpacing.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val options = listOf(
        "Кафе и ресторанов",
        "Музеев и выставок",
        "Парков и мест досуга",
        "Других мест"
    )

    var selectedOptionIndex by remember { mutableStateOf(-1) }
    val isSaving by viewModel.isSaving.collectAsState()
    val saveError by viewModel.saveError.collectAsState()

    // Определяем, откуда пришли, по previousBackStackEntry
    val cameFromOnboarding = remember {
        navController.previousBackStackEntry?.destination?.route?.startsWith("places_of_day?fromOnboarding=true") == true
    }

    LaunchedEffect(saveError) {
        saveError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .padding(horizontal = spacing.large)
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = "На сегодня всё...",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Ждём тебя завтра!",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = "Оцени сегодняшнюю подборку",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Сегодняшние места",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 1..5) {
                    Image(
                        painter = painterResource(id = R.drawable.icon_star),
                        contentDescription = "Star $i",
                        modifier = Modifier
                            .size(45.dp)
                            .clickable { rating = i },
                        colorFilter = if (i <= rating) {
                            ColorFilter.tint(MaterialTheme.colorScheme.primary)
                        } else {
                            ColorFilter.tint(Color.White)
                        }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Были\nбанальными",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .width(100.dp)
                        .align(Alignment.Top)
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Были\nуникальными",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .width(100.dp)
                        .align(Alignment.Top),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Дальше я хотел бы видеть больше",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            options.forEachIndexed { index, option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedOptionIndex = if (selectedOptionIndex == index) -1 else index
                        }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.icon_star),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        colorFilter = if (selectedOptionIndex == index) {
                            ColorFilter.tint(MaterialTheme.colorScheme.primary)
                        } else {
                            ColorFilter.tint(Color.White)
                        }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (selectedOptionIndex == index)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                PrimaryButton(
                    text = if (cameFromOnboarding) "далее" else "на главную",
                    onClick = {
                        val wantMoreCategories = if (selectedOptionIndex != -1) {
                            listOf(options[selectedOptionIndex])
                        } else {
                            emptyList()
                        }

                        viewModel.savePlacesOfDayFeedback(
                            rating = rating,
                            selectedOptionIndex = selectedOptionIndex,
                            wantMoreCategories = wantMoreCategories,
                            source = if (cameFromOnboarding) "onboarding" else "main",
                            onComplete = {
                                if (cameFromOnboarding) {
                                    // Если пришли из обучения - идем на финальный туториал
                                    navController.navigate(Screen.FinalTutorial.route) {
                                        popUpTo(Screen.FeedbackAfterPlacesOfDay.route) { inclusive = true }
                                    }
                                } else {
                                    // Если пришли с главной - возвращаемся на главную
                                    navController.navigate(Screen.Main.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    textSize = 32.sp,
                    //enabled = rating > 0
                )
            }
        }
    }
}