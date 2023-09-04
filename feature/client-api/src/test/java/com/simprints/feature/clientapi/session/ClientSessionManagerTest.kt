package com.simprints.feature.clientapi.session

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.SimNetworkUtils
import com.simprints.feature.clientapi.mappers.request.requestFactories.ConfirmIdentityActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.EnrolActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.EnrolLastBiometricsActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.IdentifyRequestActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.VerifyActionFactory
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.AuthorizationEvent
import com.simprints.infra.events.event.domain.models.AuthorizationEvent.AuthorizationPayload.AuthorizationResult
import com.simprints.infra.events.event.domain.models.CompletionCheckEvent
import com.simprints.infra.events.event.domain.models.ConnectivitySnapshotEvent
import com.simprints.infra.events.event.domain.models.InvalidIntentEvent
import com.simprints.infra.events.event.domain.models.SuspiciousIntentEvent
import com.simprints.infra.events.event.domain.models.callback.IdentificationCallbackEvent
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
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ClientSessionManagerTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var coreEventRepository: EventRepository

    @MockK
    private lateinit var timeHelper: TimeHelper

    @MockK
    private lateinit var simNetworkUtils: SimNetworkUtils

    private lateinit var clientSessionManager: ClientSessionManager

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { coreEventRepository.getCurrentCaptureSessionEvent() } returns mockk {
            coEvery { id } returns SESSION_ID
        }

        clientSessionManager = ClientSessionManager(
            coreEventRepository,
            timeHelper,
            simNetworkUtils,
            CoroutineScope(testCoroutineRule.testCoroutineDispatcher)
        )
    }

    @Test
    fun `getCurrentSessionId return current session id`() = runTest {
        // When
        val result = clientSessionManager.getCurrentSessionId()
        //Then
        assertThat(result).isEqualTo(SESSION_ID)
    }

    @Test
    fun `sessionHasIdentificationCallback return true if session has IdentificationCallbackEvent`() = runTest {
        // Given
        coEvery { coreEventRepository.observeEventsFromSession(SESSION_ID) } returns flowOf(
            mockk(), mockk(), mockk<IdentificationCallbackEvent>()
        )
        // When
        val result = clientSessionManager.sessionHasIdentificationCallback(SESSION_ID)
        //Then
        assertThat(result).isTrue()
    }

    @Test
    fun `sessionHasIdentificationCallback return false if session doesn't have IdentificationCallbackEvent`() =
        runTest {
            // Given
            coEvery { coreEventRepository.observeEventsFromSession(SESSION_ID) } returns flowOf(
                mockk(), mockk(), mockk()
            )
            // When
            val result = clientSessionManager.sessionHasIdentificationCallback(SESSION_ID)
            //Then
            assertThat(result).isFalse()
        }

    @Test
    fun `sessionHasIdentificationCallback return false if session events is empty`() =
        runTest {
            // Given
            coEvery { coreEventRepository.observeEventsFromSession(SESSION_ID) } returns emptyFlow()
            // When
            val result = clientSessionManager.sessionHasIdentificationCallback(SESSION_ID)
            //Then
            assertThat(result).isFalse()
        }

    @Test
    fun `isCurrentSessionAnIdentificationOrEnrolment return true if current session has an Identification`() =
        runTest {
            // Given
            coEvery { coreEventRepository.observeEventsFromSession(SESSION_ID) } returns flowOf(
                mockk(), mockk(), mockk<IdentificationCalloutEvent>()
            )
            // When
            val result = clientSessionManager.isCurrentSessionAnIdentificationOrEnrolment()
            //Then
            assertThat(result).isTrue()
        }

    @Test
    fun `isCurrentSessionAnIdentificationOrEnrolment return true if current session has an Enrolment`() =
        runTest {
            // Given
            coEvery { coreEventRepository.observeEventsFromSession(SESSION_ID) } returns flowOf(
                mockk(), mockk(), mockk<EnrolmentCalloutEvent>()
            )
            // When
            val result = clientSessionManager.isCurrentSessionAnIdentificationOrEnrolment()
            //Then
            assertThat(result).isTrue()
        }

    @Test
    fun `isCurrentSessionAnIdentificationOrEnrolment returns false if current session doesn't have an Identification or Enrolment`() =
        runTest {
            // Given
            coEvery { coreEventRepository.observeEventsFromSession(SESSION_ID) } returns flowOf(
                mockk(), mockk(), mockk()
            )
            // When
            val result = clientSessionManager.isCurrentSessionAnIdentificationOrEnrolment()
            //Then
            assertThat(result).isFalse()
        }

    @Test
    fun `isCurrentSessionAnIdentificationOrEnrolment returns false if session is empty`() =
        runTest {
            // Given
            coEvery { coreEventRepository.observeEventsFromSession(SESSION_ID) } returns emptyFlow()
            // When
            val result = clientSessionManager.isCurrentSessionAnIdentificationOrEnrolment()
            //Then
            assertThat(result).isFalse()
        }

    @Test
    fun `addUnknownExtrasEvent adds event if there are unknown extras`() = runTest {
        // Given
        val unknownExtras = mapOf("key" to "value")
        // When
        clientSessionManager.addUnknownExtrasEvent(unknownExtras)
        //Then
        coVerify {
            coreEventRepository.addOrUpdateEvent(any<SuspiciousIntentEvent>())
        }
    }

    @Test
    fun `addUnknownExtrasEvent does not add event if no extras`() = runTest {
        // Given
        val unknownExtras = emptyMap<String, Any>()
        // When
        clientSessionManager.addUnknownExtrasEvent(unknownExtras)
        //Then
        coVerify(exactly = 0) { coreEventRepository.addOrUpdateEvent(any()) }
    }

    @Test
    fun `addConnectivityStateEvent adds event`() = runTest {
        every { simNetworkUtils.connectionsStates } returns emptyList()
        // When
        clientSessionManager.addConnectivityStateEvent()
        //Then
        coVerify {
            coreEventRepository.addOrUpdateEvent(withArg { assertThat(it).isInstanceOf(ConnectivitySnapshotEvent::class.java) })
        }
    }

    @Test
    fun `addRequestActionEvent adds correct enrol event`() = runTest {
        // When
        clientSessionManager.addRequestActionEvent(EnrolActionFactory.getValidSimprintsRequest())
        //Then
        coVerify {
            coreEventRepository.addOrUpdateEvent(withArg { assertThat(it).isInstanceOf(EnrolmentCalloutEvent::class.java) })
        }
    }

    @Test
    fun `addRequestActionEvent adds correct idetify event`() = runTest {
        // When
        clientSessionManager.addRequestActionEvent(IdentifyRequestActionFactory.getValidSimprintsRequest())
        //Then
        coVerify {
            coreEventRepository.addOrUpdateEvent(withArg { assertThat(it).isInstanceOf(IdentificationCalloutEvent::class.java) })
        }
    }

    @Test
    fun `addRequestActionEvent adds correct verify event`() = runTest {
        // When
        clientSessionManager.addRequestActionEvent(VerifyActionFactory.getValidSimprintsRequest())
        //Then
        coVerify {
            coreEventRepository.addOrUpdateEvent(withArg { assertThat(it).isInstanceOf(VerificationCalloutEvent::class.java) })
        }
    }

    @Test
    fun `addRequestActionEvent adds correct confirm event`() = runTest {
        // When
        clientSessionManager.addRequestActionEvent(ConfirmIdentityActionFactory.getValidSimprintsRequest())
        //Then
        coVerify {
            coreEventRepository.addOrUpdateEvent(withArg { assertThat(it).isInstanceOf(ConfirmationCalloutEvent::class.java) })
        }
    }

    @Test
    fun `addRequestActionEvent adds correct enrol last event`() = runTest {
        // When
        clientSessionManager.addRequestActionEvent(EnrolLastBiometricsActionFactory.getValidSimprintsRequest())
        //Then
        coVerify {
            coreEventRepository.addOrUpdateEvent(withArg { assertThat(it).isInstanceOf(EnrolmentLastBiometricsCalloutEvent::class.java) })
        }
    }

    @Test
    fun `addInvalidIntentEvent adds event`() = runTest {
        // When
        clientSessionManager.addInvalidIntentEvent("action", emptyMap())
        //Then
        coVerify {
            coreEventRepository.addOrUpdateEvent(withArg { assertThat(it).isInstanceOf(InvalidIntentEvent::class.java) })
        }
    }

    @Test
    fun `addCompletionCheckEvent adds event`() = runTest {
        // When
        clientSessionManager.addCompletionCheckEvent(true)
        //Then
        coVerify {
            coreEventRepository.addOrUpdateEvent(withArg { assertThat(it).isInstanceOf(CompletionCheckEvent::class.java) })
        }
    }

    @Test
    fun `addAuthorizationEvent adds not authorised event`() = runTest {
        // When
        clientSessionManager.addAuthorizationEvent(EnrolActionFactory.getValidSimprintsRequest(), false)
        //Then
        coVerify {
            coreEventRepository.addOrUpdateEvent(withArg {
                assertThat((it as AuthorizationEvent).payload.result).isEqualTo(AuthorizationResult.NOT_AUTHORIZED)
                assertThat(it.payload.userInfo).isNull()
            })
        }
    }

    @Test
    fun `addAuthorizationEvent adds authorised event`() = runTest {
        // When
        clientSessionManager.addAuthorizationEvent(EnrolActionFactory.getValidSimprintsRequest(), true)
        //Then
        coVerify {
            coreEventRepository.addOrUpdateEvent(withArg {
                assertThat((it as AuthorizationEvent).payload.result).isEqualTo(AuthorizationResult.AUTHORIZED)
                assertThat(it.payload.userInfo).isNotNull()
            })
        }
    }

    @Test
    fun `closeCurrentSessionNormally closes current session`() = runTest {
        // When
        clientSessionManager.closeCurrentSessionNormally()
        //Then
        coVerify { coreEventRepository.closeCurrentSession() }
    }

    @Test
    fun `deleteSessionEvents deletes events in session`() = runTest {
        // When
        clientSessionManager.deleteSessionEvents(SESSION_ID)
        //Then
        coVerify { coreEventRepository.deleteSessionEvents(eq(SESSION_ID)) }
    }

    companion object {

        private const val SESSION_ID = "sessionId"
    }
}
