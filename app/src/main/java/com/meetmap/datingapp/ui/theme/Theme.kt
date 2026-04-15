package com.meetmap.datingapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// СВЕТЛАЯ ТЕМА
private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,              // Основной фиолетовый
    onPrimary = White,                    // Белый текст на кнопках

    background = White,                   // Основной фон
    onBackground = Black,                 // Основной текст

    surface = White,                      // Карточки, панели
    onSurface = Black,

    surfaceVariant = GrayLight,           // Разделы/фон (#E3E3E3)
    onSurfaceVariant = Black,

    outline = GrayMedium                  // Неактивная кнопка (#E0E0E0)
)

// Кастомные токены
data class DatingAppSpacing(
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp
)

data class DatingAppShapes(
    val small: androidx.compose.foundation.shape.CornerBasedShape =
        androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    val medium: androidx.compose.foundation.shape.CornerBasedShape =
        androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    val large: androidx.compose.foundation.shape.CornerBasedShape =
        androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
)

// CompositionLocal для доступа к кастомным токенам
val LocalDatingAppSpacing = staticCompositionLocalOf { DatingAppSpacing() }
val LocalDatingAppShapes = staticCompositionLocalOf { DatingAppShapes() }

@Composable
fun DatingAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme  // Используем только светлую тему

    // Настройка статус бара
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    CompositionLocalProvider(
        LocalDatingAppSpacing provides DatingAppSpacing(),
        LocalDatingAppShapes provides DatingAppShapes()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

// Extension функции для удобного доступа
val MaterialTheme.datingSpacing: DatingAppSpacing
    @Composable
    get() = LocalDatingAppSpacing.current

val MaterialTheme.datingShapes: DatingAppShapes
    @Composable
    get() = LocalDatingAppShapes.current