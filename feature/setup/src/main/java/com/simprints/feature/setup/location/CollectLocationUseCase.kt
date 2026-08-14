package com.simprints.feature.setup.location

import com.simprints.infra.events.event.domain.models.scope.Location
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject

internal class CollectLocationUseCase @Inject constructor(
    private val locationManager: LocationManager,
    private val updateSessionScopeLocationUseCase: UpdateSessionScopeLocationUseCase,
) {
    /**
     * Runs directly in the caller's coroutine context/scope so that cancelling the caller
     * (e.g. cancelling its scope's children) cancels this collection too, without needing to
     * track and cancel a [kotlinx.coroutines.Job] manually.
     */
    suspend operator fun invoke() {
        val requestStartTimeMs = System.currentTimeMillis()
        Simber.i("Started collecting location", tag = TAG)
        try {
            locationManager
                .requestLocation()
                .filterNotNull()
                .collect { location ->
                    try {
                        saveUserLocation(location, requestStartTimeMs)
                    } catch (c: CancellationException) {
                        throw c
                    } catch (t: Throwable) {
                        Simber.e("Failed to save user's location", t, tag = TAG)
                    }
                }
            Simber.d("Finished collecting location (took ${elapsedMs(requestStartTimeMs)}ms)", tag = TAG)
        } catch (c: CancellationException) {
            Simber.d("Stopped collecting location (took ${elapsedMs(requestStartTimeMs)}ms)", tag = TAG)
            throw c
        } catch (t: Throwable) {
            Simber.e("Failed to collect location", t, tag = TAG)
        }
    }

    private suspend fun saveUserLocation(
        location: Location,
        requestStartTimeMs: Long,
    ) {
        updateSessionScopeLocationUseCase(location)
        Simber.d("Saved user's location into the current session (took ${elapsedMs(requestStartTimeMs)}ms)", tag = TAG)
    }

    private fun elapsedMs(requestStartTimeMs: Long) = System.currentTimeMillis() - requestStartTimeMs

    private companion object {
        private const val TAG = "CollectLocationUseCase"
    }
}
