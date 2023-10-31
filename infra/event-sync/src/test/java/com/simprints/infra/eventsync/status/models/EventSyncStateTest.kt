package com.simprints.infra.eventsync.status.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.eventsync.status.models.EventSyncState.SyncWorkerInfo
import com.simprints.infra.eventsync.status.models.EventSyncWorkerState.*
import org.junit.Test

class EventSyncStateTest {

    @Test
    fun `isThereNotSyncHistory() is true when there are no workers`() {
        assertThat(createState(
            up = emptyList(),
            down = emptyList(),
        ).isThereNotSyncHistory()).isTrue()
    }
    @Test
    fun `isThereNotSyncHistory() is false when there are workers`() {
        assertThat(createState(
            up = listOf(createWorker(Succeeded)),
            down = emptyList(),
        ).isThereNotSyncHistory()).isFalse()
    }

    @Test
    fun `isSyncRunning() is false when there are no workers`() {
        assertThat(createState(
            up = emptyList(),
            down = emptyList(),
        ).isSyncRunning()).isFalse()
    }

    @Test
    fun `isSyncRunning() is false when when all workers completed`() {
        assertThat(createState(
            up = listOf(createWorker(Succeeded)),
            down = listOf(createWorker(Succeeded)),
        ).isSyncRunning()).isFalse()
    }

