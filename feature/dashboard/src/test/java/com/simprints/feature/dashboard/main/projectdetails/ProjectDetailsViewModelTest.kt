package com.simprints.feature.dashboard.main.projectdetails

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ProjectDetailsViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var authStore: AuthStore

    @MockK
    private lateinit var configManager: ConfigManager

    @MockK
    private lateinit var tokenizationProcessor: TokenizationProcessor

    @MockK
    private lateinit var recentUserActivityManager: RecentUserActivityManager

    private lateinit var viewModel: ProjectDetailsViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { authStore.signedInProjectId } returns PROJECT_ID
        coEvery { recentUserActivityManager.getRecentUserActivity() } returns RECENT_USER_ACTIVITY
    }

    @Test
    fun `should initialize the live data correctly`() = runTest {
        coEvery { configManager.getProject(PROJECT_ID) } returns PROJECT
        every {
            tokenizationProcessor.decrypt(
                RECENT_USER_ACTIVITY.lastUserUsed as TokenizableString.Tokenized,
                TokenKeyType.AttendantId,
                PROJECT,
            )
        } returns RECENT_USER_ACTIVITY.lastUserUsed

        viewModel = ProjectDetailsViewModel(
            configManager = configManager,
            authStore = authStore,
            recentUserActivityManager = recentUserActivityManager,
            tokenizationProcessor = tokenizationProcessor,
        )

        val expectedState = DashboardProjectState(PROJECT_NAME, LAST_USER.value, LAST_SCANNER, true)
        assertThat(viewModel.projectCardStateLiveData.value).isEqualTo(expectedState)
    }

    @Test
    fun `Should handle exception by producing correct state`() = runTest {
        coEvery { configManager.getProject(PROJECT_ID) } throws Exception()

        viewModel = ProjectDetailsViewModel(
            configManager = configManager,
            authStore = authStore,
            recentUserActivityManager = recentUserActivityManager,
            tokenizationProcessor = tokenizationProcessor,
        )

        val expectedState = DashboardProjectState(isLoaded = false)
        assertThat(viewModel.projectCardStateLiveData.value).isEqualTo(expectedState)
    }

    companion object {
        private const val PROJECT_ID = "projectID"
        private const val PROJECT_NAME = "name"
        private const val LAST_SCANNER = "scanner"
        private val LAST_USER = "user".asTokenizableEncrypted()
        private val PROJECT = Project(
            PROJECT_ID,
            PROJECT_NAME,
            ProjectState.RUNNING,
            "description",
            "creator",
            "bucket",
            "",
            tokenizationKeys = emptyMap(),
        )
        private val RECENT_USER_ACTIVITY = RecentUserActivity(
            lastScannerVersion = "",
            lastScannerUsed = LAST_SCANNER,
            lastUserUsed = LAST_USER,
            enrolmentsToday = 0,
            identificationsToday = 0,
            verificationsToday = 0,
            lastActivityTime = 0,
        )
    }
}
