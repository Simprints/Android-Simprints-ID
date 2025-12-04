package com.simprints.feature.clientapi.usecases

import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.events.EventRepository
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DeleteSessionEventsIfNeededUseCaseTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var configManager: ConfigManager

    @MockK
    private lateinit var eventRepository: EventRepository

    private lateinit var deleteUseCase: DeleteSessionEventsIfNeededUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        deleteUseCase = DeleteSessionEventsIfNeededUseCase(
            configManager,
            eventRepository,
            CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
        )
    }

    @Test
    fun `deletes session events if project missing`() = runTest {
        coEvery { configManager.getProject()?.state } returns null
        coEvery {
            configManager
                .getProjectConfiguration()
                .synchronization.up.simprints.kind
        } returns UpSynchronizationConfiguration.UpSynchronizationKind.ALL

        deleteUseCase("sessionId")

        coVerify { eventRepository.deleteEventScope("sessionId") }
    }

    @Test
    fun `deletes session events if project paused`() = runTest {
        coEvery { configManager.getProject()?.state } returns ProjectState.PROJECT_PAUSED
        coEvery {
            configManager
                .getProjectConfiguration()
                .synchronization.up.simprints.kind
        } returns UpSynchronizationConfiguration.UpSynchronizationKind.ALL

        deleteUseCase("sessionId")

        coVerify { eventRepository.deleteEventScope("sessionId") }
    }

    @Test
    fun `deletes session events if project ending`() = runTest {
        coEvery { configManager.getProject()?.state } returns ProjectState.PROJECT_ENDING
        coEvery {
            configManager
                .getProjectConfiguration()
                .synchronization.up.simprints.kind
        } returns UpSynchronizationConfiguration.UpSynchronizationKind.ALL

        deleteUseCase("sessionId")

        coVerify { eventRepository.deleteEventScope("sessionId") }
    }

    @Test
    fun `deletes session events if project ended`() = runTest {
        coEvery { configManager.getProject()?.state } returns ProjectState.PROJECT_ENDED
        coEvery {
            configManager
                .getProjectConfiguration()
                .synchronization.up.simprints.kind
        } returns UpSynchronizationConfiguration.UpSynchronizationKind.ALL

        deleteUseCase("sessionId")

        coVerify { eventRepository.deleteEventScope("sessionId") }
    }

    @Test
    fun `deletes session events if data sync disabled in running project`() = runTest {
        coEvery { configManager.getProject()?.state } returns ProjectState.RUNNING
        coEvery {
            configManager
                .getProjectConfiguration()
                .synchronization.up.simprints.kind
        } returns UpSynchronizationConfiguration.UpSynchronizationKind.NONE

        deleteUseCase("sessionId")

        coVerify { eventRepository.deleteEventScope("sessionId") }
    }

    @Test
    fun `does not delete session events if data sync enabled in running project`() = runTest {
        coEvery { configManager.getProject()?.state } returns ProjectState.RUNNING
        coEvery {
            configManager
                .getProjectConfiguration()
                .synchronization.up.simprints.kind
        } returns UpSynchronizationConfiguration.UpSynchronizationKind.ALL

        deleteUseCase("sessionId")

        coVerify(exactly = 0) { eventRepository.deleteEventScope("sessionId") }
    }
}
