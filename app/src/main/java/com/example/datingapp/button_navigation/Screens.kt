package com.example.datingapp.button_navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign


@Composable
fun Screen1() {

    Text(text="ПРАВАЫВАОДЫВ",
        modifier = Modifier.fillMaxSize(), textAlign = TextAlign.Center,  fontWeight = FontWeight.Medium
    )
}

@Composable
fun Screen2() {

    Text(text="sc1",
        modifier = Modifier.fillMaxSize(), textAlign = TextAlign.Center)
}


@Composable
fun Screen3() {

    Text(text="ПРАВАЫВАОДЫВ",
        modifier = Modifier.fillMaxSize(), textAlign = TextAlign.Center,  fontWeight = FontWeight.ExtraBold
    )
}

