package com.simprints.id.tools

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import javax.inject.Inject

class LocationManagerImpl @Inject constructor(val ctx: Context) : LocationManager {

    private val locationClient = LocationServices.getFusedLocationProviderClient(ctx)

    @SuppressLint("MissingPermission")
    override fun requestLocation(request: LocationRequest): Flow<Location?> = channelFlow {
        val cancellationTokenSource = CancellationTokenSource()
        val currentLocationTask: Task<Location> =
            locationClient.getCurrentLocation(request.priority, cancellationTokenSource.token)
        currentLocationTask.addOnCompleteListener {
            val result: Location? = if (it.isSuccessful) {
                it.result
            } else {
                null
            }
            trySend(result)
        }

        awaitClose {
            cancellationTokenSource.cancel()
        }
    }

}
