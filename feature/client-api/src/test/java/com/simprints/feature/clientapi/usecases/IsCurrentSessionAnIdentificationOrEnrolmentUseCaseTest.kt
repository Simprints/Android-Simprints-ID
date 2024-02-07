package com.simprints.feature.clientapi.usecases

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.EventRepository
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

class IsCurrentSessionAnIdentificationOrEnrolmentUseCaseTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var eventRepository: EventRepository

    private lateinit var useCase: IsCurrentSessionAnIdentificationOrEnrolmentUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { eventRepository.getCurrentSessionScope() } returns mockk {
            coEvery { id } returns "sessionId"
        }

        useCase = IsCurrentSessionAnIdentificationOrEnrolmentUseCase(eventRepository)
    }


    @Test
    fun `isCurrentSessionAnIdentificationOrEnrolment return true if current session has an Identification`() =
        runTest {
            // Given
            coEvery { eventRepository.observeEventsFromSession(any()) } returns flowOf(
                mockk(), mockk(), mockk<IdentificationCalloutEvent>()
            )
            // When
            val result = useCase()
            //Then
            assertThat(result).isTrue()
        }

    @Test
    fun `isCurrentSessionAnIdentificationOrEnrolment return true if current session has an Enrolment`() =
        runTest {
            // Given
            coEvery { eventRepository.observeEventsFromSession(any()) } returns flowOf(
                mockk(), mockk(), mockk<EnrolmentCalloutEvent>()
            )
            // When
            val result = useCase()
            //Then
            assertThat(result).isTrue()
        }

    @Test
    fun `isCurrentSessionAnIdentificationOrEnrolment returns false if current session doesn't have an Identification or Enrolment`() =
        runTest {
            // Given
            coEvery { eventRepository.observeEventsFromSession(any()) } returns flowOf(
                mockk(), mockk(), mockk()
            )
            // When
            val result = useCase()
            //Then
            assertThat(result).isFalse()
        }

    @Test
    fun `isCurrentSessionAnIdentificationOrEnrolment returns false if session is empty`() =
        runTest {
            // Given
            coEvery { eventRepository.observeEventsFromSession(any()) } returns emptyFlow()
            // When
            val result = useCase()
            //Then
            assertThat(result).isFalse()
        }
}
