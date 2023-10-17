package com.simprints.feature.dashboard.main.projectdetails

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.sync.tokenization.TokenizationProcessor
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
        private val LAST_USER = "user".asTokenizableEncrypted()
        private val PROJECT = Project(
            PROJECT_ID,
            PROJECT_NAME,
            "description",
            "creator",
            "bucket",
            "",
            tokenizationKeys = emptyMap()
        )
        private val RECENT_USER_ACTIVITY = RecentUserActivity(
            lastScannerVersion = "",
            lastScannerUsed = LAST_SCANNER,
            lastUserUsed = LAST_USER,
            enrolmentsToday = 0,
            identificationsToday = 0,
            verificationsToday = 0,
            lastActivityTime = 0
        )
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val authStore = mockk<com.simprints.infra.authstore.AuthStore> {
        every { signedInProjectId } returns PROJECT_ID
    }
    private val configManager = mockk<ConfigManager> {
        coEvery { getProject(PROJECT_ID) } returns PROJECT
    }
    private val tokenizationProcessor = mockk<TokenizationProcessor>()
    private val recentUserActivityManager = mockk<RecentUserActivityManager> {
        coEvery { getRecentUserActivity() } returns RECENT_USER_ACTIVITY
    }

    @Test
    fun `should initialize the live data correctly`() = runTest {
        every {
            tokenizationProcessor.decrypt(
                RECENT_USER_ACTIVITY.lastUserUsed as TokenizableString.Tokenized,
                TokenKeyType.AttendantId,
                PROJECT
            )
        } returns RECENT_USER_ACTIVITY.lastUserUsed
        val viewModel = ProjectDetailsViewModel(
            configManager = configManager,
            authStore = authStore,
            recentUserActivityManager = recentUserActivityManager,
            tokenizationProcessor = tokenizationProcessor
        )

        val expectedState = DashboardProjectState(PROJECT_NAME, LAST_USER.value, LAST_SCANNER, true)
        assertThat(viewModel.projectCardStateLiveData.value).isEqualTo(expectedState)
    }

    @Test
    fun `Should handle exception by producing correct state`() = runTest {
        val configManager = mockk<ConfigManager> {
            coEvery { getProject(PROJECT_ID) } throws Exception()
        }
        val viewModel = ProjectDetailsViewModel(
            configManager = configManager,
            authStore = authStore,
            recentUserActivityManager = recentUserActivityManager,
            tokenizationProcessor = tokenizationProcessor
        )

        val expectedState = DashboardProjectState(isLoaded = false)
        assertThat(viewModel.projectCardStateLiveData.value).isEqualTo(expectedState)
    }

}
