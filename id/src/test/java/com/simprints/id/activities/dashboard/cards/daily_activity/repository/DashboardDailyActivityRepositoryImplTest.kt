package com.simprints.id.activities.dashboard.cards.daily_activity.repository

import com.google.common.truth.Truth.assertThat
import com.simprints.id.activities.dashboard.cards.daily_activity.data.DailyActivityLocalDataSource
import com.simprints.id.activities.dashboard.cards.daily_activity.model.DashboardDailyActivityState
import com.simprints.id.domain.moduleapi.app.responses.AppEnrolResponse
import com.simprints.id.domain.moduleapi.app.responses.AppIdentifyResponse
import com.simprints.id.domain.moduleapi.app.responses.AppVerifyResponse
import com.simprints.id.domain.moduleapi.app.responses.entities.MatchResult
import com.simprints.id.domain.moduleapi.app.responses.entities.Tier
import com.simprints.id.tools.TimeHelper
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class DashboardDailyActivityRepositoryImplTest {

    @MockK lateinit var mockLocalDataSource: DailyActivityLocalDataSource
    @MockK lateinit var mockTimeHelper: TimeHelper

    private lateinit var repository: DashboardDailyActivityRepositoryImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        configureMocks()
        repository = DashboardDailyActivityRepositoryImpl(mockLocalDataSource, mockTimeHelper)
    }

    @Test
    fun shouldGetDailyActivity() {
        every { mockLocalDataSource.getLastActivityTime() } returns NOW_MILLIS
        val expected = DashboardDailyActivityState(
            ENROLMENTS_COUNT,
            IDENTIFICATIONS_COUNT,
            VERIFICATIONS_COUNT
        )

        val actual = repository.getDailyActivity()


        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun shouldUpdateEnrolments() {
        every { mockLocalDataSource.getLastActivityTime() } returns NOW_MILLIS
        val enrolmentResponse = AppEnrolResponse("some_guid")

        repository.updateDailyActivity(enrolmentResponse)

        verify { mockLocalDataSource.computeNewEnrolmentAndGet() }
    }

    @Test
    fun shouldUpdateIdentifications() {
        every { mockLocalDataSource.getLastActivityTime() } returns NOW_MILLIS
        val identificationResponse = AppIdentifyResponse(emptyList(), "some_session_id")

        repository.updateDailyActivity(identificationResponse)

        verify { mockLocalDataSource.computeNewIdentificationAndGet() }
    }

    @Test
    fun shouldUpdateVerifications() {
        every { mockLocalDataSource.getLastActivityTime() } returns NOW_MILLIS
        val matchResult = MatchResult("some_guid", confidence = 100, tier = Tier.TIER_1)
        val verificationResponse = AppVerifyResponse(matchResult)

        repository.updateDailyActivity(verificationResponse)

        verify { mockLocalDataSource.computeNewVerificationAndGet() }
    }

    @Test
    fun whenLastActivityTimeIsBeforeToday_shouldClearBeforeGettingDailyActivity() {
        every { mockLocalDataSource.getLastActivityTime() } returns TODAY_MILLIS - 100

        repository.getDailyActivity()

        verify { mockLocalDataSource.clearActivity() }
    }

    @Test
    fun whenLastActivityTimeIsBeforeToday_shouldClearBeforeUpdatingDailyActivity() {
        every { mockLocalDataSource.getLastActivityTime() } returns TODAY_MILLIS - 100
        val enrolmentResponse = AppEnrolResponse("some_guid")

        repository.updateDailyActivity(enrolmentResponse)

        verify { mockLocalDataSource.clearActivity() }
    }

    @Test
    fun whenLastActivityTimeIsAfterToday_shouldClearBeforeGettingDailyActivity() {
        every { mockLocalDataSource.getLastActivityTime() } returns TOMORROW_MILLIS

        repository.getDailyActivity()

        verify { mockLocalDataSource.clearActivity() }
    }

    @Test
    fun whenLastActivityTimeIsAfterToday_shouldClearBeforeUpdatingDailyActivity() {
        every { mockLocalDataSource.getLastActivityTime() } returns TOMORROW_MILLIS
        val enrolmentResponse = AppEnrolResponse("some_guid")

        repository.updateDailyActivity(enrolmentResponse)

        verify { mockLocalDataSource.clearActivity() }
    }

    @Test
    fun whenLastActivityTimeIsValid_shouldNotClearBeforeGettingDailyActivity() {
        every { mockLocalDataSource.getLastActivityTime() } returns NOW_MILLIS

        repository.getDailyActivity()

        verify(exactly = 0) { mockLocalDataSource.clearActivity() }
    }

    @Test
    fun whenLastActivityTimeIsValid_shouldNotClearBeforeUpdatingDailyActivity() {
        every { mockLocalDataSource.getLastActivityTime() } returns NOW_MILLIS
        val enrolmentResponse = AppEnrolResponse("some_guid")

        repository.updateDailyActivity(enrolmentResponse)

        verify(exactly = 0) { mockLocalDataSource.clearActivity() }
    }

    private fun configureMocks() {
        configureMockLocalDataSource()
        configureMockTimeHelper()
    }

    private fun configureMockTimeHelper() {
        with(mockTimeHelper) {
            every { now() } returns NOW_MILLIS
            every { todayInMillis() } returns TODAY_MILLIS
            every { tomorrowInMillis() } returns TOMORROW_MILLIS
        }
    }

    private fun configureMockLocalDataSource() {
        with(mockLocalDataSource) {
            every { getEnrolmentsMadeToday() } returns ENROLMENTS_COUNT
            every { getIdentificationsMadeToday() } returns IDENTIFICATIONS_COUNT
            every { getVerificationsMadeToday() } returns VERIFICATIONS_COUNT
        }
    }

    private companion object {
        const val ENROLMENTS_COUNT = 50
        const val IDENTIFICATIONS_COUNT = 40
        const val VERIFICATIONS_COUNT = 30
        const val NOW_MILLIS = 12345L
        const val TODAY_MILLIS = 1234L
        const val TOMORROW_MILLIS = 123456L
    }

}
