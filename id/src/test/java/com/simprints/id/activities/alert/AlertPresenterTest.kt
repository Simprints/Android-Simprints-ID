package com.simprints.id.activities.alert

import com.google.common.truth.Truth
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.models.AlertScreenEvent
import com.simprints.eventsystem.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.PLAY_INTEGRITY_ERROR
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.exitformhandler.ExitFormHelper
import com.simprints.infra.config.ConfigManager
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.coroutines.TestDispatcherProvider
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.CoroutineScope
import org.junit.Before
import org.junit.Rule
import org.junit.Test


internal class AlertPresenterTest {


    private val view: AlertContract.View = mockk()
    private val eventRepository: EventRepository = mockk()
    private val configManager: ConfigManager = mockk()
    private val timeHelper: TimeHelper = mockk()
    private val exitFormHelper: ExitFormHelper = mockk()

    lateinit var alertPresenter: AlertPresenter

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @Before
    fun setUp() {
        mockkStatic("com.simprints.id.domain.alert.AlertTypeKt")
    }

    @Test
    fun `test start with PLAY_INTEGRITY_ERROR should add the correct event in eventRepositry`() {
        //Given
        val alertType: AlertType = AlertType.PLAY_INTEGRITY_ERROR
        alertPresenter = AlertPresenter(
            view,
            alertType,
            eventRepository,
            configManager,
            timeHelper,
            exitFormHelper,
            CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
            TestDispatcherProvider(testCoroutineRule).io()
        )
        var event = mockk<AlertScreenEvent>()

        coEvery { eventRepository.addOrUpdateEvent(any()) } answers {
            event = args[0] as AlertScreenEvent
        }
        // When
        alertPresenter.start()
        // Then
        Truth.assertThat(event.payload.alertType)
            .isEqualTo(PLAY_INTEGRITY_ERROR)
    }
}
