package com.example.datingapp.screens.profile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.datingapp.R
import com.example.datingapp.components.blocks.FavPlace
import com.example.datingapp.components.blocks.FriendsHorizontal
import com.example.datingapp.components.blocks.UserInfo
import com.example.datingapp.components.headers.Heading_Arrow
import com.example.datingapp.data.repository.FriendStatus
import com.example.datingapp.data.repository.MyUser
import com.example.datingapp.navigation.Screen
import com.example.datingapp.viewmodels.UserViewModel
import androidx.compose.runtime.collectAsState

@Composable
fun MyProfile(navController: NavController, viewModel: UserViewModel) {
    val isUploadingImage by viewModel.isUploadingImage.collectAsState()
    val isUploadingFavoritePlace by viewModel.isUploadingFavoritePlace.collectAsState()
    val context = LocalContext.current
    val user by viewModel.myUser.collectAsState()
    val profileImageUrl by viewModel.profileImageUrl.collectAsState()
    val userData by viewModel.userData.collectAsState()
    val userPlacesCount by viewModel.userPlacesCount.collectAsState()

    var profileImageState by remember { mutableStateOf<Any?>(null) }

    LaunchedEffect(profileImageUrl) {
        profileImageState = viewModel.getProfileImageUrl(profileImageUrl)
    }

    LaunchedEffect(Unit) {
        viewModel.loadUserData()
        viewModel.loadMyUser()
        viewModel.refreshUserPlacesCount()
    }

    DisposableEffect(Unit) {
        onDispose {
        }
    }

    val username = userData?.get("username") as? String ?: user?.username ?: ""
    val favoritePlaceName = userData?.get("favoritePlace") as? String ?: user?.favoritePlace ?: ""
    val favoritePlacePhoto = userData?.get("favoritePlacePhoto") as? String ?: user?.favoritePlacePhoto ?: ""

    LaunchedEffect(userData, user) {
        Log.d("MyProfile", "favoritePlaceName: $favoritePlaceName")
        Log.d("MyProfile", "favoritePlacePhoto: $favoritePlacePhoto")
        Log.d("MyProfile", "user: $user")
        Log.d("MyProfile", "userData: $userData")
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(top = 40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 9.dp, end = 19.dp)
                ) {
                    Heading_Arrow(
                        heading = if (username.isNotBlank()) "@$username" else "Профиль",
                        navController = navController,
                        onBackClick = {
                            // ИСПРАВЛЕНО: Используем navigateUp вместо popBackStack
                            // Это вернет на предыдущий экран в стеке, а не на самый первый
                            navController.navigateUp()
                        }
                    )

                    IconButton(
                        onClick = {
                            navController.navigate(Screen.Settings.route)
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_settings),
                            contentDescription = "Настройки",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
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
            UserInfo(
                user = user,
                profileImageUrl = profileImageUrl,
                isUploadingImage = isUploadingImage,
                showTelegram = false
            )
            Spacer(modifier = Modifier.height(30.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = userPlacesCount.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    text = "места отмечено",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 20.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            FavPlace(
                placeName = favoritePlaceName,
                photoUrl = favoritePlacePhoto,
                isUploading = isUploadingFavoritePlace,
                isEditable = true,
                onCardClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
            Spacer(modifier = Modifier.height(25.dp))

            var curFriends by remember { mutableStateOf<List<MyUser>>(emptyList()) }

            LaunchedEffect(Unit) {
                curFriends = viewModel.getUsersByFriendStatus(FriendStatus.FRIEND)
            }

            FriendsHorizontal(
                "Мои друзья",
                curFriends,
                navController
            )

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}