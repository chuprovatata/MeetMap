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
fun TermsSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    showDetailsLink: Boolean = true,
    onDetailsClick: () -> Unit = {},
    enabled: Boolean = true
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    val horizontalPadding = when {
        screenWidth < 360 -> 0.dp
        screenWidth < 480 -> 2.dp
        else -> 4.dp
    }

    val textSpacing = when {
        screenWidth < 360 -> 6.dp
        screenWidth < 480 -> 8.dp
        else -> 10.dp
    }

    val detailsLinkPadding = when {
        screenWidth < 360 -> 40.dp
        screenWidth < 480 -> 48.dp
        else -> 52.dp
    }

    val mainTextSize = when {
        screenWidth < 360 -> 14.sp
        screenWidth < 480 -> 15.sp
        else -> 16.sp
    }

    val subtitleTextSize = when {
        screenWidth < 360 -> 12.sp
        screenWidth < 480 -> 13.sp
        else -> 14.sp
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = mainTextSize,
                        fontWeight = mainTextWeight
                    ),
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    },
                )

                subtitle?.let {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = subtitleTextSize
                        ),
                        color = if (enabled) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        },
                        lineHeight = subtitleTextSize * 1.2
                    )
                }
            }

            Spacer(modifier = Modifier.width(textSpacing))

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.size(width = 50.dp, height = 30.dp),
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                    uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    disabledCheckedThumbColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    disabledCheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    disabledUncheckedThumbColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    disabledUncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                )
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