package com.simprints.id.activities.dashboard.cards.daily_activity.repository

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.id.activities.dashboard.cards.daily_activity.data.DailyActivityLocalDataSource
import com.simprints.id.activities.dashboard.cards.daily_activity.model.DashboardDailyActivityState
import com.simprints.id.domain.moduleapi.app.responses.AppEnrolResponse
import com.simprints.id.domain.moduleapi.app.responses.AppIdentifyResponse
import com.simprints.id.domain.moduleapi.app.responses.AppVerifyResponse
import com.simprints.id.domain.moduleapi.app.responses.entities.MatchConfidence
import com.simprints.id.domain.moduleapi.app.responses.entities.MatchResult
import com.simprints.id.domain.moduleapi.app.responses.entities.Tier
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DashboardDailyActivityRepositoryImplTest {

    private val mockLocalDataSource = mockk<DailyActivityLocalDataSource> {
        coEvery { getEnrolmentsMadeToday() } returns ENROLMENTS_COUNT
        coEvery { getIdentificationsMadeToday() } returns IDENTIFICATIONS_COUNT
        coEvery { getVerificationsMadeToday() } returns VERIFICATIONS_COUNT
    }

    private val mockTimeHelper = mockk<TimeHelper> {
        every { now() } returns NOW_MILLIS
        every { todayInMillis() } returns TODAY_MILLIS
        every { tomorrowInMillis() } returns TOMORROW_MILLIS
    }

    private val repository =
        DashboardDailyActivityRepositoryImpl(mockLocalDataSource, mockTimeHelper)

    @Test
    fun shouldGetDailyActivity() = runTest {
        coEvery { mockLocalDataSource.getLastActivityTime() } returns NOW_MILLIS
        val expected = DashboardDailyActivityState(
            ENROLMENTS_COUNT,
            IDENTIFICATIONS_COUNT,
            VERIFICATIONS_COUNT
        )

        val actual = repository.getDailyActivity()


        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun shouldUpdateEnrolments() = runTest {
        coEvery { mockLocalDataSource.getLastActivityTime() } returns NOW_MILLIS
        val enrolmentResponse = AppEnrolResponse("some_guid")

        repository.updateDailyActivity(enrolmentResponse)

        coVerify { mockLocalDataSource.computeNewEnrolmentAndGet() }
    }

    @Test
    fun shouldUpdateIdentifications() = runTest {
        coEvery { mockLocalDataSource.getLastActivityTime() } returns NOW_MILLIS
        val identificationResponse = AppIdentifyResponse(emptyList(), "some_session_id")

        repository.updateDailyActivity(identificationResponse)

        coVerify { mockLocalDataSource.computeNewIdentificationAndGet() }
    }

    @Test
    fun shouldUpdateVerifications() = runTest {
        coEvery { mockLocalDataSource.getLastActivityTime() } returns NOW_MILLIS
        val matchResult = MatchResult(
            "some_guid",
            confidence = 100,
            tier = Tier.TIER_1,
            matchConfidence = MatchConfidence.HIGH
        )
        val verificationResponse = AppVerifyResponse(matchResult)

        repository.updateDailyActivity(verificationResponse)

        coVerify { mockLocalDataSource.computeNewVerificationAndGet() }
    }

    @Test
    fun whenLastActivityTimeIsBeforeToday_shouldClearBeforeGettingDailyActivity() = runTest {
        coEvery { mockLocalDataSource.getLastActivityTime() } returns TODAY_MILLIS - 100

        repository.getDailyActivity()

        coVerify { mockLocalDataSource.clearActivity() }
    }

    @Test
    fun whenLastActivityTimeIsBeforeToday_shouldClearBeforeUpdatingDailyActivity() = runTest {
        coEvery { mockLocalDataSource.getLastActivityTime() } returns TODAY_MILLIS - 100
        val enrolmentResponse = AppEnrolResponse("some_guid")

        repository.updateDailyActivity(enrolmentResponse)

        coVerify { mockLocalDataSource.clearActivity() }
    }

    @Test
    fun whenLastActivityTimeIsAfterToday_shouldClearBeforeGettingDailyActivity() = runTest {
        coEvery { mockLocalDataSource.getLastActivityTime() } returns TOMORROW_MILLIS

        repository.getDailyActivity()

        coVerify { mockLocalDataSource.clearActivity() }
    }

    @Test
    fun whenLastActivityTimeIsAfterToday_shouldClearBeforeUpdatingDailyActivity() = runTest {
        coEvery { mockLocalDataSource.getLastActivityTime() } returns TOMORROW_MILLIS
        val enrolmentResponse = AppEnrolResponse("some_guid")

        repository.updateDailyActivity(enrolmentResponse)

        coVerify { mockLocalDataSource.clearActivity() }
    }

    @Test
    fun whenLastActivityTimeIsValid_shouldNotClearBeforeGettingDailyActivity() = runTest {
        coEvery { mockLocalDataSource.getLastActivityTime() } returns NOW_MILLIS

        repository.getDailyActivity()

        coVerify(exactly = 0) { mockLocalDataSource.clearActivity() }
    }

    @Test
    fun whenLastActivityTimeIsValid_shouldNotClearBeforeUpdatingDailyActivity() = runTest {
        coEvery { mockLocalDataSource.getLastActivityTime() } returns NOW_MILLIS
        val enrolmentResponse = AppEnrolResponse("some_guid")

        repository.updateDailyActivity(enrolmentResponse)

        coVerify(exactly = 0) { mockLocalDataSource.clearActivity() }
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
