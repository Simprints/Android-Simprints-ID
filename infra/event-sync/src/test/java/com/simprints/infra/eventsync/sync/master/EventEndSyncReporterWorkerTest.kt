package com.simprints.infra.eventsync.sync.master

import android.os.PowerManager
import androidx.work.ListenableWorker
import androidx.work.WorkInfo
import androidx.work.workDataOf
import com.google.common.truth.Truth.*
import com.simprints.core.domain.sync.SyncFailureReason
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.events.EventRepository
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import com.simprints.infra.eventsync.sync.common.OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION
import com.simprints.infra.eventsync.sync.common.OUTPUT_FAILED_UNEXPECTEDLY
import com.simprints.infra.eventsync.sync.common.SyncWorkersInfoProvider
import com.simprints.infra.eventsync.sync.master.EventEndSyncReporterWorker.Companion.EVENT_DOWN_SYNC_SCOPE_TO_CLOSE
import com.simprints.infra.eventsync.sync.master.EventEndSyncReporterWorker.Companion.EVENT_UP_SYNC_SCOPE_TO_CLOSE
import com.simprints.infra.eventsync.sync.master.EventEndSyncReporterWorker.Companion.SYNC_ID_TO_MARK_AS_COMPLETED
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class EventEndSyncReporterWorkerTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var syncCache: EventSyncCache

    @MockK
    lateinit var eventRepository: EventRepository

    @MockK
    lateinit var syncWorkersInfoProvider: SyncWorkersInfoProvider

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        every { timeHelper.now() } returns Timestamp(1)
        every { syncWorkersInfoProvider.getSyncWorkerInfos(any()) } returns flowOf(emptyList())
    }

    @Test
    fun `doWork should fail when the sync id is empty`() = runTest {
        val endSyncReportWorker = createWorker("", null, null)
        val result = endSyncReportWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
        coVerify(exactly = 0) { syncCache.storeLastSyncOutcome(any(), any()) }
    }

    @Test
    fun `doWork should fail when the sync id is null`() = runTest {
        val endSyncReportWorker = createWorker(null, null, null)
        val result = endSyncReportWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
        coVerify(exactly = 0) { syncCache.storeLastSyncOutcome(any(), any()) }
    }

    @Test
    fun `doWork should succeed otherwise and record the attempt`() = runTest {
        val endSyncReportWorker = createWorker("sync id", null, null)
        val result = endSyncReportWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        coVerify(exactly = 1) { syncCache.storeLastSyncOutcome(any(), any()) }
    }

    @Test
    fun `doWork should close down sync scope if id provided`() = runTest {
        val endSyncReportWorker = createWorker("sync id", null, "scopeId")
        val result = endSyncReportWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        coVerify(exactly = 1) { eventRepository.closeEventScope("scopeId", any()) }
    }

    @Test
    fun `doWork should close up sync scope if id provided`() = runTest {
        val endSyncReportWorker = createWorker("sync id", null, "scopeId")
        val result = endSyncReportWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        coVerify(exactly = 1) { eventRepository.closeEventScope("scopeId", any()) }
    }

    @Test
    fun `doWork still records the attempt when workers contain failure flags`() = runTest {
        every { syncWorkersInfoProvider.getSyncWorkerInfos(any()) } returns flowOf(
            listOf(
                mockk(relaxed = true) {
                    every { outputData } returns workDataOf(OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION to true)
                },
            ),
        )

        val endSyncReportWorker = createWorker("sync id", null, null)
        val result = endSyncReportWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        // The one timestamp moves whatever the outcome, so a device whose syncs keep failing
        // cannot be mistaken for one that simply stopped syncing.
        coVerify(exactly = 1) { syncCache.storeLastSyncOutcome(Timestamp(1), SyncFailureReason.CLOUD_INTEGRATION) }
    }

    @Test
    fun `doWork stores a succeeded outcome when no worker failed`() = runTest {
        every { syncWorkersInfoProvider.getSyncWorkerInfos(any()) } returns flowOf(
            listOf(
                mockk(relaxed = true) {
                    every { outputData } returns workDataOf()
                    every { state } returns WorkInfo.State.SUCCEEDED
                },
            ),
        )

        createWorker("sync id", null, null).doWork()

        coVerify(exactly = 1) { syncCache.storeLastSyncOutcome(Timestamp(1), null) }
    }

    @Test
    fun `doWork records an uncategorised worker error as a failure, not a success`() = runTest {
        // Sync workers succeed with the reason in their output, so an error they could not
        // categorise would otherwise be stored as a clean run.
        every { syncWorkersInfoProvider.getSyncWorkerInfos(any()) } returns flowOf(
            listOf(
                mockk(relaxed = true) {
                    every { outputData } returns workDataOf(OUTPUT_FAILED_UNEXPECTEDLY to true)
                    every { state } returns WorkInfo.State.SUCCEEDED
                },
            ),
        )

        createWorker("sync id", null, null).doWork()

        coVerify(exactly = 1) { syncCache.storeLastSyncOutcome(Timestamp(1), SyncFailureReason.UNKNOWN) }
    }

    @Test
    fun `doWork records a worker left in the failed state as a failure`() = runTest {
        every { syncWorkersInfoProvider.getSyncWorkerInfos(any()) } returns flowOf(
            listOf(
                mockk(relaxed = true) {
                    every { outputData } returns workDataOf()
                    every { state } returns WorkInfo.State.FAILED
                },
            ),
        )

        createWorker("sync id", null, null).doWork()

        coVerify(exactly = 1) { syncCache.storeLastSyncOutcome(Timestamp(1), SyncFailureReason.UNKNOWN) }
    }

    @Test
    fun `doWork keeps a categorised reason over the uncategorised fallback`() = runTest {
        every { syncWorkersInfoProvider.getSyncWorkerInfos(any()) } returns flowOf(
            listOf(
                mockk(relaxed = true) {
                    every { outputData } returns workDataOf(OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION to true)
                    every { state } returns WorkInfo.State.FAILED
                },
            ),
        )

        createWorker("sync id", null, null).doWork()

        coVerify(exactly = 1) { syncCache.storeLastSyncOutcome(Timestamp(1), SyncFailureReason.CLOUD_INTEGRATION) }
    }

    private fun createWorker(
        syncId: String?,
        downScopeId: String?,
        upScopeId: String?,
    ) = EventEndSyncReporterWorker(
        mockk(relaxed = true) {
            every { getSystemService<PowerManager>(any()) } returns mockk {
                every { isIgnoringBatteryOptimizations(any()) } returns true
            }
        },
        mockk(relaxed = true) {
            every { inputData } returns workDataOf(
                SYNC_ID_TO_MARK_AS_COMPLETED to syncId,
                EVENT_DOWN_SYNC_SCOPE_TO_CLOSE to downScopeId,
                EVENT_UP_SYNC_SCOPE_TO_CLOSE to upScopeId,
            )
        },
        syncCache,
        eventRepository,
        syncWorkersInfoProvider,
        timeHelper,
        testCoroutineRule.testCoroutineDispatcher,
    )
}
