package com.simprints.feature.logincheck.usecases

import com.simprints.infra.config.store.models.SynchronizationConfiguration
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.sync.SyncOrchestrator
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
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
        coEvery { configManager.getProjectConfiguration().synchronization.frequency } returns
            SynchronizationConfiguration.Frequency.PERIODICALLY

        useCase.invoke()

        coVerify { syncOrchestrator.scheduleBackgroundWork(any()) }
    }

    @Test
    fun `Starts event sync on start if required`() = runTest {
        coEvery { configManager.getProjectConfiguration().synchronization.frequency } returns
            SynchronizationConfiguration.Frequency.PERIODICALLY_AND_ON_SESSION_START

        useCase.invoke()

        coVerify { syncOrchestrator.scheduleBackgroundWork(eq(false)) }
    }

    @Test
    fun `Does not start event sync on start if not required`() = runTest {
        coEvery { configManager.getProjectConfiguration().synchronization.frequency } returns
            SynchronizationConfiguration.Frequency.PERIODICALLY

        useCase.invoke()

        coVerify { syncOrchestrator.scheduleBackgroundWork(eq(true)) }
    }
}
