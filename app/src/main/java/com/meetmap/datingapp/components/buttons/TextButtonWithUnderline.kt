package com.example.datingapp.components.buttons

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TextButtonWithUnderline(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showUnderline: Boolean = false,
    textColor: Color? = null,
    textStyle: TextStyle? = null,
    fontWeight: FontWeight? = null,
    fontSize: Int? = null,
    enabled: Boolean = true
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    val adaptiveFontSize = fontSize ?: when {
        screenWidth < 360 -> 14 // Маленькие экраны
        screenWidth < 480 -> 16 // Средние экраны
        else -> 18 // Большие экраны
    }

    val adaptiveFontWeight = fontWeight ?: when {
        screenWidth < 360 -> FontWeight.Normal
        screenWidth < 480 -> FontWeight.Medium
        else -> FontWeight.SemiBold
    }

    val adaptivePadding = when {
        screenWidth < 360 -> 4.dp
        screenWidth < 480 -> 6.dp
        else -> 8.dp
    }

    TextButton(
        onClick = onClick,
        modifier = modifier.wrapContentWidth(),
        shape = RectangleShape,
        colors = ButtonDefaults.textButtonColors(
            contentColor = Color.Transparent
        ),
        contentPadding = PaddingValues(adaptivePadding),
        enabled = enabled
    ) {
        val annotatedText = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    color = if (enabled) {
                        textColor ?: MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    },
                    fontSize = adaptiveFontSize.sp,
                    fontWeight = adaptiveFontWeight,
                    textDecoration = if (showUnderline) TextDecoration.Underline else TextDecoration.None
                )
            ) {
                append(text)
            }
        }

        Text(
            text = annotatedText,
            style = textStyle ?: MaterialTheme.typography.bodyMedium
        )
    }
}