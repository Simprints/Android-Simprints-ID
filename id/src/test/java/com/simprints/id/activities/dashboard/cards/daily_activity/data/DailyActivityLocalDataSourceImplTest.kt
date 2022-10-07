package com.simprints.id.activities.dashboard.cards.daily_activity.data

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import io.mockk.coEvery
import io.mockk.mockk
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

    private companion object {
        const val ENROLMENTS_COUNT = 50
        const val IDENTIFICATIONS_COUNT = 40
        const val VERIFICATIONS_COUNT = 30
        const val LAST_ACTIVITY_TIME = 12345L
    }
}
