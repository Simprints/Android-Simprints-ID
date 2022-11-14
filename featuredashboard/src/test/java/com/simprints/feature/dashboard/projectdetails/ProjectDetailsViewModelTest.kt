package com.simprints.feature.dashboard.projectdetails

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.login.LoginManager
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
        private val project = Project(PROJECT_ID, PROJECT_NAME, "description", "creator", "bucket")
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val loginManager = mockk<LoginManager> {
        every { getSignedInProjectIdOrEmpty() } returns PROJECT_ID
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
            loginManager,
            recentUserActivityManager,
            testCoroutineRule.testCoroutineDispatcher
        )

        val expectedState = DashboardProjectState(PROJECT_NAME, LAST_USER, LAST_SCANNER)
        assertThat(viewModel.projectCardStateLiveData.value).isEqualTo(expectedState)
    }

}
