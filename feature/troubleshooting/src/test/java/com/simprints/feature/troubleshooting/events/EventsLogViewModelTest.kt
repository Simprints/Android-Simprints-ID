package com.simprints.feature.troubleshooting.events

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.sampledata.createAlertScreenEvent
import com.simprints.infra.events.sampledata.createSessionScope
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EventsLogViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var eventRepository: EventRepository

    private lateinit var viewModel: EventsLogViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        viewModel = EventsLogViewModel(
            eventRepository = eventRepository,
        )
    }

    @Test
    fun `sets list of scopes on request`() = runTest {
        coEvery { eventRepository.getAllScopes() } returns listOf(createSessionScope())

        val scopes = viewModel.scopes.test()
        viewModel.collectEventScopes()

        assertThat(scopes.value()).isNotEmpty()
    }

    @Test
    fun `sets list of scopes placeholder if no scopes`() = runTest {
        coEvery { eventRepository.getAllScopes() } returns emptyList()

        val scopes = viewModel.scopes.test()
        viewModel.collectEventScopes()

        assertThat(scopes.value()).isNotEmpty()
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
