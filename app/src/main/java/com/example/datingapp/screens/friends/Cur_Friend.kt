package com.example.datingapp.screens.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.datingapp.R
import com.example.datingapp.components.blocks.CompatibilityScore
import com.example.datingapp.components.blocks.FavPlace
import com.example.datingapp.components.blocks.FriendsHorizontal
import com.example.datingapp.components.blocks.MutPlaces
import com.example.datingapp.components.blocks.UserInfo
import com.example.datingapp.components.headers.Heading_Arrow
import com.example.datingapp.components.progress.ProgressLine
import com.example.datingapp.data.repository.FriendStatus
import com.example.datingapp.ui.theme.PurpleCard
import com.example.datingapp.ui.theme.PurpleDark
import com.example.datingapp.viewmodels.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun Cur_Friend(
    navController: NavController,
    friendId: String,
    viewModel: UserViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    val otherUser by viewModel.otherUser.collectAsState()
    val mutualFriends by viewModel.mutualFriends.collectAsState()
    val mutualPlaces by viewModel.mutualPlaces.collectAsState()
    val compatibilityPercent by viewModel.compatibilityPercent.collectAsState()
    val myUser by viewModel.myUser.collectAsState()
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
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Heading_Arrow занимает всю ширину
                    Heading_Arrow(
                        heading = if (username.isNotBlank()) "@$username" else "Профиль",
                        navController = navController
                    )

                    // Иконка с тремя точками наложена поверх, справа
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)  // Выравнивание по правому краю
                            .padding(end = 8.dp)  // Отступ от правого края
                    ) {
                        IconButton(
                            onClick = { showMenu = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Меню",
                                tint = PurpleDark,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Перейти в Telegram") },
                                onClick = {
                                    showMenu = false
                                    otherUser?.telegram?.let { telegram ->
                                        if (telegram.isNotBlank()) {
                                            val intent = android.content.Intent(
                                                android.content.Intent.ACTION_VIEW,
                                                android.net.Uri.parse("https://t.me/$telegram")
                                            )
                                            context.startActivity(intent)
                                        }
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.telegram),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = PurpleCard
                                    )
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Удалить из друзей", color = Color.Red) },
                                onClick = {
                                    showMenu = false
                                    showDeleteDialog = true
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_delete),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = Color.Red
                                    )
                                }
                            )
                        }
                    }
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isDeleting) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (otherUser == null) {
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
                            showTelegram = true,  // Показываем Telegram для друзей
                            isFriend = true       // Указываем, что это друг
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

    // Диалог подтверждения удаления из друзей
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить из друзей") },
            text = { Text("Вы уверены, что хотите удалить пользователя из друзей?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        isDeleting = true
                        scope.launch {
                            try {
                                // Удаляем дружбу
                                viewModel.removeFriendshipStatus(
                                    myUserId = myUser?.uid ?: "",
                                    friendId = friendId
                                )
                                // Возвращаемся назад
                                navController.popBackStack()
                            } finally {
                                isDeleting = false
                            }
                        }
                    }
                ) {
                    Text("Удалить", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}