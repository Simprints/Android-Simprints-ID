package com.simprints.infra.eventsync.status.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.eventsync.status.models.EventSyncState.SyncWorkerInfo
import com.simprints.infra.eventsync.status.models.EventSyncWorkerState.Blocked
import com.simprints.infra.eventsync.status.models.EventSyncWorkerState.Cancelled
import com.simprints.infra.eventsync.status.models.EventSyncWorkerState.Enqueued
import com.simprints.infra.eventsync.status.models.EventSyncWorkerState.Failed
import com.simprints.infra.eventsync.status.models.EventSyncWorkerState.Running
import com.simprints.infra.eventsync.status.models.EventSyncWorkerState.Succeeded
import org.junit.Test

class EventSyncStateTest {
    @Test
    fun `isThereNotSyncHistory() is true when there are no workers`() {
        assertThat(
            createState(
                up = emptyList(),
                down = emptyList(),
                reporters = emptyList(),
            ).isThereNotSyncHistory(),
        ).isTrue()
    }

    @Test
    fun `isThereNotSyncHistory() is false when there are workers`() {
        assertThat(
            createState(
                up = listOf(createWorker(Succeeded)),
                down = emptyList(),
                reporters = emptyList(),
            ).isThereNotSyncHistory(),
        ).isFalse()
    }

    @Test
    fun `isSyncRunning() is false when there are no workers`() {
        assertThat(
            createState(
                up = emptyList(),
                down = emptyList(),
                reporters = emptyList(),
            ).isSyncRunning(),
        ).isFalse()
    }

    @Test
    fun `isSyncRunning() is false when when all workers completed`() {
        assertThat(
            createState(
                up = listOf(createWorker(Succeeded)),
                down = listOf(createWorker(Succeeded)),
                reporters = emptyList(),
            ).isSyncRunning(),
        ).isFalse()
    }

    @Test
    fun `isSyncRunning() is true when there are running workers`() {
        assertThat(
            createState(
                up = listOf(createWorker(Running)),
                down = listOf(createWorker(Succeeded)),
                reporters = emptyList(),
            ).isSyncRunning(),
        ).isTrue()
        assertThat(
            createState(
                up = listOf(createWorker(Succeeded)),
                down = listOf(createWorker(Running)),
                reporters = emptyList(),
            ).isSyncRunning(),
        ).isTrue()
        assertThat(
            createState(
                up = listOf(createWorker(Running)),
                down = listOf(createWorker(Running)),
                reporters = emptyList(),
            ).isSyncRunning(),
        ).isTrue()
    }

    @Test
    fun `isSyncRunning() is true when there are enqueued workers`() {
        assertThat(
            createState(
                up = listOf(createWorker(Enqueued)),
                down = listOf(createWorker(Succeeded)),
                reporters = emptyList(),
            ).isSyncRunning(),
        ).isTrue()
        assertThat(
            createState(
                up = listOf(createWorker(Succeeded)),
                down = listOf(createWorker(Enqueued)),
                reporters = emptyList(),
            ).isSyncRunning(),
        ).isTrue()
        assertThat(
            createState(
                up = listOf(createWorker(Enqueued)),
                down = listOf(createWorker(Enqueued)),
                reporters = emptyList(),
            ).isSyncRunning(),
        ).isTrue()
    }

    @Test
    fun `isSyncCompleted() is true when all workers are completed`() {
        assertThat(
            createState(
                up = listOf(createWorker(Succeeded)),
                down = listOf(createWorker(Succeeded)),
                reporters = emptyList(),
            ).isSyncCompleted(),
        ).isTrue()
    }

    @Test
    fun `isSyncCompleted() is false when there are enqueued workers`() {
        assertThat(
            createState(
                up = listOf(createWorker(Enqueued)),
                down = listOf(createWorker(Succeeded)),
                reporters = emptyList(),
            ).isSyncCompleted(),
        ).isFalse()
    }

    @Test
    fun `isSyncCompleted() is false when there are running workers`() {
        assertThat(
            createState(
                up = listOf(createWorker(Running)),
                down = listOf(createWorker(Succeeded)),
                reporters = emptyList(),
            ).isSyncCompleted(),
        ).isFalse()
    }

