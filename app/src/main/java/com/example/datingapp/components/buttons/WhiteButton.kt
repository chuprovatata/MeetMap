// components/buttons/PrimaryButton.kt
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

@Composable
fun WhiteButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    minWidth: Boolean = false,
    width: Int = 360,
    height: Int = 57,
    textSize: TextUnit? = null
) {
    val buttonHeight = height.coerceIn(48, 57).dp
    val buttonWidth = width.coerceIn(280, 360).dp

    val textHorizontalPadding = 36.dp
    val textVerticalPadding = 4.dp

    val containerColor = if (enabled) {
        MaterialTheme.colorScheme.onPrimary // Белый
    } else {
        MaterialTheme.colorScheme.outline // Серый
    }

    val contentColor = if (enabled) {
        MaterialTheme.colorScheme.onBackground // Черный
    } else {
        Color.Gray
    }

    val baseStyle = MaterialTheme.typography.labelLarge

    val buttonTextStyle = if (textSize != null) {
        baseStyle.copy(
            fontSize = textSize,
            textAlign = TextAlign.Center,
            color = contentColor
        )
    } else {
        baseStyle.copy(
            textAlign = TextAlign.Center,
            color = contentColor
        )
    }

    val widthModifier = if (minWidth) {
        Modifier.widthIn(min = buttonWidth)
    } else {
        Modifier.width(buttonWidth)
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .then(widthModifier)
            .height(buttonHeight),
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
                modifier = Modifier
                    .padding(
                        horizontal = textHorizontalPadding,
                        vertical = textVerticalPadding
                    )
            )
        }
    }
}