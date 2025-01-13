package com.simprints.infra.eventsync.sync.master

import androidx.work.ListenableWorker
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.events.EventRepository
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import com.simprints.infra.eventsync.sync.master.EventEndSyncReporterWorker.Companion.EVENT_DOWN_SYNC_SCOPE_TO_CLOSE
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

internal class EventEndSyncReporterWorkerTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var syncCache: EventSyncCache

    @MockK
    lateinit var eventRepository: EventRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        every { timeHelper.now() } returns Timestamp(1)
    }

    @Test
    fun `doWork should fail when the sync id is empty`() = runTest {
        val endSyncReportWorker = createWorker("", null, null)
        val result = endSyncReportWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
        coVerify(exactly = 0) { syncCache.storeLastSuccessfulSyncTime(any()) }
    }

    @Test
    fun `doWork should fail when the sync id is null`() = runTest {
        val endSyncReportWorker = createWorker(null, null, null)
        val result = endSyncReportWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
        coVerify(exactly = 0) { syncCache.storeLastSuccessfulSyncTime(any()) }
    }

    @Test
    fun `doWork should succeed otherwise and save the last success time`() = runTest {
        val endSyncReportWorker = createWorker("sync id", null, null)
        val result = endSyncReportWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        coVerify(exactly = 1) { syncCache.storeLastSuccessfulSyncTime(any()) }
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

    private fun createWorker(
        syncId: String?,
        downScopeId: String?,
        upScopeId: String?,
    ) = EventEndSyncReporterWorker(
        mockk(relaxed = true),
        mockk(relaxed = true) {
            every { inputData } returns workDataOf(
                SYNC_ID_TO_MARK_AS_COMPLETED to syncId,
                EVENT_DOWN_SYNC_SCOPE_TO_CLOSE to downScopeId,
                EVENT_DOWN_SYNC_SCOPE_TO_CLOSE to upScopeId,
            )
        },
        syncCache,
        eventRepository,
        timeHelper,
        testCoroutineRule.testCoroutineDispatcher,
    )
}
