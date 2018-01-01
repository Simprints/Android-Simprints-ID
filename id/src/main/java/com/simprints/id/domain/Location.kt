package com.simprints.id.domain

import android.location.Location as AndroidLocation


data class Location(val latitude: String, val longitude: String) {

    companion object {

        fun fromAndroidLocation(androidLocation: AndroidLocation) =
            Location(androidLocation.latitude.toString(), androidLocation.longitude.toString())

    }

}
