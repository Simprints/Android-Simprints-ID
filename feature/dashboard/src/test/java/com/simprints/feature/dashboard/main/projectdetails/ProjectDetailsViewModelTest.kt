package com.simprints.feature.dashboard.main.projectdetails

import com.google.common.truth.Truth.*
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.feature.dashboard.tools.ViewModelScopeRule
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import com.simprints.infra.logging.Simber
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

class ProjectDetailsViewModelTest {
    @get:Rule(order = 0)
    val testCoroutineRule = TestCoroutineRule()

    // Inside TestCoroutineRule: scopes are cancelled while the test Main dispatcher is still set
    @get:Rule(order = 1)
    val viewModels = ViewModelScopeRule()

    @MockK
    private lateinit var configRepository: ConfigRepository

    @MockK
    private lateinit var tokenizationProcessor: TokenizationProcessor

    @MockK
    private lateinit var recentUserActivityManager: RecentUserActivityManager

    private lateinit var viewModel: ProjectDetailsViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(Simber)
        justRun { Simber.e(message = any<String>(), t = any<Throwable>(), tag = any<String>()) }

        coEvery { configRepository.getProject() } returns PROJECT
        coEvery { recentUserActivityManager.getRecentUserActivity() } returns RECENT_USER_ACTIVITY
        every { tokenizationProcessor.decrypt(LAST_USER, TokenKeyType.AttendantId, PROJECT) } returns DECRYPTED_USER

