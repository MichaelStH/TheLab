package com.riders.thelab.feature.transitions.compose

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.riders.thelab.core.ui.R
import com.riders.thelab.core.ui.compose.annotation.DevicePreviews
import com.riders.thelab.core.ui.compose.component.toolbar.TheLabTopAppBar
import com.riders.thelab.core.ui.compose.theme.TheLabTheme
import timber.log.Timber

private val boundsTransform = { _: Rect, _: Rect -> tween<Rect>(350) }

///////////////////////////////////////
//
// COMPOSE
//
///////////////////////////////////////
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MainScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
    onClicked: () -> Unit
) {
    TheLabTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(modifier)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top)
            ) {
                with(sharedTransitionScope) {
                    Image(
                        modifier = Modifier
                            .aspectRatio(16f / 9f)
                            .sharedElement(
                                state = rememberSharedContentState(key = "image-0"),
                                animatedVisibilityScope = animatedVisibilityScope,
                                boundsTransform = boundsTransform
                            ),
                        painter = painterResource(id = R.drawable.logo_colors),
                        contentDescription = "item_image",
                        contentScale = ContentScale.Crop
                    )

                    Button(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .sharedElement(
                                state = rememberSharedContentState(key = "button-0"),
                                animatedVisibilityScope = animatedVisibilityScope,
                                boundsTransform = boundsTransform
                            )
                            .skipToLookaheadSize(),
                        onClick = onClicked
                    ) {

                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = "Show Detail",
                            style = TextStyle(
                                fontWeight = FontWeight.W600,
                                fontSize = 18.sp,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DetailScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit
) {
    TheLabTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(modifier),
            contentAlignment = Alignment.Center
        ) {

            with(sharedTransitionScope) {
                Image(
                    modifier = Modifier
                        .aspectRatio(16f / 9f)
                        .sharedElement(
                            state = rememberSharedContentState(key = "image-0"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = boundsTransform
                        )
                        .layoutId("image_logo_id"),
                    painter = painterResource(id = R.drawable.logo_colors),
                    contentDescription = "item_image",
                    contentScale = ContentScale.Crop
                )

                Button(
                    modifier = Modifier
                        .padding(bottom = 32.dp)
                        .sharedElement(
                            state = rememberSharedContentState(key = "button-0"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = boundsTransform
                        )
                        .skipToLookaheadSize()
                        .align(Alignment.BottomCenter),
                    onClick = onBackClick
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = "Go Back",
                        style = TextStyle(
                            fontWeight = FontWeight.W600,
                            fontSize = 18.sp,
                        ),
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun TransitionsComposeContent() {
    val navController = rememberNavController()
    val backstackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backstackEntry?.destination?.route

    TheLabTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = { TheLabTopAppBar(title = "Transitions with Compose") }
        ) { contentPadding ->
            SharedTransitionLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                NavHost(
                    modifier = Modifier.fillMaxSize(),
                    navController = navController,
                    startDestination = Screen.Main.route.toString()
                ) {
                    composable(route = Screen.Main.route.toString()) {
                        MainScreen(
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this@composable,
                            onClicked = { navController.navigate(route = Screen.Detail.route.toString()) }
                        )
                    }

                    composable(route = "detail") {
                        DetailScreen(
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this@composable,
                            modifier = Modifier.fillMaxSize(),
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
    }
}


///////////////////////////////////////
//
// PREVIEWS
//
///////////////////////////////////////
@OptIn(ExperimentalSharedTransitionApi::class)
@DevicePreviews
@Composable
private fun PreviewMainScreen() {
    val navController = rememberNavController()
    TheLabTheme {
        SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
            NavHost(
                modifier = Modifier.fillMaxSize(),
                navController = navController,
                startDestination = Screen.Main.route.toString()
            ) {
                composable(route = Screen.Main.route.toString()) {
                    MainScreen(
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this,
                        modifier = Modifier.fillMaxSize(),
                        onClicked = {}
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@DevicePreviews
@Composable
private fun PreviewDetailScreen() {
    val navController = rememberNavController()
    TheLabTheme {
        SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
            NavHost(
                modifier = Modifier.fillMaxSize(),
                navController = navController,
                startDestination = Screen.Detail.route.toString()
            ) {
                composable(route = Screen.Detail.route.toString()) {
                    DetailScreen(
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this,
                        modifier = Modifier.fillMaxSize(),
                        onBackClick = {}
                    )
                }
            }
        }
    }
}

@DevicePreviews
@Composable
private fun PreviewTransitionsComposeContent() {
    TheLabTheme {
        TransitionsComposeContent()
    }
}