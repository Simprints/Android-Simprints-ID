package com.simprints.infra.eventsync.sync.master

import androidx.work.ListenableWorker
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.eventsync.sync.master.EventStartSyncReporterWorker.Companion.SYNC_ID_STARTED
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class EventStartSyncReporterWorkerTest {
    companion object {
        private val INPUT_DATA = workDataOf(SYNC_ID_STARTED to "SYNC_ID")
    }

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val startSyncReportWorker = EventStartSyncReporterWorker(
        mockk(relaxed = true),
        mockk(relaxed = true) {
            every { inputData } returns INPUT_DATA
        },
        testCoroutineRule.testCoroutineDispatcher,
    )

    @Test
    fun worker_shouldSucceed() = runTest {
        val result = startSyncReportWorker.doWork()
        assertThat(result).isEqualTo(ListenableWorker.Result.success(INPUT_DATA))
    }
}
