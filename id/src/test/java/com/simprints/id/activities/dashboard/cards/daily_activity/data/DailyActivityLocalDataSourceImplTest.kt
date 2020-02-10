package com.simprints.id.activities.dashboard.cards.daily_activity.data

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.prefs.events.RecentEventsPreferencesManager
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

class DailyActivityLocalDataSourceImplTest {

    @MockK lateinit var mockPreferencesManager: RecentEventsPreferencesManager

    private lateinit var localDataSource: DailyActivityLocalDataSourceImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        configureMock()
        localDataSource = DailyActivityLocalDataSourceImpl(mockPreferencesManager)
    }

    @Test
    fun shouldGetRecentEnrolments() {
        val enrolments = localDataSource.getEnrolmentsMadeToday()

        assertThat(enrolments).isEqualTo(ENROLMENTS_COUNT)
    }

    @Test
    fun shouldGetRecentIdentifications() {
        val identifications = localDataSource.getIdentificationsMadeToday()

        assertThat(identifications).isEqualTo(IDENTIFICATIONS_COUNT)
    }

    @Test
    fun shouldGetRecentVerifications() {
        val verifications = localDataSource.getVerificationsMadeToday()

        assertThat(verifications).isEqualTo(VERIFICATIONS_COUNT)
    }

    @Test
    fun shouldGetLastActivityTime() {
        val lastActivityTime = localDataSource.getLastActivityTime()

        assertThat(lastActivityTime).isEqualTo(LAST_ACTIVITY_TIME)
    }

    private fun configureMock() {
        with(mockPreferencesManager) {
            every { lastActivityTime } returns LAST_ACTIVITY_TIME
            every { enrolmentsToday } returns ENROLMENTS_COUNT
            every { identificationsToday } returns IDENTIFICATIONS_COUNT
            every { verificationsToday } returns VERIFICATIONS_COUNT
        }
    }

    private companion object {
        const val ENROLMENTS_COUNT = 50
        const val IDENTIFICATIONS_COUNT = 40
        const val VERIFICATIONS_COUNT = 30
        const val LAST_ACTIVITY_TIME = 12345L
    }

}
