package com.simprints.feature.logincheck.usecases

import com.simprints.feature.clientapi.mappers.request.requestFactories.ConfirmIdentityActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.EnrolActionFactory
import com.simprints.feature.clientapi.usecases.SimpleEventReporter
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coInvoke
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class ReportActionRequestEventsUseCaseTest {

    @MockK
    lateinit var simpleEventReporter: SimpleEventReporter

    @MockK
    lateinit var recentUserActivityManager: RecentUserActivityManager

    lateinit var useCase: ReportActionRequestEventsUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        val slot = slot<suspend (RecentUserActivity) -> RecentUserActivity>()
        coEvery { recentUserActivityManager.updateRecentUserActivity(capture(slot)) } coAnswers {
            slot.coInvoke(createBlankUserActivity())
        }

        useCase = ReportActionRequestEventsUseCase(simpleEventReporter, recentUserActivityManager)
    }

    @Test
    fun `Adds all required events for flow action`() = runTest {
        useCase(EnrolActionFactory.getValidSimprintsRequest())

        coVerify {
            simpleEventReporter.addUnknownExtrasEvent(any())
            simpleEventReporter.addConnectivityStateEvent()
            simpleEventReporter.addRequestActionEvent(any())
            recentUserActivityManager.updateRecentUserActivity(any())
        }
    }

    @Test
    fun `Adds all required events for follow up action`() = runTest {
        useCase(ConfirmIdentityActionFactory.getValidSimprintsRequest())

        coVerify {
            simpleEventReporter.addUnknownExtrasEvent(any())
            simpleEventReporter.addRequestActionEvent(any())
            recentUserActivityManager.updateRecentUserActivity(any())
        }
        coVerify(exactly = 0) { simpleEventReporter.addConnectivityStateEvent() }
    }

    private fun createBlankUserActivity() = RecentUserActivity(
        lastUserUsed = "",
        lastScannerUsed = "",
        lastScannerVersion = "",
        enrolmentsToday = 0,
        identificationsToday = 0,
        verificationsToday = 0,
        lastActivityTime = 0,
    )
}
