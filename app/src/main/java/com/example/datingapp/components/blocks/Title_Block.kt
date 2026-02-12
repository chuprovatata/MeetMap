package com.example.datingapp.components.blocks

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.navigation.NavController
import com.example.datingapp.ui.theme.PurpleCard
import com.example.datingapp.ui.theme.PurpleMedium
import com.example.datingapp.ui.theme.boundedFamily
import com.example.datingapp.ui.theme.montserratFamily
import com.example.datingapp.R
import com.example.datingapp.navigation.Screen
import com.example.datingapp.ui.theme.GrayMedium2


@Composable
fun Title_Block(
    navController: NavController,
    title: String,
    subtitle: String,
    iconId: Int = 0,
    clickable: Boolean = false
) {
    Box(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(16.dp))
            .background(color = PurpleMedium)
            .fillMaxWidth()
            .height(153.dp)
            .clickable(enabled = clickable) {
                if (clickable) {


                    navController.navigate(Screen.MyFriends.route)
                }
            },
    ) {
        Row(
            modifier = Modifier
                .padding(start = 8.dp, top = 9.dp, end=30.dp, bottom = 9.dp)

                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .width(240.dp)

            ) {
                Text(

                    text = title,
                    style = MaterialTheme.typography.headlineLarge,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .width(190.dp)

                ) {
                    Text(

                        text = subtitle,

                        style = MaterialTheme.typography.bodySmall,

                        )
                }


            }
            if (iconId != 0) {
                Box(modifier = Modifier.height(153.dp)
                    .width(120.dp),


                    contentAlignment = Alignment.Center) {

                    Icon(painter = painterResource(id = iconId), contentDescription = "person", modifier = Modifier.size(120.dp))
                }
            }
        }
    }

}

data class Place(
    val nick: String = "",
    val iconResId: Int = R.drawable.place1,
    val placeName: String = "",
    val placeAddress: String="",
    val placeMetro: String="",
    val placeMetroLine: Int = R.drawable.metro1,
    val isPlaceInMy: Boolean = true
)

@Composable
fun Sub_Block(places: List<Place>) {
    Box(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(16.dp))
            .background(color = GrayMedium2)
            .fillMaxWidth()

    ) {
        Row(
            modifier = Modifier
                .padding(start = 8.dp, top = 9.dp, bottom = 9.dp)

                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()

            ) {
                Text(

                    text = "Последние места",
                    style = MaterialTheme.typography.headlineLarge,

                    )
                Spacer(modifier = Modifier.height(16.dp))

                Text(

                    text = "Ура, есть что-то новое!",
                    style = MaterialTheme.typography.bodySmall

                )
                Spacer(modifier = Modifier.height(30.dp))

                places.forEach { place ->

                    Place(place)
                    Spacer(modifier = Modifier.height(22.dp))


                }


            }


        }
    }

}


@Composable
fun Place(
    place: Place
) {

    Row(
        modifier = Modifier

            .fillMaxWidth()
    ) {
        Image(
            modifier = Modifier
                .width(134.dp)
                .height(134.dp)
                .clip(shape = RoundedCornerShape(12.dp)),
            painter = painterResource(id = place.iconResId),
            contentDescription = "place"
        )
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                modifier = Modifier.clickable {
                    //TODO

                },

                text = "@" + place.nick,
                fontSize = 27.sp,
                fontFamily = boundedFamily,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text(

                text = place.placeName,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (place.isPlaceInMy) {
                Button(
                    onClick = {},
                    modifier = Modifier.width(128.dp),
                    shape = RoundedCornerShape(5.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleCard)
                ) {
                    Text(

                        text = "к месту",
                        fontSize = 17.sp,
                        fontFamily = boundedFamily,

                        color = Color.White
                    )

                }


            } else {
                Row() {
                    Image(
                        modifier = Modifier.size(32.dp),
                        painter = painterResource(R.drawable.plus),
                        contentDescription = "plus"
                    )
                    Column(
                        modifier = Modifier
                            .padding(start = 5.dp, top = 7.dp)
                            .fillMaxHeight()
                            .width(100.dp)
                    ) {

                        Text(

                            text = "cовпадение",
                            style = MaterialTheme.typography.bodySmall


                        )
                        Spacer(modifier = Modifier.height(2.dp))

                        Text(

                            text = "это место уже есть в твоей подборке!",
                            fontSize = 10.sp,
                            fontFamily = montserratFamily,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 12.sp,
                            letterSpacing = 0.sp

                        )

                    }
                }
            }

        }


    }


}