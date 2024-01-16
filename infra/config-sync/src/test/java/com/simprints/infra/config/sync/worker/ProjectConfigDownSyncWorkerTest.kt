package com.simprints.infra.config.sync.worker

import androidx.work.ListenableWorker
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ProjectWithConfig
import com.simprints.infra.config.sync.testtools.project
import com.simprints.infra.config.sync.testtools.projectConfiguration
import com.simprints.infra.config.sync.usecase.HandleProjectStateUseCase
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ProjectConfigDownSyncWorkerTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var authStore: AuthStore

    @MockK
    private lateinit var configRepository: ConfigRepository

    @MockK
    private lateinit var handleProjectStateUseCase: HandleProjectStateUseCase

    private lateinit var projectConfigDownSyncWorker: ProjectConfigDownSyncWorker

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        projectConfigDownSyncWorker = ProjectConfigDownSyncWorker(
            context = mockk(),
            params = mockk(relaxed = true),
            authStore = authStore,
            configRepository = configRepository,
            handleProjectState = handleProjectStateUseCase,
            dispatcher = testCoroutineRule.testCoroutineDispatcher,
        )
    }

    @Test
    fun `should fail if the signed in project id is empty`() = runTest {
        every { authStore.signedInProjectId } returns ""

        val result = projectConfigDownSyncWorker.doWork()
        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
    }

    @Test
    fun `should fail if the config service throws an exception`() = runTest {
        every { authStore.signedInProjectId } returns PROJECT_ID
        coEvery { configRepository.refreshProject(PROJECT_ID) } throws Exception()

        val result = projectConfigDownSyncWorker.doWork()
        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
    }

    @Test
    fun `should succeed if the config service doesn't throw an exception`() = runTest {
        every { authStore.signedInProjectId } returns PROJECT_ID
        coEvery { configRepository.refreshProject(PROJECT_ID) } returns ProjectWithConfig(project, projectConfiguration)

        val result = projectConfigDownSyncWorker.doWork()
        assertThat(result).isEqualTo(ListenableWorker.Result.success())

        coVerify { handleProjectStateUseCase.invoke(any(), any()) }
    }

    companion object {

        private const val PROJECT_ID = "projectId"
    }
}
