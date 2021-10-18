package com.simprints.id.tools

import android.annotation.SuppressLint
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

    @SuppressLint("MissingPermission")
    override suspend fun requestLocation(request: LocationRequest): Flow<List<Location>> = channelFlow {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                // The offer  method was deprecated in the favour of [trySend].
                // Calling offer in a closed channel throws "AbortFlowException: Flow was aborted, no more elements needed"
                // this could be a race condition where the channel is just closed before canceling this callback.
                trySend(result.locations)
            }
        }

        locationClient.requestLocationUpdates(request, locationCallback, null)

        awaitClose {
            locationClient.removeLocationUpdates(locationCallback)
        }
    }

}
