package com.meetmap.datingapp.screens.profile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.meetmap.datingapp.R
import com.meetmap.datingapp.components.blocks.FavPlace
import com.meetmap.datingapp.components.blocks.FriendsHorizontal
import com.meetmap.datingapp.components.blocks.UserInfo
import com.meetmap.datingapp.data.repository.FriendStatus
import com.meetmap.datingapp.data.repository.MyUser
import com.meetmap.datingapp.navigation.Screen
import com.meetmap.datingapp.viewmodels.UserViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import com.meetmap.datingapp.ui.theme.GrayLight
import com.meetmap.datingapp.ui.theme.GrayMedium
import com.meetmap.datingapp.ui.theme.GrayPerson
import com.meetmap.datingapp.ui.theme.PurpleMedium
import com.meetmap.datingapp.ui.theme.PurplePrimary
import com.meetmap.datingapp.ui.theme.boundedFamily
private fun getPlacesDeclension(count: Int): String {
    return when {
        count % 10 == 1 && count % 100 != 11 -> "место отмечено"
        count % 10 in 2..4 && (count % 100 < 10 || count % 100 > 20) -> "места отмечено"
        else -> "мест отмечено"
    }
}

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
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 9.dp, end = 19.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false) // занимает место, но не бесконечно
                    ) {
                        IconButton(
                            onClick = {
                                navController.navigate(Screen.Main.route) {
                                    popUpTo(0)
                                }
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.arrow_left),
                                contentDescription = "Назад",
                                tint = Color.Black
                            )
                        }

                        Text(
                            text = if (username.isNotBlank()) "@$username" else "Профиль",
                            fontSize = 35.sp,
                            fontFamily = boundedFamily,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    // Правая часть - иконка настроек
                    IconButton(
                        onClick = {
                            navController.navigate(Screen.Settings.route)
                        },
                        modifier = Modifier.size(48.dp)
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
                    text = getPlacesDeclension(userPlacesCount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "(посмотреть на карте)",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 10.sp,
                        color = PurplePrimary,
                        textDecoration = TextDecoration.Underline
                    ),
                    modifier = Modifier.clickable {
                        navController.navigate(Screen.MyProfileMap.route)
                    }
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