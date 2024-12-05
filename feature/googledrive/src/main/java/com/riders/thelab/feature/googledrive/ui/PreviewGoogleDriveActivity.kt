package com.riders.thelab.feature.googledrive.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import com.google.api.services.drive.model.File
import com.riders.thelab.core.ui.compose.annotation.DevicePreviews
import com.riders.thelab.core.ui.compose.component.NoItemFound
import com.riders.thelab.core.ui.compose.component.loading.LabLoader
import com.riders.thelab.core.ui.compose.component.toolbar.TheLabTopAppBar
import com.riders.thelab.core.ui.compose.component.toolbar.ToolbarSize
import com.riders.thelab.core.ui.compose.theme.TheLabTheme
import com.riders.thelab.core.ui.compose.theme.md_theme_dark_primary
import com.riders.thelab.core.ui.compose.theme.md_theme_light_primary
import com.riders.thelab.core.ui.compose.theme.success
import com.riders.thelab.core.ui.compose.utils.findActivity
import com.riders.thelab.core.ui.compose.utils.getCoilAsyncImagePainter
import com.riders.thelab.core.ui.compose.utils.getGlideImage
import com.riders.thelab.feature.googledrive.R
import com.riders.thelab.feature.googledrive.data.local.compose.GoogleDriveUiState
import com.riders.thelab.feature.googledrive.data.local.compose.GoogleSignInState
import com.skydoves.landscapist.glide.GlideImageState
import com.skydoves.landscapist.glide.rememberGlideImageState
import kotools.types.experimental.ExperimentalKotoolsTypesApi
import timber.log.Timber

///////////////////////////////////////
//
// COMPOSE
//
///////////////////////////////////////
@Composable
fun PlayServicesUnavailableContent() {
    val context = LocalContext.current
    val hue: Float by remember { mutableFloatStateOf(50f) }

    TheLabTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth(.65f)
                    .height(dimensionResource(id = com.riders.thelab.core.ui.R.dimen.max_card_image_height))
                    .clip(RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                    painter = painterResource(id = R.drawable.ic_google_drive_logo),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(
                        color = Color.DarkGray.copy(
                            alpha = 1f,
                            red = 253f,
                            blue = 150f,
                            green = hue
                        ),
                        blendMode = BlendMode.Saturation
                    )
                )
            }

            Text(
                text = "Unfortunately the Google Play Services are not available on your devices.\n\nYou cannot use this feature.",
                textAlign = TextAlign.Center
            )

            Button(
                onClick = { (context.findActivity() as GoogleDriveActivity).backPressed() },
                modifier = Modifier
            ) {
                Row(
                    modifier = Modifier,
                    horizontalArrangement = Arrangement.spacedBy(
                        8.dp,
                        Alignment.CenterHorizontally
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                        contentDescription = null
                    )
                    Text(text = "Go Back")
                }
            }
        }
    }
}

