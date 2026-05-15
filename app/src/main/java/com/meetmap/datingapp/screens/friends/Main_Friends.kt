package com.meetmap.datingapp.screens.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.meetmap.datingapp.R
import com.meetmap.datingapp.components.blocks.Place
import com.meetmap.datingapp.components.blocks.Sub_Block
import com.meetmap.datingapp.components.blocks.Title_Block
import com.meetmap.datingapp.components.headers.Heading

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun Main_Friends(navController: NavController ) {

    Scaffold(
        topBar = {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(), horizontal = 25.dp)
            ) {
                Heading("Мои друзья",  false, false, navController=navController)
            }
        },
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 25.dp)
        ) {


            Title_Block(
                navController,
                "Cписок друзей",
                "Посмотри, может быть ты кому-то давно не писал?",
                R.drawable.person_on_board,
                true

            )
            //Получение спсика друзей из бд
            val examplePlaces = listOf(
                Place(
                    nick = "anna",
                    iconResId = R.drawable.place1,
                    placeName = "Surf Coffee",
                    isPlaceInMy = true
                ),
                Place(
                    nick = "maksim",
                    iconResId = R.drawable.place1,
                    placeName = "Кофейня у моря",
                    isPlaceInMy = false
                ),
                Place(
                    nick = "olga",
                    iconResId = R.drawable.place1,
                    placeName = "Библиотека Бук",
                    isPlaceInMy = true
                ),
                Place(
                    nick = "dmitry",
                    iconResId = R.drawable.place1,
                    placeName = "Бургерная №1",
                    isPlaceInMy = true
                ),
                Place(
                    nick = "ekaterina",
                    iconResId = R.drawable.place1,
                    placeName = "Йога-студия",
                    isPlaceInMy = false
                ),
                Place(
                    nick = "ivan",
                    iconResId = R.drawable.place1,
                    placeName = "Парк Горького",
                    isPlaceInMy = true
                ),
                Place(
                    nick = "maria",
                    iconResId = R.drawable.place1,
                    placeName = "Книжный магазин",
                    isPlaceInMy = false
                ),
            )
            Spacer(modifier = Modifier.height(23.dp))

            Sub_Block(examplePlaces)
            Spacer(modifier = Modifier.height(100.dp))




        }
    }
}