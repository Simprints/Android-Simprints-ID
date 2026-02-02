package com.simprints.feature.dashboard.logout.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.simprints.infra.authlogic.AuthManager
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.local.migration.RealmToRoomMigrationFlagsStore
import com.simprints.infra.sync.SyncCommands
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.usecase.SyncUseCase
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
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
    private lateinit var sync: SyncUseCase

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
        every { sync(any()) } returns mockk()

        useCase = LogoutUseCase(
            sync = sync,
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

        verify { sync(SyncCommands.ScheduleOf.Everything.stop()) }
        coVerify {
            syncOrchestrator.deleteEventSyncInfo()
            authManager.signOut()
            flagsStore.clearMigrationFlags()
            enrolmentRecordRepository.closeOpenDbConnection()
        }
    }
}
