package com.simprints.feature.troubleshooting.debug

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.WorkManager
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.events.EventRepository
import com.simprints.infra.eventsync.ResetDownSyncInfoUseCase
import com.simprints.infra.sync.OneTime
import com.simprints.infra.sync.ScheduleCommand
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DebugViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var syncOrchestrator: SyncOrchestrator

    @MockK
    private lateinit var resetDownSyncInfo: ResetDownSyncInfoUseCase

    @MockK
    private lateinit var authStore: AuthStore

    @MockK
    private lateinit var eventRepository: EventRepository

    @MockK
    private lateinit var enrolmentRecordRepository: EnrolmentRecordRepository

    @MockK
    private lateinit var workManager: WorkManager

    private lateinit var ioDispatcher: TestDispatcher

    private lateinit var viewModel: DebugViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        ioDispatcher = StandardTestDispatcher(testCoroutineRule.testCoroutineDispatcher.scheduler)

        viewModel = DebugViewModel(
            syncOrchestrator = syncOrchestrator,
            resetDownSyncInfo = resetDownSyncInfo,
            authStore = authStore,
            eventRepository = eventRepository,
            enrolmentRecordRepository = enrolmentRecordRepository,
            workManager = workManager,
            ioDispatcher = ioDispatcher,
        )
    }

    @Test
    fun `clean all stops and unschedules event sync then deletes events, down-sync info, records and pruned work in that order`() =
        runTest {
            viewModel.cleanAll()
            advanceUntilIdle()

            coVerifyOrder {
                syncOrchestrator.execute(OneTime.Events.stop())
                syncOrchestrator.execute(ScheduleCommand.Events.unschedule())
                eventRepository.deleteAll()
                resetDownSyncInfo()
                enrolmentRecordRepository.deleteAll()
                workManager.pruneWork()
            }
        }

    @Test
    fun `clear firebase token clears the token once and runs none of the clean all steps`() = runTest {
        viewModel.clearFirebaseToken()
        advanceUntilIdle()

        verify(exactly = 1) { authStore.clearFirebaseToken() }
        verify(exactly = 0) { syncOrchestrator.execute(any<OneTime>()) }
        verify(exactly = 0) { syncOrchestrator.execute(any<ScheduleCommand>()) }
        coVerify(exactly = 0) { eventRepository.deleteAll() }
        coVerify(exactly = 0) { resetDownSyncInfo() }
        coVerify(exactly = 0) { enrolmentRecordRepository.deleteAll() }
        verify(exactly = 0) { workManager.pruneWork() }
    }
}
