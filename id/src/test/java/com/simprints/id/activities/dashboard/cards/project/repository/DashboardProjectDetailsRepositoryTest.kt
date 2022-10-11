package com.simprints.id.activities.dashboard.cards.project.repository

import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.login.LoginManager
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DashboardProjectDetailsRepositoryTest {

    private val mockLoginManager = mockk<LoginManager>()
    private val recentUserActivityManager = mockk<RecentUserActivityManager>()
    private val mockProject = mockk<Project>()

    private lateinit var repository: DashboardProjectDetailsRepository

    @Before
    fun setUp() {
        every { mockLoginManager.getSignedInProjectIdOrEmpty() } returns PROJECT_ID
        coEvery { recentUserActivityManager.getRecentUserActivity() } returns RecentUserActivity(
            "",
            "SP1234",
            "Some User",
            0,
            0,
            0,
            0
        )
        every { mockProject.name } returns "Mock Project"
    }

    @Test
    fun shouldGetProjectNameFromCache() = runTest(UnconfinedTestDispatcher()) {
        val mockConfigManager = mockk<ConfigManager>().also {
            coEvery { it.getProject(PROJECT_ID) } returns mockProject
        }

        repository = DashboardProjectDetailsRepository(
            mockConfigManager,
            mockLoginManager,
            recentUserActivityManager
        )

        repository.getProjectDetails()

        coVerify(exactly = 1) { mockConfigManager.getProject(PROJECT_ID) }
    }

    private companion object {
        const val PROJECT_ID = "project_id"
    }

}
