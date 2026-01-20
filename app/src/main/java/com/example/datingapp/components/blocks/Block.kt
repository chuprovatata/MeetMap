// components/blocks/Block.kt
package com.example.datingapp.components.blocks

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


sealed class BlockType {
    object Default : BlockType() // По умолчанию
    object Light : BlockType()   // Светлый фон
    object Dark : BlockType()    // Темный фон
    object Primary : BlockType() // Основной цвет (фиолетовый)
    object Gradient : BlockType() // Градиентный фон
}

sealed class BlockContent {
    data class PlaceList(val places: List<PlaceItem>) : BlockContent()
    data class SinglePlace(val place: PlaceItem) : BlockContent()
    data class SinglePerson(val person: PersonItem) : BlockContent()
    data class Statistics(val data: StatisticsData) : BlockContent()
    data class Navigation(val section: String, val icon: ImageVector? = null) : BlockContent()
}

data class PlaceItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector? = null,
    val distance: String? = null,
    val matchPercentage: Int? = null
)

data class PersonItem(
    val id: String,
    val name: String,
    val age: Int,
    val university: String,
    val icon: ImageVector? = null,
    val matchPercentage: Int
)

data class StatisticsData(
    val title: String,
    val value: String,
    val subtitle: String,
    val trend: String? = null
)


@Composable
fun Block(
    title: String,
    subtitle: String? = null,
    type: BlockType = BlockType.Default,
    content: BlockContent,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    elevation: Dp = 4.dp,
    showDivider: Boolean = false,
    backgroundColor: Color? = null,
    contentColor: Color? = null
) {
    val (bgColor, textColor, secondaryColor) = when (type) {
        is BlockType.Light -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurface,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        is BlockType.Dark -> Triple(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.onSurface,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        is BlockType.Primary -> Triple(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.onPrimary,
            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
        )
        is BlockType.Gradient -> Triple(
            Color.Transparent,
            Color.White,
            Color.White.copy(alpha = 0.8f)
        )
        else -> Triple(
            backgroundColor ?: MaterialTheme.colorScheme.surface,
            contentColor ?: MaterialTheme.colorScheme.onSurface,
            contentColor?.copy(alpha = 0.8f) ?: MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(
            containerColor = if (type is BlockType.Gradient) {
                Color.Transparent
            } else {
                bgColor
            }
        )
    ) {
        if (type is BlockType.Gradient) {
            Box(
                modifier = Modifier
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    )
                    .clip(RoundedCornerShape(16.dp))
            ) {
                BlockContent(
                    title = title,
                    subtitle = subtitle,
                    textColor = textColor,
                    secondaryColor = secondaryColor,
                    content = content,
                    showDivider = showDivider
                )
            }
        } else {
            BlockContent(
                title = title,
                subtitle = subtitle,
                textColor = textColor,
                secondaryColor = secondaryColor,
                content = content,
                showDivider = showDivider
            )
        }
    }
}

@Composable
private fun BlockContent(
    title: String,
    subtitle: String?,
    textColor: Color,
    secondaryColor: Color,
    content: BlockContent,
    showDivider: Boolean
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = secondaryColor
                    )
                )
            }
        }

        if (showDivider) {
            Divider(
                color = secondaryColor.copy(alpha = 0.2f),
                thickness = 1.dp
            )
        }

        when (content) {
            is BlockContent.PlaceList -> PlaceListContent(
                places = content.places,
                textColor = textColor,
                secondaryColor = secondaryColor
            )
            is BlockContent.SinglePlace -> SinglePlaceContent(
                place = content.place,
                textColor = textColor,
                secondaryColor = secondaryColor
            )
            is BlockContent.SinglePerson -> SinglePersonContent(
                person = content.person,
                textColor = textColor,
                secondaryColor = secondaryColor
            )
            is BlockContent.Statistics -> StatisticsContent(
                data = content.data,
                textColor = textColor,
                secondaryColor = secondaryColor
            )
            is BlockContent.Navigation -> NavigationContent(
                section = content.section,
                icon = content.icon,
                textColor = textColor,
                secondaryColor = secondaryColor
            )
        }
    }
}

@Composable
private fun PlaceListContent(
    places: List<PlaceItem>,
    textColor: Color,
    secondaryColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        places.forEach { place ->
            PlaceItemRow(
                place = place,
                textColor = textColor,
                secondaryColor = secondaryColor,
                showDistance = true
            )
        }
    }
}

@Composable
private fun SinglePlaceContent(
    place: PlaceItem,
    textColor: Color,
    secondaryColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Иконка места
            if (place.icon != null) {
                Icon(
                    imageVector = place.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            } else {
                // Заглушка
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = place.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                )
                place.subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = secondaryColor
                        )
                    )
                }
            }

            place.matchPercentage?.let { percentage ->
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

@Composable
private fun SinglePersonContent(
    person: PersonItem,
    textColor: Color,
    secondaryColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (person.icon != null) {
            Icon(
                imageVector = person.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(80.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = person.name.first().toString(),
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = person.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                )
                Text(
                    text = "${person.age} лет",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = secondaryColor
                    )
                )
            }

            Text(
                text = person.university,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = secondaryColor
                )
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = person.matchPercentage / 100f,
                    modifier = Modifier
                        .height(8.dp)
                        .weight(1f),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )
                Text(
                    text = "${person.matchPercentage}%",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                )
            }
        }
    }
}

@Composable
private fun StatisticsContent(
    data: StatisticsData,
    textColor: Color,
    secondaryColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = data.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = secondaryColor
            )
        )
        Text(
            text = data.value,
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = data.subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = secondaryColor
                )
            )
            data.trend?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Composable
private fun NavigationContent(
    section: String,
    icon: ImageVector?,
    textColor: Color,
    secondaryColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = section,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        )
        Icon(
            imageVector = icon ?: Icons.Default.ArrowForward,
            contentDescription = "Перейти",
            tint = secondaryColor,
            modifier = Modifier.size(20.dp)
        )
    }
}


@Composable
private fun PlaceItemRow(
    place: PlaceItem,
    textColor: Color,
    secondaryColor: Color,
    showDistance: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (place.icon != null) {
                Icon(
                    imageVector = place.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = place.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                )
                place.subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = secondaryColor
                        )
                    )
                }
            }
        }

        if (showDistance && place.distance != null) {
            Text(
                text = place.distance,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = secondaryColor
                )
            )
        }
    }
}