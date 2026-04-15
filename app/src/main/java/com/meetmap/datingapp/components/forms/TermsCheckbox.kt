package com.meetmap.datingapp.components.forms

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TermsCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    showDetailsLink: Boolean = true,
    onDetailsClick: () -> Unit = {},
    enabled: Boolean = true
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    val checkboxSize = when {
        screenWidth < 360 -> 20.dp    // Маленькие экраны
        screenWidth < 480 -> 22.dp    // Средние экраны
        else -> 24.dp                 // Большие экраны
    }

    val horizontalPadding = when {
        screenWidth < 360 -> 0.dp     // Маленькие экраны
        screenWidth < 480 -> 2.dp     // Средние экраны
        else -> 4.dp                  // Большие экраны
    }

    val textSpacing = when {
        screenWidth < 360 -> 6.dp     // Маленькие экраны
        screenWidth < 480 -> 8.dp     // Средние экраны
        else -> 10.dp                 // Большие экраны
    }

    val detailsLinkPadding = when {
        screenWidth < 360 -> 40.dp    // Маленькие экраны
        screenWidth < 480 -> 48.dp    // Средние экраны
        else -> 52.dp                 // Большие экраны
    }

    val mainTextSize = when {
        screenWidth < 360 -> 14.sp
        screenWidth < 480 -> 15.sp
        else -> 16.sp
    }

    val linkTextSize = when {
        screenWidth < 360 -> 13.sp
        screenWidth < 480 -> 14.sp
        else -> 15.sp
    }

    val mainTextWeight = when {
        screenWidth < 360 -> FontWeight.Normal
        screenWidth < 480 -> FontWeight.Medium
        else -> FontWeight.SemiBold
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                    disabledCheckedColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    disabledUncheckedColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                modifier = Modifier.size(checkboxSize),
                enabled = enabled
            )

            Spacer(modifier = Modifier.width(textSpacing))

            Text(
                text = "Я согласен с правилами обработки персональных данных",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = mainTextSize,
                    fontWeight = mainTextWeight
                ),
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                },
                modifier = Modifier.weight(1f)
            )
        }

        if (showDetailsLink) {
            TextButton(
                onClick = onDetailsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = detailsLinkPadding),
                enabled = enabled,
                contentPadding = PaddingValues(
                    vertical = 4.dp,
                    horizontal = 0.dp
                )
            ) {
                Text(
                    text = "Читать подробнее",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = linkTextSize
                    ),
                    color = if (enabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                )
            }
        }
    }
}