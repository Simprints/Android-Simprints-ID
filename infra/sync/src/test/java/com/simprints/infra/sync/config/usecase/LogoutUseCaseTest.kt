package com.simprints.infra.sync.config.usecase

import com.simprints.infra.authlogic.AuthManager
import com.simprints.infra.sync.ScheduleCommand
import com.simprints.infra.sync.SyncOrchestrator
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class LogoutUseCaseTest {
    @MockK
    private lateinit var syncOrchestrator: SyncOrchestrator

    @MockK
    private lateinit var authManager: AuthManager

    private lateinit var useCase: LogoutUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        every { syncOrchestrator.executeSchedulingCommand(any()) } returns Job().apply { complete() }

        useCase = LogoutUseCase(
            syncOrchestrator = syncOrchestrator,
            authManager = authManager,
        )
    }

    @Test
    fun `Fully logs out when called`() = runTest {
        useCase.invoke()

        verify { syncOrchestrator.executeSchedulingCommand(ScheduleCommand.Everything.unschedule()) }
        coVerify {
            syncOrchestrator.deleteEventSyncInfo()
            authManager.signOut()
        }
    }
}
