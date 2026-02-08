package com.example.datingapp.screens.meets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.datingapp.components.headers.Heading
import com.example.datingapp.ui.theme.LocalDatingAppSpacing

@Composable
fun MainMeets(navController: NavController) {
    val spacing = LocalDatingAppSpacing.current

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp).padding(start=9.dp, end=19.dp)
                //ТАК КАК ОТСТУПЫ В ХЕНДИНГЕ НЕ ТАКИЕ КАК В ОСТАЛЬНОМ ПРИЛОЖЕНИИ
                //НАЛИПАЕТ
                //ИЗМЕНИТЬ ГОРИЗОНТАЛЬНЫЙ СПЕЙСИНГ?


            ) {
                Heading("Знакомства",  true, true, navController= navController)

            }
        },
    ) { paddingValues -> }
}