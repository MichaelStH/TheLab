package com.riders.thelab.core.data.remote.dto.tmdb

import kotlinx.serialization.SerialName
import java.io.Serializable

@kotlinx.serialization.Serializable
data class MovieDto(
    @SerialName(value = "id")
    val id: Int,
    @SerialName(value = "title")
    val title: String,
    @SerialName(value = "adult")
    val adult: Boolean,
    @SerialName(value = "backdrop_path")
    val backdropPath: String?,
    @SerialName(value = "genre_ids")
    val genresID: Set<Int>,
    @SerialName(value = "original_language")
    val originalLanguage: String,
    @SerialName(value = "original_title")
    val originalTitle: String,
    @SerialName(value = "overview")
    val overview: String,
    @SerialName(value = "popularity")
    val popularity: Double,
    @SerialName(value = "poster_path")
    val poster: String?,
    @SerialName(value = "release_date")
    val releaseDate: String,
    @SerialName(value = "video")
    val video: Boolean,
    @SerialName(value = "vote_average")
    val rating: Double,
    @SerialName(value = "vote_count")
    val voteNumber: Int
) : Serializable {

    constructor() : this(0, "", false, "", emptySet(), "", "", "", 0.0, "", "", false, 0.0, 0)

    companion object {
        val mockMovie = MovieDto(
            0,
            "",
            false,
            "/efpojdpcjzidcjpzdko.jpg",
            emptySet(),
            "en-US",
            "Expend4bles",
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum",
            50.6,
            "/fv45onsdvdv.jpg",
            "2023-10-25",
            false,
            50.56,
            3455
        )

        val platform2MockMovie = MovieDto(
            id = 1125510,
            title = "The Platform 2",
            backdropPath = "/3m0j3hCS8kMAaP9El6Vy5Lqnyft.jpg",
            genresID = setOf(878, 53, 27, 18),
            originalLanguage = "es",
            originalTitle = "El hoyo 2",
            overview = "After a mysterious leader imposes his law in a brutal system of vertical cells, a new arrival battles against a dubious food distribution method.",
            popularity = 1012.359,
            poster = "/z2m3g8QOEMNyslgBU5vsyzRwgSz.jpg",
            releaseDate = "2024-09-27",
            video = false,
            adult = false,
            rating = 5.7,
            voteNumber = 701
        )

        val venomMockMovie = MovieDto(
            id = 912649,
            title = "Venom: The Last Dance",
            backdropPath = "/3V4kLQg0kSqPLctI5ziYWabAZYF.jpg",
            genresID = setOf(28, 878, 12),
            originalLanguage = "en",
            originalTitle = "Venom: The Last Dance",
            overview = "Eddie and Venom are on the run. Hunted by both of their worlds and with the net closing in, the duo are forced into a devastating decision that will bring the curtains down on Venom and Eddie's last dance.",
            popularity = 2994.212,
            poster = "/k42Owka8v91trK1qMYwCQCNwJKr.jpg",
            releaseDate = "2024-10-22",
            video = false,
            adult = false,
            rating = 6.7,
            voteNumber = 94
        )

        /*fun getMockMovie(): MovieDto = MovieDto(
            0,
            "",
            false,
            "/efpojdpcjzidcjpzdko.jpg",
            emptySet(),
            "en-US",
            "Expend4bles",
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum",
            50.6,
            "/fv45onsdvdv.jpg",
            "2023-10-25",
            false,
            50.56,
            3455
        )*/
    }
}