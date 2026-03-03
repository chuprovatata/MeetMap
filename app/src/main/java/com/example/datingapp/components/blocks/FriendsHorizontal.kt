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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.datingapp.R
import com.example.datingapp.data.repository.MyUser
import com.example.datingapp.navigation.Screen
import com.example.datingapp.screens.friends.User
import com.example.datingapp.ui.theme.PurpleCard
import com.example.datingapp.ui.theme.boundedFamily

@Composable
fun FriendsHorizontal(header: String, friends: List<MyUser>, navController: NavController) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = header,
            style = MaterialTheme.typography.displayMedium,
            fontSize = 25.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (friends.isEmpty()) {
            // Текст без горизонтального скролла
            Column(
                modifier = Modifier
                    .fillMaxWidth()

            ) {
                Text(
                    text = "Находи единомышленников по любимым местам в разделе ",
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Знакомства",
                    color = PurpleCard,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        navController.navigate(Screen.MainMeets.route)
                    }
                )

                Text(
                    text = "или добавляй в друзья, тех кого уже знаешь, в разделе ",
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Друзья",
                    color = PurpleCard,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        navController.navigate(Screen.MainMeets.route)                  }
                )


            }
        } else {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                friends.forEach { friend ->
                    ItemFriendsHorizontal(friend, navController)
                    Spacer(modifier = Modifier.width(23.dp))
                }
            }
        }
    }
}

@Composable
fun ItemFriendsHorizontal(user: MyUser, navController: NavController) {
    Column(modifier = Modifier.clickable {

        navController.navigate(Screen.CurFriend.route)

    }, horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = R.drawable.profile_male),
            contentDescription = "icon",
            modifier = Modifier.size(78.dp)
        )
        Text(
            text = user.username,

            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraLight,
            fontFamily = boundedFamily
        )
    }
}