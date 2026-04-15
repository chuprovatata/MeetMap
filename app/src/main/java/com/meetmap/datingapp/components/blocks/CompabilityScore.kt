package com.meetmap.datingapp.components.blocks

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CompatibilityScore(
    percent: Int,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "$percent%",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = "ваших мест совпадают!\n${getScoreDescription(percent)}",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 15.sp
        )
    }
}

fun getScoreDescription(percent: Int): String {
    return when {
        percent >= 80 -> "вы очень близки по интересам!"
        percent >= 60 -> "это больше, чем в среднем"
        percent >= 40 -> "есть общие интересы"
        percent >= 20 -> "можно найти что-то общее"
        else -> "попробуйте узнать друг друга лучше"
    }
}