package com.simprints.feature.clientapi.usecases

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.callback.IdentificationCallbackEvent
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SessionHasIdentificationCallbackUseCaseTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var eventRepository: EventRepository

    private lateinit var useCase: SessionHasIdentificationCallbackUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = SessionHasIdentificationCallbackUseCase(eventRepository)
    }

    @Test
    fun `sessionHasIdentificationCallback return true if session has IdentificationCallbackEvent`() = runTest {
        // Given
        coEvery { eventRepository.getEventsFromScope(any()) } returns listOf(
            mockk(),
            mockk(),
            mockk<IdentificationCallbackEvent>(),
        )
        // Then
        assertThat(useCase("sessionId")).isTrue()
    }

    @Test
    fun `sessionHasIdentificationCallback return false if session doesn't have IdentificationCallbackEvent`() = runTest {
        // Given
        coEvery { eventRepository.getEventsFromScope(any()) } returns listOf(
            mockk(),
            mockk(),
            mockk(),
        )
        // Then
        assertThat(useCase("sessionId")).isFalse()
    }

    @Test
    fun `sessionHasIdentificationCallback return false if session events is empty`() = runTest {
        // Given
        coEvery { eventRepository.getEventsFromScope(any()) } returns emptyList()
        // Then
        assertThat(useCase("sessionId")).isFalse()
    }
}