        // Created after TestCoroutineRule has set the test Main dispatcher: the base class starts its action loop in init,
        // and the init block sends the first Load.
        viewModel = createViewModel()
    }

    @After
    fun tearDown() {
        unmockkObject(Simber)
    }

    @Test
    fun `init loads the project card with the decrypted last user`() = runTest {
        assertThat(viewModel.uiState.value).isEqualTo(
            ProjectDetailsUiState(
                isLoaded = true,
                title = PROJECT_NAME,
                lastUser = DECRYPTED_USER.value,
                lastScanner = LAST_SCANNER,
            ),
        )
        verify(exactly = 1) { tokenizationProcessor.decrypt(LAST_USER, TokenKeyType.AttendantId, PROJECT) }
    }

    @Test
    fun `no cached project produces the not loaded state without reading the user activity`() = runTest {
        coEvery { configRepository.getProject() } returns null

        viewModel.onAction(ProjectDetailsUiAction.Load)

        assertThat(viewModel.uiState.value).isEqualTo(ProjectDetailsUiState(isLoaded = false))
        // Only the init load read it.
        coVerify(exactly = 1) { recentUserActivityManager.getRecentUserActivity() }
    }

    @Test
    fun `a load after the user activity changed reflects the new last user and scanner`() = runTest {
        coEvery { recentUserActivityManager.getRecentUserActivity() } returns RECENT_USER_ACTIVITY.copy(
            lastUserUsed = OTHER_USER,
            lastScannerUsed = OTHER_SCANNER,
        )
        every { tokenizationProcessor.decrypt(OTHER_USER, TokenKeyType.AttendantId, PROJECT) } returns OTHER_DECRYPTED_USER

        viewModel.onAction(ProjectDetailsUiAction.Load)

        assertThat(viewModel.uiState.value).isEqualTo(
            ProjectDetailsUiState(
                isLoaded = true,
                title = PROJECT_NAME,
                lastUser = OTHER_DECRYPTED_USER.value,
                lastScanner = OTHER_SCANNER,
            ),
        )
        coVerify(exactly = 2) { recentUserActivityManager.getRecentUserActivity() }
    }

    @Test
    fun `raw last user passes through without decrypting`() = runTest {
        clearMocks(tokenizationProcessor, answers = false)
        coEvery { recentUserActivityManager.getRecentUserActivity() } returns RECENT_USER_ACTIVITY.copy(lastUserUsed = RAW_USER)

        viewModel.onAction(ProjectDetailsUiAction.Load)

        assertThat(viewModel.uiState.value).isEqualTo(
            ProjectDetailsUiState(
                isLoaded = true,
                title = PROJECT_NAME,
                lastUser = RAW_USER.value,
                lastScanner = LAST_SCANNER,
            ),
        )
        verify { tokenizationProcessor wasNot Called }
    }

    @Test
    fun `failing user activity read produces the not loaded state, logs the failure and a later load recovers`() = runTest {
        val failure = IllegalStateException("boom")
        coEvery { recentUserActivityManager.getRecentUserActivity() } throws failure

        viewModel.onAction(ProjectDetailsUiAction.Load)

        assertThat(viewModel.uiState.value).isEqualTo(ProjectDetailsUiState(isLoaded = false))
        verify(exactly = 1) { Simber.e("Failed to load project details on dashboard", failure, any<String>()) }

        coEvery { recentUserActivityManager.getRecentUserActivity() } returns RECENT_USER_ACTIVITY
        viewModel.onAction(ProjectDetailsUiAction.Load)

        assertThat(viewModel.uiState.value).isEqualTo(
            ProjectDetailsUiState(
                isLoaded = true,
                title = PROJECT_NAME,
                lastUser = DECRYPTED_USER.value,
                lastScanner = LAST_SCANNER,
            ),
        )
    }

    @Test
    fun `failing project read produces the not loaded state and logs the failure`() = runTest {
        val failure = IllegalStateException("boom")
        coEvery { configRepository.getProject() } throws failure

        viewModel.onAction(ProjectDetailsUiAction.Load)

        assertThat(viewModel.uiState.value).isEqualTo(ProjectDetailsUiState(isLoaded = false))
        verify(exactly = 1) { Simber.e("Failed to load project details on dashboard", failure, any<String>()) }
    }

    @Test
    fun `cancellation thrown by the project read while the ViewModel is alive produces the not loaded state without a log`() = runTest {
        coEvery { configRepository.getProject() } throws CancellationException("cancelled elsewhere")

        viewModel.onAction(ProjectDetailsUiAction.Load)

        assertThat(viewModel.uiState.value).isEqualTo(ProjectDetailsUiState(isLoaded = false))
        verify(exactly = 0) { Simber.e(message = any<String>(), t = any<Throwable>(), tag = any<String>()) }
    }

    @Test
    fun `a real cancellation during the load leaves the state untouched`() = runTest {
        coEvery { configRepository.getProject() } coAnswers {
            currentCoroutineContext().cancel() // the action loop is cancelled, as when the ViewModel is cleared
            throw CancellationException("cleared")
        }

        val viewModel = createViewModel()

        assertThat(viewModel.uiState.value).isEqualTo(ProjectDetailsUiState())
        verify(exactly = 0) { Simber.e(message = any<String>(), t = any<Throwable>(), tag = any<String>()) }
    }

    @Test
    fun `init and resume loads run one after the other and the last one wins`() = runTest {
        val firstRead = CompletableDeferred<RecentUserActivity>()
        val secondRead = CompletableDeferred<RecentUserActivity>()
        val reads = ArrayDeque(listOf(firstRead, secondRead))
        coEvery { recentUserActivityManager.getRecentUserActivity() } coAnswers { reads.removeFirst().await() }

        val viewModel = createViewModel() // init sends the first Load, which suspends on firstRead
        viewModel.onAction(ProjectDetailsUiAction.Load) // the Fragment's onResume

        // The second load has not started while the first one is suspended.
        assertThat(reads).containsExactly(secondRead)
        assertThat(viewModel.uiState.value).isEqualTo(ProjectDetailsUiState())

        firstRead.complete(RECENT_USER_ACTIVITY.copy(lastUserUsed = RAW_USER, lastScannerUsed = LAST_SCANNER))

        assertThat(reads).isEmpty()
        assertThat(viewModel.uiState.value).isEqualTo(
            ProjectDetailsUiState(isLoaded = true, title = PROJECT_NAME, lastUser = RAW_USER.value, lastScanner = LAST_SCANNER),
        )

        secondRead.complete(RECENT_USER_ACTIVITY.copy(lastUserUsed = OTHER_RAW_USER, lastScannerUsed = OTHER_SCANNER))

        assertThat(viewModel.uiState.value).isEqualTo(
            ProjectDetailsUiState(isLoaded = true, title = PROJECT_NAME, lastUser = OTHER_RAW_USER.value, lastScanner = OTHER_SCANNER),
        )
    }

    private fun createViewModel() = viewModels.track(
        ProjectDetailsViewModel(
            configRepository = configRepository,
            recentUserActivityManager = recentUserActivityManager,
            tokenizationProcessor = tokenizationProcessor,
        ),
    )

    companion object {
        private const val PROJECT_ID = "projectID"
        private const val PROJECT_NAME = "name"
        private const val LAST_SCANNER = "scanner"
        private const val OTHER_SCANNER = "SP2"
        private val LAST_USER = "user".asTokenizableEncrypted()
        private val DECRYPTED_USER = "decrypted-user".asTokenizableRaw()
        private val OTHER_USER = "user-2".asTokenizableEncrypted()
        private val OTHER_DECRYPTED_USER = "decrypted-user-2".asTokenizableRaw()
        private val RAW_USER = "raw-user".asTokenizableRaw()
        private val OTHER_RAW_USER = "raw-user-2".asTokenizableRaw()
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
