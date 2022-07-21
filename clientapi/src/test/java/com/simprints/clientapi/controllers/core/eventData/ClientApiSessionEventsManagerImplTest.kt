package com.simprints.clientapi.controllers.core.eventData

import com.google.common.truth.Truth
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.models.callback.IdentificationCallbackEvent
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClientApiSessionEventsManagerImplTest {


    private lateinit var clientApiSessionEventsManager: ClientApiSessionEventsManager
    private lateinit var coreEventRepository: EventRepository
    private val sessionId ="sessionId"
    @Before
    fun setup(){
        coreEventRepository = mockk()
        clientApiSessionEventsManager = ClientApiSessionEventsManagerImpl(
            coreEventRepository, mockk(), mockk(), mockk()
        )
    }

    @Test
    fun `test isSessionHasIdentificationCallback return true if session has IdentificationCallbackEvent`() = runTest {
        // Given
        coEvery { coreEventRepository.getEventsFromSession(sessionId) } returns flowOf(
            mockk(), mockk(), mockk<IdentificationCallbackEvent>()
        )
        // When
        val result= clientApiSessionEventsManager.isSessionHasIdentificationCallback(sessionId)
        //Then
        Truth.assertThat(result).isTrue()
    }
    @Test
    fun `test isSessionHasIdentificationCallback return fasle if session doesn't hav IdentificationCallbackEvent`() = runTest {
        // Given
        coEvery { coreEventRepository.getEventsFromSession(sessionId) } returns flowOf(
            mockk(), mockk(), mockk()
        )
        // When
        val result= clientApiSessionEventsManager.isSessionHasIdentificationCallback(sessionId)
        //Then
        Truth.assertThat(result).isFalse()
    }
    @Test
    fun `test isSessionHasIdentificationCallback return fasle if session events is empty`() = runTest {
        // Given
        coEvery { coreEventRepository.getEventsFromSession(sessionId) } returns emptyFlow()
        // When
        val result= clientApiSessionEventsManager.isSessionHasIdentificationCallback(sessionId)
        //Then
        Truth.assertThat(result).isFalse()
    }
}
