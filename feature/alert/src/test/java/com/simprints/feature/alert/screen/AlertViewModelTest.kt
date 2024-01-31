package com.simprints.feature.alert.screen

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.AlertScreenEvent
import com.simprints.infra.events.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class AlertViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var eventRepository: EventRepository

    lateinit var alertViewModel: AlertViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        alertViewModel = AlertViewModel(
            timeHelper,
            eventRepository,
            CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
        )
    }

    @Test
    fun saveAlertEvent() = runTest {
        every { timeHelper.nowTimestamp() } returns Timestamp(42)

        alertViewModel.saveAlertEvent(AlertScreenEventType.DISCONNECTED)

        coVerify {
            eventRepository.addOrUpdateEvent(withArg {
                val payload = it.payload as AlertScreenEvent.AlertScreenPayload

                assertThat(payload.createdAt.ms).isEqualTo(42)
                assertThat(payload.type).isEqualTo(EventType.ALERT_SCREEN)
                assertThat(payload.alertType).isEqualTo(AlertScreenEventType.DISCONNECTED)
            })
        }
    }
}
