package com.simprints.eventsystem.event_sync.models

import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.events_sync.models.EventSyncState
import com.simprints.eventsystem.events_sync.models.EventSyncWorkerState
import com.simprints.eventsystem.events_sync.models.EventSyncWorkerType
import org.junit.Test

class EventSyncStateTest {

    @Test
    fun testIsRunning() {
        // No workers
        assertThat(createState(
            up = emptyList(),
            down = emptyList(),
        ).isSyncRunning()).isFalse()

        // All workers completed
        assertThat(createState(
            up = listOf(createWorker(EventSyncWorkerState.Succeeded)),
            down = listOf(createWorker(EventSyncWorkerState.Succeeded)),
        ).isSyncRunning()).isFalse()

        // Has running workers
        assertThat(createState(
            up = listOf(createWorker(EventSyncWorkerState.Running)),
            down = listOf(createWorker(EventSyncWorkerState.Succeeded)),
        ).isSyncRunning()).isTrue()
        assertThat(createState(
            up = listOf(createWorker(EventSyncWorkerState.Succeeded)),
            down = listOf(createWorker(EventSyncWorkerState.Running)),
        ).isSyncRunning()).isTrue()
        assertThat(createState(
            up = listOf(createWorker(EventSyncWorkerState.Running)),
            down = listOf(createWorker(EventSyncWorkerState.Running)),
        ).isSyncRunning()).isTrue()

        // Has enqueued workers
        assertThat(createState(
            up = listOf(createWorker(EventSyncWorkerState.Enqueued)),
            down = listOf(createWorker(EventSyncWorkerState.Succeeded)),
        ).isSyncRunning()).isTrue()
        assertThat(createState(
            up = listOf(createWorker(EventSyncWorkerState.Succeeded)),
            down = listOf(createWorker(EventSyncWorkerState.Enqueued)),
        ).isSyncRunning()).isTrue()
        assertThat(createState(
            up = listOf(createWorker(EventSyncWorkerState.Enqueued)),
            down = listOf(createWorker(EventSyncWorkerState.Enqueued)),
        ).isSyncRunning()).isTrue()
    }

    private fun createState(
        up: List<EventSyncState.SyncWorkerInfo>,
        down: List<EventSyncState.SyncWorkerInfo>,
    ) = EventSyncState("id", 0, 0, up, down)

    private fun createWorker(state: EventSyncWorkerState) =
        EventSyncState.SyncWorkerInfo(type = EventSyncWorkerType.DOWNLOADER, state = state)

}
