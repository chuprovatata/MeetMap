package com.example.datingapp.components.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SelectableCard(
    imagePainter: Painter,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    val cardCornerRadius = when {
        screenWidth < 360 -> 16.dp    // Маленькие экраны
        screenWidth < 480 -> 18.dp    // Средние экраны
        else -> 19.dp                 // Большие экраны
    }

    val imageCornerRadius = when {
        screenWidth < 360 -> 10.dp    // Маленькие экраны
        screenWidth < 480 -> 11.dp    // Средние экраны
        else -> 12.dp                 // Большие экраны
    }

    val cardPadding = when {
        screenWidth < 360 -> 8.dp     // Маленькие экраны
        screenWidth < 480 -> 10.dp    // Средние экраны
        else -> 12.dp                 // Большие экраны
    }

    val textSize = when {
        screenWidth < 360 -> 12.sp    // Маленькие экраны
        screenWidth < 480 -> 13.sp    // Средние экраны
        else -> 14.sp                 // Большие экраны
    }

    val textTopPadding = when {
        screenWidth < 360 -> 2.dp     // Маленькие экраны
        screenWidth < 480 -> 3.dp     // Средние экраны
        else -> 4.dp                  // Большие экраны
    }

    val borderWidth = when {
        screenWidth < 360 -> if (isSelected) 1.5.dp else 0.8.dp
        screenWidth < 480 -> if (isSelected) 1.8.dp else 1.dp
        else -> if (isSelected) 2.dp else 1.dp
    }

    val defaultBorderColor = Color(0xFFB3B3B3)
    val selectedBorderColor = Color(0xFFB09AFF)
    val selectedBackgroundColor = Color(0xFFF5DEFF)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.98f)
            .background(
                color = if (isSelected) selectedBackgroundColor else Color.Transparent,
                shape = RoundedCornerShape(cardCornerRadius)
            )
            .border(
                width = borderWidth,
                color = if (isSelected) selectedBorderColor else defaultBorderColor,
                shape = RoundedCornerShape(cardCornerRadius)
            )
            .clip(RoundedCornerShape(cardCornerRadius))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.7f)
                    .clip(RoundedCornerShape(imageCornerRadius)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = imagePainter,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.3f)
                    .padding(top = textTopPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = textSize
                    ),
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = when {
                        screenWidth < 360 -> 2  // Маленькие экраны
                        else -> 3               // Остальные
                    },
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

