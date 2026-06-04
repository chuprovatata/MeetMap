package com.meetmap.datingapp.utils

data class CompatibilityShortText(
    val accent: String,
    val description: String
)

fun getCompatibilityReasonText(percent: Int): String {
    return when (percent.coerceIn(0, 100)) {
        in 0..9 -> "Вы выбираете разные места"
        in 10..19 -> "У вас нашлась первая общая точка"
        in 20..29 -> "Вы бываете в похожих местах"
        in 30..39 -> "У вас есть общие любимые места"
        in 40..49 -> "Ваши маршруты заметно пересекаются"
        in 50..59 -> "Вы часто выбираете похожие места"
        in 60..69 -> "Ваши любимые места сильно пересекаются"
        in 70..79 -> "У вас много общих точек на карте"
        in 80..89 -> "Ваши маршруты почти совпадают"
        else -> "Вы почти идеально совпали по местам"
    }
}

fun getCompatibilityShortText(percent: Int): CompatibilityShortText {
    return when (percent.coerceIn(0, 100)) {
        in 0..20 -> CompatibilityShortText(
            accent = "Слабое",
            description = "совпадение"
        )

        in 20..70 -> CompatibilityShortText(
            accent = "Среднее",
            description = "совпадение"
        )

        else -> CompatibilityShortText(
            accent = "Сильное",
            description = "совпадение"
        )
    }
}