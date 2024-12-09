package com.riders.thelab.feature.theaters.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.riders.thelab.core.common.network.NetworkState
import com.riders.thelab.core.data.local.model.compose.theaters.TMDBUiState
import com.riders.thelab.core.data.remote.dto.tmdb.TMDBMovieResponse
import com.riders.thelab.core.ui.compose.annotation.DevicePreviews
import com.riders.thelab.core.ui.compose.component.network.PreviewProviderNetworkState
import com.riders.thelab.core.ui.compose.theme.TheLabTheme
import com.riders.thelab.core.ui.compose.theme.md_theme_dark_background
import com.riders.thelab.feature.theaters.splashscreen.TheatersSplash
import kotlinx.coroutines.delay


val trendingItemImageHeight: Dp = 550.dp


///////////////////////////////////////
//
// COMPOSE
//
///////////////////////////////////////
@Composable
fun TheatersContainer(
    networkState: NetworkState,
    isActivitiesSplashScreenEnable: Boolean,
    categories: List<String>,
    tabRowSelected: Int,
    trendingMovieItem: TMDBUiState.TMDBTrendingMovieItemUiState,
    movies: TMDBUiState.TMDBMoviesUiState,
    upcomingMovies: TMDBUiState.TMDBUpcomingMoviesUiState,
    trendingTvShowItem: TMDBUiState.TMDBTrendingTvShowItemUiState,
    trendingTvShows: TMDBUiState.TMDBTvShowsUiState,
    isRefreshing: Boolean,
    uiEvent: (UiEvent) -> Unit
) {
    val switch = remember { mutableStateOf(false) }

    TheLabTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(md_theme_dark_background)
        ) {
            if (isActivitiesSplashScreenEnable) {
                AnimatedContent(
                    modifier = Modifier.align(Alignment.Center),
                    transitionSpec = {
                        (slideInVertically { height -> height } + fadeIn()).togetherWith(
                            slideOutVertically { height -> -height } + fadeOut())
                    },
                    targetState = if (LocalInspectionMode.current) true else switch.value,
                    label = "splashscreen animation"
                ) { targetState ->
                    if (!targetState) {
                        TheatersSplash()
                    } else {
                        TheatersContent(
                            networkState = networkState,
                            categories = categories,
                            tabRowSelected = tabRowSelected,
                            trendingMovieItem = trendingMovieItem,
                            movies = movies,
                            upcomingMovies = upcomingMovies,
                            trendingTvShowItem = trendingTvShowItem,
                            trendingTvShows = trendingTvShows,
                            isRefreshing = isRefreshing,
                            uiEvent = uiEvent
                        )
                    }
                }
            } else {
                TheatersContent(
                    networkState = networkState,
                    categories = categories,
                    tabRowSelected = tabRowSelected,
                    trendingMovieItem = trendingMovieItem,
                    movies = movies,
                    upcomingMovies = upcomingMovies,
                    trendingTvShowItem = trendingTvShowItem,
                    trendingTvShows = trendingTvShows,
                    isRefreshing = isRefreshing,
                    uiEvent = uiEvent
                )
            }
        }
    }

    LaunchedEffect(switch) {
        delay(3000L)
        switch.value = true
//        viewModel.updateOnce()
    }
}


///////////////////////////////////////
//
// PREVIEWS
//
///////////////////////////////////////
@DevicePreviews
@Composable
private fun PreviewTheatersContainer(@PreviewParameter(PreviewProviderNetworkState::class) networkState: NetworkState) {
    TheLabTheme(darkTheme = true) {
        TheatersContainer(
            isActivitiesSplashScreenEnable = false,
            networkState = networkState,
            categories = listOf("Movies", "Tv Shows"),
            tabRowSelected = 0,
            trendingMovieItem = TMDBUiState.TMDBTrendingMovieItemUiState.Loading,
            movies = TMDBUiState.TMDBMoviesUiState.Success(TMDBMovieResponse.mockTMDBMovieResponse),
            upcomingMovies = TMDBUiState.TMDBUpcomingMoviesUiState.Loading,
            trendingTvShowItem = TMDBUiState.TMDBTrendingTvShowItemUiState.Loading,
            trendingTvShows = TMDBUiState.TMDBTvShowsUiState.Loading,
            isRefreshing = false,
            uiEvent = {}
        )
    }
}