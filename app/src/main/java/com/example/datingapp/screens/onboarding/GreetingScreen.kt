package com.example.datingapp.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.datingapp.R
import androidx.compose.material3.MaterialTheme

@Composable
fun GreetingScreen(
    onNavigateToLogin: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Привет!",
                    fontSize = 60.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily(
                        Font(R.font.montserrat_bold, FontWeight.Bold)
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 200.dp)
                )

                Image(
                    painter = painterResource(id = R.drawable.picture_second_screen),
                    contentDescription = "Приветственное изображение",
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .scale(1.1f)
                        .padding(start = 16.94.dp)
                        .offset(x = 10.dp, y = 28.dp)
                )
            }
        }
    }
}