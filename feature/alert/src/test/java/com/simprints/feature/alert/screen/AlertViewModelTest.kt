package com.simprints.feature.alert.screen

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.events.SessionEventRepository
import com.simprints.infra.events.event.domain.models.AlertScreenEvent
import com.simprints.infra.events.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
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
    lateinit var eventRepository: SessionEventRepository

    @MockK
    lateinit var authStore: AuthStore

    lateinit var alertViewModel: AlertViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        alertViewModel = AlertViewModel(
            DEVICE_ID,
            timeHelper,
            eventRepository,
            authStore,
            CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
        )
    }

    @Test
    fun `alert event reported correctly`() = runTest {
        every { timeHelper.now() } returns Timestamp(42)

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

    @Test
    fun `exported data contains all the correct data`() = runTest {
        every { timeHelper.now() } returns Timestamp(42)
        every { authStore.signedInProjectId } returns PROJECT_ID
        every { authStore.signedInUserId } returns TokenizableString.Raw(USER_ID)
        coJustRun { eventRepository.addOrUpdateEvent(any()) }
        coEvery { eventRepository.getCurrentSessionScope().id } returns SCOPE_ID

        alertViewModel.saveAlertEvent(AlertScreenEventType.DISCONNECTED)
        val exportData = alertViewModel.collectExportData()

        assertThat(exportData).contains("42")
        assertThat(exportData).contains(DEVICE_ID)
        assertThat(exportData).contains(PROJECT_ID)
        assertThat(exportData).contains(USER_ID)
        assertThat(exportData).contains(SCOPE_ID)
        assertThat(exportData).contains(AlertScreenEventType.DISCONNECTED.name)
    }

    companion object {

        private const val DEVICE_ID = "device-id"
        private const val PROJECT_ID = "project-id"
        private const val USER_ID = "user-id"
        private const val SCOPE_ID = "scope-id"
    }
}
