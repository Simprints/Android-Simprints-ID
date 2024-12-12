package com.simprints.fingerprint.connect.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.events.event.domain.models.AlertScreenEvent
import com.simprints.infra.events.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ReportAlertScreenEventUseCaseTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var eventRepository: SessionEventRepository

    private lateinit var useCase: ReportAlertScreenEventUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { timeHelper.now() } returns Timestamp(0L)

        useCase = ReportAlertScreenEventUseCase(
            timeHelper,
            eventRepository,
            CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
        )
    }

    @Test
    fun `Reporting BT not enabled correctly`() {
        useCase.reportBluetoothNotEnabled()
        verifyAlertType(AlertScreenEventType.BLUETOOTH_NOT_ENABLED)
    }

    @Test
    fun `Reporting nfc not enabled correctly`() {
        useCase.reportNfcNotEnabled()
        verifyAlertType(AlertScreenEventType.NFC_NOT_ENABLED)
    }

    @Test
    fun `Reportingnfc pairing correctly`() {
        useCase.reportNfcPairing()
        verifyAlertType(AlertScreenEventType.NFC_PAIR)
    }

    @Test
    fun `Reporting serial entry correctly`() {
        useCase.reportSerialEntry()
        verifyAlertType(AlertScreenEventType.SERIAL_ENTRY_PAIR)
    }

    @Test
    fun `Reporting scanner off correctly`() {
        useCase.reportScannerOff()
        verifyAlertType(AlertScreenEventType.DISCONNECTED)
    }

    @Test
    fun `Reporting OTA correctly`() {
        useCase.reportOta()
        verifyAlertType(AlertScreenEventType.OTA)
    }

    @Test
    fun `ReportingOTA failed correctly`() {
        useCase.reportOtaFailed()
        verifyAlertType(AlertScreenEventType.OTA_FAILED)
    }

    @Test
    fun `Reporting OTA recovery correctly`() {
        useCase.reportOtaRecovery()
        verifyAlertType(AlertScreenEventType.OTA_RECOVERY)
    }

    private fun verifyAlertType(expected: AlertScreenEventType) = coVerify {
        eventRepository.addOrUpdateEvent(
            withArg<AlertScreenEvent> {
                assertThat(it.payload.alertType).isEqualTo(expected)
            },
        )
    }
}
