package com.meetmap.datingapp.screens.events

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.meetmap.datingapp.R
import com.meetmap.datingapp.components.blocks.FriendsHorizontal
import com.meetmap.datingapp.components.buttons.PrimaryButton
import com.meetmap.datingapp.components.cards.EventCard
import com.meetmap.datingapp.components.headers.Heading
import com.meetmap.datingapp.components.headers.Heading_Arrow
import com.meetmap.datingapp.data.repository.MyUser
import com.meetmap.datingapp.navigation.Screen
import com.meetmap.datingapp.ui.theme.PurpleCard
import com.meetmap.datingapp.viewmodels.EventViewModel
import com.meetmap.datingapp.viewmodels.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetail(
    eventId: String,
    navController: NavController,
    userViewModel: UserViewModel,
    eventViewModel: EventViewModel = hiltViewModel()
) {
    val event by eventViewModel.currentEvent.collectAsState()
    val isLoading by eventViewModel.isLoading.collectAsState()
    val isUserJoined by eventViewModel.isUserJoined.collectAsState()
    val joinLoading by eventViewModel.joinLoading.collectAsState()
    val errorMessage by eventViewModel.errorMessage.collectAsState()
    val currentUser = userViewModel.myUser.value

    val isOrganizer = currentUser?.uid != null && event?.organizerId == currentUser.uid

    LaunchedEffect(eventId) {
        if (eventId.isBlank()) {
            navController.popBackStack()
            return@LaunchedEffect
        }
        eventViewModel.loadEventById(eventId)
        if (currentUser?.uid != null) {
            eventViewModel.checkUserJoined(eventId, currentUser.uid)
        }
    }

    var participants by remember { mutableStateOf<List<MyUser>>(emptyList()) }
    var isLoadingParticipants by remember { mutableStateOf(true) }


    LaunchedEffect(event) {
        if (event != null && event!!.participantsList.isNotEmpty()) {
            isLoadingParticipants = true
            val loadedParticipants = mutableListOf<MyUser>()
            for (userId in event!!.participantsList) {
                val user = userViewModel.getUserById(userId)
                user?.let { loadedParticipants.add(it) }
            }
            participants = loadedParticipants
            isLoadingParticipants = false
        } else {
            isLoadingParticipants = false
        }
    }



    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 6.dp)
                    .padding(
                        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                        bottom = 20.dp
                    )
            ) {
                Heading_Arrow("Мероприятия", navController,  textSize =  30.sp)
            }
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Ошибка: $errorMessage")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { navController.popBackStack() }) {
                            Text("Вернуться назад")
                        }
                    }
                }
            }

            event == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Мероприятие не найдено")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { navController.popBackStack() }) {
                            Text("Вернуться назад")
                        }
                    }
                }
            }

            else -> {
                val currentEvent = event!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(Color.White)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 25.dp)
                        .padding(bottom = 100.dp)
                ) {
                    Spacer(modifier = Modifier.height(15.dp))


                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE0E0E0))
                    ) {
                        if (!currentEvent.imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = currentEvent.imageUrl,
                                contentDescription = "Event image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                error = painterResource(R.drawable.place1)
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.place1),
                                contentDescription = "Event image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))


                    Text(
                        text = currentEvent.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))


                    Text(
                        text = "${currentEvent.date} ${currentEvent.time}, ${currentEvent.place}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row {
                        if (currentEvent.university.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = PurpleCard.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = currentEvent.university,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PurpleCard,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Box(
                            modifier = Modifier
                                .background(
                                    color = PurpleCard.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (currentEvent.isForAll) "для всех желающих" else "только для студентов ${currentEvent.university}",
                                style = MaterialTheme.typography.bodySmall,
                                color = PurpleCard,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Описание
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        )
                    ) {
                        Text(
                            text = currentEvent.description.ifBlank { "описания пока нет(" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Организатор
                    Text(
                        text = "Организатор",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier
                            .clickable {
                                val orgId = currentEvent.organizerId
                                if (orgId.isNotBlank()) {
                                    navController.navigate(
                                        Screen.ReqFriend.passParams(
                                            orgId,
                                            "Профиль"
                                        )
                                    )
                                }
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.profile_female),
                            contentDescription = "Organizer avatar",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(20.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentEvent.organizerUsername,
                            style = MaterialTheme.typography.bodyMedium,
                            color = PurpleCard,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (!isLoadingParticipants && participants.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))

                        FriendsHorizontal(
                            header = "Участники (${participants.size})",
                            friends = participants,
                            navController = navController,
                            fontSize=12.sp,
                            size=40.dp


                        )
                    }

                }



                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    if (isOrganizer) {

                        PrimaryButton(
                            text = "РЕДАКТИРОВАТЬ",
                            onClick = {
                                navController.navigate("edit_event/${eventId}")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = PurpleCard,
                            contentColor = Color.White,
                            fixedHeight = 56.dp,
                            textSize = 16.sp
                        )
                    } else {

                        PrimaryButton(
                            text = if (isUserJoined) "Я ИДУ" else "Я ПОЙДУ",
                            onClick = {
                                val userId = currentUser?.uid ?: return@PrimaryButton
                                if (isUserJoined) {
                                    eventViewModel.leaveEvent(eventId, userId)
                                } else {
                                    eventViewModel.joinEvent(eventId, userId)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !joinLoading,
                            containerColor = if (isUserJoined) Color(0xFFE0E0E0) else PurpleCard,
                            contentColor = if (isUserJoined) Color(0xFF666666) else Color.White,
                            fixedHeight = 56.dp,
                            textSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}