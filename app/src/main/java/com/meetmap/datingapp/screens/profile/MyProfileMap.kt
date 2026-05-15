package com.meetmap.datingapp.screens.profile


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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.hilt.navigation.compose.hiltViewModel

import androidx.navigation.NavController
import com.meetmap.datingapp.R
import com.meetmap.datingapp.components.map.AllPointMap

import com.meetmap.datingapp.data.models.PlaceInfo

import com.meetmap.datingapp.navigation.Screen

import com.meetmap.datingapp.ui.theme.GrayPerson
import com.meetmap.datingapp.ui.theme.PurplePrimary
import com.meetmap.datingapp.ui.theme.boundedFamily
import com.meetmap.datingapp.viewmodels.MyPlacesViewModel
import com.meetmap.datingapp.viewmodels.UserViewModel


@Composable
fun MyProfileMap(
    navController: NavController,
    userViewModel: UserViewModel
) {
    val myPlacesViewModel: MyPlacesViewModel = hiltViewModel()
    val combinedPlaces by myPlacesViewModel.combinedPlaces.collectAsState()
    val isLoading by myPlacesViewModel.isLoading.collectAsState()

    var selectedPlace by remember { mutableStateOf<PlaceInfo?>(null) }

    val validPlaces = remember(combinedPlaces) {
        combinedPlaces
            .mapNotNull { (_, placeInfo) -> placeInfo }
            .filter { it.latitude != 0.0 && it.longitude != 0.0 }
    }

    LaunchedEffect(Unit) {
        myPlacesViewModel.loadUserPlaces()
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
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            navController.navigate(Screen.MyProfile.route) {
                                navController.popBackStack()
                            }
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.arrow_left),
                                contentDescription = "Назад",
                                tint = Color.Black
                            )
                        }
                        Text(
                            text = "Мои места",
                            fontSize = 35.sp,
                            fontFamily = boundedFamily,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(onClick = {
                        navController.navigate(Screen.Settings.route)
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.icon_settings),
                            contentDescription = "Настройки",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp).padding(bottom = 30.dp)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                validPlaces.isEmpty() -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.icon_location),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "У вас пока нет сохранённых мест",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Добавьте места через экран 'Места дня'",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    AllPointMap(
                        places = validPlaces,
                        onPlaceClick = { selectedPlace = it }
                    )
                }
            }
        }
    }

    // Диалог
    selectedPlace?.let { place ->
        AlertDialog(
            onDismissRequest = { selectedPlace = null },
            title = { Text(place.name, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(place.address, color = Color.DarkGray)
                    if (place.metroStation.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Icon(
                                painterResource(id = R.drawable.icon_subway),
                                contentDescription = "metro",
                                modifier = Modifier.size(18.dp),
                                tint = GrayPerson
                            )
                            Text("  ${place.metroStation}", color = GrayPerson)
                        }
                    }
                    if (place.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(place.description)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { navController.navigate("myPlaceDetail/${place.id}")}) {
                    Text("Подробнее..", color = PurplePrimary)
                }
            }
        )
    }
}



