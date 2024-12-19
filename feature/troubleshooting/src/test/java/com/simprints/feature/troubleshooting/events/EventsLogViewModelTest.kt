package com.simprints.feature.troubleshooting.events

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.sampledata.createAlertScreenEvent
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.text.SimpleDateFormat

class EventsLogViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var eventRepository: EventRepository

    @MockK
    private lateinit var dateFormatter: SimpleDateFormat

    private lateinit var viewModel: EventsLogViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        every { dateFormatter.format(any()) } returns "date"

        viewModel = EventsLogViewModel(
            eventRepository = eventRepository,
            dateFormatter = dateFormatter,
        )
    }

    @Test
    fun `sets list of events on request`() = runTest {
        coEvery { eventRepository.getEventsFromScope(any()) } returns listOf(createAlertScreenEvent())

        val scopes = viewModel.events.test()
        viewModel.collectEvents("scopeId")

        assertThat(scopes.value()).isNotEmpty()
    }

    @Test
    fun `sets list of events placeholder if no events`() = runTest {
        coEvery { eventRepository.getEventsFromScope(any()) } returns emptyList()

        val scopes = viewModel.events.test()
        viewModel.collectEvents("scopeId")

        assertThat(scopes.value()).isNotEmpty()
    }
}
