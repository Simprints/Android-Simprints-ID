package com.simprints.id.activities.dashboard.cards.daily_activity.data

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DailyActivityLocalDataSourceImplTest {

    private val recentUserActivityManager = mockk<RecentUserActivityManager> {
        coEvery { getRecentUserActivity() } returns RecentUserActivity(
            "",
            "",
            "",
            ENROLMENTS_COUNT,
            IDENTIFICATIONS_COUNT,
            VERIFICATIONS_COUNT,
            LAST_ACTIVITY_TIME,
        )
    }

    private val localDataSource = DailyActivityLocalDataSourceImpl(recentUserActivityManager)

    @Test
    fun shouldGetRecentEnrolments() = runTest {
        val enrolments = localDataSource.getEnrolmentsMadeToday()

        assertThat(enrolments).isEqualTo(ENROLMENTS_COUNT)
    }

    @Test
    fun shouldGetRecentIdentifications() = runTest {
        val identifications = localDataSource.getIdentificationsMadeToday()

        assertThat(identifications).isEqualTo(IDENTIFICATIONS_COUNT)
    }

    @Test
    fun shouldGetRecentVerifications() = runTest {
        val verifications = localDataSource.getVerificationsMadeToday()

        assertThat(verifications).isEqualTo(VERIFICATIONS_COUNT)
    }

    @Test
    fun shouldGetLastActivityTime() = runTest {
        val lastActivityTime = localDataSource.getLastActivityTime()

        assertThat(lastActivityTime).isEqualTo(LAST_ACTIVITY_TIME)
    }

    @Test
    fun `computeNewEnrolmentAndGet should compute the correct number of enrolments`() = runTest {
        val updateActivityFn = slot<suspend (RecentUserActivity) -> RecentUserActivity>()
        coEvery { recentUserActivityManager.updateRecentUserActivity(capture(updateActivityFn)) } returns mockk {
            every { enrolmentsToday } returns ENROLMENTS_COUNT
        }

        val enrolmentCount = localDataSource.computeNewEnrolmentAndGet()
        val updatedActivity =
            updateActivityFn.captured(RecentUserActivity("", "", "", 0, 0, 0, 0))
        assertThat(enrolmentCount).isEqualTo(ENROLMENTS_COUNT)
        assertThat(updatedActivity.enrolmentsToday).isEqualTo(1)
    }

    @Test
    fun `computeNewIdentificationAndGet should compute the correct number of enrolments`() =
        runTest {
            val updateActivityFn = slot<suspend (RecentUserActivity) -> RecentUserActivity>()
            coEvery { recentUserActivityManager.updateRecentUserActivity(capture(updateActivityFn)) } returns mockk {
                every { identificationsToday } returns IDENTIFICATIONS_COUNT
            }

            val identificationCount = localDataSource.computeNewIdentificationAndGet()
            val updatedActivity =
                updateActivityFn.captured(RecentUserActivity("", "", "", 0, 0, 0, 0))
            assertThat(identificationCount).isEqualTo(IDENTIFICATIONS_COUNT)
            assertThat(updatedActivity.identificationsToday).isEqualTo(1)
        }

    @Test
    fun `computeNewVerificationAndGet should compute the correct number of enrolments`() =
        runTest {
            val updateActivityFn = slot<suspend (RecentUserActivity) -> RecentUserActivity>()
            coEvery { recentUserActivityManager.updateRecentUserActivity(capture(updateActivityFn)) } returns mockk {
                every { verificationsToday } returns VERIFICATIONS_COUNT
            }

            val identificationCount = localDataSource.computeNewVerificationAndGet()
            val updatedActivity =
                updateActivityFn.captured(RecentUserActivity("", "", "", 0, 0, 0, 0))
            assertThat(identificationCount).isEqualTo(VERIFICATIONS_COUNT)
            assertThat(updatedActivity.verificationsToday).isEqualTo(1)
        }

    @Test
    fun `setLastActivityTime should set the last activity time`() =
        runTest {
            val updateActivityFn = slot<suspend (RecentUserActivity) -> RecentUserActivity>()
            coEvery { recentUserActivityManager.updateRecentUserActivity(capture(updateActivityFn)) } returns mockk {}

            localDataSource.setLastActivityTime(30)
            val updatedActivity =
                updateActivityFn.captured(RecentUserActivity("", "", "", 0, 0, 0, 0))
            assertThat(updatedActivity.lastActivityTime).isEqualTo(30)
        }

    @Test
    fun `clearActivity should call the correct method`() = runTest {
        localDataSource.clearActivity()

        coVerify(exactly = 1) { recentUserActivityManager.clearRecentActivity() }
    }

    private companion object {
        const val ENROLMENTS_COUNT = 50
        const val IDENTIFICATIONS_COUNT = 40
        const val VERIFICATIONS_COUNT = 30
        const val LAST_ACTIVITY_TIME = 12345L
    }
}
