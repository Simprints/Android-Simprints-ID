package com.simprints.infra.sync.config.usecase

import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.sync.OneTime
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.devicestate.DeviceStateDataTracker
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class HandleProjectStateUseCaseTest {
    @MockK
    private lateinit var deviceStateDataTracker: DeviceStateDataTracker

    @MockK
    private lateinit var syncOrchestrator: SyncOrchestrator

    private lateinit var useCase: HandleProjectStateUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = HandleProjectStateUseCase(
            deviceStateDataTracker = deviceStateDataTracker,
            syncOrchestrator = syncOrchestrator,
        )
    }

    @Test
    fun `Fully logs out when project has ended`() = runTest {
        coEvery { deviceStateDataTracker.hasPendingEvents() } returns false

        useCase(ProjectState.PROJECT_ENDED)

        coVerify { syncOrchestrator.execute(eq(OneTime.LogoutCommand(true))) }
    }

    @Test
    fun `Logs out when project has ending and no items to upload`() = runTest {
        coEvery { deviceStateDataTracker.hasPendingEvents() } returns false

        useCase(ProjectState.PROJECT_ENDING)

        coVerify { syncOrchestrator.execute(eq(OneTime.LogoutCommand(true))) }
    }

    @Test
    fun `Does not logs out when project has ending and has items to upload`() = runTest {
        coEvery { deviceStateDataTracker.hasPendingEvents() } returns true

        useCase(ProjectState.PROJECT_ENDING)

        coVerify(exactly = 0) { syncOrchestrator.execute(eq(OneTime.LogoutCommand(true))) }
    }

    @Test
    fun `Does not logs out when project is running`() = runTest {
        coEvery { deviceStateDataTracker.hasPendingEvents() } returns false

        useCase(ProjectState.RUNNING)

        coVerify(exactly = 0) { syncOrchestrator.execute(eq(OneTime.LogoutCommand(true))) }
    }

    @Test
    fun `Re-reads pending events on every invocation rather than reusing an earlier value`() = runTest {
        // The previous implementation read a shareIn(replay = 1) flow, so events arriving after the
        // first read were invisible - and this sign-out deletes them.
        coEvery { deviceStateDataTracker.hasPendingEvents() } returnsMany listOf(false, true)

        useCase(ProjectState.PROJECT_ENDING)
        useCase(ProjectState.PROJECT_ENDING)

        coVerify(exactly = 1) { syncOrchestrator.execute(eq(OneTime.LogoutCommand(true))) }
    }

    @Test
    fun `Does not log out when the pending check fails`() = runTest {
        coEvery { deviceStateDataTracker.hasPendingEvents() } throws RuntimeException()

        assertThrows<RuntimeException> { useCase(ProjectState.PROJECT_ENDING) }

        coVerify(exactly = 0) { syncOrchestrator.execute(eq(OneTime.LogoutCommand(true))) }
    }
}
