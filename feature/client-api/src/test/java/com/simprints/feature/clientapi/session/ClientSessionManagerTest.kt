package com.simprints.feature.clientapi.session

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.callback.IdentificationCallbackEvent
import com.simprints.infra.events.event.domain.models.callout.EnrolmentCalloutEvent
import com.simprints.infra.events.event.domain.models.callout.IdentificationCalloutEvent
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
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

    private lateinit var clientSessionManager: ClientSessionManager

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { coreEventRepository.getCurrentCaptureSessionEvent() } returns mockk {
            coEvery { id } returns SESSION_ID
        }

        clientSessionManager = ClientSessionManager(
            coreEventRepository,
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

    companion object {

        private const val SESSION_ID = "sessionId"
    }
}
