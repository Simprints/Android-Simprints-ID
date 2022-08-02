package com.simprints.id.activities.dashboard.cards.project.repository

import com.simprints.core.sharedpreferences.PreferencesManager
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.db.project.domain.Project
import com.simprints.infra.login.LoginManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class DashboardProjectDetailsRepositoryTest {

    private val mockLoginManager = mockk<LoginManager>()
    private val mockPreferencesManager = mockk<PreferencesManager>()
    private val mockProject = mockk<Project>()

    private lateinit var repository: DashboardProjectDetailsRepository

    @Before
    fun setUp() {
        every { mockLoginManager.getSignedInProjectIdOrEmpty() } returns PROJECT_ID
        every { mockPreferencesManager.lastUserUsed } returns "Some User"
        every { mockPreferencesManager.lastScannerUsed } returns "SP1234"
        every { mockProject.name } returns "Mock Project"
    }

    @Test
    fun shouldGetProjectNameFromCache() = runTest(UnconfinedTestDispatcher()) {
        val mockProjectRepository = mockk<ProjectRepository>().also {
            coEvery { it.loadFromCache(PROJECT_ID) } returns mockProject
        }

        repository = DashboardProjectDetailsRepository(
            mockProjectRepository,
            mockLoginManager,
            mockPreferencesManager
        )

        repository.getProjectDetails()

        coVerify(exactly = 1) { mockProjectRepository.loadFromCache(PROJECT_ID) }
        coVerify(exactly = 0) { mockProjectRepository.loadFromRemoteAndRefreshCache(PROJECT_ID) }
    }

    @Test
    fun whenFetchingFromCacheFails_shouldGetProjectNameFromRemote() = runTest(
        UnconfinedTestDispatcher()
    ) {
        val mockProjectRepository = mockk<ProjectRepository>().also {
            coEvery { it.loadFromCache(PROJECT_ID) } returns null
            coEvery { it.loadFromRemoteAndRefreshCache(PROJECT_ID) } returns mockProject
        }

        repository = DashboardProjectDetailsRepository(
            mockProjectRepository,
            mockLoginManager,
            mockPreferencesManager
        )

        repository.getProjectDetails()

        coVerify(exactly = 1) { mockProjectRepository.loadFromCache(PROJECT_ID) }
        coVerify(exactly = 1) { mockProjectRepository.loadFromRemoteAndRefreshCache(PROJECT_ID) }
    }

    private companion object {
        const val PROJECT_ID = "project_id"
    }

}
