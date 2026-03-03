package com.example.datingapp.button_navigation


import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.datingapp.ui.theme.BlackNav
import com.example.datingapp.ui.theme.montserratFamily

@Composable
fun ButtonNavigation(
    navController: NavController
) {
    val listItems = listOf(
        BottomItem.Screen2,
        BottomItem.Screen1,



        )
    NavigationBar(
        containerColor = Color.White
    ) {
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route
        listItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute in item.relatedRoutes,
                onClick = {

                    navController.navigate(item.route) {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                },
                icon = {

                    Icon(painter = painterResource(id = item.iconId), contentDescription = "456")

                },
                label = {
                    Text(text = item.title, fontSize = 14.sp,  fontFamily =montserratFamily,
                        fontWeight = if (currentRoute == item.route){
                            FontWeight.SemiBold
                        }
                        else{
                            FontWeight.Medium

                        }
                    )

                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    unselectedIconColor =  BlackNav.copy(alpha = 0.3f),
                    selectedTextColor = Color.Black,
                    unselectedTextColor = Color.Black.copy(alpha = 0.3f),
                    indicatorColor = Color.Transparent


                )
            )


        }


    }

}