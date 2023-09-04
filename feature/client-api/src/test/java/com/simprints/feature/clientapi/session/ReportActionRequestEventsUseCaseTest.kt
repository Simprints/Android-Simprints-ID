package com.simprints.feature.clientapi.session

import com.simprints.feature.clientapi.mappers.request.requestFactories.ConfirmIdentityActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.EnrolActionFactory
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coInvoke
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.invoke
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

internal class ReportActionRequestEventsUseCaseTest {

    @MockK
    lateinit var clientSessionManager: ClientSessionManager

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

        useCase = ReportActionRequestEventsUseCase(clientSessionManager, recentUserActivityManager)
    }

    @Test
    fun `Adds all required events for flow action`() = runTest {
        useCase(EnrolActionFactory.getValidSimprintsRequest())

        coVerify {
            clientSessionManager.addUnknownExtrasEvent(any())
            clientSessionManager.addConnectivityStateEvent()
            clientSessionManager.addRequestActionEvent(any())
            recentUserActivityManager.updateRecentUserActivity(any())
        }
    }

    @Test
    fun `Adds all required events for follow up action`() = runTest {
        useCase(ConfirmIdentityActionFactory.getValidSimprintsRequest())

        coVerify {
            clientSessionManager.addUnknownExtrasEvent(any())
            clientSessionManager.addRequestActionEvent(any())
            recentUserActivityManager.updateRecentUserActivity(any())
        }
        coVerify(exactly = 0) { clientSessionManager.addConnectivityStateEvent() }
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
