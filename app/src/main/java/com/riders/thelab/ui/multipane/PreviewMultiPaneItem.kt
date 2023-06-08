package com.riders.thelab.ui.multipane

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import coil.size.Size
import com.riders.thelab.R
import com.riders.thelab.core.compose.annotation.DevicePreviews
import com.riders.thelab.core.compose.ui.theme.TheLabTheme
import com.riders.thelab.core.compose.ui.theme.Typography
import com.riders.thelab.data.local.bean.MovieEnum
import com.riders.thelab.data.local.model.Movie


///////////////////////////////////////
//
// COMPOSE
//
///////////////////////////////////////
@Composable
fun TrendingMovie(movie: Movie) {
    val painter = rememberAsyncImagePainter(
        model = ImageRequest
            .Builder(LocalContext.current)
            .data(movie.urlThumbnail)
            .apply {
                crossfade(true)
                allowHardware(false)
                //transformations(RoundedCornersTransformation(32.dp.value))
                size(Size.ORIGINAL)
                scale(Scale.FIT)
            }
            .build(),
        placeholder = painterResource(R.drawable.logo_colors),
    )
    val state = painter.state

    TheLabTheme {
        Box(
            modifier = Modifier
                .defaultMinSize(1.dp)
                .height(650.dp)
        ) {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(650.dp)
                    .clip(RoundedCornerShape(bottomStart = 35.dp, bottomEnd = 35.dp))
                    .align(Alignment.Center),
                painter = painter,
                contentDescription = "weather icon wth coil",
                contentScale = ContentScale.Crop,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(.4f)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black,
                                Color.Black,
                            )
                        )
                    )
                    .align(Alignment.BottomCenter),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom)
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                    text = "Trending right now",
                    color = Color.White,
                    style = Typography.titleLarge,
                    textAlign = TextAlign.Start
                )

                Text(
                    modifier = Modifier.padding(24.dp),
                    text = "${movie.title}",
                    color = Color.White
                )
            }
        }
    }

    //TODO : Create button add more info
}

@Composable
fun MovieItem(movie: Movie) {
    val painter = rememberAsyncImagePainter(
        model = ImageRequest
            .Builder(LocalContext.current)
            .data(movie.urlThumbnail)
            .apply {
                crossfade(true)
                allowHardware(false)
                //transformations(RoundedCornersTransformation(32.dp.value))
                size(Size.ORIGINAL)
                scale(Scale.FIT)
            }
            .build(),
        placeholder = painterResource(R.drawable.logo_colors),
    )
    val state = painter.state

    TheLabTheme {

        Card(
            modifier = Modifier.size(
                width = dimensionResource(id = R.dimen.max_card_image_height),
                height = dimensionResource(id = R.dimen.max_card_image_width)
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(2.5f)
                        .clip(RoundedCornerShape(12.dp)),
                    painter = painter,
                    contentDescription = "weather icon wth coil",
                    contentScale = ContentScale.Crop,
                )

                Text(modifier = Modifier.weight(.5f), text = "${movie.title}")
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
private fun PreviewTrendingMovie() {
    val movie = MovieEnum.getMovies().random()

    TheLabTheme {
        TrendingMovie(movie)
    }
}

@DevicePreviews
@Composable
private fun PreviewMovieItem() {
    val movie = MovieEnum.getMovies().random()

    TheLabTheme {
        MovieItem(movie)
    }
}