package com.riders.thelab.data.local.model

import kotlinx.serialization.SerialName
import java.io.Serializable

@kotlinx.serialization.Serializable
data class Director(
    @SerialName("lastname")
    override  val lastName:String,
    @SerialName("firstname")
    override val firstName:String,
    @SerialName("thumbnail")
    override val urlThumbnail: String
): Members, Serializable
