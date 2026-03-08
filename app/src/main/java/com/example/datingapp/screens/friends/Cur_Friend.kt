package com.example.datingapp.screens.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.datingapp.components.blocks.CompatibilityScore
import com.example.datingapp.components.blocks.FavPlace
import com.example.datingapp.components.blocks.FriendsHorizontal
import com.example.datingapp.components.blocks.MutPlaces
import com.example.datingapp.components.blocks.UserInfo
import com.example.datingapp.components.headers.Heading_Arrow
import com.example.datingapp.components.progress.ProgressLine
import com.example.datingapp.viewmodels.UserViewModel

@Composable
fun Cur_Friend(navController: NavController, friendId: String, viewModel: UserViewModel) {
    val otherUser by viewModel.otherUser.collectAsState()
    val mutualFriends by viewModel.mutualFriends.collectAsState()
    val mutualPlaces by viewModel.mutualPlaces.collectAsState()
    val compatibilityPercent by viewModel.compatibilityPercent.collectAsState()
    val username = otherUser?.username ?: ""

    LaunchedEffect(friendId) {
        viewModel.loadMutualFriends(friendId)
        viewModel.loadUserById(friendId)
        viewModel.loadMutualPlaces(friendId)
        viewModel.loadCompatibility(friendId)
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 6.dp)
                    .padding(top = 40.dp, bottom = 20.dp)
            ) {
                Heading_Arrow(
                    heading = if (username.isNotBlank()) "@$username" else "Профиль",
                    navController = navController
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (otherUser == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 25.dp)
                ) {
                    otherUser?.let { user ->
                        UserInfo(
                            user = user,
                            profileImageUrl = null,
                            isUploadingImage = false,
                            showTelegram = true
                        )

                        Spacer(modifier = Modifier.height(30.dp))

                        ProgressLine(compatibilityPercent / 100f, height = 12)
                        Spacer(modifier = Modifier.height(15.dp))

                        CompatibilityScore(percent = compatibilityPercent)

                        Spacer(modifier = Modifier.height(24.dp))

                        FavPlace(
                            placeName = user.favoritePlace,
                            photoUrl = user.favoritePlacePhoto,
                            isEditable = false
                        )

                        Spacer(modifier = Modifier.height(25.dp))

                        if (mutualPlaces.isNotEmpty()) {
                            MutPlaces(
                                places = mutualPlaces,
                                onPlaceClick = { place ->
                                    navController.navigate("myPlaceDetail/${place.id}")
                                }
                            )
                            Spacer(modifier = Modifier.height(25.dp))
                        }

                        FriendsHorizontal("Общие друзья", mutualFriends, navController)

                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }
    }
}