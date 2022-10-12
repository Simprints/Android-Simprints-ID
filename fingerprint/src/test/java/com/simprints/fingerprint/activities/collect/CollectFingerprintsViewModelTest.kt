package com.simprints.fingerprint.activities.collect

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.activities.collect.CollectFingerprintsViewModelTest.MockAcquireImageResult.OK
import com.simprints.fingerprint.activities.collect.CollectFingerprintsViewModelTest.MockCaptureFingerprintResponse.*
import com.simprints.fingerprint.activities.collect.domain.FingerPriorityDeterminer
import com.simprints.fingerprint.activities.collect.domain.StartingStateDeterminer
import com.simprints.fingerprint.activities.collect.state.*
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.image.FingerprintImageManager
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier
import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.fingerprint.scanner.domain.AcquireImageResponse
import com.simprints.fingerprint.scanner.domain.CaptureFingerprintResponse
import com.simprints.fingerprint.scanner.exceptions.safe.NoFingerDetectedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapper
import com.simprints.fingerprint.testtools.*
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.Vero2Configuration
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.mock.MockTimer
import com.simprints.testtools.unit.EncodingUtilsImplForTests
import io.mockk.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class CollectFingerprintsViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val mockTimer = MockTimer()
    private val timeHelper: FingerprintTimeHelper = mockk(relaxed = true) {
        every { newTimer() } returns mockTimer
    }
    private val sessionEventsManager: FingerprintSessionEventsManager = mockk(relaxed = true)
    private val vero2Configuration = mockk<Vero2Configuration> {
        every { displayLiveFeedback } returns false
        every { captureStrategy } returns Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1000_DPI
        every { imageSavingStrategy } returns Vero2Configuration.ImageSavingStrategy.NEVER
    }
    private val configManager = mockk<ConfigManager> {
        coEvery { getProjectConfiguration() } returns mockk {
            every { fingerprint } returns mockk {
                every { qualityThreshold } returns 60
                every { vero2 } returns vero2Configuration
            }
        }
    }
    private val scanner: ScannerWrapper = mockk<ScannerWrapper>(relaxUnitFun = true).apply {
        every { isLiveFeedbackAvailable() } returns false
        every { isImageTransferSupported() } returns true
    }
    private lateinit var scannerManager: ScannerManager

    private val imageManager: FingerprintImageManager = mockk(relaxed = true)

    private lateinit var vm: CollectFingerprintsViewModel

    private val mockDispatcher = mockk<DispatcherProvider> {
        every { main() } returns testCoroutineRule.testCoroutineDispatcher
        every { default() } returns testCoroutineRule.testCoroutineDispatcher
        every { io() } returns testCoroutineRule.testCoroutineDispatcher
    }

    @Before
    fun setUp() {
        mockBase64EncodingForSavingTemplateInSession()

        scannerManager = mockk(relaxed = true) {
            every { scanner } returns this@CollectFingerprintsViewModelTest.scanner
            every { isScannerAvailable } returns true
        }

        vm = CollectFingerprintsViewModel(
            scannerManager,
            configManager,
            imageManager,
            timeHelper,
            sessionEventsManager,
            FingerPriorityDeterminer(),
            StartingStateDeterminer(),
            EncodingUtilsImplForTests,
            testCoroutineRule.testCoroutineScope,
            mockDispatcher
        )
    }

    private fun mockBase64EncodingForSavingTemplateInSession() {
        mockkObject(EncodingUtilsImplForTests)
        every { EncodingUtilsImplForTests.byteArrayToBase64(any()) } returns "BASE64TEMPLATE"
    }

    @Test
    fun viewModel_start_beginsWithCorrectState() {
        vm.start(TWO_FINGERS_IDS)

        assertThat(vm.state()).isEqualTo(
            CollectFingerprintsState(
                fingerStates = TWO_FINGERS_IDS.map {
                    FingerState(
                        it,
                        listOf(CaptureState.NotCollected)
                    )
                },
                currentFingerIndex = 0,
                isAskingRescan = false,
                isShowingConfirmDialog = false,
                isShowingSplashScreen = false
            )
        )
    }

    @Test
    @ExperimentalTime
    fun shouldNot_launchAlertScreen_whenOngoingFingerScan_isCancelled() {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(NEVER_RETURNS)

        vm.start(TWO_FINGERS_IDS)

        // start and cancel finger scan
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()

        vm.launchAlert.assertEventWithContentNeverReceived()
    }

    @Test
    @ExperimentalTime
    fun scanPressed_noImageTransfer_updatesStateToScanningDuringScan() {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(NEVER_RETURNS)
        noImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()

        assertThat(vm.state().currentCaptureState()).isEqualTo(CaptureState.Scanning())
    }

    @Test
    fun `test scanner supports imagetransfer then isImageTransferRequired should be true`() {
        withImageTransfer()
        every { scanner.isImageTransferSupported() } returns true

        assertThat(vm.isImageTransferRequired()).isTrue()
    }

    @Test
    fun `test scanner doesn't support imageTransfer then isImageTransferRequired should be false`() {
        withImageTransfer()
        every { scanner.isImageTransferSupported() } returns false

        assertThat(vm.isImageTransferRequired()).isFalse()
    }

    @Test
    @ExperimentalTime
    fun scanPressed_withImageTransfer_updatesStateToTransferringImageAfterScan() {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(GOOD_SCAN)
        acquireImageResponses(MockAcquireImageResult.NEVER_RETURNS)
        withImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()

        assertThat(vm.state().currentCaptureState()).isEqualTo(
            CaptureState.TransferringImage(
                ScanResult(
                    GOOD_QUALITY,
                    TEMPLATE,
                    null,
                    60
                )
            )
        )
    }

    @Test
    @ExperimentalTime
    fun scanPressed_noImageTransfer_goodScan_updatesStatesCorrectlyAndCreatesEvent() {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(GOOD_SCAN)
        noImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()

        assertThat(vm.state().currentCaptureState()).isEqualTo(
            CaptureState.Collected(
                ScanResult(
                    GOOD_QUALITY,
                    TEMPLATE,
                    null,
                    60
                )
            )
        )
        vm.vibrate.assertEventReceived()

        mockTimer.executeNextTask()
        assertThat(vm.state().currentFingerIndex).isEqualTo(1)

        coVerify { sessionEventsManager.addEvent(any()) }
    }

    @Test
    @ExperimentalTime
    fun scanPressed_withImageTransfer_goodScan_updatesStatesCorrectlyAndCreatesEvent() {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(GOOD_SCAN)
        acquireImageResponses(OK)
        withImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()

        assertThat(vm.state().currentCaptureState()).isEqualTo(
            CaptureState.Collected(
                ScanResult(
                    GOOD_QUALITY,
                    TEMPLATE,
                    IMAGE,
                    60
                )
            )
        )
        vm.vibrate.assertEventReceived()
        coVerify { sessionEventsManager.addEvent(any()) }

        mockTimer.executeNextTask()
        assertThat(vm.state().currentFingerIndex).isEqualTo(1)
    }

    @Test
    @ExperimentalTime
    fun scanPressed_noImageTransfer_badScan_updatesStatesCorrectlyAndCreatesEvent() {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(BAD_SCAN)
        noImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()

        assertThat(vm.state().currentCaptureState()).isEqualTo(
            CaptureState.Collected(
                ScanResult(
                    BAD_QUALITY,
                    TEMPLATE,
                    null,
                    60
                ), 1
            )
        )
        vm.vibrate.assertEventReceived()
        coVerify { sessionEventsManager.addEvent(any()) }
    }

    @Test
    @ExperimentalTime
    fun scanPressed_withImageTransfer_badScan_doesNotTransferImage_updatesStatesCorrectlyAndCreatesEvent() {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(BAD_SCAN)
        withImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()

        assertThat(vm.state().currentCaptureState()).isEqualTo(
            CaptureState.Collected(
                ScanResult(
                    BAD_QUALITY,
                    TEMPLATE,
                    null,
                    60
                ), 1
            )
        )
        vm.vibrate.assertEventReceived()
        coVerify { sessionEventsManager.addEvent(any()) }
    }

    @Test
    @ExperimentalTime
    fun scanPressed_noFingerDetected_updatesStatesCorrectlyAndCreatesEvent() {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(NO_FINGER_DETECTED)
        withImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()

        assertThat(vm.state().currentCaptureState()).isEqualTo(CaptureState.NotDetected())
        vm.vibrate.assertEventReceived()
        coVerify { sessionEventsManager.addEvent(any()) }
    }

    @Test
    fun scanPressed_scannerDisconnectedDuringScan_updatesStateCorrectlyAndReconnects() {
        mockScannerSetUiIdle()
        coEvery {
            scanner.captureFingerprint(
                any(),
                any(),
                any()
            )
        } throws ScannerDisconnectedException()
        withImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()

        assertThat(vm.state().currentCaptureState()).isEqualTo(CaptureState.NotCollected)
        vm.launchReconnect.assertEventReceived()
    }

    @Test
    @ExperimentalTime
    fun scanPressed_scannerDisconnectedDuringTransfer_updatesStateCorrectlyAndReconnects() {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(GOOD_SCAN)
        acquireImageResponses(MockAcquireImageResult.DISCONNECTED)
        withImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()

        assertThat(vm.state().currentCaptureState()).isEqualTo(CaptureState.NotCollected)
        vm.launchReconnect.assertEventReceived()
    }

    @Test
    @ExperimentalTime
    fun badScanLastAttempt_withImageTransfer_transfersImageAndUpdatesStatesCorrectly() = runTest {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(BAD_SCAN)
        acquireImageResponses(OK)
        withImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()
        assertThat(vm.state().currentCaptureState()).isEqualTo(
            CaptureState.Collected(
                ScanResult(
                    BAD_QUALITY,
                    TEMPLATE,
                    null,
                    60
                ), 1
            )
        )
        vm.handleScanButtonPressed()
        assertThat(vm.state().currentCaptureState()).isEqualTo(
            CaptureState.Collected(
                ScanResult(
                    BAD_QUALITY,
                    TEMPLATE,
                    null,
                    60
                ), 2
            )
        )
        vm.handleScanButtonPressed()
        assertThat(vm.state().currentCaptureState()).isEqualTo(
            CaptureState.Collected(
                ScanResult(
                    BAD_QUALITY,
                    TEMPLATE,
                    IMAGE,
                    60
                ), 3
            )
        )

        assertThat(vm.state().isShowingSplashScreen).isTrue()
        mockTimer.executeNextTask()
        assertThat(vm.state().fingerStates.size).isEqualTo(3)
        mockTimer.executeNextTask()
        assertThat(vm.state().isShowingSplashScreen).isFalse()
        assertThat(vm.state().currentFingerIndex).isEqualTo(1)

        coVerify(exactly = 1) { scanner.acquireImage(any()) }
        coVerify(exactly = 3) { sessionEventsManager.addEvent(any()) }
    }

    @Test
    @ExperimentalTime
    fun receivesOnlyBadScansThenConfirm_performsImageTransferEventually_resultsInCorrectStateAndSavesFingersCorrectly() {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(BAD_SCAN)
        acquireImageResponses(OK)
        withImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        repeat(12) { // 3 times for each of the 4 fingers (2 original + 2 auto-added)
            vm.handleScanButtonPressed()
            mockTimer.executeAllTasks()
        }

        assertThat(vm.state()).isEqualTo(
            CollectFingerprintsState(
                fingerStates = FOUR_FINGERS_IDS.map {
                    FingerState(
                        it,
                        listOf(
                            CaptureState.Collected(
                                ScanResult(BAD_QUALITY, TEMPLATE, IMAGE, 60),
                                numberOfBadScans = 3
                            )
                        )
                    )
                },
                currentFingerIndex = 3,
                isAskingRescan = false,
                isShowingConfirmDialog = true,
                isShowingSplashScreen = false
            )
        )
        coVerify(exactly = 12) { sessionEventsManager.addEvent(any()) }

        vm.handleConfirmFingerprintsAndContinue()
        coVerify(exactly = 4) { imageManager.save(any(), any(), any()) }

        vm.finishWithFingerprints.assertEventReceivedWithContentAssertions { actualFingerprints ->
            assertThat(actualFingerprints).hasSize(FOUR_FINGERS_IDS.size)
            assertThat(actualFingerprints.map { it.fingerId }).containsExactlyElementsIn(
                FOUR_FINGERS_IDS
            )
            actualFingerprints.forEach {
                assertThat(it.templateBytes).isEqualTo(TEMPLATE)
                assertThat(it.imageRef).isNotNull()
            }
        }
    }

    @Test
    @ExperimentalTime
    fun receivesOnlyGoodScansThenConfirm_withImageTransfer_resultsInCorrectStateAndSavesFingersCorrectly() {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(GOOD_SCAN)
        acquireImageResponses(OK)
        withImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        repeat(2) {
            vm.handleScanButtonPressed()
            mockTimer.executeAllTasks()
        }

        assertThat(vm.state()).isEqualTo(
            CollectFingerprintsState(
                fingerStates = TWO_FINGERS_IDS.map {
                    FingerState(
                        it,
                        listOf(
                            CaptureState.Collected(
                                ScanResult(
                                    GOOD_QUALITY,
                                    TEMPLATE,
                                    IMAGE,
                                    60
                                )
                            )
                        )
                    )
                },
                currentFingerIndex = 1,
                isAskingRescan = false,
                isShowingConfirmDialog = true,
                isShowingSplashScreen = false
            )
        )
        coVerify(exactly = 4) { sessionEventsManager.addEvent(any()) }

        vm.handleConfirmFingerprintsAndContinue()
        coVerify(exactly = 2) { imageManager.save(any(), any(), any()) }

        vm.finishWithFingerprints.assertEventReceivedWithContentAssertions { actualFingerprints ->
            assertThat(actualFingerprints).hasSize(TWO_FINGERS_IDS.size)
            assertThat(actualFingerprints.map { it.fingerId }).containsExactlyElementsIn(
                TWO_FINGERS_IDS
            )
            actualFingerprints.forEach {
                assertThat(it.templateBytes).isEqualTo(TEMPLATE)
                assertThat(it.imageRef).isNotNull()
            }
        }
    }

    @Test
    @ExperimentalTime
    fun receivesOnlyGoodScansThenConfirm_noImageTransfer_resultsInCorrectStateAndReturnsFingersCorrectly() {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(GOOD_SCAN)
        noImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        repeat(2) {
            vm.handleScanButtonPressed()
            mockTimer.executeAllTasks()
        }

        assertThat(vm.state()).isEqualTo(
            CollectFingerprintsState(
                fingerStates = TWO_FINGERS_IDS.map {
                    FingerState(
                        it,
                        listOf(CaptureState.Collected(ScanResult(GOOD_QUALITY, TEMPLATE, null, 60)))
                    )
                },
                currentFingerIndex = 1,
                isAskingRescan = false,
                isShowingConfirmDialog = true,
                isShowingSplashScreen = false
            )
        )
        coVerify(exactly = 4) { sessionEventsManager.addEvent(any()) }

        vm.handleConfirmFingerprintsAndContinue()
        coVerify(exactly = 0) { imageManager.save(any(), any(), any()) }

        vm.finishWithFingerprints.assertEventReceivedWithContentAssertions { actualFingerprints ->
            assertThat(actualFingerprints).hasSize(TWO_FINGERS_IDS.size)
            assertThat(actualFingerprints.map { it.fingerId }).containsExactlyElementsIn(
                TWO_FINGERS_IDS
            )
            actualFingerprints.forEach {
                assertThat(it.templateBytes).isEqualTo(TEMPLATE)
                assertThat(it.imageRef).isNull()
            }
        }
    }

    @Test
    @ExperimentalTime
    fun goodScan_swipeBackThenRescan_updatesStateCorrectly() {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(GOOD_SCAN, DIFFERENT_GOOD_SCAN)
        noImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()
        mockTimer.executeNextTask()
        assertThat(vm.state().currentFingerIndex).isEqualTo(1)

        vm.updateSelectedFinger(0)
        assertThat(vm.state().currentFingerIndex).isEqualTo(0)

        vm.handleScanButtonPressed()
        assertThat(vm.state().isAskingRescan).isTrue()

        vm.handleScanButtonPressed()
        assertThat(vm.state().isAskingRescan).isFalse()
        assertThat(vm.state().currentCaptureState()).isEqualTo(
            CaptureState.Collected(
                ScanResult(
                    DIFFERENT_GOOD_QUALITY,
                    DIFFERENT_TEMPLATE,
                    null,
                    60
                )
            )
        )

        coVerify(exactly = 4) { sessionEventsManager.addEvent(any()) }
    }

    @Test
    fun missingFinger_updatesStateCorrectly() {
        mockScannerSetUiIdle()

        vm.start(TWO_FINGERS_IDS)
        vm.handleMissingFingerButtonPressed()
        assertThat(vm.state().currentCaptureState()).isEqualTo(CaptureState.Skipped)
        assertThat(vm.state().isShowingSplashScreen).isTrue()
        mockTimer.executeNextTask()
        assertThat(vm.state().fingerStates.size).isEqualTo(3)
        mockTimer.executeNextTask()
        assertThat(vm.state().isShowingSplashScreen).isFalse()
        assertThat(vm.state().currentFingerIndex).isEqualTo(1)

        coVerify { sessionEventsManager.addEvent(any()) }
    }

    @Test
    fun receivesOnlyMissingFingersThenConfirm_showsToastAndResetsState() {
        mockScannerSetUiIdle()

        vm.start(TWO_FINGERS_IDS)
        repeat(4) { // 2 original + 2 auto-added
            vm.handleMissingFingerButtonPressed()
            mockTimer.executeAllTasks()
        }

        assertThat(vm.state()).isEqualTo(
            CollectFingerprintsState(
                fingerStates = FOUR_FINGERS_IDS.map {
                    FingerState(
                        it,
                        listOf(CaptureState.Skipped)
                    )
                },
                currentFingerIndex = 3,
                isAskingRescan = false,
                isShowingConfirmDialog = true,
                isShowingSplashScreen = false
            )
        )
        coVerify(exactly = 4) { sessionEventsManager.addEvent(any()) }

        vm.handleConfirmFingerprintsAndContinue()

        vm.noFingersScannedToast.assertEventReceived()
        assertThat(vm.state()).isEqualTo(
            CollectFingerprintsState(
                fingerStates = TWO_FINGERS_IDS.map {
                    FingerState(
                        it,
                        listOf(CaptureState.NotCollected)
                    )
                },
                currentFingerIndex = 0,
                isAskingRescan = false,
                isShowingConfirmDialog = false,
                isShowingSplashScreen = false
            )
        )
    }

    @Test
    @ExperimentalTime
    fun receivesMixOfScanResults_withImageTransfer_updatesStateCorrectlyAndReturnsFingersCorrectly() {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(
            BAD_SCAN, NO_FINGER_DETECTED, BAD_SCAN, GOOD_SCAN,
            BAD_SCAN, NO_FINGER_DETECTED, NO_FINGER_DETECTED, // skipped
            NO_FINGER_DETECTED, BAD_SCAN, BAD_SCAN, BAD_SCAN,
            NO_FINGER_DETECTED, GOOD_SCAN
        )
        acquireImageResponses(OK)
        withImageTransfer()

        vm.start(TWO_FINGERS_IDS)

        // Finger 1
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        mockTimer.executeAllTasks()

        // Finger 2
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        vm.handleMissingFingerButtonPressed()
        mockTimer.executeAllTasks()

        // Finger 3
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        mockTimer.executeAllTasks()

        // Finger 4
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()

        assertThat(vm.state()).isEqualTo(
            CollectFingerprintsState(
                fingerStates = listOf(
                    FingerState(
                        FOUR_FINGERS_IDS[0],
                        listOf(
                            CaptureState.Collected(
                                ScanResult(GOOD_QUALITY, TEMPLATE, IMAGE, 60),
                                numberOfBadScans = 2
                            )
                        )
                    ),
                    FingerState(FOUR_FINGERS_IDS[1], listOf(CaptureState.Skipped)),
                    FingerState(
                        FOUR_FINGERS_IDS[2],
                        listOf(
                            CaptureState.Collected(
                                ScanResult(BAD_QUALITY, TEMPLATE, IMAGE, 60),
                                numberOfBadScans = 3
                            )
                        )
                    ),
                    FingerState(
                        FOUR_FINGERS_IDS[3],
                        listOf(
                            CaptureState.Collected(
                                ScanResult(GOOD_QUALITY, TEMPLATE, IMAGE, 60),
                                numberOfBadScans = 0
                            )
                        )
                    )
                ),
                currentFingerIndex = 3,
                isAskingRescan = false,
                isShowingConfirmDialog = true,
                isShowingSplashScreen = false
            )
        )
        coVerify(exactly = 16) { sessionEventsManager.addEvent(any()) }

        vm.handleConfirmFingerprintsAndContinue()
        coVerify(exactly = 3) { imageManager.save(any(), any(), any()) }

        vm.finishWithFingerprints.assertEventReceivedWithContentAssertions { actualFingerprints ->
            assertThat(actualFingerprints).hasSize(3)
            assertThat(actualFingerprints.map { it.fingerId }).containsExactly(
                FingerIdentifier.LEFT_THUMB,
                FingerIdentifier.RIGHT_THUMB,
                FingerIdentifier.RIGHT_INDEX_FINGER
            )
            actualFingerprints.forEach {
                assertThat(it.templateBytes).isEqualTo(TEMPLATE)
                assertThat(it.imageRef).isNotNull()
            }
        }
    }

    @Test
    @ExperimentalTime
    fun receivesMixOfScanResults_withEagerImageTransfer_updatesStateCorrectlyAndReturnsFingersCorrectly() {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(
            BAD_SCAN, NO_FINGER_DETECTED, BAD_SCAN, GOOD_SCAN,
            BAD_SCAN, NO_FINGER_DETECTED, NO_FINGER_DETECTED, // skipped
            NO_FINGER_DETECTED, BAD_SCAN, BAD_SCAN, BAD_SCAN,
            NO_FINGER_DETECTED, GOOD_SCAN
        )
        acquireImageResponses(OK)
        withImageTransfer(isEager = true)

        vm.start(TWO_FINGERS_IDS)

        // Finger 1
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        mockTimer.executeAllTasks()

        // Finger 2
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        vm.handleMissingFingerButtonPressed()
        mockTimer.executeAllTasks()

        // Finger 3
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        mockTimer.executeAllTasks()

        // Finger 4
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()

        assertThat(vm.state()).isEqualTo(
            CollectFingerprintsState(
                fingerStates = listOf(
                    FingerState(
                        FOUR_FINGERS_IDS[0],
                        listOf(
                            CaptureState.Collected(
                                ScanResult(GOOD_QUALITY, TEMPLATE, IMAGE, 60),
                                numberOfBadScans = 2
                            )
                        )
                    ),
                    FingerState(FOUR_FINGERS_IDS[1], listOf(CaptureState.Skipped)),
                    FingerState(
                        FOUR_FINGERS_IDS[2],
                        listOf(
                            CaptureState.Collected(
                                ScanResult(BAD_QUALITY, TEMPLATE, IMAGE, 60),
                                numberOfBadScans = 3
                            )
                        )
                    ),
                    FingerState(
                        FOUR_FINGERS_IDS[3],
                        listOf(
                            CaptureState.Collected(
                                ScanResult(GOOD_QUALITY, TEMPLATE, IMAGE, 60),
                                numberOfBadScans = 0
                            )
                        )
                    )
                ),
                currentFingerIndex = 3,
                isAskingRescan = false,
                isShowingConfirmDialog = true,
                isShowingSplashScreen = false
            )
        )
        coVerify(exactly = 16) { sessionEventsManager.addEvent(any()) }

        // If eager, expect that images were saved before confirm was pressed, including bad scans
        coVerify(exactly = 8) { imageManager.save(any(), any(), any()) }

        vm.handleConfirmFingerprintsAndContinue()

        vm.finishWithFingerprints.assertEventReceivedWithContentAssertions { actualFingerprints ->
            assertThat(actualFingerprints).hasSize(3)
            assertThat(actualFingerprints.map { it.fingerId }).containsExactly(
                FingerIdentifier.LEFT_THUMB,
                FingerIdentifier.RIGHT_THUMB,
                FingerIdentifier.RIGHT_INDEX_FINGER
            )
            actualFingerprints.forEach {
                assertThat(it.templateBytes).isEqualTo(TEMPLATE)
                assertThat(it.imageRef).isNotNull()
            }
        }
    }

    @Test
    @ExperimentalTime
    fun pressingCancel_duringScan_cancelsProperly() {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(NEVER_RETURNS)
        withImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()
        assertThat(vm.state().currentCaptureState()).isEqualTo(CaptureState.Scanning())
        vm.handleScanButtonPressed()
        assertThat(vm.state().currentCaptureState()).isEqualTo(CaptureState.NotCollected)
    }

    @Test
    @ExperimentalTime
    fun pressingCancel_duringImageTransfer_doesNothing() {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(GOOD_SCAN)
        acquireImageResponses(MockAcquireImageResult.NEVER_RETURNS)
        withImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()
        assertThat(vm.state().currentCaptureState()).isEqualTo(
            CaptureState.TransferringImage(
                ScanResult(
                    GOOD_QUALITY,
                    TEMPLATE,
                    null,
                    60
                )
            )
        )
        vm.handleScanButtonPressed()
        assertThat(vm.state().currentCaptureState()).isEqualTo(
            CaptureState.TransferringImage(
                ScanResult(
                    GOOD_QUALITY,
                    TEMPLATE,
                    null,
                    60
                )
            )
        )
    }

    @Test
    @ExperimentalTime
    fun restartAfterScans_resetsStateCorrectly() {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(GOOD_SCAN)
        acquireImageResponses(OK)
        withImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()
        mockTimer.executeAllTasks()
        vm.handleMissingFingerButtonPressed()
        mockTimer.executeAllTasks()
        vm.handleScanButtonPressed()
        mockTimer.executeAllTasks()

        assertThat(vm.state().isShowingConfirmDialog).isTrue()

        vm.handleRestart()

        assertThat(vm.state()).isEqualTo(
            CollectFingerprintsState(
                fingerStates = TWO_FINGERS_IDS.map {
                    FingerState(
                        it,
                        listOf(CaptureState.NotCollected)
                    )
                },
                currentFingerIndex = 0,
                isAskingRescan = false,
                isShowingConfirmDialog = false,
                isShowingSplashScreen = false
            )
        )
    }

    @Test
    @ExperimentalTime
    fun backPressed_whileScanning_cancelsScanning() {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(NEVER_RETURNS)
        withImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()
        assertThat(vm.state().currentCaptureState()).isEqualTo(CaptureState.Scanning())
        vm.handleOnBackPressed()
        assertThat(vm.state().currentCaptureState()).isEqualTo(CaptureState.NotCollected)
    }

    @Test
    fun shouldLaunch_reconnectActivity_whenScanner_isNotAvailable() {
        every { scannerManager.isScannerAvailable } returns false

        vm.start(TWO_FINGERS_IDS)

        assertThat(vm.state().currentCaptureState()).isEqualTo(CaptureState.NotCollected)
        vm.launchReconnect.assertEventReceived()
    }


    @Test
    @ExperimentalTime
    fun backPressed_whileTransferringImage_cancelsTransfer() {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(GOOD_SCAN)
        acquireImageResponses(MockAcquireImageResult.NEVER_RETURNS)
        withImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()
        assertThat(vm.state().currentCaptureState()).isEqualTo(
            CaptureState.TransferringImage(
                ScanResult(
                    GOOD_QUALITY,
                    TEMPLATE,
                    null,
                    60
                )
            )
        )
        vm.handleOnBackPressed()
        assertThat(vm.state().currentCaptureState()).isEqualTo(CaptureState.NotCollected)
    }

    @Test
    @ExperimentalTime
    fun backPressed_whileIdle_doesNothing() {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(GOOD_SCAN)
        noImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()
        vm.handleOnBackPressed()
        assertThat(vm.state().currentCaptureState()).isEqualTo(
            CaptureState.Collected(
                ScanResult(
                    GOOD_QUALITY,
                    TEMPLATE,
                    null,
                    60
                )
            )
        )
    }

    @Test
    @ExperimentalTime
    fun unexpectedErrorWhileScanning_launchesAlert() {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(UNKNOWN_ERROR)
        noImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()
        assertThat(vm.state().currentCaptureState()).isEqualTo(CaptureState.NotCollected)

        vm.launchAlert.assertEventReceivedWithContent(FingerprintAlert.UNEXPECTED_ERROR)
    }

    @Test
    fun onResumeCalled_registersScannerTrigger() {
        vm.start(TWO_FINGERS_IDS)
        vm.handleOnResume()

        verify { scanner.registerTriggerListener(any()) }
    }

    @Test
    fun onPauseCalled_unregistersScannerTrigger() {
        vm.start(TWO_FINGERS_IDS)
        vm.handleOnPause()

        verify { scanner.unregisterTriggerListener(any()) }
    }

    @Test
    fun whenStart_AndLiveFeedbackIsEnabled_liveFeedbackIsStarted() {
        setupLiveFeedbackOn()

        vm.start(TWO_FINGERS_IDS)

        coVerify { scanner.startLiveFeedback() }
        assertThat(vm.liveFeedbackState).isEqualTo(LiveFeedbackState.START)
    }

    @Test
    fun whenStart_AndLiveFeedbackIsNotEnabled_liveFeedbackIsNotStarted() {
        vm.start(TWO_FINGERS_IDS)

        coVerify(exactly = 0) { scanner.startLiveFeedback() }
    }

    @Test
    @ExperimentalTime
    fun whenScanButtonPressed_AndLiveFeedbackIsEnabled_liveFeedbackIsPaused() {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(NEVER_RETURNS)
        setupLiveFeedbackOn()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()

        assertThat(vm.liveFeedbackState).isEqualTo(LiveFeedbackState.PAUSE)
    }

    @Test
    @ExperimentalTime
    fun whenGoodScan_AndLiveFeedbackIsEnabled_liveFeedbackIsStarted() {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(GOOD_SCAN)
        setupLiveFeedbackOn()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()

        coVerify { scanner.startLiveFeedback() }
        assertThat(vm.liveFeedbackState).isEqualTo(LiveFeedbackState.START)
    }

    @Test
    @ExperimentalTime
    fun whenBadScan_AndLiveFeedbackIsEnabled_liveFeedbackIsStarted() {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(BAD_SCAN)
        setupLiveFeedbackOn()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()

        coVerify { scanner.startLiveFeedback() }
        assertThat(vm.liveFeedbackState).isEqualTo(LiveFeedbackState.START)
    }

    @Test
    @ExperimentalTime
    fun whenSecondScan_AndLiveFeedbackIsEnabled_liveFeedbackIsPaused() {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(GOOD_SCAN, NEVER_RETURNS)
        setupLiveFeedbackOn()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()
        mockTimer.executeAllTasks()
        vm.handleScanButtonPressed()

        assertThat(vm.liveFeedbackState).isEqualTo(LiveFeedbackState.PAUSE)
    }

    @Test
    @ExperimentalTime
    fun whenEndOfWorkflow_AndLiveFeedbackIsEnabled_liveFeedbackIsStopped() {
        mockScannerSetUiIdle()
        setupLiveFeedbackOn()

        getToEndOfWorkflow()

        coVerify { scanner.stopLiveFeedback() }
        assertThat(vm.liveFeedbackState).isEqualTo(LiveFeedbackState.STOP)
    }

    @Test
    fun whenRestart_AndLiveFeedbackIsEnabled_liveFeedbackIsStarted() {
        mockScannerSetUiIdle()
        setupLiveFeedbackOn()

        vm.start(TWO_FINGERS_IDS)
        vm.handleRestart()

        coVerify { scanner.startLiveFeedback() }
        assertThat(vm.liveFeedbackState).isEqualTo(LiveFeedbackState.START)
    }

    @Test
    fun whenPause_AndLiveFeedbackIsEnabled_liveFeedbackIsStopped() {
        setupLiveFeedbackOn()

        vm.start(TWO_FINGERS_IDS)
        vm.handleOnPause()

        coVerify { scanner.stopLiveFeedback() }
        assertThat(vm.liveFeedbackState).isEqualTo(LiveFeedbackState.STOP)
    }

    @Test
    fun whenResume_AndLiveFeedbackWasStarted_liveFeedbackIsStarted() {
        setupLiveFeedbackOn()

        vm.start(TWO_FINGERS_IDS)
        vm.handleOnPause()
        vm.handleOnResume()

        coVerify { scanner.startLiveFeedback() }
        assertThat(vm.liveFeedbackState).isEqualTo(LiveFeedbackState.START)
    }

    @Test
    @ExperimentalTime
    fun whenResume_AndLiveFeedbackWasStopped_liveFeedbackIsStopped() {
        mockScannerSetUiIdle()
        setupLiveFeedbackOn()

        getToEndOfWorkflow()
        vm.handleOnPause()
        vm.handleOnResume()

        coVerify { scanner.stopLiveFeedback() }
        assertThat(vm.liveFeedbackState).isEqualTo(LiveFeedbackState.STOP)
    }

    @ExperimentalTime
    private fun getToEndOfWorkflow() {
        setupCaptureFingerprintResponses(GOOD_SCAN)
        vm.start(TWO_FINGERS_IDS)
        repeat(2) {
            vm.handleScanButtonPressed()
            mockTimer.executeAllTasks()
        }
    }

    private fun noImageTransfer() {
        every { vero2Configuration.imageSavingStrategy } returns Vero2Configuration.ImageSavingStrategy.NEVER
    }

    private fun withImageTransfer(isEager: Boolean = false) {
        every { vero2Configuration.imageSavingStrategy } returns
            if (isEager) Vero2Configuration.ImageSavingStrategy.EAGER else Vero2Configuration.ImageSavingStrategy.ONLY_GOOD_SCAN
        coEvery { imageManager.save(any(), any(), any()) } returns mockk()
    }

    private fun mockScannerSetUiIdle() {
        coEvery { scanner.setUiIdle() } returns Unit
    }

    @ExperimentalTime
    private fun setupCaptureFingerprintResponses(vararg mockResponses: MockCaptureFingerprintResponse) {
        val initialMock = coEvery { scanner.captureFingerprint(any(), any(), any()) }
        val fingerprintResponses = mockResponses.map { it.toCaptureFingerprintResponse() }

        // capture the first response in the list
        val firstResponse = fingerprintResponses.first()
        val subsequentMock = when {
            mockResponses[0] == NEVER_RETURNS -> initialMock.coAnswers { neverReturnResponse() }
            firstResponse is Throwable -> initialMock.throws(firstResponse)
            else -> initialMock.returns(firstResponse as CaptureFingerprintResponse)
        }

        // capture subsequent responses except the first.
        fingerprintResponses.forEachIndexed { index, response ->
            // skip the first response
            if (index != 0) {
                when {
                    mockResponses[index] == NEVER_RETURNS -> subsequentMock.coAndThen { neverReturnResponse() }
                    response is Throwable -> subsequentMock.andThenThrows(response)
                    else -> subsequentMock.andThen(response as CaptureFingerprintResponse)
                }
            }
        }
    }

    @ExperimentalTime
    private fun acquireImageResponses(response: MockAcquireImageResult) {
        val mock = coEvery { scanner.acquireImage(any()) }
        when (response) {
            OK -> mock.returns(AcquireImageResponse(IMAGE))
            MockAcquireImageResult.DISCONNECTED -> mock.throws(ScannerDisconnectedException())
            MockAcquireImageResult.NEVER_RETURNS -> mock.coAnswers { neverReturnResponse() }
        }
    }

    @ExperimentalTime
    private suspend inline fun <reified T : Any> neverReturnResponse(): T {
        withContext(testCoroutineRule.testCoroutineDispatcher) {
            delay(Duration.INFINITE)
            throw Error("Should Never Return")
        }
    }

    private fun setupLiveFeedbackOn() {
        every { scanner.isLiveFeedbackAvailable() } returns true
        every { vero2Configuration.displayLiveFeedback } returns true
    }

    private enum class MockCaptureFingerprintResponse {
        GOOD_SCAN, DIFFERENT_GOOD_SCAN, BAD_SCAN, NO_FINGER_DETECTED, DISCONNECTED, UNKNOWN_ERROR, NEVER_RETURNS;


        fun toCaptureFingerprintResponse(): Any = when (this) {
            GOOD_SCAN -> CaptureFingerprintResponse(TEMPLATE, GOOD_QUALITY)
            DIFFERENT_GOOD_SCAN -> CaptureFingerprintResponse(
                DIFFERENT_TEMPLATE,
                DIFFERENT_GOOD_QUALITY
            )
            BAD_SCAN -> CaptureFingerprintResponse(TEMPLATE, BAD_QUALITY)
            NO_FINGER_DETECTED -> NoFingerDetectedException()
            DISCONNECTED -> ScannerDisconnectedException()
            UNKNOWN_ERROR -> Error("Oops!")
            NEVER_RETURNS -> {
                // runBlocking { delay(Duration.INFINITE) }
                Error("Nothing to return!")
            }
        }
    }

    private enum class MockAcquireImageResult {
        OK, DISCONNECTED, NEVER_RETURNS
    }

    companion object {
        val TWO_FINGERS_IDS =
            listOf(FingerIdentifier.LEFT_THUMB, FingerIdentifier.LEFT_INDEX_FINGER)
        val FOUR_FINGERS_IDS = listOf(
            FingerIdentifier.LEFT_THUMB,
            FingerIdentifier.LEFT_INDEX_FINGER,
            FingerIdentifier.RIGHT_THUMB,
            FingerIdentifier.RIGHT_INDEX_FINGER
        )

        const val GOOD_QUALITY = 80
        const val DIFFERENT_GOOD_QUALITY = 80
        const val BAD_QUALITY = 20

        val TEMPLATE = FingerprintGenerator.generateRandomFingerprint().templateBytes
        val DIFFERENT_TEMPLATE = FingerprintGenerator.generateRandomFingerprint().templateBytes

        val IMAGE = byteArrayOf(0x05, 0x06, 0x07, 0x08)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}
