package com.meetmap.datingapp.screens.favoriteplace

import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.meetmap.datingapp.R
import com.meetmap.datingapp.components.blocks.Title_Block
import com.meetmap.datingapp.components.buttons.PrimaryButton
import com.meetmap.datingapp.components.forms.DatingTextField
import com.meetmap.datingapp.ui.theme.LocalDatingAppSpacing
import com.meetmap.datingapp.ui.theme.PurpleCard
import com.meetmap.datingapp.ui.theme.boundedFamily
import com.meetmap.datingapp.viewmodels.UserViewModel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritePlaceScreen(
    navController: NavController,
    viewModel: UserViewModel = hiltViewModel()
) {
    val userData by viewModel.userData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val saveError by viewModel.saveError.collectAsState()
    val favoritePlacePhotoUrl by viewModel.favoritePlacePhotoUrl.collectAsState()

    var favoritePlace by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var hasUnsavedChanges by remember { mutableStateOf(false) }
    var isUploadingPhoto by remember { mutableStateOf(false) }
    var showDeletePhotoDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val spacing = LocalDatingAppSpacing.current
    val context = LocalContext.current

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            isUploadingPhoto = true
            scope.launch {
                try {
                    viewModel.uploadFavoritePlacePhoto(it, context.contentResolver)
                    snackbarHostState.showSnackbar("Фото загружено")
                    hasUnsavedChanges = true
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Ошибка загрузки: ${e.message}")
                } finally {
                    isUploadingPhoto = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadUserData()
    }

    LaunchedEffect(userData) {
        userData?.let { data ->
            favoritePlace = data["favoritePlace"] as? String ?: ""
            address = data["favoritePlaceAddress"] as? String ?: ""
            comment = data["favoritePlaceComment"] as? String ?: ""
            hasUnsavedChanges = false
        }
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            scope.launch {
                snackbarHostState.showSnackbar("Сохранено!")
            }
            viewModel.clearSaveSuccess()
            hasUnsavedChanges = false
        }
    }

    LaunchedEffect(saveError) {
        saveError?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar("Ошибка: $error")
            }
            viewModel.clearSaveError()
        }
    }

    fun saveChanges() {
        if (favoritePlace.isBlank()) {
            scope.launch {
                snackbarHostState.showSnackbar("Укажи название любимого места")
            }
            return
        }

        val data = mutableMapOf<String, Any?>(
            "favoritePlace" to favoritePlace,
            "favoritePlaceAddress" to address,
            "favoritePlaceComment" to comment
        )

        viewModel.updateUserData(data)
    }

    fun deletePhoto() {
        scope.launch {
            try {
                viewModel.deleteFavoritePlacePhoto()
                snackbarHostState.showSnackbar("Фото удалено")
                hasUnsavedChanges = true
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Ошибка удаления: ${e.message}")
            }
        }
    }

    if (showDeletePhotoDialog) {
        AlertDialog(
            onDismissRequest = { showDeletePhotoDialog = false },
            title = { Text("Удалить фото") },
            text = { Text("Ты уверен, что хочешь удалить фото любимого места?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeletePhotoDialog = false
                        deletePhoto()
                    }
                ) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeletePhotoDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 6.dp)
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(), bottom = 20.dp)
            ) {
                // Локальный заголовок с уменьшенным шрифтом только для этого экрана
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_left),
                            contentDescription = "arrow_left",
                            tint = Color.Black
                        )
                    }

                    Text(
                        text = "Любимое место",
                        fontSize = 28.sp,
                        fontFamily = boundedFamily,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp, end = 8.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.25f)
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp, bottom = 4.dp)
                ) {
                    Title_Block(
                        navController,
                        "Где тебя найти?",
                        "Добавь любимое место, чтобы другие могли узнать тебя лучше!",
                        R.drawable.picrure_bike,
                        false
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        DatingTextField(
                            value = favoritePlace,
                            onValueChange = {
                                favoritePlace = it
                                hasUnsavedChanges = true
                            },
                            label = "Название",
                            placeholder = "Например: Центральный парк, Кофейня «Уют»",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spacing.large)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        DatingTextField(
                            value = address,
                            onValueChange = {
                                address = it
                                hasUnsavedChanges = true
                            },
                            label = "Адрес",
                            placeholder = "Например: ул. Пушкина, д. 10",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spacing.large)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        DatingTextField(
                            value = comment,
                            onValueChange = {
                                comment = it
                                hasUnsavedChanges = true
                            },
                            label = "Комментарий",
                            placeholder = "Расскажи, почему это место особенное для тебя...",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spacing.large)
                                .heightIn(min = 120.dp),
                            singleLine = false,
                            maxLines = Int.MAX_VALUE,
                            maxCharacters = 300,
                            showCharacterCounter = true
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    item {
                        Text(
                            text = "Фото места",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = spacing.large)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Добавь фото, чтобы поделиться атмосферой",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = spacing.large)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spacing.large)
                        ) {
                            if (favoritePlacePhotoUrl != null &&
                                favoritePlacePhotoUrl != com.meetmap.datingapp.utils.CloudImageUtils.NO_PICTURE_URL
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(favoritePlacePhotoUrl)
                                            .crossfade(true)
                                            .addHeader("User-Agent", "MeetMap-Android-App/1.0")
                                            .build(),
                                        contentDescription = "Фото любимого места",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                shape = RoundedCornerShape(16.dp)
                                            ),
                                        contentScale = ContentScale.Crop,
                                        error = painterResource(id = R.drawable.picture_museum_background),
                                        placeholder = painterResource(id = R.drawable.picture_museum_background)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 8.dp, y = (-8).dp)
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color.White)
                                            .border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .clickable { showDeletePhotoDialog = true },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Удалить фото",
                                            modifier = Modifier.size(20.dp),
                                            tint = Color.Black
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable(enabled = favoritePlace.isNotBlank() && !isUploadingPhoto) {
                                            if (favoritePlace.isNotBlank()) {
                                                photoPicker.launch(
                                                    PickVisualMediaRequest(
                                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                                    )
                                                )
                                            }
                                        }
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (isUploadingPhoto) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(32.dp),
                                                strokeWidth = 3.dp
                                            )
                                        } else {
                                            Icon(
                                                painter = painterResource(id = R.drawable.icon_plus),
                                                contentDescription = "Добавить фото",
                                                modifier = Modifier.size(32.dp),
                                                tint = if (favoritePlace.isNotBlank())
                                                    PurpleCard
                                                else
                                                    Color.Gray.copy(alpha = 0.4f)
                                            )
                                            Text(
                                                text = if (favoritePlace.isNotBlank())
                                                    "Нажми, чтобы добавить фото места"
                                                else
                                                    "Сначала укажи название места",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (favoritePlace.isNotBlank())
                                                    PurpleCard
                                                else
                                                    Color.Gray.copy(alpha = 0.5f),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = spacing.large)
                        .padding(bottom = 24.dp)
                ) {
                    PrimaryButton(
                        text = "Сохранить",
                        textSize = 18.sp,
                        onClick = { saveChanges() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = favoritePlace.isNotBlank() && !isSaving,
                        isLoading = isSaving
                    )
                }
            }
        }
    }
}