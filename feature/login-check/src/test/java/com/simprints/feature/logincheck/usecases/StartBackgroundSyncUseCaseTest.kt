package com.simprints.feature.logincheck.usecases

import com.simprints.infra.config.store.models.Frequency
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.sync.SyncOrchestrator
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class StartBackgroundSyncUseCaseTest {
    @MockK
    lateinit var syncOrchestrator: SyncOrchestrator

    @MockK
    lateinit var configManager: ConfigManager

    private lateinit var useCase: StartBackgroundSyncUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = StartBackgroundSyncUseCase(
            syncOrchestrator,
            configManager,
        )
    }

    @Test
    fun `Schedules all syncs when called`() = runTest {
        coEvery {
            configManager
                .getProjectConfiguration()
                .synchronization.down.simprints?.frequency
        } returns
            Frequency.PERIODICALLY

        useCase.invoke()

        coVerify { syncOrchestrator.scheduleBackgroundWork(any()) }
    }

    @Test
    fun `Starts event sync on start if required`() = runTest {
        coEvery {
            configManager
                .getProjectConfiguration()
                .synchronization.down.simprints?.frequency
        } returns
            Frequency.PERIODICALLY_AND_ON_SESSION_START

        useCase.invoke()

        coVerify { syncOrchestrator.scheduleBackgroundWork(eq(false)) }
    }

    @Test
    fun `Does not start event sync on start if not required`() = runTest {
        coEvery {
            configManager
                .getProjectConfiguration()
                .synchronization.down.simprints?.frequency
        } returns
            Frequency.PERIODICALLY

        useCase.invoke()

        coVerify { syncOrchestrator.scheduleBackgroundWork(eq(true)) }
    }

    @Test
    fun `Does not start event sync on start if not Simprints sync`() = runTest {
        coEvery {
            configManager
                .getProjectConfiguration()
                .synchronization.down.simprints
        } returns null

        useCase.invoke()

        coVerify { syncOrchestrator.scheduleBackgroundWork(eq(true)) }
    }
}
