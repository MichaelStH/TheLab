package com.riders.thelab.feature.theaters.main

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.riders.thelab.core.common.network.NetworkState
import com.riders.thelab.core.data.local.model.compose.theaters.TMDBUiState
import com.riders.thelab.core.ui.compose.annotation.DevicePreviews
import com.riders.thelab.core.ui.compose.component.LabHorizontalViewPagerGeneric
import com.riders.thelab.core.ui.compose.component.network.NoNetworkConnection
import com.riders.thelab.core.ui.compose.component.network.PreviewProviderNetworkState
import com.riders.thelab.core.ui.compose.component.tab.LabTabRow
import com.riders.thelab.core.ui.compose.component.toolbar.TheLabTopAppBar
import com.riders.thelab.core.ui.compose.theme.TheLabTheme
import kotlinx.coroutines.launch
import timber.log.Timber


///////////////////////////////////////
//
// COMPOSE
//
///////////////////////////////////////
@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TheatersContent(
    networkState: NetworkState,
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
    val scope = rememberCoroutineScope()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            uiEvent.invoke(UiEvent.OnPullToRefresh(true))
            if (isRefreshing) {
                uiEvent.invoke(UiEvent.OnFetchTMDBData)
            }
        }
    )

    val pagerState: PagerState = rememberPagerState { categories.size }

    val transitionSpec: () -> ContentTransform = {
        fadeIn(tween(durationMillis = 200)) togetherWith fadeOut(
            tween(
                durationMillis = 200
            )
        )
    }

    TheLabTheme(darkTheme = true) {
        Scaffold(
            topBar = {
                TheLabTopAppBar(
                    mainCustomContent = {
                        AnimatedContent(
                            targetState = networkState,
                            transitionSpec = { transitionSpec.invoke() },
                            label = "network state animation"
                        ) { targetState: NetworkState ->
                            when (targetState) {
                                is NetworkState.Available -> {
                                    LabTabRow(
                                        selectedItemIndex = tabRowSelected,
                                        items = categories,
                                        onClick = { index ->
                                            uiEvent.invoke(UiEvent.OnUpdateTabRowSelected(index))
                                        }
                                    )
                                }

                                else -> {
                                    Box {}
                                }
                            }
                        }
                    },
                    withGradientBackground = true,
                    navigationIconColor = Color.White
                ) {}
            }
        ) {

            AnimatedContent(
                targetState = networkState,
                transitionSpec = { transitionSpec() },
                label = "network state animation"
            ) { targetState: NetworkState ->
                when (targetState) {
                    is NetworkState.Available -> {

                        Box(
                            modifier = Modifier.pullRefresh(
                                state = pullRefreshState,
                                enabled = true
                            )
                        ) {
                            LabHorizontalViewPagerGeneric(
                                pagerState = pagerState,
                                items = categories,
                                onCurrentPageChanged = {}
                            ) { page, _ ->

                                when (page) {
                                    0 -> {
                                        ScreenMovieContent(
                                            trendingMovieItem,
                                            movies,
                                            upcomingMovies,
                                            uiEvent = uiEvent
                                        )
                                    }

                                    1 -> {
                                        ScreenTvShowsContent(
                                            trendingTvShowItem,
                                            trendingTvShows,
                                            uiEvent = uiEvent
                                        )
                                    }
                                }
                            }

                            PullRefreshIndicator(
                                modifier = Modifier.align(Alignment.TopCenter),
                                refreshing = isRefreshing,
                                state = pullRefreshState
                            )
                        }
                    }

                    else -> {
                        NoNetworkConnection()
                    }
                }

            }
        }
    }

    LaunchedEffect(key1 = tabRowSelected) {
        Timber.d("LaunchedEffect | tabRowSelected : $tabRowSelected | with: ${this.coroutineContext}")

        scope.launch {
            pagerState.animateScrollToPage(tabRowSelected)
        }
    }

    LaunchedEffect(key1 = isRefreshing) {
        Timber.d("LaunchedEffect | isRefreshing : $isRefreshing | with: ${this.coroutineContext}")

        if (isRefreshing) {
            uiEvent.invoke(UiEvent.OnPullToRefresh(isRefreshing))
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
private fun PreviewTheatersContent(@PreviewParameter(PreviewProviderNetworkState::class) networkState: NetworkState) {

    TheLabTheme(darkTheme = true) {
        TheatersContent(
            networkState = networkState,
            categories = listOf("Movies", "Tv Shows"),
            tabRowSelected = 0,
            trendingMovieItem = TMDBUiState.TMDBTrendingMovieItemUiState.Loading,
            movies = TMDBUiState.TMDBMoviesUiState.Loading,
            upcomingMovies = TMDBUiState.TMDBUpcomingMoviesUiState.Loading,
            trendingTvShowItem = TMDBUiState.TMDBTrendingTvShowItemUiState.Loading,
            trendingTvShows = TMDBUiState.TMDBTvShowsUiState.Loading,
            isRefreshing = false,
            uiEvent = {}
        )
    }
}