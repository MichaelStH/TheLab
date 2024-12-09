package com.riders.thelab.feature.locationonmaps

import com.google.android.libraries.places.api.model.Place

object Constants {

    private const val SEPARATOR = "/"

    /////////////////////////////////////////////
    //
    // Google Maps, Directions & Places
    //
    /////////////////////////////////////////////
    const val BASE_ENDPOINT_GOOGLE_DIRECTIONS_API = "https://maps.googleapis.com$SEPARATOR"

    /* Note: findCurrentPlace() and fetchPlace() support different sets of fields.
     * findCurrentPlace() does NOT support the following fields:
     * Place.Field.ADDRESS_COMPONENTS, Place.Field.OPENING_HOURS, Place.Field.PHONE_NUMBER,
     * Place.Field.UTC_OFFSET, and Place.Field.WEBSITE_URI.
     */
    @Suppress("DEPRECATION")
    val CURRENT_PLACE_FIELDS = listOf(
        Place.Field.ADDRESS,
        Place.Field.ID,
        Place.Field.LAT_LNG,
        Place.Field.NAME
    )

    @Suppress("DEPRECATION")
    val PLACES_FIELDS = listOf(
        Place.Field.ADDRESS,
        Place.Field.ADDRESS_COMPONENTS,
        Place.Field.BUSINESS_STATUS,
        Place.Field.ID,
        Place.Field.LAT_LNG,
        Place.Field.NAME,
        Place.Field.OPENING_HOURS,
        Place.Field.PHONE_NUMBER,
        Place.Field.TYPES,
        Place.Field.VIEWPORT,
        Place.Field.UTC_OFFSET
    )
}