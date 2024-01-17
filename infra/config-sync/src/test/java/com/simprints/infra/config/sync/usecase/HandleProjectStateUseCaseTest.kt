package com.simprints.infra.config.sync.usecase

import com.simprints.infra.authlogic.AuthManager
import com.simprints.infra.authlogic.worker.SecurityStateScheduler
import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.sync.ProjectConfigurationScheduler
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.images.ImageUpSyncScheduler
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class HandleProjectStateUseCaseTest {


    @MockK
    private lateinit var configScheduler: ProjectConfigurationScheduler

    @MockK
    private lateinit var imageUpSyncScheduler: ImageUpSyncScheduler

    @MockK
    private lateinit var eventSyncManager: EventSyncManager

    @MockK
    private lateinit var securityStateScheduler: SecurityStateScheduler

    @MockK
    private lateinit var authManager: AuthManager

    private lateinit var useCase: HandleProjectStateUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = HandleProjectStateUseCase(
            configScheduler = configScheduler,
            securityStateScheduler = securityStateScheduler,
            imageUpSyncScheduler = imageUpSyncScheduler,
            eventSyncManager = eventSyncManager,
            authManager = authManager,
        )
    }

    @Test
    fun `Fully logs out when project has ended`() = runTest {
        coEvery { eventSyncManager.countEventsToUpload(PROJECT_ID, null) } returns flowOf(0)

        useCase(PROJECT_ID, ProjectState.PROJECT_ENDED)

        verify {
            securityStateScheduler.cancelSecurityStateCheck()
            imageUpSyncScheduler.cancelImageUpSync()
            configScheduler.cancelScheduledSync()
            eventSyncManager.cancelScheduledSync()
        }
        coVerify {
            authManager.signOut()
            eventSyncManager.deleteSyncInfo()
        }
    }

    @Test
    fun `Logs out when project has ending and no items to upload`() = runTest {
        coEvery { eventSyncManager.countEventsToUpload(PROJECT_ID, null) } returns flowOf(0)

        useCase(PROJECT_ID, ProjectState.PROJECT_ENDING)

        coVerify { authManager.signOut() }
    }

    @Test
    fun `Does not logs out when project has ending and has items to upload`() = runTest {
        coEvery { eventSyncManager.countEventsToUpload(PROJECT_ID, null) } returns flowOf(5)

        useCase(PROJECT_ID, ProjectState.PROJECT_ENDING)

        coVerify(exactly = 0) { authManager.signOut() }
    }

    @Test
    fun `Does not logs out when project is running`() = runTest {
        coEvery { eventSyncManager.countEventsToUpload(PROJECT_ID, null) } returns flowOf(0)

        useCase(PROJECT_ID, ProjectState.RUNNING)

        coVerify(exactly = 0) { authManager.signOut() }
    }

    companion object {

        private const val PROJECT_ID = "project_id"
    }

}