    @Test
    fun `isSyncInProgress() is false when there are no running workers`() {
        assertThat(
            createState(
                up = listOf(createWorker(Enqueued)),
                down = listOf(createWorker(Succeeded)),
                reporters = emptyList(),
            ).isSyncInProgress(),
        ).isFalse()
    }

    @Test
    fun `isSyncInProgress() is true when there are running workers`() {
        assertThat(
            createState(
                up = listOf(createWorker(Running)),
                down = listOf(createWorker(Succeeded), createWorker(Enqueued)),
                reporters = emptyList(),
            ).isSyncInProgress(),
        ).isTrue()
    }

    @Test
    fun `isSyncConnecting() is false when there are no enqueued workers`() {
        assertThat(
            createState(
                up = listOf(createWorker(Running)),
                down = listOf(createWorker(Succeeded)),
                reporters = emptyList(),
            ).isSyncConnecting(),
        ).isFalse()
    }

    @Test
    fun `isSyncConnecting() is true when there are enqueued workers`() {
        assertThat(
            createState(
                up = listOf(createWorker(Running)),
                down = listOf(createWorker(Succeeded), createWorker(Enqueued)),
                reporters = emptyList(),
            ).isSyncConnecting(),
        ).isTrue()
    }

    @Test
    fun `isSyncFailedBecauseReloginRequired() is false when there are no workers with that status`() {
        assertThat(
            createState(
                up = listOf(createWorker(Failed())),
                down = listOf(createWorker(Succeeded)),
                reporters = emptyList(),
            ).isSyncFailedBecauseReloginRequired(),
        ).isFalse()
    }

    @Test
    fun `isSyncFailedBecauseReloginRequired() is true when there are workers with that status`() {
        assertThat(
            createState(
                up = listOf(createWorker(Failed(failedBecauseReloginRequired = true))),
                down = listOf(createWorker(Succeeded), createWorker(Enqueued)),
                reporters = emptyList(),
            ).isSyncFailedBecauseReloginRequired(),
        ).isTrue()
    }

    @Test
    fun `isSyncFailedBecauseTooManyRequests() is false when there are no workers with that status`() {
        assertThat(
            createState(
                up = listOf(createWorker(Failed())),
                down = listOf(createWorker(Succeeded)),
                reporters = emptyList(),
            ).isSyncFailedBecauseTooManyRequests(),
        ).isFalse()
    }

    @Test
    fun `isSyncFailedBecauseTooManyRequests() is true when there are workers with that status`() {
        assertThat(
            createState(
                up = listOf(createWorker(Failed(failedBecauseTooManyRequest = true))),
                down = listOf(createWorker(Succeeded), createWorker(Enqueued)),
                reporters = emptyList(),
            ).isSyncFailedBecauseTooManyRequests(),
        ).isTrue()
    }

    @Test
    fun `isSyncFailedBecauseCloudIntegration() is false when there are no workers with that status`() {
        assertThat(
            createState(
                up = listOf(createWorker(Failed())),
                down = listOf(createWorker(Succeeded)),
                reporters = emptyList(),
            ).isSyncFailedBecauseCloudIntegration(),
        ).isFalse()
    }

    @Test
    fun `isSyncFailedBecauseBackendMaintenance() is true when there are workers with that status`() {
        assertThat(
            createState(
                up = listOf(createWorker(Failed(failedBecauseBackendMaintenance = true))),
                down = listOf(createWorker(Succeeded), createWorker(Enqueued)),
                reporters = emptyList(),
            ).isSyncFailedBecauseBackendMaintenance(),
        ).isTrue()
    }

    @Test
    fun `isSyncFailedBecauseBackendMaintenance() is false when there are no workers with that status`() {
        assertThat(
            createState(
                up = listOf(createWorker(Failed())),
                down = listOf(createWorker(Succeeded)),
                reporters = emptyList(),
            ).isSyncFailedBecauseBackendMaintenance(),
        ).isFalse()
    }

    @Test
    fun `isSyncFailedBecauseCloudIntegration() is true when there are workers with that status`() {
        assertThat(
            createState(
                up = listOf(createWorker(Failed(failedBecauseCloudIntegration = true))),
                down = listOf(createWorker(Succeeded), createWorker(Enqueued)),
                reporters = emptyList(),
            ).isSyncFailedBecauseCloudIntegration(),
        ).isTrue()
    }

