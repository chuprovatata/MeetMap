package com.example.datingapp.components.blocks

import android.widget.Space
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.datingapp.ui.theme.PurpleMedium
import com.example.datingapp.ui.theme.montserratFamily

@Composable
fun FavPlace(place: Place) {
    Box(
        modifier = Modifier
            .fillMaxWidth().clickable{
                //TODO
            }
            .clip(RoundedCornerShape(16.dp))
            .background(color = PurpleMedium)
            .height(240.dp)
            .padding(top = 15.dp)
            .padding(horizontal = 13.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Любимое место",
                style = MaterialTheme.typography.displayMedium,
                fontSize = 25.sp

            )
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()

            ) {
                Image(
                    modifier = Modifier
                        .size(158.dp)
                        .clip(shape = RoundedCornerShape(12.dp)),
                    painter = painterResource(id = place.iconResId),
                    contentDescription = "place"
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column() {

                    Text(
                        text = place.placeName,
                        fontFamily = montserratFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 25.sp,
                        lineHeight = 30.sp,
                        letterSpacing = 0.sp
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                    Text(
                        text = place.placeAddress,
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 15.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(id = place.placeMetroLine),
                            contentDescription = "line"
                        )
                        Spacer(modifier = Modifier.width(9.dp))
                        Text(
                            text = place.placeMetro, style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

            }

        }
    }
}