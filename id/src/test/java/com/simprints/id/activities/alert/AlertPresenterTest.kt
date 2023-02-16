package com.simprints.id.activities.alert

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.core.ExternalScope
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.models.AlertScreenEvent
import com.simprints.eventsystem.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType
import com.simprints.eventsystem.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.INTEGRITY_SERVICE_ERROR
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.exitformhandler.ExitFormHelper
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.resources.R
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import org.junit.Before
import org.junit.Rule
import org.junit.Test


internal class AlertPresenterTest {

    @MockK
    private lateinit var view: AlertContract.View

    @MockK
    private lateinit var configManager: ConfigManager

    @MockK
    private lateinit var eventRepository: EventRepository

    @MockK
    private lateinit var timeHelper: TimeHelper

    @MockK
    private lateinit var exitFormHelper: ExitFormHelper

    private lateinit var alertPresenter: AlertPresenter

    @ExternalScope
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()
    private lateinit var externalScope: CoroutineScope


    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        externalScope = CoroutineScope(testCoroutineRule.testCoroutineDispatcher)
        mockkStatic("com.simprints.id.domain.alert.AlertTypeKt")
    }

    @Test
    fun `test start with INTEGRITY_SERVICE_ERROR should add the correct event in eventRepository`() {
        //Given
        val alertType: AlertType = AlertType.INTEGRITY_SERVICE_ERROR
        alertPresenter = AlertPresenter(
            view,
            alertType,
            eventRepository,
            configManager,
            timeHelper,
            exitFormHelper,
            CoroutineScope(testCoroutineRule.testCoroutineDispatcher)
        )
        val eventSlot = slot<AlertScreenEvent>()
        coEvery { eventRepository.addOrUpdateEvent(capture(eventSlot)) } just runs

        // When
        alertPresenter.start()
        // Then
        assertThat(eventSlot.captured.payload.alertType)
            .isEqualTo(INTEGRITY_SERVICE_ERROR)
    }
    @Test
    fun `start the presenter with alertType GOOGLE_PLAY_SERVICES_OUTDATED shows red screen and saves AlertScreenEvent of GOOGLE_PLAY_SERVICES_OUTDATED type`() {
        // Given
        val alertType = AlertType.GOOGLE_PLAY_SERVICES_OUTDATED
        alertPresenter = AlertPresenter(
            view,
            alertType,
            eventRepository,
            configManager,
            timeHelper,
            exitFormHelper,
            CoroutineScope(testCoroutineRule.testCoroutineDispatcher)
        )

        val eventSlot = slot<AlertScreenEvent>()
        coEvery { eventRepository.addOrUpdateEvent(capture(eventSlot)) } just runs

        // When
        alertPresenter.start()

        // Then
        assertThat(eventSlot.captured.payload.alertType)
            .isEqualTo(AlertScreenEventType.GOOGLE_PLAY_SERVICES_OUTDATED)
        verify {
            view.getColorForColorRes(R.color.simprints_red)
        }
    }

    @Test
    fun `start the presenter with alertType MISSING_GOOGLE_PLAY_SERVICES shows red screen and saves AlertScreenEvent of MISSING_GOOGLE_PLAY_SERVICES type`() {
        // Given
        val alertType = AlertType.MISSING_GOOGLE_PLAY_SERVICES
        alertPresenter = AlertPresenter(
            view,
            alertType,
            eventRepository,
            configManager,
            timeHelper,
            exitFormHelper,
            externalScope
        )
        val eventSlot = slot<AlertScreenEvent>()
        coEvery { eventRepository.addOrUpdateEvent(capture(eventSlot)) } just runs

        // When
        alertPresenter.start()

        // Then
        assertThat(eventSlot.captured.payload.alertType)
            .isEqualTo(AlertScreenEventType.MISSING_GOOGLE_PLAY_SERVICES)
        verify {
            view.getColorForColorRes(R.color.simprints_red)
        }
    }
    @Test
    fun `start the presenter with alertType MissingOrOutdatedGooglePlayStoreApp shows gray screen and saves AlertScreenEvent of MissingOrOutdatedGooglePlayStoreApp type`() {
        // Given
        val alertType = AlertType.MissingOrOutdatedGooglePlayStoreApp
        alertPresenter = AlertPresenter(
            view,
            alertType,
            eventRepository,
            configManager,
            timeHelper,
            exitFormHelper,
            externalScope
        )
        val eventSlot = slot<AlertScreenEvent>()
        coEvery { eventRepository.addOrUpdateEvent(capture(eventSlot)) } just runs

        // When
        alertPresenter.start()

        // Then
        assertThat(eventSlot.captured.payload.alertType)
            .isEqualTo(AlertScreenEventType.MissingOrOutdatedGooglePlayStoreApp)
        verify {
            view.getColorForColorRes(R.color.simprints_grey)
        }
    }
}
