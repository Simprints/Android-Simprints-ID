package com.simprints.feature.setup.location

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.simprints.infra.events.event.domain.models.scope.Location
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import javax.inject.Inject
import android.location.Location as SystemLocation

internal class LocationManager @Inject constructor(
    @param:ApplicationContext val ctx: Context,
) {
    private val locationClient = LocationServices.getFusedLocationProviderClient(ctx)

    @SuppressLint("MissingPermission")
    fun requestLocation(request: LocationRequest): Flow<Location?> = channelFlow {
        val cancellationTokenSource = CancellationTokenSource()
        val currentLocationTask: Task<SystemLocation> = locationClient.getCurrentLocation(request.priority, cancellationTokenSource.token)

        currentLocationTask.addOnCompleteListener { task ->
            trySend(task.takeIf { it.isSuccessful }?.result?.let { Location(it.latitude, it.longitude) })
        }

        awaitClose {
            cancellationTokenSource.cancel()
        }
    }
}
