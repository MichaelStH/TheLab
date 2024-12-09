package com.riders.thelab.feature.theaters.previewprovider

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.riders.thelab.core.data.local.model.compose.theaters.TMDBUiState
import com.riders.thelab.core.data.local.model.tmdb.TMDBItemModel
import com.riders.thelab.core.data.local.model.tmdb.TMDBVideoModel
import com.riders.thelab.core.data.local.model.tmdb.toItemModel
import com.riders.thelab.core.data.remote.dto.tmdb.MovieDto


class PreviewProviderTMDBVideoModel : PreviewParameterProvider<TMDBVideoModel> {
    override val values: Sequence<TMDBVideoModel>
        get() = sequenceOf(
            TMDBVideoModel(
                name = "20th Anniversary Trailer",
                key = "dfeUzm6KF4g",
                site = "YouTube",
                size = 1080,
                type = "Trailer",
                official = true,
                publishedAt = "2019-10-15T18:59:47.000Z",
                id = "64fb16fbdb4ed610343d72c3"
            )
        )
}

class PreviewProviderTMDBItemModel : PreviewParameterProvider<TMDBItemModel> {
    override val values: Sequence<TMDBItemModel>
        get() = sequenceOf(
            MovieDto.venomMockMovie.toItemModel(),
            MovieDto.platform2MockMovie.toItemModel()
        )
}

class PreviewProviderTMDBDetailUiState : PreviewParameterProvider<TMDBUiState.TMDBDetailUiState> {
    override val values: Sequence<TMDBUiState.TMDBDetailUiState>
        get() = sequenceOf(
            TMDBUiState.TMDBDetailUiState.Error("Error while fetching movie's details"),
            TMDBUiState.TMDBDetailUiState.Success(MovieDto.venomMockMovie.toItemModel()),
            TMDBUiState.TMDBDetailUiState.Loading
        )
}