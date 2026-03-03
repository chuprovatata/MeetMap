package com.example.datingapp.screens.recommendation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.datingapp.R
import com.example.datingapp.components.blocks.Title_Block
import com.example.datingapp.components.headers.Heading_Arrow
import com.example.datingapp.viewmodels.UserViewModel


@Composable
fun PeopleOfDay(navController: NavController, viewModel: UserViewModel) {


    Scaffold(
        topBar = {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 6.dp)
                    .padding(top = 40.dp, bottom = 20.dp)
            ) {
                Heading_Arrow("Люди дня", navController)
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
                "У вас схожие интересы",
                "Вы часто посещаете одни и те же места, может быть это знак?",
                R.drawable.person_on_board,
                false
            )

            //ЭКРАН С ЛЮДЬМИ УЖЕ РЕАЛИЗОВАН!!!!!!!
            //Вызов:


            //navController.navigate(
            // Screen.ReqFriend.passParams(
            //friendId = user.uid, //тут id потенциального друга
            //pageTitle = "Люди дня"
            //)
            //)
        }
    }
}