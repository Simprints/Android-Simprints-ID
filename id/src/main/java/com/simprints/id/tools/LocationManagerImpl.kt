package com.simprints.id.tools

import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

class LocationManagerImpl(val ctx: Context) : LocationManager {

    private val locationClient = LocationServices.getFusedLocationProviderClient(ctx)

    override suspend fun requestLocation(request: LocationRequest): Flow<List<Location>> = channelFlow {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                offer(result.locations)
            }
        }

        locationClient.requestLocationUpdates(request, locationCallback, null)

        awaitClose {
            locationClient.removeLocationUpdates(locationCallback)
        }
    }

}
