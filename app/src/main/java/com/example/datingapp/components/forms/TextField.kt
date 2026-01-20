package com.example.datingapp.components.forms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.datingapp.ui.theme.GrayLight
import com.example.datingapp.ui.theme.White

@Composable
fun DatingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    maxCharacters: Int = Int.MAX_VALUE,
    showCharacterCounter: Boolean = false,
    focusRequester: FocusRequester? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    val adaptiveLabelSize = when {
        screenWidth < 360 -> 12.sp    // Маленькие экраны
        screenWidth < 480 -> 13.sp    // Средние экраны
        else -> 14.sp                 // Большие экраны
    }

    val adaptiveTextSize = when {
        screenWidth < 360 -> 14.sp    // Маленькие экраны
        screenWidth < 480 -> 15.sp    // Средние экраны
        else -> 16.sp                 // Большие экраны
    }

    val adaptivePlaceholderSize = when {
        screenWidth < 360 -> 13.sp    // Маленькие экраны
        screenWidth < 480 -> 14.sp    // Средние экраны
        else -> 15.sp                 // Большие экраны
    }

    val adaptiveCounterSize = when {
        screenWidth < 360 -> 10.sp    // Маленькие экраны
        screenWidth < 480 -> 11.sp    // Средние экраны
        else -> 12.sp                 // Большие экраны
    }

    val adaptiveErrorSize = when {
        screenWidth < 360 -> 11.sp    // Маленькие экраны
        screenWidth < 480 -> 12.sp    // Средние экраны
        else -> 13.sp                 // Большие экраны
    }

    // Адаптивные отступы
    val adaptiveLabelPadding = when {
        screenWidth < 360 -> 4.dp     // Маленькие экраны
        screenWidth < 480 -> 6.dp     // Средние экраны
        else -> 8.dp                  // Большие экраны
    }

    val adaptiveFieldPadding = when {
        screenWidth < 360 -> PaddingValues(horizontal = 12.dp, vertical = 10.dp)
        screenWidth < 480 -> PaddingValues(horizontal = 14.dp, vertical = 12.dp)
        else -> PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    }

    val adaptiveErrorPadding = when {
        screenWidth < 360 -> 2.dp     // Маленькие экраны
        screenWidth < 480 -> 3.dp     // Средние экраны
        else -> 4.dp                  // Большие экраны
    }

    val adaptiveSpacing = when {
        screenWidth < 360 -> 6.dp     // Маленькие экраны
        screenWidth < 480 -> 8.dp     // Средние экраны
        else -> 10.dp                 // Большие экраны
    }

    val handleValueChange = { newValue: String ->
        if (newValue.length <= maxCharacters) {
            onValueChange(newValue)
        } else {
            onValueChange(newValue.take(maxCharacters))
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Лейбл
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(
                fontSize = adaptiveLabelSize
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = adaptiveLabelPadding)
        )

        // Контейнер для поля ввода
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (screenWidth < 360) 0.8.dp else 1.dp,
                    color = if (isError) MaterialTheme.colorScheme.error else GrayLight,
                    shape = RoundedCornerShape(if (screenWidth < 360) 6.dp else 8.dp)
                )
                .background(White, RoundedCornerShape(if (screenWidth < 360) 6.dp else 8.dp))
                .padding(adaptiveFieldPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Текстовое поле
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        BasicTextField(
                            value = value,
                            onValueChange = handleValueChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        view.postDelayed({
                                            keyboardController?.show()
                                        }, 100)
                                    }
                                },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = adaptiveTextSize
                            ),
                            visualTransformation = visualTransformation,
                            keyboardOptions = keyboardOptions,
                            keyboardActions = keyboardActions,
                            singleLine = singleLine,
                            maxLines = maxLines,
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            decorationBox = { innerTextField ->
                                Box(
                                    contentAlignment = Alignment.TopStart,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Плейсхолдер
                                    if (value.isEmpty()) {
                                        Text(
                                            text = placeholder,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                fontSize = adaptivePlaceholderSize
                                            )
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }

                    if (trailingIcon != null) {
                        Spacer(modifier = Modifier.width(adaptiveSpacing))
                        trailingIcon()
                    }
                }

                // Счетчик символов
                if (showCharacterCounter && maxCharacters != Int.MAX_VALUE) {
                    Spacer(modifier = Modifier.height(if (screenWidth < 360) 2.dp else 4.dp))
                    Text(
                        text = "${value.length}/$maxCharacters",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = adaptiveCounterSize
                        ),
                        color = when {
                            value.length > maxCharacters -> MaterialTheme.colorScheme.error
                            value.length > maxCharacters * 0.8 -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        },
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }

        if (isError && errorMessage != null) {
            Spacer(modifier = Modifier.height(adaptiveErrorPadding))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = adaptiveErrorSize
                ),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = if (screenWidth < 360) 2.dp else 4.dp)
            )
        }
    }
}