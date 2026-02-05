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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.datingapp.R
import com.example.datingapp.components.buttons.PrimaryButton
import com.example.datingapp.components.buttons.TextButtonWithUnderline
import com.example.datingapp.components.progress.ProgressLine
import com.example.datingapp.navigation.NavigationProgress
import com.example.datingapp.navigation.Screen
import com.example.datingapp.ui.theme.LocalDatingAppSpacing
import com.example.datingapp.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinalTutorialScreen(
    navController: NavController? = null,
    onSkipClick: () -> Unit,
    onReadyClick: () -> Unit,
    progress: Float = NavigationProgress.getProgress(Screen.FinalTutorial)
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
                            text = "Ты добавил первые места и познакомился с нашим приложением!",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                            lineHeight = Typography.bodyLarge.lineHeight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.15f)
                    .padding(horizontal = 24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Теперь ты готов сам отмечать места и знакомиться с новыми людьми.",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        lineHeight = Typography.bodyLarge.lineHeight
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.01f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.99f)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.picture_final_tutorial),
                    contentDescription = "Иллюстрация финального экрана обучения",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 48.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    PrimaryButton(
                        text = "Завершить обучение",
                        onClick = onReadyClick,
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