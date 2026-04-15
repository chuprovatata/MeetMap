package com.example.datingapp.components.blocks

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.datingapp.R
import com.example.datingapp.data.repository.MyUser
import com.example.datingapp.ui.theme.boundedFamily
import com.example.datingapp.ui.theme.montserratFamily
import com.example.datingapp.utils.CloudImageUtils

fun getAgeString(age: Int?): String {
    return when {
        age == null -> ""
        age % 100 in 11..14 -> "$age лет"
        age % 10 == 1 -> "$age год"
        age % 10 in 2..4 -> "$age года"
        else -> "$age лет"
    }
}

@Composable
fun UserInfo(
    user: MyUser?,
    profileImageUrl: String? = null,
    isUploadingImage: Boolean = false,
    showTelegram: Boolean = false,
    isFriend: Boolean = false,
    showDescription: Boolean = true
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }

    val shouldShowTelegram = showTelegram || isFriend
    val shouldShowDescription = showDescription && !user?.bio.isNullOrBlank()

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
            ) {
                if (isUploadingImage) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    val imageModel = if (!profileImageUrl.isNullOrBlank() &&
                        profileImageUrl != CloudImageUtils.NO_PICTURE_URL) {
                        profileImageUrl
                    } else {
                        when (user?.gender) {
                            "M" -> R.drawable.profile_male
                            "F" -> R.drawable.profile_female
                            else -> R.drawable.picture_defaullt_profile
                        }
                    }

                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageModel)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Фото профиля",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = when (user?.gender) {
                            "M" -> R.drawable.profile_male
                            "F" -> R.drawable.profile_female
                            else -> R.drawable.picture_defaullt_profile
                        }),
                        placeholder = painterResource(id = when (user?.gender) {
                            "M" -> R.drawable.profile_male
                            "F" -> R.drawable.profile_female
                            else -> R.drawable.picture_defaullt_profile
                        })
                    )
                }
            }

            Spacer(modifier = Modifier.width(21.dp))

            Column(
                modifier = Modifier.fillMaxHeight()
            ) {
                Text(
                    text = user?.name ?: "",
                    fontFamily = montserratFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 25.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = getAgeString(user?.age),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = user?.university ?: "",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Normal
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            if (shouldShowDescription) {
                Text(
                    text = "О себе",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = user?.bio ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded }
                )

                if (!isExpanded && (user?.bio?.length ?: 0) > 100) {
                    Text(
                        text = "ещё",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { isExpanded = true }
                            .padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            if (shouldShowTelegram && !user?.telegram.isNullOrBlank()) {
                Text(
                    text = "Написать в Telegram @${user?.telegram}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.clickable {
                        val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://t.me/${user?.telegram}")
                        )
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}