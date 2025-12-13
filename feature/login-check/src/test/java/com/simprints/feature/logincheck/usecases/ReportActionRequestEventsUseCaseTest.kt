package com.simprints.feature.logincheck.usecases

import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.SimNetworkUtils
import com.simprints.infra.events.event.domain.models.ConnectivitySnapshotEvent
import com.simprints.infra.events.event.domain.models.SuspiciousIntentEvent
import com.simprints.infra.events.event.domain.models.ConfirmationCalloutEventV3
import com.simprints.infra.events.event.domain.models.EnrolmentCalloutEventV3
import com.simprints.infra.events.event.domain.models.EnrolmentLastBiometricsCalloutEventV3
import com.simprints.infra.events.event.domain.models.IdentificationCalloutEventV3
import com.simprints.infra.events.event.domain.models.VerificationCalloutEventV3
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coInvoke
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class ReportActionRequestEventsUseCaseTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var sessionEventRepository: SessionEventRepository

    @MockK
    private lateinit var timeHelper: TimeHelper

    @MockK
    private lateinit var simNetworkUtils: SimNetworkUtils

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

        useCase = ReportActionRequestEventsUseCase(
            sessionEventRepository,
            timeHelper,
            simNetworkUtils,
            recentUserActivityManager,
            CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
        )
    }

    @Test
    fun `Adds event if there are unknown extras`() = runTest {
        // When
        useCase.invoke(ActionFactory.getIdentifyRequest(extras = mapOf("key" to "value")))
        // Then
        coVerify(exactly = 1) {
            sessionEventRepository.addOrUpdateEvent(withArg { assert(it is SuspiciousIntentEvent) })
        }
    }

    @Test
    fun `Does not add event if no extras`() = runTest {
        // When
        useCase.invoke(ActionFactory.getIdentifyRequest(extras = emptyMap()))
        // Then
        coVerify(exactly = 0) {
            sessionEventRepository.addOrUpdateEvent(withArg { assert(it is SuspiciousIntentEvent) })
        }
    }

    @Test
    fun `Adds all required events for enrol action`() = runTest {
        useCase(ActionFactory.getEnrolRequest())

        coVerify(exactly = 1) { sessionEventRepository.addOrUpdateEvent(withArg { assert(it is ConnectivitySnapshotEvent) }) }
        coVerify(exactly = 1) { sessionEventRepository.addOrUpdateEvent(withArg { assert(it is EnrolmentCalloutEventV3) }) }
        coVerify(exactly = 1) { recentUserActivityManager.updateRecentUserActivity(any()) }
    }

    @Test
    fun `Adds all required events for confirmation action`() = runTest {
        useCase(ActionFactory.getConfirmationRequest())

        coVerify(exactly = 0) { sessionEventRepository.addOrUpdateEvent(withArg { assert(it is ConnectivitySnapshotEvent) }) }
        coVerify(exactly = 1) { sessionEventRepository.addOrUpdateEvent(withArg { assert(it is ConfirmationCalloutEventV3) }) }
        coVerify(exactly = 1) { recentUserActivityManager.updateRecentUserActivity(any()) }
    }

    @Test
    fun `Adds callout for identify action`() = runTest {
        useCase(ActionFactory.getIdentifyRequest())

        coVerify(exactly = 1) {
            sessionEventRepository.addOrUpdateEvent(withArg { assert(it is IdentificationCalloutEventV3) })
        }
    }

    @Test
    fun `Adds callout for verify action`() = runTest {
        useCase(ActionFactory.getVerifyRequest())

        coVerify(exactly = 1) {
            sessionEventRepository.addOrUpdateEvent(withArg { assert(it is VerificationCalloutEventV3) })
        }
    }

    @Test
    fun `Adds callout for enrol last action`() = runTest {
        useCase(ActionFactory.getEnrolLastRequest())

        coVerify(exactly = 1) {
            sessionEventRepository.addOrUpdateEvent(withArg { assert(it is EnrolmentLastBiometricsCalloutEventV3) })
        }
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
