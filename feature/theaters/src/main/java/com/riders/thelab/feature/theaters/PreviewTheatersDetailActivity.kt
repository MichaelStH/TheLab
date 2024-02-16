package com.riders.thelab.feature.theaters

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.compose.AsyncImagePainter
import com.riders.thelab.core.data.local.model.compose.TMDBUiState.TMDBDetailUiState
import com.riders.thelab.core.data.local.model.tmdb.TMDBVideoModel
import com.riders.thelab.core.ui.R
import com.riders.thelab.core.ui.compose.annotation.DevicePreviews
import com.riders.thelab.core.ui.compose.component.Lottie
import com.riders.thelab.core.ui.compose.component.ProvidedBy
import com.riders.thelab.core.ui.compose.component.TheLabTopAppBar
import com.riders.thelab.core.ui.compose.theme.TheLabTheme
import com.riders.thelab.core.ui.compose.utils.getCoilAsyncImagePainter
import com.riders.thelab.core.ui.utils.loadImage
import kotlinx.coroutines.launch
import timber.log.Timber


///////////////////////////////////////
//
// COMPOSE
//
///////////////////////////////////////
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "UnusedBoxWithConstraintsScope")
@Composable
fun TheatersDetailContent(
    tmdbState: TMDBDetailUiState,
    isTrailerVisible: Boolean,
    onTrailerVisible: (Boolean) -> Unit
) {

    Timber.d("TheatersDetailContent() | Recomposition")

    TheLabTheme(darkTheme = true) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = { TheLabTopAppBar(navigationIcon = {}) }
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                when (tmdbState) {
                    is TMDBDetailUiState.Loading -> {
                        Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                            Lottie(
                                modifier = Modifier.fillMaxSize(),
                                url = "https://assets2.lottiefiles.com/packages/lf20_kk62um5v.json"
                            )
                        }
                    }

                    is TMDBDetailUiState.Success -> {
                        val scope = rememberCoroutineScope()
                        val lazyListState = rememberLazyListState()

                        val painter =
                            getCoilAsyncImagePainter(
                                context = LocalContext.current,
                                dataUrl = if (null != tmdbState.item.poster) {
                                    tmdbState.item.getPosterImageUrl()
                                } else {
                                    tmdbState.item.getBackdropImageUrl()
                                }
                            )
                        val state = painter.state

                        /* Create the Palette, pass the bitmap to it */
                        var palette: Palette

                        /* Get the dark vibrant swatch */
                        val lightVibrantSwatch = remember { mutableIntStateOf(0) }
                        val darkVibrantSwatch = remember { mutableIntStateOf(0) }
                        val dominantSwatch = remember { mutableIntStateOf(0) }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            state = lazyListState
                        ) {
                            // Image
                            item {
                                BoxWithConstraints(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(1.dp)
                                        .height(trendingItemImageHeight),
                                    contentAlignment = Alignment.Center
                                ) {

                                    AnimatedContent(
                                        targetState = state,
                                        transitionSpec = { fadeIn() + expandIn() togetherWith fadeOut() },
                                        label = "animated content state"
                                    ) { targetState: AsyncImagePainter.State ->

                                        when (targetState) {
                                            is AsyncImagePainter.State.Loading -> {
                                                Timber.i("state is AsyncImagePainter.State.Loading")
                                                CircularProgressIndicator(
                                                    modifier = Modifier.scale(0.5f),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }

                                            is AsyncImagePainter.State.Success -> {
                                                Timber.d("state is AsyncImagePainter.State.Success")

                                                Image(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .defaultMinSize(1.dp)
                                                        .height(trendingItemImageHeight),
                                                    painter = painter,
                                                    contentDescription = "movie image",
                                                    contentScale = ContentScale.Crop,
                                                )

                                                LaunchedEffect(key1 = AsyncImagePainter.State.Empty.painter) {
                                                    scope.launch {
                                                        val image = painter.loadImage()

                                                        palette = Palette.from(
                                                            image.toBitmap(
                                                                image.intrinsicWidth,
                                                                image.intrinsicHeight
                                                            )
                                                        ).generate()

                                                        ////////////////

                                                        lightVibrantSwatch.intValue =
                                                            palette.lightVibrantSwatch?.rgb ?: 0
                                                        darkVibrantSwatch.intValue =
                                                            palette.darkVibrantSwatch?.rgb ?: 0
                                                        dominantSwatch.intValue =
                                                            palette.dominantSwatch?.rgb ?: 0
                                                        //paletteColorList.value = generatePalette(palette)
                                                    }
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

                            // Titles
                            item {
                                Titles(
                                    tmdbState.item.title,
                                    tmdbState.item.originalTitle,
                                    Color(lightVibrantSwatch.intValue)
                                )
                            }

                            // Popularity and Rating
                            item {
                                PopularityAndRating(tmdbState.item)
                            }

                            // Trailer
                            item {
                                Trailer {
                                    onTrailerVisible(true)
                                }
                            }

                            // Overview
                            item {
                                Overview(tmdbState.item)
                            }

                            // Director
                            /*item {
                                Director(movie)
                            }*/

                            // Scenarists
                            /*item {
                                Scenarists(movie)
                            }*/

                            if (tmdbState.item.cast.isNotEmpty()) {
                                // Casting
                                item {
                                    Casting(tmdbState.item.cast)
                                }
                            } else {
                                Timber.e("TheatersDetailContent() | Recomposition | null == tmdbItem.cast. No casting")
                            }

                            item {
                                ProvidedBy(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    providerIcon = R.drawable.tmdb_logo
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = isTrailerVisible && tmdbState.item.videos.isNotEmpty(),
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            BoxWithConstraints(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        if (!isTrailerVisible) Color.Transparent else Color.Black.copy(
                                            alpha = .83f
                                        )
                                    ), contentAlignment = Alignment.Center
                            ) {
                                PopUpTrailer(
                                    itemName = tmdbState.item.title,
                                    tmdbVideoModel = tmdbState.item.videos.first {
                                        it.name.contains("official trailer", true) ||
                                                it.name.contains("official clip", true) ||
                                                it.name.contains("official", true)  /* ||
                                                                      it.name.contains(tmdbItem.title, true)*/ &&
                                                (it.type.equals("Trailer", true) ||
                                                        it.type.equals("teaser", true)) &&
                                                it.site.equals("youtube", true)
                                    }
                                ) {
                                    onTrailerVisible(it)
                                }
                            }
                        }
                    }

                    is TMDBDetailUiState.Error -> {

                    }
                }
            }
        }
    }

    LaunchedEffect(tmdbState) {
        if (tmdbState is TMDBDetailUiState.Success) {
            Timber.d("LaunchedEffect | state: ${tmdbState.item.cast.size} | coroutineContext: ${this.coroutineContext}")
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
private fun PreviewTheatersDetailContent(@PreviewParameter(PreviewProviderTMDBDetailUiState::class) uiState: TMDBDetailUiState) {
    TheLabTheme(darkTheme = true) {
        TheatersDetailContent(uiState, isTrailerVisible = true) {}
    }
}