    @Test
    fun `isSyncFailed() is false when there are no Failed, Blocked or Cancelled workers`() {
        assertThat(
            createState(
                up = listOf(createWorker(Enqueued), createWorker(Running)),
                down = listOf(createWorker(Succeeded)),
                reporters = emptyList(),
            ).isSyncFailed(),
        ).isFalse()
    }

    @Test
    fun `isSyncFailed() is true when there are no Failed workers`() {
        assertThat(
            createState(
                up = listOf(createWorker(Enqueued), createWorker(Running)),
                down = listOf(createWorker(Succeeded), createWorker(Failed())),
                reporters = emptyList(),
            ).isSyncFailed(),
        ).isTrue()
    }

    @Test
    fun `isSyncFailed() is true when there are no Blocked workers`() {
        assertThat(
            createState(
                up = listOf(createWorker(Enqueued), createWorker(Running)),
                down = listOf(createWorker(Succeeded), createWorker(Blocked)),
                reporters = emptyList(),
            ).isSyncFailed(),
        ).isTrue()
    }

    @Test
    fun `isSyncFailed() is true when there are no Cancelled workers`() {
        assertThat(
            createState(
                up = listOf(createWorker(Enqueued), createWorker(Running)),
                down = listOf(createWorker(Succeeded), createWorker(Cancelled)),
                reporters = emptyList(),
            ).isSyncFailed(),
        ).isTrue()
    }

    @Test
    fun `getEstimatedBackendMaintenanceOutage() returns outage value when there is a worker with that status`() {
        val outage: Long = 666
        assertThat(
            createState(
                up = listOf(
                    createWorker(Enqueued),
                    createWorker(Running),
                    createWorker(
                        Failed(
                            failedBecauseBackendMaintenance = true,
                            estimatedOutage = outage,
                        ),
                    ),
                ),
                down = listOf(createWorker(Succeeded), createWorker(Cancelled)),
                reporters = emptyList(),
            ).getEstimatedBackendMaintenanceOutage(),
        ).isEqualTo(outage)
    }

    @Test
    fun `getEstimatedBackendMaintenanceOutage() returns null when there is no worker with that status`() {
        assertThat(
            createState(
                up = listOf(createWorker(Enqueued), createWorker(Running)),
                down = listOf(createWorker(Succeeded), createWorker(Cancelled)),
                reporters = emptyList(),
            ).getEstimatedBackendMaintenanceOutage(),
        ).isNull()
    }

    @Test
    fun `isSyncReporterCompleted() is false when there are enqueued workers`() {
        assertThat(
            createState(
                up = emptyList(),
                down = emptyList(),
                reporters = listOf(createWorker(Enqueued)),
            ).isSyncReporterCompleted(),
        ).isFalse()
    }

    @Test
    fun `isSyncReporterCompleted() is false when there are blocked workers`() {
        assertThat(
            createState(
                up = emptyList(),
                down = emptyList(),
                reporters = listOf(createWorker(Blocked)),
            ).isSyncReporterCompleted(),
        ).isFalse()
    }

    @Test
    fun `isSyncReporterCompleted() is true when there are completed workers`() {
        assertThat(
            createState(
                up = emptyList(),
                down = emptyList(),
                reporters = listOf(createWorker(Succeeded)),
            ).isSyncReporterCompleted(),
        ).isTrue()
    }

    @Test
    fun `isSyncReporterCompleted() is true when all workers failed`() {
        assertThat(
            createState(
                up = emptyList(),
                down = emptyList(),
                reporters = listOf(createWorker(Failed())),
            ).isSyncReporterCompleted(),
        ).isTrue()
    }

    private fun createState(
        up: List<SyncWorkerInfo>,
        down: List<SyncWorkerInfo>,
        reporters: List<SyncWorkerInfo>,
    ) = EventSyncState("id", 0, 0, up, down, reporters)

    private fun createWorker(state: EventSyncWorkerState) = SyncWorkerInfo(type = EventSyncWorkerType.DOWNLOADER, state = state)
}