@Composable
fun Header(signInState: GoogleSignInState, uiEvent: (UiEvent) -> Unit) {
    val backgroundColor: Color = when (signInState) {
        is GoogleSignInState.Connected -> success
        is GoogleSignInState.Disconnected -> com.riders.thelab.core.ui.compose.theme.error
        else -> Color.Transparent
    }

    TheLabTheme {
        AnimatedVisibility(visible = signInState is GoogleSignInState.Disconnected || signInState is GoogleSignInState.Connected) {
            Row(
                modifier = Modifier
                    .background(backgroundColor)
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(2f),
                    text = when (signInState) {
                        is GoogleSignInState.Connected -> "You're connected as ${signInState.account.displayName}"
                        is GoogleSignInState.Disconnected -> "You're disconnected"
                        else -> ""
                    },
                    color = Color.White
                )

                AnimatedVisibility(
                    modifier = Modifier.weight(.5f),
                    visible = signInState is GoogleSignInState.Connected
                ) {
                    Box(modifier = Modifier, contentAlignment = Alignment.Center) {
                        IconButton(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(shape = CircleShape)
                                .background(if (!isSystemInDarkTheme()) md_theme_light_primary else md_theme_dark_primary),
                            onClick = { uiEvent.invoke(UiEvent.OnSignOut) }
                        ) {
                            Icon(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                tint = if (!isSystemInDarkTheme()) Color.White else Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GoogleDriveImage(fileId: String) {
    val context = LocalContext.current
//    val imageUrl =   "https://drive.google.com/file/d/${file.id}/view?usp=sharing"
    val imageUrl = "https://drive.google.com/uc?export=view&id=${fileId}"
//    val imageUrl =   "https://drive.usercontent.google.com/download?export=view&id=${file.id}"
//    val imageUrl =    "${Constants.BASE_ENDPOINT_GOOGLE_DRIVE_VIEW}${file.id}"

    TheLabTheme {
      /*  val driveFilePainter = getCoilAsyncImagePainter(
            context,
            dataUrl = imageUrl,
            isSvg = false,
            onState = { state ->
                when (state) {
                    AsyncImagePainter.State.Empty -> {
                        Timber.i("state is AsyncImagePainter.State.Empty")
                    }

                    is AsyncImagePainter.State.Error -> {
                        Timber.e("state is AsyncImagePainter.State.Error | ${state.result.throwable.message}")
                    }

                    is AsyncImagePainter.State.Loading -> {
//                        Timber.i("state is AsyncImagePainter.State.Loading")
                    }

                    is AsyncImagePainter.State.Success -> {
//                        Timber.d("state is AsyncImagePainter.State.Success")
                    }
                }
            }
        )

        val u = rememberGlideImageState(initialState = GlideImageState.None)*/

        getGlideImage(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(1.dp, 140.dp)
                .padding(horizontal = 16.dp)
                .clip(shape = RoundedCornerShape(35.dp)),
            url = imageUrl
        )

        /*Image(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(1.dp, 140.dp)
                .padding(horizontal = 16.dp)
                .clip(shape = RoundedCornerShape(35.dp)),
            painter = driveFilePainter,
            contentDescription = "drive_file_image"
        )*/
    }
}

@OptIn(ExperimentalKotoolsTypesApi::class)
@Composable
fun GoogleDriveContentSuccess(
    signInState: GoogleSignInState,
    hasInternetConnection: Boolean,
    driveFileList: List<File>,
    uiEvent: (UiEvent) -> Unit
) {
    val context = LocalContext.current

    val driveListState = rememberLazyListState()

    TheLabTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Header(signInState = signInState, uiEvent = uiEvent)

            AnimatedContent(
                modifier = Modifier.fillMaxSize(),
                targetState = signInState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                contentAlignment = Alignment.TopStart,
                label = "sign_in_content_animation"
            ) { targetState ->
                when (targetState) {
                    is GoogleSignInState.Connected -> {

                        val painter = targetState.account.profilePictureUri?.let { profileUrl ->
                            getCoilAsyncImagePainter(
                                context = context,
                                dataUrl = profileUrl.toString(),
                                isSvg = false,
                                placeholderResId = com.riders.thelab.core.ui.R.drawable.logo_colors
                            )
                        } ?: run { null }

                        val painterState = painter?.state

                        LazyColumn(
                            modifier = Modifier.padding(top = 24.dp),
                            state = driveListState,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(
                                16.dp,
                                Alignment.Top
                            )
                        ) {
                            item {
                                if (null != painter && null != painterState) {
                                    Card(
                                        modifier = Modifier.clip(CircleShape),
                                        shape = CircleShape
                                    ) {
                                        AnimatedContent(
                                            targetState = painterState,
                                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                                            label = "animated content state"
                                        ) { targetState: AsyncImagePainter.State ->
                                            when (targetState) {
                                                is AsyncImagePainter.State.Loading -> {
                                                    Timber.i("state is AsyncImagePainter.State.Loading")
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(36.dp),
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }

                                                is AsyncImagePainter.State.Success -> {
                                                    Timber.d("state is AsyncImagePainter.State.Success")
                                                    Box(
                                                        modifier = Modifier
                                                            .size(dimensionResource(id = com.riders.thelab.core.ui.R.dimen.max_card_image_height))
                                                            .clip(CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Image(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .defaultMinSize(1.dp),
                                                            painter = painter,
                                                            contentDescription = "user_profile_picture",
                                                            contentScale = ContentScale.Crop,
                                                        )
                                                    }
                                                }

                                                is AsyncImagePainter.State.Error -> {
                                                    Timber.e("state is AsyncImagePainter.State.Error | ${targetState.result}")
                                                }

                                                else -> {
                                                    Timber.e("state | else branch")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            item {
                                Text(
                                    text = targetState.account.displayName?.toString() ?: "N/A"
                                )
                            }

                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(text = targetState.account.firstName?.toString() ?: "N/A")
                                    Text(text = targetState.account.familyName?.toString() ?: "N/A")
                                    Text(text = targetState.account.emailAddress.toString())
                                }
                            }

                            // Google drive files list
                            if (driveFileList.isNotEmpty()) {
                                item {
                                    repeat(driveFileList.size) { index ->
                                        val file = driveFileList[index]

                                        if (file.mimeType.contains("image", true)) {
                                            GoogleDriveImage(file.id)
                                        }

                                        Text(text = "${file.name} | ${file.mimeType}")
                                    }
                                }
                            }
                        }
                    }

                    is GoogleSignInState.Disconnected -> {
                        Box(
                            modifier = Modifier,
                            contentAlignment = Alignment.TopStart
                        ) {
                            Button(
                                modifier = Modifier.padding(16.dp),
                                onClick = {
                                    uiEvent.invoke(UiEvent.OnSignIn(context = context))
                                    /*scope.launch {
                                        GoogleSignInManager(context).signIn { account: GoogleAccountModel ->
                                            uiEvent.invoke(UiEvent.OnHandleAccount(account))
                                        }
                                    }*/
                                },
                                enabled = hasInternetConnection
                            ) {
                                Text(text = "Sign in with Google")
                            }
                        }
                    }

                    else -> Box(modifier = Modifier)
                }
            }
        }
    }
}


@Composable
fun GoogleDriveContent(
    uiState: GoogleDriveUiState,
    signInState: GoogleSignInState,
    hasInternetConnection: Boolean,
    driveFileList: List<File>,
    uiEvent: (UiEvent) -> Unit
) {
    TheLabTheme {
        Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
            TheLabTopAppBar(
                toolbarSize = ToolbarSize.SMALL,
                mainCustomContent = {
                    Row(
                        modifier = Modifier.fillMaxHeight(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(8.dp),
                                painter = painterResource(id = com.riders.thelab.core.ui.R.drawable.googleg_color),
                                contentDescription = "google_icon"
                            )
                        }

                        Text(
                            text = stringResource(id = com.riders.thelab.core.ui.R.string.activity_title_google_drive),
                            color = Color.White
                        )
                    }
                },
                withGradientBackground = true
            )
        }) { contentPadding ->
            AnimatedContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                targetState = uiState,
                contentAlignment = Alignment.Center,
                label = "youtube_like_animated_content"
            ) { targetState ->
                when (targetState) {
                    is GoogleDriveUiState.Loading -> {
                        LabLoader(modifier = Modifier.size(56.dp))
                    }

                    is GoogleDriveUiState.Error -> {
                        NoItemFound("An error occurred while loading\n${targetState.message}")
                    }

                    is GoogleDriveUiState.GooglePlayServicesUnavailable -> {
                        PlayServicesUnavailableContent()
                    }

                    is GoogleDriveUiState.Success -> {
                        GoogleDriveContentSuccess(
                            signInState = signInState,
                            hasInternetConnection = hasInternetConnection,
                            driveFileList = driveFileList,
                            uiEvent = uiEvent
                        )
                    }
                }
            }
        }
    }
}


///////////////////////////////////////
//
// PREVIEWS
//
///////////////////////////////////////
@DevicePreviews
@Composable
private fun PreviewPlayServicesUnavailableContent() {
    TheLabTheme {
        PlayServicesUnavailableContent()
    }
}

@DevicePreviews
@Composable
private fun PreviewHeader(@PreviewParameter(PreviewProviderGoogleSignInState::class) signInState: GoogleSignInState) {
    TheLabTheme {
        Header(signInState = signInState) {}
    }
}

@DevicePreviews
@Composable
private fun PreviewGoogleDriveContentSuccess(@PreviewParameter(PreviewProviderGoogleSignInState::class) signInState: GoogleSignInState) {
    TheLabTheme {
        GoogleDriveContentSuccess(
            signInState = signInState,
            driveFileList = emptyList(),
            hasInternetConnection = true
        ) {}
    }
}

@DevicePreviews
@Composable
private fun PreviewGoogleDriveContent(@PreviewParameter(PreviewProviderUiState::class) state: GoogleDriveUiState) {
    TheLabTheme {
        GoogleDriveContent(
            uiState = state,
            signInState = GoogleSignInState.Disconnected,
            driveFileList = emptyList(),
            hasInternetConnection = true
        ) {}
    }
}