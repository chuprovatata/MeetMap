package com.example.datingapp.components.buttons

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textSize: TextUnit? = null,
    minHeight: Int = 48,
    maxHeight: Int = 64,
    minWidthFraction: Float = 0.85f,
    maxWidthFraction: Float = 0.9f,
    fixedWidth: Dp? = null,
    fixedHeight: Dp? = null
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val buttonHeight = fixedHeight ?: when {
        configuration.screenWidthDp < 360 -> minHeight.dp // Маленькие экраны
        configuration.screenWidthDp < 600 -> 52.dp       // Средние экраны
        else -> 57.dp                                    // Большие экраны
    }.coerceIn(minHeight.dp, maxHeight.dp)

    val buttonWidth = fixedWidth ?: (screenWidth * minWidthFraction)
        .coerceAtMost(screenWidth * maxWidthFraction)
        .coerceAtLeast(280.dp) // Минимальная ширина

    val textHorizontalPadding = when {
        configuration.screenWidthDp < 360 -> 24.dp
        configuration.screenWidthDp < 480 -> 28.dp
        configuration.screenWidthDp < 600 -> 32.dp
        else -> 36.dp
    }

    val textVerticalPadding = when {
        configuration.screenWidthDp < 360 -> 2.dp
        configuration.screenWidthDp < 480 -> 3.dp
        else -> 4.dp
    }

    val adaptiveTextSize = textSize ?: when {
        configuration.screenWidthDp < 360 -> 16.sp
        configuration.screenWidthDp < 480 -> 18.sp
        else -> 20.sp
    }

    val containerColor = if (enabled) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    val contentColor = if (enabled) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        Color.Gray
    }

    val buttonTextStyle = MaterialTheme.typography.labelLarge.copy(
        fontSize = adaptiveTextSize,
        textAlign = TextAlign.Center,
        color = contentColor
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .widthIn(min = 280.dp, max = buttonWidth)
            .heightIn(min = minHeight.dp, max = buttonHeight)
            .fillMaxWidth(fraction = minWidthFraction),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor,
            disabledContentColor = contentColor
        ),
        enabled = enabled,
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = buttonTextStyle,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(
                        horizontal = textHorizontalPadding,
                        vertical = textVerticalPadding
                    )
            )
        }
    }
}