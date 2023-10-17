package com.simprints.fingerprint.activities.connect.issues.ota

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.activities.connect.result.FetchOtaResult
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.ScannerFirmwareUpdateEvent
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.infra.scanner.ScannerManager
import com.simprints.fingerprint.infra.scanner.domain.ota.AvailableOta
import com.simprints.fingerprint.infra.scanner.domain.ota.CypressOtaStep
import com.simprints.fingerprint.infra.scanner.domain.ota.OtaRecoveryStrategy
import com.simprints.fingerprint.infra.scanner.domain.ota.StmOtaStep
import com.simprints.fingerprint.infra.scanner.domain.ota.Un20OtaStep
import com.simprints.fingerprint.infra.scanner.exceptions.safe.OtaFailedException
import com.simprints.fingerprint.infra.scanner.wrapper.ScannerOtaOperationsWrapper
import com.simprints.fingerprint.testtools.assertEventReceived
import com.simprints.fingerprint.testtools.assertEventReceivedWithContent
import com.simprints.fingerprint.testtools.assertEventReceivedWithContentAssertions
import com.simprints.fingerprint.testtools.assertEventWithContentNeverReceived
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.Vero2Configuration
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.testObserver
import com.simprints.testtools.common.mock.MockTimer
import io.mockk.CapturingSlot
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class OtaViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val sessionEventsManagerMock: FingerprintSessionEventsManager = mockk(relaxed = true)
    private val mockTimer = MockTimer()
    private val timeHelperMock: FingerprintTimeHelper = mockk(relaxed = true) {
        every { newTimer() } returns mockTimer
    }
    private val scannerOtaWrapper: ScannerOtaOperationsWrapper = mockk()
    private val scannerManager: ScannerManager = mockk {
        every { otaOperationsWrapper } returns scannerOtaWrapper
    }

    private val recentUserActivityManager = mockk<RecentUserActivityManager> {
        coEvery { getRecentUserActivity() } returns RecentUserActivity(
            HARDWARE_VERSION,
            "",
            "",
            0,
            0,
            0,
            0
        )
    }
    private val configManager = mockk<ConfigManager> {
        coEvery { getProjectConfiguration() } returns mockk {
            every { fingerprint } returns mockk {
                every { vero2 } returns mockk {
                    every { firmwareVersions } returns mapOf(
                        HARDWARE_VERSION to Vero2Configuration.Vero2FirmwareVersions(
                            NEW_CYPRESS_VERSION,
                            NEW_STM_VERSION,
                            NEW_UN20_VERSION
                        )
                    )
                }
            }
        }
    }

    private lateinit var otaViewModel: OtaViewModel

    @Before
    fun setup() {
        every { scannerOtaWrapper.performCypressOta(any()) } returns CYPRESS_OTA_STEPS.asFlow()
        every { scannerOtaWrapper.performStmOta(any()) } returns STM_OTA_STEPS.asFlow()
        every { scannerOtaWrapper.performUn20Ota(any()) } returns UN20_OTA_STEPS.asFlow()

        otaViewModel = OtaViewModel(
            scannerManager,
            sessionEventsManagerMock,
            timeHelperMock,
            recentUserActivityManager,
            configManager
        )
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
        coEvery { scannerOtaWrapper.performStmOta(any()) } returns flow {
            emit(STM_OTA_STEPS[0])
            throw OtaFailedException("oops")
        }

        otaViewModel.startOta(listOf(AvailableOta.CYPRESS, AvailableOta.STM, AvailableOta.UN20), 0)

        verify(exactly = 2) { sessionEventsManagerMock.addEventInBackground(any()) }
        assertThat(capturedEvents[1].chip).isEqualTo("stm")
        assertThat(capturedEvents[1].targetAppVersion).isEqualTo(NEW_STM_STRING)
        assertThat(capturedEvents[1].failureReason).isNotEmpty()

        otaViewModel.otaRecovery.assertEventReceivedWithContentAssertions {
            assertThat(it.recoveryStrategy).isEqualTo(OtaRecoveryStrategy.HARD_RESET)
            assertThat(it.remainingOtas).isEqualTo(listOf(AvailableOta.STM, AvailableOta.UN20))
        }
    }

    @Test
    fun otaFailsLastAttempt_otaException_correctlyUpdatesStateAndSavesEvent() {
        val capturedEvents = mutableListOf<ScannerFirmwareUpdateEvent>()
        every { sessionEventsManagerMock.addEventInBackground(capture(capturedEvents)) } returns Unit
        coEvery { scannerOtaWrapper.performStmOta(any()) } returns flow {
            emit(STM_OTA_STEPS[0])
            throw OtaFailedException("oops")
        }

        otaViewModel.startOta(
            listOf(AvailableOta.CYPRESS, AvailableOta.STM, AvailableOta.UN20),
            OtaViewModel.MAX_RETRY_ATTEMPTS
        )

        verify(exactly = 2) { sessionEventsManagerMock.addEventInBackground(any()) }
        assertThat(capturedEvents[1].chip).isEqualTo("stm")
        assertThat(capturedEvents[1].targetAppVersion).isEqualTo(NEW_STM_STRING)
        assertThat(capturedEvents[1].failureReason).isNotEmpty()

        otaViewModel.otaFailed.assertEventReceivedWithContent(FetchOtaResult())
    }

    @Test
    fun otaFailsLastAttempt_backendMaintenanceException_correctlyUpdatesStateAndSavesEvent() {
        val capturedEvents = mutableListOf<ScannerFirmwareUpdateEvent>()
        every { sessionEventsManagerMock.addEventInBackground(capture(capturedEvents)) } returns Unit
        coEvery { scannerOtaWrapper.performStmOta(any()) } returns flow {
            emit(STM_OTA_STEPS[0])
            throw BackendMaintenanceException(estimatedOutage = null)
        }

        otaViewModel.startOta(
            listOf(AvailableOta.CYPRESS, AvailableOta.STM, AvailableOta.UN20),
            OtaViewModel.MAX_RETRY_ATTEMPTS
        )

        verify(exactly = 2) { sessionEventsManagerMock.addEventInBackground(any()) }
        assertThat(capturedEvents[1].chip).isEqualTo("stm")
        assertThat(capturedEvents[1].targetAppVersion).isEqualTo(NEW_STM_STRING)
        assertThat(capturedEvents[1].failureReason).isNotEmpty()

        otaViewModel.otaFailed.assertEventReceivedWithContent(FetchOtaResult(isMaintenanceMode = true))
    }

    @Test
    fun otaFailsFirstAttempt_otaExceptionRequiringDelay_correctlyDelaysThenUpdatesStateAndSavesEvent() {
        val capturedEvents = mutableListOf<ScannerFirmwareUpdateEvent>()
        every { sessionEventsManagerMock.addEventInBackground(capture(capturedEvents)) } returns Unit
        coEvery { scannerOtaWrapper.performUn20Ota(any()) } returns flow {
            emit(Un20OtaStep.AwaitingCacheCommit)
            throw OtaFailedException("oops")
        }

        otaViewModel.startOta(listOf(AvailableOta.CYPRESS, AvailableOta.STM, AvailableOta.UN20), 0)

        verify(exactly = 3) { sessionEventsManagerMock.addEventInBackground(any()) }
        assertThat(capturedEvents[2].chip).isEqualTo("un20")
        assertThat(capturedEvents[2].targetAppVersion).isEqualTo(NEW_UN20_STRING)
        assertThat(capturedEvents[2].failureReason).isNotEmpty()

        otaViewModel.otaRecovery.assertEventWithContentNeverReceived()

        mockTimer.executeNextTask()

        otaViewModel.otaRecovery.assertEventReceivedWithContentAssertions {
            assertThat(it.recoveryStrategy).isEqualTo(OtaRecoveryStrategy.SOFT_RESET)
            assertThat(it.remainingOtas).isEqualTo(listOf(AvailableOta.UN20))
        }
    }

    private fun Iterable<Float?>.assertAlmostContainsExactlyElementsIn(
        expected: Iterable<Float>,
        threshold: Float = 1e-5f
    ) {
        this.zip(expected).forEach { (a, b) -> assertThat(a).isWithin(threshold).of(b) }
    }

    companion object {
        private const val HARDWARE_VERSION = "E-1"
        private const val NEW_CYPRESS_STRING = "5.E-1.1"
        private const val NEW_STM_STRING = "4.E-1.7"
        private const val NEW_UN20_STRING = "8.E-1.0"

        private const val NEW_CYPRESS_VERSION = "5.E-1.1"
        private const val NEW_STM_VERSION = "4.E-1.7"
        private const val NEW_UN20_VERSION = "8.E-1.0"

        private val OTA_PROGRESS_VALUES = listOf(0.0f, 0.2f, 0.4f, 0.6f, 0.8f, 1.0f)

        private val CYPRESS_OTA_STEPS =
            listOf(CypressOtaStep.EnteringOtaMode, CypressOtaStep.CommencingTransfer) +
                OTA_PROGRESS_VALUES.map { CypressOtaStep.TransferInProgress(it) } +
                listOf(
                    CypressOtaStep.ReconnectingAfterTransfer,
                    CypressOtaStep.ValidatingNewFirmwareVersion,
                    CypressOtaStep.UpdatingUnifiedVersionInformation
                )

        private val STM_OTA_STEPS = listOf(
            StmOtaStep.EnteringOtaModeFirstTime, StmOtaStep.ReconnectingAfterEnteringOtaMode,
            StmOtaStep.EnteringOtaModeSecondTime, StmOtaStep.CommencingTransfer
        ) +
            OTA_PROGRESS_VALUES.map { StmOtaStep.TransferInProgress(it) } +
            listOf(
                StmOtaStep.ReconnectingAfterTransfer,
                StmOtaStep.EnteringMainMode,
                StmOtaStep.ValidatingNewFirmwareVersion,
                StmOtaStep.ReconnectingAfterValidating,
                StmOtaStep.UpdatingUnifiedVersionInformation
            )

        private val UN20_OTA_STEPS =
            listOf(
                Un20OtaStep.EnteringMainMode,
                Un20OtaStep.TurningOnUn20BeforeTransfer,
                Un20OtaStep.CommencingTransfer
            ) +
                OTA_PROGRESS_VALUES.map { Un20OtaStep.TransferInProgress(it) } +
                listOf(
                    Un20OtaStep.AwaitingCacheCommit,
                    Un20OtaStep.TurningOffUn20AfterTransfer,
                    Un20OtaStep.TurningOnUn20AfterTransfer,
                    Un20OtaStep.ValidatingNewFirmwareVersion,
                    Un20OtaStep.ReconnectingAfterValidating,
                    Un20OtaStep.UpdatingUnifiedVersionInformation
                )
    }
}
