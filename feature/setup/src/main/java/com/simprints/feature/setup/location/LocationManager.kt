package com.simprints.feature.setup.location

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.experimental
import com.simprints.infra.events.event.domain.models.scope.Location
import com.simprints.infra.logging.LoggingConstants.CrashReportTag
import com.simprints.infra.logging.Simber
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

internal class LocationManager @Inject constructor(
    @param:ApplicationContext val ctx: Context,
    private val configRepository: ConfigRepository,
) {
    private val locationClient = LocationServices.getFusedLocationProviderClient(ctx)

    /**
     * Returns up to 2 results in order - lastLocation and getCurrentLocation.
     * Current location is more accurate, but takes longer to return.
     */
    @SuppressLint("MissingPermission")
    fun requestLocation(): Flow<Location?> = flow {
        val cancellationTokenSource = CancellationTokenSource()

        val priority = if (configRepository.getProjectConfiguration().experimental().useBalancedLocationAccuracy) {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        } else {
            Priority.PRIORITY_HIGH_ACCURACY
        }

        try {
            val lastLocation = locationClient.lastLocation.await()
            if (lastLocation != null) {
                Simber.i("Returning last known location", tag = CrashReportTag.SESSION)
                emit(
                    Location(
                        latitude = lastLocation.latitude,
                        longitude = lastLocation.longitude,
                        lastLocationTime = lastLocation.time,
                    ),
                )
            }

            val currentLocation = locationClient
                .getCurrentLocation(priority, cancellationTokenSource.token)
                .await()
            if (currentLocation != null) {
                Simber.i("Returning current location", tag = CrashReportTag.SESSION)
                emit(
                    Location(
                        latitude = currentLocation.latitude,
                        longitude = currentLocation.longitude,
                    ),
                )
            }
        } finally {
            cancellationTokenSource.cancel()
        }
    }
}
