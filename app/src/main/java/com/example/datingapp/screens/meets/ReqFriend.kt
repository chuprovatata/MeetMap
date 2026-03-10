package com.example.datingapp.screens.meets

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.datingapp.R
import com.example.datingapp.components.blocks.FavPlace
import com.example.datingapp.components.blocks.FriendsHorizontal
import com.example.datingapp.components.blocks.MutPlaces
import com.example.datingapp.components.blocks.UserInfo
import com.example.datingapp.components.headers.Heading_Arrow
import com.example.datingapp.components.progress.ProgressLine
import com.example.datingapp.data.repository.FriendStatus
import com.example.datingapp.navigation.Screen
import com.example.datingapp.ui.theme.GrayDark
import com.example.datingapp.ui.theme.LocalDatingAppSpacing
import com.example.datingapp.ui.theme.PurpleCard
import com.example.datingapp.viewmodels.NotificationViewModel
import com.example.datingapp.viewmodels.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun ReqFriend(
    navController: NavController,
    viewModel: UserViewModel,
    friendId: String,
    pageTitle: String,
    hasBottomNavigation: Boolean = false  // По умолчанию false, так как экран может открываться из разных мест
) {
    val spacing = LocalDatingAppSpacing.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    // Получаем NotificationViewModel
    val notificationViewModel: NotificationViewModel = hiltViewModel()

    // Состояние для отслеживания отправки/отмены заявки
    var isProcessing by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }

    // Состояние для отслеживания проверки дружбы
    var friendshipChecked by remember { mutableStateOf(false) }

    // Динамический отступ снизу в зависимости от наличия нижней навигации
    val bottomButtonPadding = if (hasBottomNavigation) {
        // Если есть нижнее меню, поднимаем кнопку выше
        (screenHeight * 0.12f).coerceAtLeast(140.dp)
    } else {
        // Если нет нижнего меню, стандартный отступ (ближе к низу)
        60.dp
    }

    LaunchedEffect(friendId) {
        viewModel.loadUserById(friendId)
        viewModel.loadMutualFriends(friendId)
        viewModel.loadMutualPlaces(friendId)
        viewModel.loadCompatibility(friendId)
    }

    val mutualFriends by viewModel.mutualFriends.collectAsState()
    val mutualPlaces by viewModel.mutualPlaces.collectAsState()
    val otherUser by viewModel.otherUser.collectAsState()
    val myUser by viewModel.myUser.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val compatibilityPercent by viewModel.compatibilityPercent.collectAsState()

    // Статус дружбы
    val friendshipStatus = myUser?.friends?.get(friendId)?.status ?: ""

    // Проверяем, является ли пользователь другом, и перенаправляем на Cur_Friend
    LaunchedEffect(friendshipStatus, otherUser, friendshipChecked) {
        if (!friendshipChecked && friendshipStatus == FriendStatus.FRIEND.value && otherUser != null) {
            friendshipChecked = true
            // Перенаправляем на страницу друга
            navController.navigate(Screen.CurFriend.passFriendId(friendId)) {
                // Удаляем текущий экран из бэктэка
                popUpTo(navController.currentBackStackEntry?.destination?.route ?: return@navigate) {
                    inclusive = true
                }
            }
        }
    }

    if (isLoading && otherUser == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Scaffold(
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 6.dp)
                        .padding(top = 40.dp, bottom = 20.dp)
                ) {
                    Heading_Arrow(pageTitle, navController)
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(paddingValues)
                        .background(Color.White)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 25.dp)
                        .padding(bottom = bottomButtonPadding + 40.dp)  // Нижний отступ для контента
                ) {
                    otherUser?.let { user ->
                        UserInfo(
                            user = user,
                            showTelegram = false,  // Явно указываем, что Telegram не показывать
                            isFriend = false       // Или можно вообще не передавать, так как по умолчанию false
                        )

                        Spacer(modifier = Modifier.height(30.dp))

                        ProgressLine(compatibilityPercent / 100f, height = 12)

                        Spacer(modifier = Modifier.height(15.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "$compatibilityPercent%",
                                style = MaterialTheme.typography.displayLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(20.dp))
                            Text(
                                text = "ваших мест совпадают!\nэто больше, чем в среднем",
                                style = MaterialTheme.typography.bodyLarge,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Любимое место пользователя
                        if (user.favoritePlace.isNotBlank()) {
                            FavPlace(
                                placeName = user.favoritePlace,
                                photoUrl = user.favoritePlacePhoto,
                                isEditable = false
                            )
                            Spacer(modifier = Modifier.height(25.dp))
                        }

                        // Общие места
                        if (mutualPlaces.isNotEmpty()) {
                            MutPlaces(
                                places = mutualPlaces,
                                onPlaceClick = { place ->
                                    navController.navigate("myPlaceDetail/${place.id}")
                                }
                            )
                            Spacer(modifier = Modifier.height(25.dp))
                        }

                        // Общие друзья
                        if (mutualFriends.isNotEmpty()) {
                            FriendsHorizontal("Общие друзья", mutualFriends, navController)
                            Spacer(modifier = Modifier.height(25.dp))
                        }
                    }
                }

                // Диалог подтверждения отмены заявки
                if (showCancelDialog) {
                    AlertDialog(
                        onDismissRequest = { showCancelDialog = false },
                        title = { Text("Отменить заявку") },
                        text = { Text("Вы уверены, что хотите отменить заявку в друзья?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showCancelDialog = false
                                    scope.launch {
                                        isProcessing = true
                                        try {
                                            viewModel.removeFriendshipStatus(
                                                myUserId = myUser?.uid ?: "",
                                                friendId = friendId
                                            )
                                            snackbarHostState.showSnackbar("Заявка отменена")
                                            viewModel.loadUserById(friendId)
                                        } finally {
                                            isProcessing = false
                                        }
                                    }
                                }
                            ) {
                                Text("Отменить", color = PurpleCard)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showCancelDialog = false }) {
                                Text("Закрыть")
                            }
                        }
                    )
                }

                // Нижняя панель с кнопками - с динамическим отступом
                when {
                    isProcessing -> {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(horizontal = spacing.large)
                                .padding(bottom = bottomButtonPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    friendshipStatus == "request" -> {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(horizontal = spacing.large)
                                .padding(bottom = bottomButtonPadding)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Кнопка "Принять"
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(13.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                        .clickable {
                                            scope.launch {
                                                isProcessing = true
                                                try {
                                                    viewModel.updateFriendshipStatus(
                                                        myUserId = myUser?.uid ?: "",
                                                        friendId = friendId,
                                                        newStatusForMe = FriendStatus.FRIEND,
                                                        newStatusForFriend = FriendStatus.FRIEND
                                                    )

                                                    // Отправляем уведомление о принятии заявки
                                                    notificationViewModel.createFriendAcceptedNotification(
                                                        fromUserId = myUser?.uid ?: "",
                                                        toUserId = friendId
                                                    )

                                                    snackbarHostState.showSnackbar("Заявка принята")
                                                    navController.popBackStack()
                                                } finally {
                                                    isProcessing = false
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Принять",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.surface
                                    )
                                }

                                // Кнопка "Отклонить"
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(13.dp))
                                        .background(GrayDark)
                                        .clickable {
                                            scope.launch {
                                                isProcessing = true
                                                try {
                                                    viewModel.updateFriendshipStatus(
                                                        myUserId = myUser?.uid ?: "",
                                                        friendId = friendId,
                                                        newStatusForMe = FriendStatus.DENY,
                                                        newStatusForFriend = FriendStatus.MY_APPLICATION
                                                    )
                                                    snackbarHostState.showSnackbar("Заявка отклонена")
                                                    navController.popBackStack()
                                                } finally {
                                                    isProcessing = false
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Отклонить",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    friendshipStatus == "my_application" -> {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(horizontal = spacing.large)
                                .padding(bottom = bottomButtonPadding)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(Color.Gray.copy(alpha = 0.3f))
                                    .clickable { showCancelDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Заявка отправлена",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }

                    friendshipStatus == "deny" -> {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(horizontal = spacing.large)
                                .padding(bottom = bottomButtonPadding)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(Color.Gray.copy(alpha = 0.3f))
                                    .clickable(enabled = false) { },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Заявка отклонена",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }

                    friendshipStatus.isEmpty() || friendshipStatus == "none" -> {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(horizontal = spacing.large)
                                .padding(bottom = bottomButtonPadding)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable {
                                        scope.launch {
                                            isProcessing = true
                                            try {
                                                viewModel.updateFriendshipStatus(
                                                    myUserId = myUser?.uid ?: "",
                                                    friendId = friendId,
                                                    newStatusForMe = FriendStatus.MY_APPLICATION,
                                                    newStatusForFriend = FriendStatus.REQUEST
                                                )

                                                // Отправляем уведомление о новой заявке
                                                notificationViewModel.createFriendRequestNotification(
                                                    fromUserId = myUser?.uid ?: "",
                                                    toUserId = friendId
                                                )

                                                snackbarHostState.showSnackbar("Заявка отправлена")
                                                viewModel.loadUserById(friendId)
                                            } finally {
                                                isProcessing = false
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Отправить заявку",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.surface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}