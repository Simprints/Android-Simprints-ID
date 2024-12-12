package com.simprints.feature.setup.location

import android.location.Location

internal object TestLocationData {
    fun buildFakeLocation() = Location(PROVIDER).apply {
        longitude = LNG
        latitude = LAT
        accuracy = ACCURACY
    }

    private const val PROVIDER = "flp"
    private const val LAT = 37.377166
    private const val LNG = -122.086966
    private const val ACCURACY = 3.0f
}
