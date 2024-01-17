package com.simprints.infra.config.sync.usecase

import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.eventsync.EventSyncManager
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class HandleProjectStateUseCaseTest {

    @MockK
    private lateinit var eventSyncManager: EventSyncManager

    @MockK
    private lateinit var logoutUseCase: LogoutUseCase

    private lateinit var useCase: HandleProjectStateUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = HandleProjectStateUseCase(
            eventSyncManager = eventSyncManager,
            logoutUseCase = logoutUseCase,
        )
    }

    @Test
    fun `Fully logs out when project has ended`() = runTest {
        coEvery { eventSyncManager.countEventsToUpload(PROJECT_ID, null) } returns flowOf(0)

        useCase(PROJECT_ID, ProjectState.PROJECT_ENDED)

        coVerify { logoutUseCase.invoke() }
    }

    @Test
    fun `Logs out when project has ending and no items to upload`() = runTest {
        coEvery { eventSyncManager.countEventsToUpload(PROJECT_ID, null) } returns flowOf(0)

        useCase(PROJECT_ID, ProjectState.PROJECT_ENDING)

        coVerify { logoutUseCase.invoke() }
    }

    @Test
    fun `Does not logs out when project has ending and has items to upload`() = runTest {
        coEvery { eventSyncManager.countEventsToUpload(PROJECT_ID, null) } returns flowOf(5)

        useCase(PROJECT_ID, ProjectState.PROJECT_ENDING)

        coVerify(exactly = 0) { logoutUseCase.invoke() }
    }

    @Test
    fun `Does not logs out when project is running`() = runTest {
        coEvery { eventSyncManager.countEventsToUpload(PROJECT_ID, null) } returns flowOf(0)

        useCase(PROJECT_ID, ProjectState.RUNNING)

        coVerify(exactly = 0) { logoutUseCase.invoke() }
    }

    companion object {

        private const val PROJECT_ID = "project_id"
    }

}
