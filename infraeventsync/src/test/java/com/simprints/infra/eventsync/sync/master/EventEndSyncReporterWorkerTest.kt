package com.simprints.infra.eventsync.sync.master

import androidx.work.ListenableWorker
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import com.simprints.infra.eventsync.sync.master.EventEndSyncReporterWorker.Companion.SYNC_ID_TO_MARK_AS_COMPLETED
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EventEndSyncReporterWorkerTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var syncCache: EventSyncCache

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
    }

    @Test
    fun `doWork should fail when the sync id is empty`() = runTest {
        val endSyncReportWorker = createWorker("")
        val result = endSyncReportWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
        coVerify(exactly = 0) { syncCache.storeLastSuccessfulSyncTime(any()) }
    }

    @Test
    fun `doWork should fail when the sync id is null`() = runTest {
        val endSyncReportWorker = createWorker(null)
        val result = endSyncReportWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
        coVerify(exactly = 0) { syncCache.storeLastSuccessfulSyncTime(any()) }
    }

    @Test
    fun `doWork should succeed otherwise and save the last success time`() = runTest {
        val endSyncReportWorker = createWorker("sync id")
        val result = endSyncReportWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        coVerify(exactly = 1) { syncCache.storeLastSuccessfulSyncTime(any()) }
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
