package com.simprints.id.services.sync.events.master.models

import androidx.work.WorkInfo
import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.events_sync.models.EventSyncWorkerState
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
        val state =
            EventSyncWorkerState.fromWorkInfo(WorkInfo.State.SUCCEEDED)

        assertThat(state).isInstanceOf(EventSyncWorkerState.Succeeded::class.java)
    }
}
