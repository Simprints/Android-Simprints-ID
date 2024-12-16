package com.simprints.feature.dashboard.main.dailyactivity

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DailyActivityViewModelTest {
    companion object {
        private const val DATE = "2022-11-15"
        private const val ENROLMENTS_COUNT = 2
        private const val IDENTIFICATIONS_COUNT = 5
        private const val VERIFICATIONS_COUNT = 6
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val timeHelper = mockk<TimeHelper> {
        every { getCurrentDateAsString() } returns DATE
    }
    private val recentUserActivityManager = mockk<RecentUserActivityManager> {
        coEvery { getRecentUserActivity() } returns RecentUserActivity(
            "",
            "",
            "".asTokenizableEncrypted(),
            ENROLMENTS_COUNT,
            IDENTIFICATIONS_COUNT,
            VERIFICATIONS_COUNT,
            0,
        )
    }

    @Test
    fun `should initialize the live data correctly`() = runTest {
        val viewModel = DailyActivityViewModel(
            recentUserActivityManager,
            timeHelper,
        )

        val expectedState = DashboardDailyActivityState(
            ENROLMENTS_COUNT,
            IDENTIFICATIONS_COUNT,
            VERIFICATIONS_COUNT,
        )
        assertThat(viewModel.dailyActivity.value).isEqualTo(expectedState)
    }

    @Test
    fun `getCurrentDateAsString should return the date`() = runTest {
        val viewModel = DailyActivityViewModel(
            recentUserActivityManager,
            timeHelper,
        )

        assertThat(viewModel.getCurrentDateAsString()).isEqualTo(DATE)
    }
}
