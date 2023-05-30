package com.simprints.feature.dashboard.main.projectdetails

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ProjectDetailsViewModelTest {

    companion object {
        private const val PROJECT_ID = "projectID"
        private const val PROJECT_NAME = "name"
        private const val LAST_SCANNER = "scanner"
        private const val LAST_USER = "user"
        private val project = Project(PROJECT_ID, PROJECT_NAME, "description", "creator", "bucket", "")
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val authStore = mockk<com.simprints.infra.authstore.AuthStore> {
        every { signedInProjectId } returns PROJECT_ID
    }
    private val configManager = mockk<ConfigManager> {
        coEvery { getProject(PROJECT_ID) } returns project
    }
    private val recentUserActivityManager = mockk<RecentUserActivityManager> {
        coEvery { getRecentUserActivity() } returns RecentUserActivity(
            "",
            LAST_SCANNER,
            LAST_USER,
            0,
            0,
            0,
            0
        )
    }

    @Test
    fun `should initialize the live data correctly`() = runTest {
        val viewModel = ProjectDetailsViewModel(
            configManager,
            authStore,
            recentUserActivityManager,
        )

        val expectedState = DashboardProjectState(PROJECT_NAME, LAST_USER, LAST_SCANNER, true)
        assertThat(viewModel.projectCardStateLiveData.value).isEqualTo(expectedState)
    }

    @Test
    fun `Should handle exception by producing correct state`() = runTest {
        val configManager = mockk<ConfigManager> {
            coEvery { getProject(PROJECT_ID) } throws Exception()
        }
        val viewModel = ProjectDetailsViewModel(
            configManager,
            authStore,
            recentUserActivityManager,
        )

        val expectedState = DashboardProjectState(isLoaded = false)
        assertThat(viewModel.projectCardStateLiveData.value).isEqualTo(expectedState)
    }

}
