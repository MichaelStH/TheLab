package com.riders.thelab.feature.theaters.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.riders.thelab.core.common.network.LabNetworkManager
import com.riders.thelab.core.data.local.model.tmdb.TMDBItemModel
import com.riders.thelab.core.ui.compose.base.BaseComponentActivity
import com.riders.thelab.core.ui.compose.theme.TheLabTheme
import com.riders.thelab.feature.theaters.detail.TheatersDetailActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber


@AndroidEntryPoint
class TheatersActivity : BaseComponentActivity() {

    private val mTheatersViewModel: TheatersViewModel by viewModels()

    private val mNetworkManager: LabNetworkManager by lazy {
        LabNetworkManager(context = this@TheatersActivity, lifecycle = this.lifecycle)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mTheatersViewModel.observeNetworkState(mNetworkManager)

        lifecycleScope.launch {
            Timber.d("coroutine launch with name ${this.coroutineContext}")
            repeatOnLifecycle(Lifecycle.State.CREATED) {

                setContent {

                    val networkState by mNetworkManager.networkState.collectAsStateWithLifecycle()

                    val trendingMovieItem by mTheatersViewModel.tmdbTrendingMovieItemUiState.collectAsStateWithLifecycle()
                    val movies by mTheatersViewModel.tmdbMoviesUiState.collectAsStateWithLifecycle()
                    val upcomingMovies by mTheatersViewModel.tmdbUpcomingMoviesUiState.collectAsStateWithLifecycle()
                    val trendingTvShowItem by mTheatersViewModel.tmdbTrendingTvShowItemUiState.collectAsStateWithLifecycle()
                    val trendingTvShows by mTheatersViewModel.tmdbTrendingTvShowsUiState.collectAsStateWithLifecycle()

                    TheLabTheme {
                        // A surface container using the 'background' color from the theme
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            TheatersContainer(
                                networkState = networkState,
                                isActivitiesSplashScreenEnable = mTheatersViewModel.isActivitiesSplashEnabled,
                                categories = mTheatersViewModel.categories,
                                tabRowSelected = mTheatersViewModel.tabRowSelected,
                                trendingMovieItem = trendingMovieItem,
                                movies = movies,
                                upcomingMovies = upcomingMovies,
                                trendingTvShowItem = trendingTvShowItem,
                                trendingTvShows = trendingTvShows,
                                isRefreshing = mTheatersViewModel.isRefreshing,
                                uiEvent = { event ->
                                    when (event) {
                                        is UiEvent.OnItemDetailClicked -> launchTMDBItemDetailActivity(
                                            event.item
                                        )

                                        else -> mTheatersViewModel.onEvent(event)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun backPressed() {
        finish()
    }


    private fun launchTMDBItemDetailActivity(item: TMDBItemModel) {
        Timber.d("launchTMDBItemDetailActivity() | movie: $item")

        Intent(this@TheatersActivity, TheatersDetailActivity::class.java)
            .apply {
                putExtra(TheatersDetailActivity.EXTRA_TMDB_ITEM, Json.encodeToString(item))
            }
            .runCatching {
                startActivity(this)
            }
            .onFailure {
                Timber.e("runCatching - onFailure() | Error caught: ${it.message}")
            }
            .onSuccess {
                Timber.d("runCatching - onSuccess() | Activity launched successfully")
            }
    }
}