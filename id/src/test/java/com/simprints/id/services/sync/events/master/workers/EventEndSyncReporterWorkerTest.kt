package com.simprints.id.services.sync.events.master.workers

import androidx.work.ListenableWorker
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import com.simprints.id.services.sync.events.master.internal.EventSyncCache
import com.simprints.id.services.sync.events.master.workers.EventEndSyncReporterWorker.Companion.SYNC_ID_TO_MARK_AS_COMPLETED
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class EventEndSyncReporterWorkerTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val syncCache = mockk<EventSyncCache>()

    @Test
    fun `doWork should fail when the sync id is empty`() = runTest {
        val endSyncReportWorker = createWorker("")
        val result = endSyncReportWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
        verify(exactly = 0) { syncCache.storeLastSuccessfulSyncTime(any()) }
    }

    @Test
    fun `doWork should fail when the sync id is null`() = runTest {
        val endSyncReportWorker = createWorker(null)
        val result = endSyncReportWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
        verify(exactly = 0) { syncCache.storeLastSuccessfulSyncTime(any()) }
    }

    @Test
    fun `doWork should succeed otherwise and save the last success time`() = runTest {
        val endSyncReportWorker = createWorker("sync id")
        val result = endSyncReportWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        verify(exactly = 1) { syncCache.storeLastSuccessfulSyncTime(any()) }
    }

    private fun createWorker(syncId: String?): EventEndSyncReporterWorker =
        EventEndSyncReporterWorker(
            mockk(relaxed = true),
            mockk(relaxed = true) {
                every { inputData } returns workDataOf(SYNC_ID_TO_MARK_AS_COMPLETED to syncId)
            },
            syncCache,
            testCoroutineRule.testCoroutineDispatcher
        )
}
