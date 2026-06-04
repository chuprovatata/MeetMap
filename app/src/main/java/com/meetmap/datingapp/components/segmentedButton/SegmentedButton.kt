package com.meetmap.datingapp.components.segmentedButton

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meetmap.datingapp.ui.theme.GrayMedium
import com.meetmap.datingapp.ui.theme.PurpleCard
import com.meetmap.datingapp.ui.theme.montserratFamily

@Composable
fun CustomTabsComponent(
    title1: String,
    title2: String,
    icon1: Int = 0,
    icon2: Int = 0,
    selectedTab: Int=0,
    onTabSelected: (Int) -> Unit = {}
) {




    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = GrayMedium)

    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center

        ) {

            Box(
                modifier = Modifier

                    .fillMaxHeight()
                    .weight(0.5f)


                    .clickable {

                        onTabSelected(0)
                    }


                    .background(
                        color = if (selectedTab == 0) PurpleCard else GrayMedium,
                        shape = RoundedCornerShape(
                            topStart = if (selectedTab == 0) 0.dp else 16.dp,
                            topEnd = if (selectedTab == 0) 16.dp else 0.dp,
                            bottomEnd = if (selectedTab == 0) 16.dp else 0.dp,
                            bottomStart = if (selectedTab == 0) 0.dp else 16.dp
                        )

                    )


            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {


                    Text(
                        text = title1,
                        fontFamily = montserratFamily,
                        fontSize = 19.sp,

                        fontWeight = if (
                            selectedTab == 0) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Medium
                        },

                        color = if (selectedTab == 0) {
                            Color.White
                        } else {
                            Color.Black
                        }

                    )

                    if (icon1 != 0) {
                        Icon(painter = painterResource(id = icon1), contentDescription = "icon")
                    }
                }


            }
            Box(
                modifier = Modifier


                    .fillMaxHeight()
                    .weight(0.5f)


                    .clickable {

                        onTabSelected(1)
                    }


                    .background(
                        color = if (selectedTab == 1) PurpleCard else GrayMedium,
                        shape = RoundedCornerShape(
                            topStart = if (selectedTab == 0) 0.dp else 16.dp,
                            topEnd = if (selectedTab == 0) 16.dp else 0.dp,
                            bottomEnd = if (selectedTab == 0) 16.dp else 0.dp,
                            bottomStart = if (selectedTab == 0) 0.dp else 16.dp
                        )

                    )


            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {


                    Text(
                        text = title2,
                        fontFamily = montserratFamily,
                        fontSize = 19.sp,

                        fontWeight = if (selectedTab == 1) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Medium
                        },

                        color = if (selectedTab == 1) {
                            Color.White
                        } else {
                            Color.Black
                        }

                    )

                    if (icon2 != 0) {
                        Icon(painter = painterResource(id = icon2), contentDescription = "icon")
                    }
                }


            }
        }
    }





}