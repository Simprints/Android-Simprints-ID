package com.simprints.infra.eventsync.status.models

import androidx.work.WorkInfo
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EventSyncWorkerStateTest {
    @Test
    fun shouldMapWorkInfoCorrectlyIfError() {
        val state =
            EventSyncWorkerState.fromWorkInfo(WorkInfo.State.FAILED, failedBecauseBackendMaintenance = true, estimatedOutage = 600)

        assertThat(state).isInstanceOf(EventSyncWorkerState.Failed::class.java)
        assertThat((state as EventSyncWorkerState.Failed).failedBecauseBackendMaintenance).isTrue()
        assertThat(state.estimatedOutage).isEqualTo(600L)
    }

    @Test
    fun shouldMapWorkInfoCorrectlyIfNoError() {
        mapOf(
            WorkInfo.State.ENQUEUED to EventSyncWorkerState.Enqueued::class.java,
            WorkInfo.State.RUNNING to EventSyncWorkerState.Running::class.java,
            WorkInfo.State.SUCCEEDED to EventSyncWorkerState.Succeeded::class.java,
            WorkInfo.State.BLOCKED to EventSyncWorkerState.Blocked::class.java,
            WorkInfo.State.CANCELLED to EventSyncWorkerState.Cancelled::class.java,
        ).forEach { (state, stateInstance) ->
            assertThat(EventSyncWorkerState.fromWorkInfo(state)).isInstanceOf(stateInstance)
        }
    }
}
