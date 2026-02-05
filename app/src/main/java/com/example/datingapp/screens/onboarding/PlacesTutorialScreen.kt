package com.example.datingapp.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.datingapp.R
import com.example.datingapp.components.buttons.TextButtonWithUnderline
import com.example.datingapp.components.progress.ProgressLine
import com.example.datingapp.navigation.NavigationProgress
import com.example.datingapp.navigation.Screen
import com.example.datingapp.ui.theme.LocalDatingAppSpacing
import com.example.datingapp.ui.theme.Typography
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun PlacesTutorialScreen(
    navController: NavController? = null,
    onSkipClick: () -> Unit,
    progress: Float = NavigationProgress.getProgress(Screen.PlacesTutorial)
) {
    val spacing = LocalDatingAppSpacing.current
    var showSecondImage by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(2000)
        showSecondImage = true
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
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
                        onClick = onSkipClick,
                        showUnderline = false,
                        textColor = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    style = SpanStyle(
                                        fontFamily = Typography.bodyLarge.fontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = Typography.bodyLarge.fontSize,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                ) {
                                    append("Мои места")
                                }
                                withStyle(
                                    style = SpanStyle(
                                        fontFamily = Typography.bodyLarge.fontFamily,
                                        fontWeight = Typography.bodyLarge.fontWeight,
                                        fontSize = Typography.bodyLarge.fontSize,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                ) {
                                    append(" — архив мест, которые ты посетил. Приложение определяет места по твоей геолокации. Ты сможешь добавить их на свою карту, если захочешь.")
                                }
                            },
                            lineHeight = Typography.bodyLarge.lineHeight
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier.size(79.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.picture_map_pin),
                            contentDescription = "Иконка мест",
                            modifier = Modifier.size(79.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.15f)
            ) {
                this@Column.AnimatedVisibility(
                    visible = showSecondImage,
                    enter = fadeIn(
                        animationSpec = tween(
                            durationMillis = 2000,
                            easing = FastOutSlowInEasing
                        )
                    ) + scaleIn(
                        animationSpec = tween(2000),
                        initialScale = 0.8f
                    ),
                    exit = fadeOut() + scaleOut(),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 40.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.picture_tutorial_2),
                        contentDescription = "Вторая иллюстрация обучения",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.7f)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.picture_tutorial),
                    contentDescription = "Иллюстрация обучения",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            }
        }
    }
}