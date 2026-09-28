package com.simprints.feature.troubleshooting.overview.usecase

import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import com.simprints.infra.sync.devicestate.DeviceStateDataTracker
import javax.inject.Inject

internal class CollectDeviceDataStateUseCase @Inject constructor(
    private val deviceStateDataTracker: DeviceStateDataTracker,
    private val timeHelper: TimeHelper,
) {
    suspend operator fun invoke(): String {
        val state = deviceStateDataTracker.getCurrentDeviceDataState()

        return """
            Project ID: ${state.projectId ?: SIGNED_OUT}
            Records on device: ${state.recordCount.render()}
            Pending events: ${state.pendingEvents.render()}
            Pending enrolments: ${state.pendingEnrolments.render()}
            Pending samples: ${state.pendingSamples.render()}
            Pending scopes: ${state.pendingScopes.render()}
            Last event sync: ${state.lastEventSyncAt.renderTime()}
            Last sample sync: ${state.lastSampleSyncAt.renderTime()}
            """.trimIndent()
    }

    /**
     * A count that could not be read must never read as 0 here - this screen is used to decide
     * whether a device is holding data at risk.
     */
    private fun Int?.render(): String = this?.toString() ?: UNKNOWN

    private fun Map<EventScopeType, Int>?.render(): String = this
        ?.entries
        ?.joinToString { "${it.key}: ${it.value}" }
        ?: UNKNOWN

    /** Null covers both "never synced on this install" and "the store could not be read". */
    private fun Long?.renderTime(): String = this
        ?.let { timeHelper.readableBetweenNowAndTime(Timestamp(it)) }
        ?: NEVER

    companion object {
        private const val UNKNOWN = "unknown"
        private const val NEVER = "never"
        private const val SIGNED_OUT = "signed out"
    }
}
