package com.example.datingapp.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.datingapp.components.buttons.WhiteButton
import com.example.datingapp.components.progress.ProgressLine
import com.example.datingapp.navigation.NavigationProgress
import com.example.datingapp.navigation.Screen
import com.example.datingapp.ui.theme.LocalDatingAppSpacing
import com.example.datingapp.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesTutorialScreen2(
    navController: NavController? = null,
    onSkipClick: () -> Unit,
    onReadyClick: () -> Unit,
    progress: Float = NavigationProgress.getProgress(Screen.PlacesTutorial2)
) {
    val spacing = LocalDatingAppSpacing.current

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
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 24.dp)
                    .padding(top = 16.dp)
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
                            append("Места дня и люди дня")
                        }
                        withStyle(
                            style = SpanStyle(
                                fontFamily = Typography.bodyLarge.fontFamily,
                                fontWeight = Typography.bodyLarge.fontWeight,
                                fontSize = Typography.bodyLarge.fontSize,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        ) {
                            append(" — ежедневные рекомендации, которые помогут открывать новые места и знакомиться с людьми, разделяющими твои интересы.")
                        }
                    },
                    lineHeight = Typography.bodyLarge.lineHeight,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Сейчас мы покажем тебе подборку дня — ты сможешь добавить понравившиеся места в свой список.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    lineHeight = Typography.bodyLarge.lineHeight,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Готов начать?",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                    lineHeight = Typography.bodyLarge.lineHeight,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.picture_tutorial),
                    contentDescription = "Иллюстрация обучения",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 48.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    WhiteButton(
                        text = "Да",
                        onClick = {
                            // Передаем параметр fromOnboarding=true при навигации к местам дня
                            navController?.navigate("places_of_day?fromOnboarding=true") {
                                popUpTo(Screen.PlacesTutorial2.route) { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(57.dp)
                            .padding(horizontal = 24.dp),
                    )
                }
            }
        }
    }
}