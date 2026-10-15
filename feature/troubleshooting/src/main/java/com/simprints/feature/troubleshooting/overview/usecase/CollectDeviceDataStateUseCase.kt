package com.simprints.feature.troubleshooting.overview.usecase

import com.simprints.core.domain.sync.SyncFailureReason
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
            Last event sync: ${state.lastEventSyncAt.renderTime()} ${state.lastEventSyncFailure.renderFailure()}
            Last sample sync: ${state.lastSampleSyncAt.renderTime()} ${state.lastSampleSyncFailure.renderFailure()}
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

    private fun Timestamp?.renderTime(): String = this
        ?.let { timeHelper.readableBetweenNowAndTime(it) }
        ?: NEVER

    private fun SyncFailureReason?.renderFailure(): String = this?.let { "($FAILED $it)" }.orEmpty()

    companion object {
        private const val UNKNOWN = "unknown"
        private const val FAILED = "failed:"
        private const val NEVER = "never"
        private const val SIGNED_OUT = "signed out"
    }
}