    @Test
    fun `isSyncRunning() is true when there are running workers`() {
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
    fun `isSyncRunning() is true when there are enqueued workers`() {
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

    @Test
    fun `isSyncCompleted() is true when all workers are completed`() {
        assertThat(createState(
            up = listOf(createWorker(Succeeded)),
            down = listOf(createWorker(Succeeded)),
        ).isSyncCompleted()).isTrue()
    }

    @Test
    fun `isSyncCompleted() is false when there are enqueued workers`() {
        assertThat(createState(
            up = listOf(createWorker(Enqueued)),
            down = listOf(createWorker(Succeeded)),
        ).isSyncCompleted()).isFalse()
    }

    @Test
    fun `isSyncCompleted() is false when there are running workers`() {
        assertThat(createState(
            up = listOf(createWorker(Running)),
            down = listOf(createWorker(Succeeded)),
        ).isSyncCompleted()).isFalse()
    }

    @Test
    fun `isSyncInProgress() is false when there are no running workers`() {
        assertThat(createState(
            up = listOf(createWorker(Enqueued)),
            down = listOf(createWorker(Succeeded)),
        ).isSyncInProgress()).isFalse()
    }

    @Test
    fun `isSyncInProgress() is true when there are running workers`() {
        assertThat(createState(
            up = listOf(createWorker(Running)),
            down = listOf(createWorker(Succeeded), createWorker(Enqueued)),
        ).isSyncInProgress()).isTrue()
    }

    @Test
    fun `isSyncConnecting() is false when there are no enqueued workers`() {
        assertThat(createState(
            up = listOf(createWorker(Running)),
            down = listOf(createWorker(Succeeded)),
        ).isSyncConnecting()).isFalse()
    }

    @Test
    fun `isSyncConnecting() is true when there are enqueued workers`() {
        assertThat(createState(
            up = listOf(createWorker(Running)),
            down = listOf(createWorker(Succeeded), createWorker(Enqueued)),
        ).isSyncConnecting()).isTrue()
    }

    @Test
    fun `isSyncFailedBecauseReloginRequired() is false when there are no workers with that status`() {
        assertThat(createState(
            up = listOf(createWorker(Failed())),
            down = listOf(createWorker(Succeeded)),
        ).isSyncFailedBecauseReloginRequired()).isFalse()
    }

    @Test
    fun `isSyncFailedBecauseReloginRequired() is true when there are workers with that status`() {
        assertThat(createState(
            up = listOf(createWorker(Failed(failedBecauseReloginRequired = true))),
            down = listOf(createWorker(Succeeded), createWorker(Enqueued)),
        ).isSyncFailedBecauseReloginRequired()).isTrue()
    }

    @Test
    fun `isSyncFailedBecauseTooManyRequests() is false when there are no workers with that status`() {
        assertThat(createState(
            up = listOf(createWorker(Failed())),
            down = listOf(createWorker(Succeeded)),
        ).isSyncFailedBecauseTooManyRequests()).isFalse()
    }

    @Test
    fun `isSyncFailedBecauseTooManyRequests() is true when there are workers with that status`() {
        assertThat(createState(
            up = listOf(createWorker(Failed(failedBecauseTooManyRequest = true))),
            down = listOf(createWorker(Succeeded), createWorker(Enqueued)),
        ).isSyncFailedBecauseTooManyRequests()).isTrue()
    }

    @Test
    fun `isSyncFailedBecauseCloudIntegration() is false when there are no workers with that status`() {
        assertThat(createState(
            up = listOf(createWorker(Failed())),
            down = listOf(createWorker(Succeeded)),
        ).isSyncFailedBecauseCloudIntegration()).isFalse()
    }

    @Test
    fun `isSyncFailedBecauseBackendMaintenance() is true when there are workers with that status`() {
        assertThat(createState(
            up = listOf(createWorker(Failed(failedBecauseBackendMaintenance = true))),
            down = listOf(createWorker(Succeeded), createWorker(Enqueued)),
        ).isSyncFailedBecauseBackendMaintenance()).isTrue()
    }

    @Test
    fun `isSyncFailedBecauseBackendMaintenance() is false when there are no workers with that status`() {
        assertThat(createState(
            up = listOf(createWorker(Failed())),
            down = listOf(createWorker(Succeeded)),
        ).isSyncFailedBecauseBackendMaintenance()).isFalse()
    }

    @Test
    fun `isSyncFailedBecauseCloudIntegration() is true when there are workers with that status`() {
        assertThat(createState(
            up = listOf(createWorker(Failed(failedBecauseCloudIntegration = true))),
            down = listOf(createWorker(Succeeded), createWorker(Enqueued)),
        ).isSyncFailedBecauseCloudIntegration()).isTrue()
    }

    @Test
    fun `isSyncFailed() is false when there are no Failed, Blocked or Cancelled workers`() {
        assertThat(createState(
            up = listOf(createWorker(Enqueued), createWorker(Running)),
            down = listOf(createWorker(Succeeded)),
        ).isSyncFailed()).isFalse()
    }

    @Test
    fun `isSyncFailed() is true when there are no Failed workers`() {
        assertThat(createState(
            up = listOf(createWorker(Enqueued), createWorker(Running)),
            down = listOf(createWorker(Succeeded), createWorker(Failed())),
        ).isSyncFailed()).isTrue()
    }

    @Test
    fun `isSyncFailed() is true when there are no Blocked workers`() {
        assertThat(createState(
            up = listOf(createWorker(Enqueued), createWorker(Running)),
            down = listOf(createWorker(Succeeded), createWorker(Blocked)),
        ).isSyncFailed()).isTrue()
    }

    @Test
    fun `isSyncFailed() is true when there are no Cancelled workers`() {
        assertThat(createState(
            up = listOf(createWorker(Enqueued), createWorker(Running)),
            down = listOf(createWorker(Succeeded), createWorker(Cancelled)),
        ).isSyncFailed()).isTrue()
    }

    @Test
    fun `getEstimatedBackendMaintenanceOutage() returns outage value when there is a worker with that status`() {
        val outage: Long = 666
        assertThat(createState(
            up = listOf(
                createWorker(Enqueued),
                createWorker(Running),
                createWorker(Failed(failedBecauseBackendMaintenance = true, estimatedOutage = outage)))
            ,
            down = listOf(createWorker(Succeeded), createWorker(Cancelled)),
        ).getEstimatedBackendMaintenanceOutage()).isEqualTo(outage)
    }

    @Test
    fun `getEstimatedBackendMaintenanceOutage() returns null when there is no worker with that status`() {
        assertThat(createState(
            up = listOf(createWorker(Enqueued), createWorker(Running)),
            down = listOf(createWorker(Succeeded), createWorker(Cancelled)),
        ).getEstimatedBackendMaintenanceOutage()).isNull()
    }

    private fun createState(
        up: List<SyncWorkerInfo>,
        down: List<SyncWorkerInfo>,
    ) = EventSyncState("id", 0, 0, up, down)

    private fun createWorker(state: EventSyncWorkerState) =
        SyncWorkerInfo(type = EventSyncWorkerType.DOWNLOADER, state = state)

}
