package com.simprints.feature.dashboard.logout.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.simprints.infra.authlogic.AuthManager
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.local.migration.RealmToRoomMigrationFlagsStore
import com.simprints.infra.sync.ScheduleCommand
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LogoutUseCaseTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var syncOrchestrator: SyncOrchestrator

    @MockK
    private lateinit var authManager: AuthManager

    @MockK
    private lateinit var flagsStore: RealmToRoomMigrationFlagsStore

    @MockK lateinit var enrolmentRecordRepository: EnrolmentRecordRepository

    private lateinit var useCase: LogoutUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        every { syncOrchestrator.execute(any<ScheduleCommand>()) } returns Job().apply { complete() }

        useCase = LogoutUseCase(
            syncOrchestrator = syncOrchestrator,
            authManager = authManager,
            flagsStore = flagsStore,
            enrolmentRecordRepository = enrolmentRecordRepository,
            ioDispatcher = testCoroutineRule.testCoroutineDispatcher,
        )
    }

    @Test
    fun `Fully logs out when called`() = runTest {
        useCase.invoke()

        verify { syncOrchestrator.execute(ScheduleCommand.Everything.unschedule()) }
        coVerify {
            syncOrchestrator.deleteEventSyncInfo()
            authManager.signOut()
            flagsStore.clearMigrationFlags()
            enrolmentRecordRepository.closeOpenDbConnection()
        }
    }
}
