package com.simprints.feature.logincheck.usecases

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.Frequency
import com.simprints.infra.sync.ScheduleCommand
import com.simprints.infra.sync.SyncOrchestrator
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StartBackgroundSyncUseCaseTest {
    @MockK
    lateinit var syncOrchestrator: SyncOrchestrator

    @MockK
    lateinit var configRepository: ConfigRepository

    private lateinit var useCase: StartBackgroundSyncUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        every { syncOrchestrator.execute(any<ScheduleCommand>()) } returns Job().apply { complete() }

        useCase = StartBackgroundSyncUseCase(
            configRepository,
            syncOrchestrator,
        )
    }

    @Test
    fun `Schedules all syncs when called`() = runTest {
        coEvery {
            configRepository
                .getProjectConfiguration()
                .synchronization.down.simprints
                ?.frequency
        } returns
            Frequency.PERIODICALLY

        assertUseCaseAwaitsSync(ScheduleCommand.Everything.reschedule(withDelay = true))
    }

    @Test
    fun `Starts event sync on start if required`() = runTest {
        coEvery {
            configRepository
                .getProjectConfiguration()
                .synchronization.down.simprints
                ?.frequency
        } returns
            Frequency.PERIODICALLY_AND_ON_SESSION_START

        assertUseCaseAwaitsSync(ScheduleCommand.Everything.reschedule(withDelay = false))
    }

    @Test
    fun `Does not start event sync on start if not required`() = runTest {
        coEvery {
            configRepository
                .getProjectConfiguration()
                .synchronization.down.simprints
                ?.frequency
        } returns
            Frequency.PERIODICALLY

        assertUseCaseAwaitsSync(ScheduleCommand.Everything.reschedule(withDelay = true))
    }

    @Test
    fun `Does not start event sync on start if not Simprints sync`() = runTest {
        coEvery {
            configRepository
            .getProjectConfiguration()
            .synchronization.down.simprints
        } returns null

        assertUseCaseAwaitsSync(ScheduleCommand.Everything.reschedule(withDelay = true))
    }

    private suspend fun TestScope.assertUseCaseAwaitsSync(expectedCommand: ScheduleCommand) {
        val syncCommandJob = Job()
        every { syncOrchestrator.execute(any<ScheduleCommand>()) } returns syncCommandJob

        val useCaseJob = async { useCase.invoke() }

        runCurrent()
        assertThat(useCaseJob.isCompleted).isFalse()

        syncCommandJob.complete()
        runCurrent()
        useCaseJob.await()

        verify { syncOrchestrator.execute(expectedCommand) }
    }
}
