package com.example.datingapp.button_navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.datingapp.ui.theme.BlackNav
import com.example.datingapp.ui.theme.montserratFamily
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height

@Composable
fun ButtonNavigation(
    navController: NavController
) {
    // Новый порядок: Мои места, Главная, Знакомства
    val listItems = listOf(
        BottomItem.Screen2,    // Мои места
        BottomItem.ScreenMain, // Главная
        BottomItem.Screen1,    // Знакомства
    )

    // Фиолетовый цвет из ресурсов
    val purpleColor = Color(0xFFA75CC6)

    NavigationBar(
        containerColor = Color.White
    ) {
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        listItems.forEach { item ->
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                },
                icon = {
                    if (item is BottomItem.ScreenMain) {
                        // Специальное оформление для кнопки "Главная" - увеличенный квадрат
                        Box(
                            modifier = Modifier
                                .wrapContentSize()
                                .clip(RoundedCornerShape(20.dp))
                                .then(
                                    if (isSelected) {
                                        Modifier.background(purpleColor)
                                    } else {
                                        Modifier.border(
                                            width = 1.5.dp,
                                            color = purpleColor,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                    }
                                )
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    painter = painterResource(id = item.iconId),
                                    contentDescription = item.title,
                                    tint = if (isSelected) Color.White else BlackNav.copy(alpha = 0.3f)
                                )
                                Text(
                                    text = item.title,
                                    fontSize = 14.sp,
                                    fontFamily = montserratFamily,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else BlackNav.copy(alpha = 0.3f)
                                )
                            }
                        }
                    } else {
                        // Обычное оформление для других кнопок
                        Icon(
                            painter = painterResource(id = item.iconId),
                            contentDescription = item.title,
                            tint = if (isSelected) purpleColor else BlackNav.copy(alpha = 0.3f)
                        )
                    }
                },
                label = {
                    if (item !is BottomItem.ScreenMain) {
                        Text(
                            text = item.title,
                            fontSize = 14.sp,
                            fontFamily = montserratFamily,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isSelected) purpleColor else BlackNav.copy(alpha = 0.3f)
                        )
                    } else {
                        // Для главной текст уже включен в квадрат, поэтому здесь ничего не выводим
                        Box {}
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Transparent,
                    unselectedIconColor = Color.Transparent,
                    selectedTextColor = Color.Transparent,
                    unselectedTextColor = Color.Transparent,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}