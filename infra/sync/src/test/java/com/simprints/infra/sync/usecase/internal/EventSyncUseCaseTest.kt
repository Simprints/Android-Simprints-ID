package com.simprints.infra.sync.usecase.internal

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.eventsync.sync.EventSyncStateProcessor
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class EventSyncUseCaseTest {
    @MockK
    private lateinit var eventSyncStateProcessor: EventSyncStateProcessor

    private lateinit var eventSyncUseCase: EventSyncUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        eventSyncUseCase = EventSyncUseCase(eventSyncStateProcessor)
    }

    @Test
    fun `invocation should call sync processor`() = runTest {
        val expected = EventSyncState(
            syncId = "",
            progress = null,
            total = null,
            upSyncWorkersInfo = emptyList(),
            downSyncWorkersInfo = emptyList(),
            reporterStates = emptyList(),
            lastSyncTime = null,
        )
        every { eventSyncStateProcessor.getLastSyncState() } returns flowOf(expected)

        val result = eventSyncUseCase().firstOrNull()

        verify(exactly = 1) { eventSyncStateProcessor.getLastSyncState() }
        assertThat(result).isEqualTo(expected)
    }

}
