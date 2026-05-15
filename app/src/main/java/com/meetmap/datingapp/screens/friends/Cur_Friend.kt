package com.meetmap.datingapp.screens.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.meetmap.datingapp.R
import com.meetmap.datingapp.components.blocks.CompatibilityScore
import com.meetmap.datingapp.components.blocks.FavPlace
import com.meetmap.datingapp.components.blocks.FriendsHorizontal
import com.meetmap.datingapp.components.blocks.MutPlaces
import com.meetmap.datingapp.components.blocks.UserInfo
import com.meetmap.datingapp.components.progress.ProgressLine
import com.meetmap.datingapp.ui.theme.PurpleCard
import com.meetmap.datingapp.ui.theme.PurpleDark
import com.meetmap.datingapp.ui.theme.boundedFamily
import com.meetmap.datingapp.viewmodels.UserViewModel
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
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(), bottom = 20.dp)
            ) {
                // Убираем Box и делаем Row с правильным распределением
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Левая часть - стрелка и заголовок
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        IconButton(
                            onClick = {
                                navController.popBackStack()
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.arrow_left),
                                contentDescription = "arrow_left",
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

                    // Правая часть - меню с тремя точками
                    Box {
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
                            showTelegram = true,
                            isFriend = true,
                            profileImageUrl = user.profileImageUrl
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