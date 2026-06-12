package com.meetmap.datingapp.screens.events

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.meetmap.datingapp.components.cards.EventCard
import com.meetmap.datingapp.components.headers.Heading
import com.meetmap.datingapp.components.headers.Heading_Arrow
import com.meetmap.datingapp.navigation.Screen
import com.meetmap.datingapp.viewmodels.EventViewModel
import com.meetmap.datingapp.viewmodels.UserViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import com.meetmap.datingapp.R
import com.meetmap.datingapp.components.segmentedButton.CustomTabsComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventMain(
    navController: NavController,
    userViewModel: UserViewModel,
    eventViewModel: EventViewModel = hiltViewModel()
) {
    val allEvents by eventViewModel.events.collectAsState()
    val myEvents by eventViewModel.myEvents.collectAsState()
    val isLoading by eventViewModel.isLoading.collectAsState()
    val currentUser = userViewModel.myUser.value
    val userId = currentUser?.uid

    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        eventViewModel.loadEvents()
        eventViewModel.loadMyEvents(userId ?: "")
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
                Heading_Arrow("Мероприятия", navController, textSize = 30.sp)
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("create_event")
                },
                containerColor = Color.Transparent,

                modifier = Modifier
                    .size(50.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.plus),
                    contentDescription = "Создать мероприятие",
                    modifier = Modifier.size(50.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 1.dp, bottom = 8.dp)
            ) {
                CustomTabsComponent(
                    "Все",
                    "Мои",
                    0, 0,
                    selectedTab = selectedTab,
                    onTabSelected = { tabIndex -> selectedTab = tabIndex }
                )
            }


            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // влкдадка - все меприятия
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        } else if (allEvents.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("Мероприятий пока не запланировано((")
                                Text("Создай первое!")
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(allEvents) { event ->
                                    val isUserJoined = userId != null && event.participantsList.contains(userId)
                                    val isOrganizer = userId != null && event.organizerId == userId

                                    EventCard(
                                        title = event.title,
                                        date = "${event.date}, ${event.time}",
                                        description = event.description,
                                        imageUrl = event.imageUrl,
                                        isUserJoined = isUserJoined,
                                        isOrganizer = isOrganizer,
                                        onJoinClick = {
                                            val uid = userId ?: return@EventCard
                                            if (isUserJoined) {
                                                eventViewModel.leaveEvent(event.id, uid) { _ ->
                                                    eventViewModel.loadEvents()
                                                    eventViewModel.loadMyEvents(userId )
                                                }
                                            } else {
                                                eventViewModel.joinEvent(event.id, uid) { _ ->
                                                    eventViewModel.loadEvents()
                                                    eventViewModel.loadMyEvents(userId)
                                                }
                                            }
                                        },
                                        onEditClick = {
                                            navController.navigate("edit_event/${event.id}")
                                        },
                                        onClick = {
                                            navController.navigate("event_detail/${event.id}")
                                        }
                                    )
                                }
                                item{
                                    Spacer(modifier = Modifier.height(30.dp))
                                }
                            }
                        }
                    }
                    1 -> {
                        // влкдадка - мои (где я орг + где я записавшийся)

                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        } else if (myEvents.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("Мероприятий пока не запланировано((")
                                Text("Создай первое!")
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(myEvents) { event ->
                                    val isUserJoined = userId != null && event.participantsList.contains(userId)
                                    val isOrganizer = userId != null && event.organizerId == userId

                                    EventCard(
                                        title = event.title,
                                        date = "${event.date}, ${event.time}",
                                        description = event.description,
                                        imageUrl = event.imageUrl,
                                        isUserJoined = isUserJoined,
                                        isOrganizer = isOrganizer,
                                        onJoinClick = {
                                            val uid = userId ?: return@EventCard
                                            if (isUserJoined) {
                                                eventViewModel.leaveEvent(event.id, uid) { _ ->
                                                    eventViewModel.loadEvents()
                                                    eventViewModel.loadMyEvents(userId)
                                                }
                                            } else {
                                                eventViewModel.joinEvent(event.id, uid) { _ ->
                                                    eventViewModel.loadEvents()
                                                    eventViewModel.loadMyEvents(userId)
                                                }
                                            }
                                        },
                                        onEditClick = {
                                            navController.navigate("edit_event/${event.id}")
                                        },
                                        onClick = {
                                            navController.navigate("event_detail/${event.id}")
                                        }
                                    )
                                }
                                item{
                                    Spacer(modifier = Modifier.height(30.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}