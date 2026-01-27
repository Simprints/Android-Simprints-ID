package com.simprints.infra.sync.config.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.authlogic.AuthManager
import com.simprints.infra.sync.ExecutableSyncCommand
import com.simprints.infra.sync.SyncAction
import com.simprints.infra.sync.SyncCommand
import com.simprints.infra.sync.SyncCommands
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.SyncTarget
import com.simprints.infra.sync.usecase.SyncUseCase
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class LogoutUseCaseTest {
    @MockK
    private lateinit var sync: SyncUseCase

    @MockK
    private lateinit var syncOrchestrator: SyncOrchestrator

    @MockK
    private lateinit var authManager: AuthManager

    private lateinit var useCase: LogoutUseCase
    private val syncCommandSlot = slot<SyncCommand>()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        every { sync(capture(syncCommandSlot)) } returns mockk()

        useCase = LogoutUseCase(
            syncOrchestrator = syncOrchestrator,
            sync = sync,
            authManager = authManager,
        )
    }

    @Test
    fun `Fully logs out when called`() = runTest {
        useCase.invoke()

        val command = syncCommandSlot.captured as ExecutableSyncCommand
        assertThat(command.target).isEqualTo(SyncTarget.SCHEDULE_EVERYTHING)
        assertThat(command.action).isEqualTo(SyncAction.STOP)
        assertThat(command).isEqualTo(SyncCommands.Schedule.Everything.stop())

        verify { sync(SyncCommands.Schedule.Everything.stop()) }
        coVerify {
            syncOrchestrator.deleteEventSyncInfo()
            authManager.signOut()
        }
    }
}
