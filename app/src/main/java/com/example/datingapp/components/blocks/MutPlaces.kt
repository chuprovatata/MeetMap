package com.example.datingapp.components.blocks

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.datingapp.ui.theme.GrayMedium2
import com.example.datingapp.ui.theme.PurpleMedium

@Composable
fun MutPlaces(places: List<Place>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(color = GrayMedium2)

            .padding(top = 10.dp)
            .padding(horizontal = 13.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Любимое место",
                style = MaterialTheme.typography.displayMedium,
                fontSize = 25.sp

            )
            Spacer(modifier = Modifier.height(20.dp))

            places.forEach { place ->

                MutItemPlaces(place)
                Spacer(modifier = Modifier.height(15.dp))


            }


        }
    }
}


@Composable
fun MutItemPlaces(place: Place) {
    Row(modifier = Modifier.fillMaxWidth().clickable{
        //TODO
    }) {
        Image(
            modifier = Modifier
                .size(78.dp)
                .clip(shape = RoundedCornerShape(12.dp)),
            painter = painterResource(id = place.iconResId),
            contentDescription = "place"
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = place.placeName,
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 20.sp

        )
    }


}