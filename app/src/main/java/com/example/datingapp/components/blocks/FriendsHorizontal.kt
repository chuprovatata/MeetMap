package com.example.datingapp.components.blocks

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.datingapp.navigation.Screen
import com.example.datingapp.screens.friends.User
import com.example.datingapp.ui.theme.boundedFamily

@Composable
fun FriendsHorizontal(header: String, friends: List<User>,navController: NavController) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = header,
            style = MaterialTheme.typography.displayMedium,
            fontSize = 25.sp

        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            friends.forEach { friend->
                ItemFriendsHorizontal(friend,navController)
                Spacer(modifier = Modifier.width(23.dp))

            }


        }

    }


}

@Composable
fun ItemFriendsHorizontal(user: User,navController: NavController) {
    Column( modifier = Modifier.clickable{
        //ИЗМЕНИТЬ ПОТОМ
        navController.navigate(Screen.CurFriend.route)

    }, horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = user.icon),
            contentDescription = "icon",
            modifier = Modifier.size(78.dp)
        )
        Text(
            text=user.username,

            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraLight,
            fontFamily = boundedFamily
        )
    }
}