package com.simprints.id.activities.consent

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.models.ConsentEvent
import com.simprints.eventsystem.event.domain.models.session.SessionCaptureEvent
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class ConsentViewModelTest {

    @MockK
    private lateinit var configManager: ConfigManager

    @MockK
    private lateinit var eventRepository: EventRepository

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private lateinit var vm: ConsentViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        configureMocks()

        val scope = CoroutineScope(testCoroutineRule.testCoroutineDispatcher)
        vm = ConsentViewModel(
            configManager,
            eventRepository,
            scope
        )
    }

    private fun configureMocks() {
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { general } returns mockk {
                every { modalities } returns listOf(GeneralConfiguration.Modality.FACE)
            }
        }
    }

    @Test
    fun `ViewModel init should update project configuration`() = runTest {
        //init() called in setUp()
        coVerify { configManager.getProjectConfiguration() }
        val config = vm.configuration.value
        assertThat(config?.general?.modalities)
            .isEqualTo(listOf(GeneralConfiguration.Modality.FACE))
    }

    @Test
    fun `calling addConsentEvent adds it to the repository`() = runTest {
        val event: ConsentEvent = mockk()
        vm.addConsentEvent(event)
        coVerify { eventRepository.addOrUpdateEvent(event) }
    }

    @Test
    fun `calling deleteLocationInfoFromSession deletes location from the session in repository`() = runTest {
        val session: SessionCaptureEvent = mockk(relaxed = true)
        coEvery { eventRepository.getCurrentCaptureSessionEvent() } returns session

        vm.deleteLocationInfoFromSession()
        coVerify { eventRepository.getCurrentCaptureSessionEvent() }
        coVerify { eventRepository.addOrUpdateEvent(any()) }
        coVerify { session.payload.location = null }
    }
}
