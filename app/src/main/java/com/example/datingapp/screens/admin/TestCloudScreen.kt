// app/src/main/java/com/example/datingapp/screens/admin/TestCloudScreen.kt
package com.example.datingapp.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.datingapp.utils.CloudImageUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestCloudScreen(
    navController: NavController
) {
    var testUrl by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Тест облака") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Прямой URL тест
            TextField(
                value = testUrl,
                onValueChange = { testUrl = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("URL изображения") },
                label = { Text("URL теста") }
            )

            Button(
                onClick = {
                    if (testUrl.isBlank()) {
                        testUrl = CloudImageUtils.NO_PICTURE_URL
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Использовать тестовый URL")
            }

            // Отображение изображения
            if (testUrl.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    AsyncImage(
                        model = testUrl,
                        contentDescription = "Тестовое изображение",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Text(
                    text = "URL: $testUrl",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Тестовые URL
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Тестовые URL:", style = MaterialTheme.typography.titleSmall)

                // Оригинальный URL с параметрами
                Button(
                    onClick = {
                        testUrl = "https://storage.yandexcloud.net/meetmap/NO%20Picture.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=YCAJEwyujEU34SnldwmEhCSvI%2F20260209%2Fru-central1%2Fs3%2Faws4_request&X-Amz-Date=20260209T110919Z&X-Amz-Expires=86400&X-Amz-Signature=3ed8552a6ad336d541f7c2bd70720c01793b3bf2dce07166d6dc419a3d5b1029&X-Amz-SignedHeaders=host&response-content-disposition=attachment"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Оригинальный URL (с параметрами)")
                }

                // Чистый URL
                Button(
                    onClick = {
                        testUrl = "https://storage.yandexcloud.net/meetmap/NO%20Picture.png"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Чистый URL (без параметров)")
                }

                // Домен с точкой
                Button(
                    onClick = {
                        testUrl = "https://meetmap.storage.yandexcloud.net/NO%20Picture.png"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("URL с доменом meetmap.")
                }
            }
        }
    }
}