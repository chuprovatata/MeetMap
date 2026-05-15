package com.meetmap.datingapp.screens.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import com.meetmap.datingapp.R
import com.meetmap.datingapp.components.headers.Heading_Arrow


@Composable
fun My_Friends(navController: NavController) {

    Scaffold(
        topBar = {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 6.dp)
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(), bottom = 20.dp)
            ) {
                Heading_Arrow("Мои друзья", navController)
            }
        }
    ) { paddingValues ->


        val friends = listOf(
            User(
                name = "Александр",
                username = "@alex_tech",
                icon = R.drawable.profile_male,
                age = "25",
                university = "МГТУ",
                mutFriends = listOf(),

                mutPlaces = listOf()
            ),

            User(
                name = "Екатерина",
                username = "@katya_design",
                icon = R.drawable.profile_female,
                age = "22",
                university = "ВШЭ",
                mutFriends = listOf(),
                mutPlaces =
                    listOf(),
            ),

            User(
                name = "Михаил",
                username = "@mike_sports",
                icon = R.drawable.profile_male,
                age = "24",
                university = "РГУФК",
                mutFriends = listOf(),
                mutPlaces =
                    listOf()
            ),

            User(
                name = "Анна",
                username = "@anna_science",
                icon = R.drawable.profile_female,
                age = "23",
                university = "МГУ",
                mutFriends = listOf(),
                mutPlaces =
                    listOf()
            ),

            User(
                name = "Дмитрий",
                username = "@dima_music",
                icon = R.drawable.profile_male,
                age = "21",
                university = "Консерватория",
                mutFriends = listOf(),
                mutPlaces =
                    listOf()
            ),

            User(
                name = "Ольга",
                username = "@olga_foodie",
                icon = R.drawable.profile_female,
                age = "26",
                university = "РЭУ",
                mutFriends = listOf(),
                mutPlaces =
                    listOf()
            ),

            User(
                name = "Артем",
                username = "@artem_gamedev",
                icon = R.drawable.profile_male,
                age = "24",
                university = "МИРЭА",
                mutFriends = listOf(),
                mutPlaces =
                    listOf()
            ),

            User(
                name = "София",
                username = "@sofia_books",
                icon = R.drawable.profile_female,
                age = "20",
                university = "МПГУ",
                mutFriends = listOf(),
                mutPlaces = listOf()
            ),

            User(
                name = "Иван",
                username = "@ivan_travel",
                icon = R.drawable.profile_male,
                age = "27",
                university = "МГИМО",
                mutFriends = listOf(),
                mutPlaces = listOf()
            ),

            User(
                name = "Мария",
                username = "@maria_art",
                icon = R.drawable.profile_female,
                age = "22",
                university = "Строгановка",
                mutFriends = listOf(),
                mutPlaces = listOf()
            ),

            User(
                name = "Кирилл",
                username = "@kirill_it",
                icon = R.drawable.profile_male,
                age = "25",
                university = "МФТИ",
                mutFriends = listOf(),
                mutPlaces = listOf()
            ),

            User(
                name = "Полина",
                username = "@polina_dance",
                icon = R.drawable.profile_female,
                age = "21",
                university = "ГИТИС",
                mutFriends = listOf(),
                mutPlaces = listOf()
            ),

            User(
                "Паулина ИВанован Саыаываывввввввввввввввввввввввввввввввввво",
                "@anna",
                R.drawable.profile_female,
                "20",
                "ВШЭ",
                listOf(),
                mutPlaces = listOf()

            )

        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 10.dp,
                    end = 28.dp,
                    bottom = 80.dp,
                    top = paddingValues.calculateTopPadding()
                )
        ) {
            items(friends.size) { item ->

                UserItem(friends[item],navController)
            }
        }

    }
}






