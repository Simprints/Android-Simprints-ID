package com.simprints.infra.eventsync.status.models

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.Timestamp
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
    fun `hasSyncHistory() is false when there are no workers`() {
        assertThat(
            createState(
                up = emptyList(),
                down = emptyList(),
                reporters = emptyList(),
            ).hasSyncHistory(),
        ).isFalse()
    }

    @Test
    fun `hasSyncHistory() is true when there are workers`() {
        assertThat(
            createState(
                up = listOf(createWorker(Succeeded)),
                down = emptyList(),
                reporters = emptyList(),
            ).hasSyncHistory(),
        ).isTrue()
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
    fun `isSyncCompleted() is false when there are no workers`() {
        assertThat(
            createState(
                up = emptyList(),
                down = emptyList(),
                reporters = emptyList(),
            ).isSyncCompleted(),
        ).isFalse()
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
    fun `isSyncReporterCompleted() is false when there are no reporter workers`() {
        assertThat(
            createState(
                up = emptyList(),
                down = emptyList(),
                reporters = emptyList(),
            ).isSyncReporterCompleted(),
        ).isFalse()
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

    @Test
    fun `isUninitialized() is true when all values are blank or empty`() {
        assertThat(
            createUninitializedState()
                .isUninitialized(),
        ).isTrue()
    }

    @Test
    fun `isUninitialized() is false when syncId is not blank`() {
        assertThat(
            createUninitializedState()
                .copy(syncId = "id")
                .isUninitialized(),
        ).isFalse()
    }

    @Test
    fun `isUninitialized() is false when progress is set`() {
        assertThat(
            createUninitializedState()
                .copy(progress = 0)
                .isUninitialized(),
        ).isFalse()
    }

    @Test
    fun `isUninitialized() is false when total is set`() {
        assertThat(
            createUninitializedState()
                .copy(total = 0)
                .isUninitialized(),
        ).isFalse()
    }

    @Test
    fun `isUninitialized() is false when up workers are present`() {
        assertThat(
            createUninitializedState()
                .copy(upSyncWorkersInfo = listOf(createWorker(Succeeded)))
                .isUninitialized(),
        ).isFalse()
    }

    @Test
    fun `isUninitialized() is false when down workers are present`() {
        assertThat(
            createUninitializedState()
                .copy(downSyncWorkersInfo = listOf(createWorker(Succeeded)))
                .isUninitialized(),
        ).isFalse()
    }

    @Test
    fun `isUninitialized() is false when reporter workers are present`() {
        assertThat(
            createUninitializedState()
                .copy(reporterStates = listOf(createWorker(Succeeded)))
                .isUninitialized(),
        ).isFalse()
    }

    @Test
    fun `isUninitialized() is false when lastSyncTime is set`() {
        assertThat(
            createUninitializedState()
                .copy(lastSyncTime = Timestamp(ms = 0L))
                .isUninitialized(),
        ).isFalse()
    }

    private fun createState(
        up: List<SyncWorkerInfo>,
        down: List<SyncWorkerInfo>,
        reporters: List<SyncWorkerInfo>,
    ) = EventSyncState("id", 0, 0, up, down, reporters, null)

    private fun createUninitializedState() = EventSyncState(
        syncId = "",
        progress = null,
        total = null,
        upSyncWorkersInfo = emptyList(),
        downSyncWorkersInfo = emptyList(),
        reporterStates = emptyList(),
        lastSyncTime = null,
    )

    private fun createWorker(state: EventSyncWorkerState) = SyncWorkerInfo(type = EventSyncWorkerType.DOWNLOADER, state = state)
}
