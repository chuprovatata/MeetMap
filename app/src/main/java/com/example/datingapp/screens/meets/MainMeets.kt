package com.example.datingapp.screens.meets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.datingapp.R
import com.example.datingapp.components.blocks.Title_Block
import com.example.datingapp.components.headers.Heading
import com.example.datingapp.components.segmentedButton.CustomTabsComponent
import com.example.datingapp.navigation.Screen
import com.example.datingapp.screens.friends.User
import com.example.datingapp.screens.friends.UserItem
import com.example.datingapp.ui.theme.GrayMedium
import com.example.datingapp.ui.theme.LocalDatingAppSpacing
import com.example.datingapp.ui.theme.PurpleCard
import com.example.datingapp.ui.theme.montserratFamily


val friends = listOf(
    User(
        name = "Александр Ляля",
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

@Composable
fun MainMeets(navController: NavController) {
    val spacing = LocalDatingAppSpacing.current
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp)

                //ТАК КАК ОТСТУПЫ В ХЕНДИНГЕ НЕ ТАКИЕ КАК В ОСТАЛЬНОМ ПРИЛОЖЕНИИ
                //НАЛИПАЕТ
                //ИЗМЕНИТЬ ГОРИЗОНТАЛЬНЫЙ СПЕЙСИНГ?


            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 9.dp, end = 19.dp)
                ) {
                    Heading("Знакомства", true, true, navController = navController)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 25.dp)
                ) {
                    CustomTabsComponent(
                        "Рекомендации",
                        "Заявки в друзья",
                        0, 0,
                        onTabSelected = { tabIndex -> selectedTab = tabIndex }
                    )
                }


            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .padding(top = 30.dp)
        ) {

            when (selectedTab) {
                0 -> {
                    // Экран для вкладки "Рекомендации"
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),

                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        item {
                            Title_Block(
                                navController,
                                "У вас схожие интересы",
                                "Вы часто посещаете одни и те же места, может быть это знак?",
                                0,
                                false

                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                        items(friends.size) { index ->
                            ItemMeets(friends[index], navController,"meet")
                        }

                    }

                }

                1 -> {

                    // Экран для вкладки "познакомиться"
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),

                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        item {
                            Title_Block(
                                navController,
                                "С тобой хотят познакомиться",
                                "Посмотри, может быть ваши пути сойдутся?",
                                R.drawable.person_on_board,
                                false

                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                        items(friends.size) { index ->
                            ItemMeets(friends[index], navController, "notmeet")
                        }

                    }

                }
            }


        }


    }


}




