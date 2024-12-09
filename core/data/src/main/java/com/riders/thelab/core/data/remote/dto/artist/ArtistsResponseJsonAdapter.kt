package com.riders.thelab.core.data.remote.dto.artist

/*import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson*/

class ArtistsResponseJsonAdapter {
//    @FromJson
    fun artistsFromJson(artistsEventJson: List<Artist>): List<Artist> {
        return artistsEventJson
    }

//    @ToJson
    fun artistsToJson(artists: List<Artist?>): String {
        return artists.toString()
    }
}