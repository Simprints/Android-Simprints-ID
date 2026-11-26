package com.simprints.feature.dashboard.main.dailyactivity

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.tools.time.TimeHelper
import com.simprints.feature.dashboard.main.dailyactivity.DailyActivityUiState.DailyActivityUi
import com.simprints.feature.dashboard.tools.ViewModelScopeRule
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DailyActivityViewModelTest {
    companion object {
        private const val DATE = "2022-11-15"
        private const val OTHER_DATE = "2022-11-16"
        private const val ENROLMENTS_COUNT = 2
        private const val IDENTIFICATIONS_COUNT = 5
        private const val VERIFICATIONS_COUNT = 6

        private fun recentUserActivity(
            enrolments: Int,
            identifications: Int,
            verifications: Int,
        ) = RecentUserActivity(
            "",
            "",
            "".asTokenizableEncrypted(),
            enrolments,
            identifications,
            verifications,
            0,
        )

        private fun ui(
            enrolments: Int,
            identifications: Int,
            verifications: Int,
        ) = DailyActivityUi(enrolments, identifications, verifications, DATE)
    }

    @get:Rule(order = 0)
    val testCoroutineRule = TestCoroutineRule()

    // Inside TestCoroutineRule: scopes are cancelled while the test Main dispatcher is still set
    @get:Rule(order = 1)
    val viewModels = ViewModelScopeRule()

    private val timeHelper = mockk<TimeHelper> {
        every { getCurrentDateAsString() } returns DATE
    }
    private val recentUserActivityManager = mockk<RecentUserActivityManager> {
        coEvery { getRecentUserActivity() } returns recentUserActivity(ENROLMENTS_COUNT, IDENTIFICATIONS_COUNT, VERIFICATIONS_COUNT)
    }

    private lateinit var viewModel: DailyActivityViewModel

    @Before
    fun setUp() {
        // Created after TestCoroutineRule has set the test Main dispatcher: the base class starts its action loop in init,
        // and the init block sends the first Load.
        viewModel = createViewModel()
    }

    @Test
    fun `init loads today's counts from the recent user activity`() = runTest {
        val activity = viewModel.uiState.value.activity

        assertThat(activity?.enrolments).isEqualTo(ENROLMENTS_COUNT)
        assertThat(activity?.identifications).isEqualTo(IDENTIFICATIONS_COUNT)
        assertThat(activity?.verifications).isEqualTo(VERIFICATIONS_COUNT)
        coVerify(exactly = 1) { recentUserActivityManager.getRecentUserActivity() }
    }

    @Test
    fun `init takes the current date from the time helper`() = runTest {
        assertThat(viewModel.uiState.value.activity?.currentDate).isEqualTo(DATE)
    }

    @Test
    fun `a load after the counts changed reflects the new counts`() = runTest {
        coEvery { recentUserActivityManager.getRecentUserActivity() } returns recentUserActivity(7, 8, 9)

        viewModel.onAction(DailyActivityUiAction.Load)

        assertThat(viewModel.uiState.value).isEqualTo(
            DailyActivityUiState(activity = DailyActivityUi(7, 8, 9, DATE)),
        )
        coVerify(exactly = 2) { recentUserActivityManager.getRecentUserActivity() }
    }

    @Test
    fun `a later load captures the current date again`() = runTest {
        every { timeHelper.getCurrentDateAsString() } returns OTHER_DATE

        viewModel.onAction(DailyActivityUiAction.Load)

        assertThat(viewModel.uiState.value.activity?.currentDate).isEqualTo(OTHER_DATE)
    }

    @Test
    fun `state stays empty until the first load finishes and loads run one after the other`() = runTest {
        val firstRead = CompletableDeferred<RecentUserActivity>()
        val secondRead = CompletableDeferred<RecentUserActivity>()
        val reads = ArrayDeque(listOf(firstRead, secondRead))
        coEvery { recentUserActivityManager.getRecentUserActivity() } coAnswers { reads.removeFirst().await() }

        val viewModel = createViewModel() // init sends the first Load, which suspends on firstRead
        viewModel.onAction(DailyActivityUiAction.Load) // the Fragment's onResume

        // The second load has not started while the first one is suspended.
        assertThat(reads).containsExactly(secondRead)
        assertThat(viewModel.uiState.value).isEqualTo(DailyActivityUiState())

        firstRead.complete(recentUserActivity(1, 0, 0))

        assertThat(reads).isEmpty()
        assertThat(viewModel.uiState.value.activity).isEqualTo(ui(1, 0, 0))

        secondRead.complete(recentUserActivity(0, 2, 3))

        assertThat(viewModel.uiState.value.activity).isEqualTo(ui(0, 2, 3))
    }

    private fun createViewModel() = viewModels.track(
        DailyActivityViewModel(
            recentUserActivityManager = recentUserActivityManager,
            timeHelper = timeHelper,
        ),
    )
}
