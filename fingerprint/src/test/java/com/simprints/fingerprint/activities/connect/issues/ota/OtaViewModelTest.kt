package com.simprints.fingerprint.activities.connect.issues.ota

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.commontesttools.time.MockTimer
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.ScannerFirmwareUpdateEvent
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.scanner.ScannerManagerImpl
import com.simprints.fingerprint.scanner.data.FirmwareFileManager
import com.simprints.fingerprint.scanner.domain.ota.*
import com.simprints.fingerprint.scanner.domain.versions.ChipFirmwareVersion
import com.simprints.fingerprint.scanner.domain.versions.ScannerFirmwareVersions
import com.simprints.fingerprint.scanner.exceptions.safe.OtaFailedException
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapper
import com.simprints.fingerprint.testtools.FullUnitTestConfigRule
import com.simprints.fingerprint.testtools.assertEventReceived
import com.simprints.fingerprint.testtools.assertEventReceivedWithContentAssertions
import com.simprints.fingerprint.testtools.assertEventWithContentNeverReceived
import com.simprints.testtools.common.livedata.testObserver
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class OtaViewModelTest {

    @get:Rule
    var unitTestConfigRule = FullUnitTestConfigRule()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val firmwareFileManager: FirmwareFileManager = mockk()
    private val sessionEventsManagerMock: FingerprintSessionEventsManager = mockk(relaxed = true)
    private val crashReportManagerMock: FingerprintCrashReportManager = mockk(relaxed = true)
    private val mockTimer = MockTimer()
    private val timeHelperMock: FingerprintTimeHelper = mockk(relaxed = true) {
        every { newTimer() } returns mockTimer
    }
    private val scannerMock: ScannerWrapper = mockk()
    private val scannerManager = ScannerManagerImpl(mockk(), mockk(), mockk(), mockk()).also {
        it.scanner = scannerMock
    }

    private val otaViewModel = OtaViewModel(scannerManager, firmwareFileManager, sessionEventsManagerMock, crashReportManagerMock, timeHelperMock)

    @Before
    fun setup() {
        every { firmwareFileManager.getAvailableScannerFirmwareVersions() } returns
            ScannerFirmwareVersions(cypress = NEW_CYPRESS_VERSION, stm = NEW_STM_VERSION, un20 = NEW_UN20_VERSION)
        every { scannerMock.performCypressOta() } returns Observable.fromIterable(CYPRESS_OTA_STEPS)
        every { scannerMock.performStmOta() } returns Observable.fromIterable(STM_OTA_STEPS)
        every { scannerMock.performUn20Ota() } returns Observable.fromIterable(UN20_OTA_STEPS)
    }

    @Test
    fun oneOta_updatesStateCorrectlyAndSavesEvent() {
        val progressObserver = otaViewModel.progress.testObserver()

        otaViewModel.startOta(listOf(AvailableOta.CYPRESS), 0)

        progressObserver.observedValues.assertAlmostContainsExactlyElementsIn(
            listOf(0f) + CYPRESS_OTA_STEPS.map { it.totalProgress } + listOf(1f))
        otaViewModel.otaComplete.assertEventReceived()

        val savedEvent = CapturingSlot<ScannerFirmwareUpdateEvent>()
        verify { sessionEventsManagerMock.addEventInBackground(capture(savedEvent)) }
        assertThat(savedEvent.captured.chip).isEqualTo("cypress")
        assertThat(savedEvent.captured.targetAppVersion).isEqualTo(NEW_CYPRESS_STRING)
        assertThat(savedEvent.captured.failureReason).isNull()
    }

    @Test
    fun multipleOtas_updatesStateCorrectlyAndSavesEvents() {
        val capturedEvents = mutableListOf<ScannerFirmwareUpdateEvent>()
        every { sessionEventsManagerMock.addEventInBackground(capture(capturedEvents)) } returns Unit
        val progressObserver = otaViewModel.progress.testObserver()

        otaViewModel.startOta(listOf(AvailableOta.CYPRESS, AvailableOta.STM, AvailableOta.UN20), 0)

        progressObserver.observedValues.assertAlmostContainsExactlyElementsIn(listOf(0f) +
            CYPRESS_OTA_STEPS.map { it.totalProgress / 3f } +
            STM_OTA_STEPS.map { (it.totalProgress + 1f) / 3f } +
            UN20_OTA_STEPS.map { (it.totalProgress + 2f) / 3f } +
            listOf(1f))
        otaViewModel.otaComplete.assertEventReceived()

        assertThat(capturedEvents.size).isEqualTo(3)
        assertThat(capturedEvents[0].chip).isEqualTo("cypress")
        assertThat(capturedEvents[0].targetAppVersion).isEqualTo(NEW_CYPRESS_STRING)
        assertThat(capturedEvents[0].failureReason).isNull()
        assertThat(capturedEvents[1].chip).isEqualTo("stm")
        assertThat(capturedEvents[1].targetAppVersion).isEqualTo(NEW_STM_STRING)
        assertThat(capturedEvents[1].failureReason).isNull()
        assertThat(capturedEvents[2].chip).isEqualTo("un20")
        assertThat(capturedEvents[2].targetAppVersion).isEqualTo(NEW_UN20_STRING)
        assertThat(capturedEvents[2].failureReason).isNull()
    }

    @Test
    fun otaFailsFirstAttempt_otaException_correctlyUpdatesStateAndSavesEvent() {
        val capturedEvents = mutableListOf<ScannerFirmwareUpdateEvent>()
        every { sessionEventsManagerMock.addEventInBackground(capture(capturedEvents)) } returns Unit
        every { scannerMock.performStmOta() } returns Observable.concat(Observable.just(STM_OTA_STEPS[0]), Observable.error(OtaFailedException("oops")))

        otaViewModel.startOta(listOf(AvailableOta.CYPRESS, AvailableOta.STM, AvailableOta.UN20), 0)

        verify(exactly = 2) { sessionEventsManagerMock.addEventInBackground(any()) }
        assertThat(capturedEvents[1].chip).isEqualTo("stm")
        assertThat(capturedEvents[1].targetAppVersion).isEqualTo(NEW_STM_STRING)
        assertThat(capturedEvents[1].failureReason).isNotEmpty()

        verify { crashReportManagerMock.logExceptionOrSafeException(ofType<OtaFailedException>()) }

        otaViewModel.otaRecovery.assertEventReceivedWithContentAssertions {
            assertThat(it.recoveryStrategy).isEqualTo(OtaRecoveryStrategy.HARD_RESET)
            assertThat(it.remainingOtas).isEqualTo(listOf(AvailableOta.STM, AvailableOta.UN20))
        }
    }

    @Test
    fun otaFailsLastAttempt_otaException_correctlyUpdatesStateAndSavesEvent() {
        val capturedEvents = mutableListOf<ScannerFirmwareUpdateEvent>()
        every { sessionEventsManagerMock.addEventInBackground(capture(capturedEvents)) } returns Unit
        every { scannerMock.performStmOta() } returns Observable.concat(Observable.just(STM_OTA_STEPS[0]), Observable.error(OtaFailedException("oops")))

        otaViewModel.startOta(listOf(AvailableOta.CYPRESS, AvailableOta.STM, AvailableOta.UN20), OtaViewModel.MAX_RETRY_ATTEMPTS)

        verify(exactly = 2) { sessionEventsManagerMock.addEventInBackground(any()) }
        assertThat(capturedEvents[1].chip).isEqualTo("stm")
        assertThat(capturedEvents[1].targetAppVersion).isEqualTo(NEW_STM_STRING)
        assertThat(capturedEvents[1].failureReason).isNotEmpty()

        verify { crashReportManagerMock.logExceptionOrSafeException(ofType<OtaFailedException>()) }

        otaViewModel.otaFailed.assertEventReceived()
    }

    @Test
    fun otaFailsFirstAttempt_otaExceptionRequiringDelay_correctlyDelaysThenUpdatesStateAndSavesEvent() {
        val capturedEvents = mutableListOf<ScannerFirmwareUpdateEvent>()
        every { sessionEventsManagerMock.addEventInBackground(capture(capturedEvents)) } returns Unit
        every { scannerMock.performUn20Ota() } returns Observable.concat(Observable.just(Un20OtaStep.AwaitingCacheCommit), Observable.error(OtaFailedException("oops")))

        otaViewModel.startOta(listOf(AvailableOta.CYPRESS, AvailableOta.STM, AvailableOta.UN20), 0)

        verify(exactly = 3) { sessionEventsManagerMock.addEventInBackground(any()) }
        assertThat(capturedEvents[2].chip).isEqualTo("un20")
        assertThat(capturedEvents[2].targetAppVersion).isEqualTo(NEW_UN20_STRING)
        assertThat(capturedEvents[2].failureReason).isNotEmpty()

        verify { crashReportManagerMock.logExceptionOrSafeException(ofType<OtaFailedException>()) }

        otaViewModel.otaRecovery.assertEventWithContentNeverReceived()

        mockTimer.executeNextTask()

        otaViewModel.otaRecovery.assertEventReceivedWithContentAssertions {
            assertThat(it.recoveryStrategy).isEqualTo(OtaRecoveryStrategy.SOFT_RESET)
            assertThat(it.remainingOtas).isEqualTo(listOf(AvailableOta.UN20))
        }
    }

    private fun Iterable<Float?>.assertAlmostContainsExactlyElementsIn(expected: Iterable<Float>, threshold: Float = 1e-5f) {
        this.zip(expected).forEach { (a, b) -> assertThat(a).isWithin(threshold).of(b) }
    }

    companion object {
        private const val NEW_CYPRESS_STRING = "5.1"
        private const val NEW_STM_STRING = "4.7"
        private const val NEW_UN20_STRING = "8.0"

        private val NEW_CYPRESS_VERSION = ChipFirmwareVersion(5, 1)
        private val NEW_STM_VERSION = ChipFirmwareVersion(4, 7)
        private val NEW_UN20_VERSION = ChipFirmwareVersion(8, 0)

        private val OTA_PROGRESS_VALUES = listOf(0.0f, 0.2f, 0.4f, 0.6f, 0.8f, 1.0f)

        private val CYPRESS_OTA_STEPS = listOf(CypressOtaStep.EnteringOtaMode, CypressOtaStep.CommencingTransfer) +
            OTA_PROGRESS_VALUES.map { CypressOtaStep.TransferInProgress(it) } +
            listOf(CypressOtaStep.ReconnectingAfterTransfer, CypressOtaStep.ValidatingNewFirmwareVersion, CypressOtaStep.UpdatingUnifiedVersionInformation)

        private val STM_OTA_STEPS = listOf(StmOtaStep.EnteringOtaModeFirstTime, StmOtaStep.ReconnectingAfterEnteringOtaMode,
            StmOtaStep.EnteringOtaModeSecondTime, StmOtaStep.CommencingTransfer) +
            OTA_PROGRESS_VALUES.map { StmOtaStep.TransferInProgress(it) } +
            listOf(StmOtaStep.ReconnectingAfterTransfer, StmOtaStep.EnteringMainMode, StmOtaStep.ValidatingNewFirmwareVersion,
                StmOtaStep.ReconnectingAfterValidating, StmOtaStep.UpdatingUnifiedVersionInformation)

        private val UN20_OTA_STEPS = listOf(Un20OtaStep.EnteringMainMode, Un20OtaStep.TurningOnUn20BeforeTransfer, Un20OtaStep.CommencingTransfer) +
            OTA_PROGRESS_VALUES.map { Un20OtaStep.TransferInProgress(it) } +
            listOf(Un20OtaStep.AwaitingCacheCommit, Un20OtaStep.TurningOffUn20AfterTransfer, Un20OtaStep.TurningOnUn20AfterTransfer,
                Un20OtaStep.ValidatingNewFirmwareVersion, Un20OtaStep.ReconnectingAfterValidating, Un20OtaStep.UpdatingUnifiedVersionInformation)
    }
}
