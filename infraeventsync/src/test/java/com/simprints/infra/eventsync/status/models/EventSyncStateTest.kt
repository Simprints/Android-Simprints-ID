package com.simprints.infra.eventsync.status.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.eventsync.status.models.EventSyncState.SyncWorkerInfo
import com.simprints.infra.eventsync.status.models.EventSyncWorkerState.*
import org.junit.Test

class EventSyncStateTest {

    @Test
    fun `is not running when there are no workers`() {
        assertThat(createState(
            up = emptyList(),
            down = emptyList(),
        ).isSyncRunning()).isFalse()
    }

    @Test
    fun `is not running when when all workers completed`() {
        assertThat(createState(
            up = listOf(createWorker(Succeeded)),
            down = listOf(createWorker(Succeeded)),
        ).isSyncRunning()).isFalse()
    }

    @Test
    fun `is running when there are running workers`() {
        assertThat(createState(
            up = listOf(createWorker(Running)),
            down = listOf(createWorker(Succeeded)),
        ).isSyncRunning()).isTrue()
        assertThat(createState(
            up = listOf(createWorker(Succeeded)),
            down = listOf(createWorker(Running)),
        ).isSyncRunning()).isTrue()
        assertThat(createState(
            up = listOf(createWorker(Running)),
            down = listOf(createWorker(Running)),
        ).isSyncRunning()).isTrue()
    }

    @Test
    fun `is running when there are enqueued workers`() {
        assertThat(createState(
            up = listOf(createWorker(Enqueued)),
            down = listOf(createWorker(Succeeded)),
        ).isSyncRunning()).isTrue()
        assertThat(createState(
            up = listOf(createWorker(Succeeded)),
            down = listOf(createWorker(Enqueued)),
        ).isSyncRunning()).isTrue()
        assertThat(createState(
            up = listOf(createWorker(Enqueued)),
            down = listOf(createWorker(Enqueued)),
        ).isSyncRunning()).isTrue()
    }

    private fun createState(
        up: List<SyncWorkerInfo>,
        down: List<SyncWorkerInfo>,
    ) = EventSyncState("id", 0, 0, up, down)

    private fun createWorker(state: EventSyncWorkerState) =
        SyncWorkerInfo(type = EventSyncWorkerType.DOWNLOADER, state = state)

}
