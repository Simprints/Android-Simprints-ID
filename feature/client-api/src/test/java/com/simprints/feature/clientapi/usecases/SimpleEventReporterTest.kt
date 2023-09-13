package com.simprints.feature.clientapi.usecases

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.SimNetworkUtils
import com.simprints.feature.clientapi.mappers.request.requestFactories.ConfirmIdentityActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.EnrolActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.EnrolLastBiometricsActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.IdentifyRequestActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.VerifyActionFactory
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.CompletionCheckEvent
import com.simprints.infra.events.event.domain.models.ConnectivitySnapshotEvent
import com.simprints.infra.events.event.domain.models.InvalidIntentEvent
import com.simprints.infra.events.event.domain.models.SuspiciousIntentEvent
import com.simprints.infra.events.event.domain.models.callout.ConfirmationCalloutEvent
import com.simprints.infra.events.event.domain.models.callout.EnrolmentCalloutEvent
import com.simprints.infra.events.event.domain.models.callout.EnrolmentLastBiometricsCalloutEvent
import com.simprints.infra.events.event.domain.models.callout.IdentificationCalloutEvent
import com.simprints.infra.events.event.domain.models.callout.VerificationCalloutEvent
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SimpleEventReporterTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var coreEventRepository: EventRepository

    @MockK
    private lateinit var timeHelper: TimeHelper

    private lateinit var simpleEventReporter: SimpleEventReporter

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { coreEventRepository.getCurrentCaptureSessionEvent() } returns mockk {
            coEvery { id } returns SESSION_ID
        }

        simpleEventReporter = SimpleEventReporter(
            coreEventRepository,
            timeHelper,
            CoroutineScope(testCoroutineRule.testCoroutineDispatcher)
        )
    }

    @Test
    fun `addInvalidIntentEvent adds event`() = runTest {
        // When
        simpleEventReporter.addInvalidIntentEvent("action", emptyMap())
        //Then
        coVerify {
            coreEventRepository.addOrUpdateEvent(withArg { assertThat(it).isInstanceOf(InvalidIntentEvent::class.java) })
        }
    }

    @Test
    fun `addCompletionCheckEvent adds event`() = runTest {
        // When
        simpleEventReporter.addCompletionCheckEvent(true)
        //Then
        coVerify {
            coreEventRepository.addOrUpdateEvent(withArg { assertThat(it).isInstanceOf(CompletionCheckEvent::class.java) })
        }
    }

    @Test
    fun `closeCurrentSessionNormally closes current session`() = runTest {
        // When
        simpleEventReporter.closeCurrentSessionNormally()
        //Then
        coVerify { coreEventRepository.closeCurrentSession() }
    }

    companion object {

        private const val SESSION_ID = "sessionId"
    }
}
