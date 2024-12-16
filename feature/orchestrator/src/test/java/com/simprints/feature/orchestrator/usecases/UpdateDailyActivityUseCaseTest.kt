package com.simprints.feature.orchestrator.usecases

import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.orchestration.data.responses.AppEnrolResponse
import com.simprints.infra.orchestration.data.responses.AppIdentifyResponse
import com.simprints.infra.orchestration.data.responses.AppRefusalResponse
import com.simprints.infra.orchestration.data.responses.AppVerifyResponse
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coInvoke
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdateDailyActivityUseCaseTest {
    @MockK
    lateinit var recentUserActivityManager: RecentUserActivityManager

    @MockK
    lateinit var timeHelper: TimeHelper

    private lateinit var useCase: UpdateDailyActivityUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        val slot = slot<suspend (RecentUserActivity) -> RecentUserActivity>()
        coEvery { recentUserActivityManager.updateRecentUserActivity(capture(slot)) } coAnswers {
            slot.coInvoke(createBlankUserActivity())
        }

        useCase = UpdateDailyActivityUseCase(
            recentUserActivityManager,
            timeHelper,
        )
    }

    @Test
    fun `Update daily activity on enrol response`() = runTest {
        useCase(AppEnrolResponse("guid"))

        coVerify { recentUserActivityManager.updateRecentUserActivity(any()) }
    }

    @Test
    fun `Update daily activity on verify response`() = runTest {
        useCase(AppVerifyResponse(mockk()))

        coVerify { recentUserActivityManager.updateRecentUserActivity(any()) }
    }

    @Test
    fun `Update daily activity on identify response`() = runTest {
        useCase(AppIdentifyResponse(emptyList(), "guid"))

        coVerify { recentUserActivityManager.updateRecentUserActivity(any()) }
    }

    @Test
    fun `Do not update daily activity on refuse response`() = runTest {
        useCase(AppRefusalResponse("reason", "extra"))

        coVerify(exactly = 0) { recentUserActivityManager.updateRecentUserActivity(any()) }
    }

    private fun createBlankUserActivity() = RecentUserActivity(
        lastUserUsed = "".asTokenizableRaw(),
        lastScannerUsed = "",
        lastScannerVersion = "",
        enrolmentsToday = 0,
        identificationsToday = 0,
        verificationsToday = 0,
        lastActivityTime = 0,
    )
}
