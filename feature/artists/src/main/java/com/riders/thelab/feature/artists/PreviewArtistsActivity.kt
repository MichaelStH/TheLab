package com.riders.thelab.feature.artists

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.riders.thelab.core.data.local.model.compose.artists.ArtistsUiState
import com.riders.thelab.core.data.local.model.music.ArtistModel
import com.riders.thelab.core.ui.compose.annotation.DevicePreviews
import com.riders.thelab.core.ui.compose.component.NoItemFound
import com.riders.thelab.core.ui.compose.component.loading.LabLoader
import com.riders.thelab.core.ui.compose.component.toolbar.TheLabTopAppBar
import com.riders.thelab.core.ui.compose.component.toolbar.ToolbarSize
import com.riders.thelab.core.ui.compose.theme.TheLabTheme
import timber.log.Timber


///////////////////////////////////////
//
// COMPOSE
//
///////////////////////////////////////
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ArtistsContentSuccess(artists: List<ArtistModel>) {
    val navController = rememberNavController()
    val backstackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backstackEntry?.destination?.route

    SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
        NavHost(
            modifier = Modifier.fillMaxSize(),
            navController = navController,
            startDestination = ArtistScreen.List
        ) {
            composable<ArtistScreen.List> {
                ArtistMainScreen(
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@composable,
                    artists = artists,
                    onArtistClicked = { index ->
                        navController.navigate(ArtistScreen.Detail(index))
                    }
                )
            }

            composable<ArtistScreen.Detail> { backStackEntry ->
                // val id = backStackEntry.arguments?.getByte("id") ?: error("No ID")
                /*
                 * For the Detail screen, we use the toRoute() extension method to recreate the Detail object from the NavBackStackEntry
                 * and its arguments. There’s a similar extension method on SavedStateHandle,
                 * making it just as easy to get the type safe arguments in your ViewModel
                 * as well without needing to reference specific argument keys.
                 */
                val detail = backStackEntry.toRoute<ArtistScreen.Detail>()

                ArtistDetailScreen(
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@composable,
                    modifier = Modifier.fillMaxSize(),
                    artist = artists[detail.id],
                    index = detail.id,
                    onBackClick = { navController.popBackStack() },
                )
            }
        }
    }

    LaunchedEffect(currentDestination) {
        currentDestination?.let {
            Timber.d("LaunchedEffect | currentDestination | current destination: $currentDestination")

        }
    }
}

@Composable
fun ArtistsContent(state: ArtistsUiState) {
    TheLabTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TheLabTopAppBar(
                    toolbarSize = ToolbarSize.SMALL,
                    title = stringResource(R.string.app_name_artists),
                    withGradientBackground = true,
                    actions = null
                )
            }
        ) { contentPadding ->
            AnimatedContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                targetState = state,
                contentAlignment = Alignment.Center,
                label = "artists_animated_content"
            ) { targetState ->
                when (targetState) {
                    is ArtistsUiState.Loading -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(
                                16.dp,
                                Alignment.CenterVertically
                            )
                        ) {
                            LabLoader(modifier = Modifier.size(56.dp))
                            Text(text = "${targetState.message}")
                        }
                    }

                    is ArtistsUiState.Error -> {
                        NoItemFound("An error occurred while loading\n${targetState.message}")
                    }

                    is ArtistsUiState.Success -> {
                        ArtistsContentSuccess(targetState.artists)
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
private fun PreviewArtistsContentSuccess(@PreviewParameter(PreviewProviderArtists::class) artists: List<ArtistModel>) {
    TheLabTheme {
        ArtistsContentSuccess(artists)
    }
}

@DevicePreviews
@Composable
fun PreviewArtistsContent(@PreviewParameter(PreviewProviderArtistUiState::class) state: ArtistsUiState) {
    TheLabTheme {
        ArtistsContent(state)
    }
}