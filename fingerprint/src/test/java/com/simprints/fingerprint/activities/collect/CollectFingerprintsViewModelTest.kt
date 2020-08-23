package com.simprints.fingerprint.activities.collect

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.EncodingUtils
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.activities.collect.CollectFingerprintsViewModelTest.MockAcquireImageResult.OK
import com.simprints.fingerprint.activities.collect.CollectFingerprintsViewModelTest.MockCaptureFingerprintResponse.*
import com.simprints.fingerprint.activities.collect.state.CollectFingerprintsState
import com.simprints.fingerprint.activities.collect.state.CaptureState
import com.simprints.fingerprint.activities.collect.state.ScanResult
import com.simprints.fingerprint.commontesttools.generators.FingerprintGenerator
import com.simprints.fingerprint.commontesttools.time.MockTimer
import com.simprints.fingerprint.controllers.core.analytics.FingerprintAnalyticsManager
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.image.FingerprintImageManager
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier
import com.simprints.fingerprint.data.domain.images.SaveFingerprintImagesStrategy
import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.fingerprint.scanner.ScannerManagerImpl
import com.simprints.fingerprint.scanner.domain.AcquireImageResponse
import com.simprints.fingerprint.scanner.domain.CaptureFingerprintResponse
import com.simprints.fingerprint.scanner.exceptions.safe.NoFingerDetectedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapper
import com.simprints.fingerprint.testtools.FullUnitTestConfigRule
import com.simprints.fingerprint.testtools.assertEventReceived
import com.simprints.fingerprint.testtools.assertEventReceivedWithContent
import com.simprints.fingerprint.testtools.assertEventReceivedWithContentAssertions
import io.mockk.*
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.mock.declareModule

class CollectFingerprintsViewModelTest : KoinTest {

    @get:Rule
    var unitTestConfigRule = FullUnitTestConfigRule()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val mockTimer = MockTimer()
    private val timeHelper: FingerprintTimeHelper = mockk(relaxed = true) {
        every { newTimer() } returns mockTimer
    }
    private val sessionEventsManager: FingerprintSessionEventsManager = mockk(relaxed = true)
    private val fingerprintAnalyticsManager: FingerprintAnalyticsManager = mockk(relaxed = true)
    private val crashReportManager: FingerprintCrashReportManager = mockk(relaxed = true)
    private val preferencesManager: FingerprintPreferencesManager = mockk(relaxed = true) {
        every { qualityThreshold } returns 60
    }
    private val scanner: ScannerWrapper = mockk(relaxUnitFun = true)
    private val scannerManager: ScannerManager = ScannerManagerImpl(mockk(), mockk(), mockk(), mockk()).also {
        it.scanner = scanner
    }
    private val imageManager: FingerprintImageManager = mockk(relaxed = true)

    private lateinit var vm: CollectFingerprintsViewModel

    @Before
    fun setUp() {
        mockBase64EncodingForSavingTemplateInSession()

        declareModule {
            factory { timeHelper }
            factory { sessionEventsManager }
            factory { fingerprintAnalyticsManager }
            factory { crashReportManager }
            factory { preferencesManager }
            factory { scannerManager }
            factory { imageManager }
        }
        vm = get()
    }

    private fun mockBase64EncodingForSavingTemplateInSession() {
        mockkStatic(EncodingUtils::class)
        every { EncodingUtils.byteArrayToBase64(any()) } returns "BASE64TEMPLATE"
    }

    @Test
    fun viewModel_start_beginsWithCorrectState() {
        vm.start(TWO_FINGERS_IDS)

        assertThat(vm.state()).isEqualTo(CollectFingerprintsState(
            fingerStates = TWO_FINGERS_IDS.map { CaptureState.NotCollected },
            currentFingerIndex = 0,
            isAskingRescan = false,
            isShowingConfirmDialog = false,
            isShowingSplashScreen = false
        ))
    }

    @Test
    fun scanPressed_noImageTransfer_updatesStateToScanningDuringScan() {
        mockScannerSetUiIdle()
        captureFingerprintResponses(NEVER_RETURNS)
        noImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()

        assertThat(vm.state().currentFingerState()).isEqualTo(CaptureState.Scanning(TWO_FINGERS_IDS[0]))
    }

    @Test
    fun scanPressed_withImageTransfer_updatesStateToTransferringImageAfterScan() {
        mockScannerSetUiIdle()
        captureFingerprintResponses(GOOD_SCAN)
        acquireImageResponses(MockAcquireImageResult.NEVER_RETURNS)
        withImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()

        assertThat(vm.state().currentFingerState()).isEqualTo(CaptureState.TransferringImage(TWO_FINGERS_IDS[0], ScanResult(GOOD_QUALITY, TEMPLATE, null, 60)))
    }

    @Test
    fun scanPressed_noImageTransfer_goodScan_updatesStatesCorrectlyAndCreatesEvent() {
        mockScannerSetUiIdle()
        captureFingerprintResponses(GOOD_SCAN)
        noImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()

        assertThat(vm.state().currentFingerState()).isEqualTo(CaptureState.Collected(TWO_FINGERS_IDS[0], ScanResult(GOOD_QUALITY, TEMPLATE, null, 60)))
        vm.vibrate.assertEventReceived()

        mockTimer.executeNextTask()
        assertThat(vm.state().currentFingerIndex).isEqualTo(1)

        verify { sessionEventsManager.addEventInBackground(any()) }
    }

    @Test
    fun scanPressed_withImageTransfer_goodScan_updatesStatesCorrectlyAndCreatesEvent() {
        mockScannerSetUiIdle()
        captureFingerprintResponses(GOOD_SCAN)
        acquireImageResponses(OK)
        withImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()

        assertThat(vm.state().currentFingerState()).isEqualTo(CaptureState.Collected(TWO_FINGERS_IDS[0], ScanResult(GOOD_QUALITY, TEMPLATE, IMAGE, 60)))
        vm.vibrate.assertEventReceived()
        verify { sessionEventsManager.addEventInBackground(any()) }

        mockTimer.executeNextTask()
        assertThat(vm.state().currentFingerIndex).isEqualTo(1)
    }

    @Test
    fun scanPressed_noImageTransfer_badScan_updatesStatesCorrectlyAndCreatesEvent() {
        mockScannerSetUiIdle()
        captureFingerprintResponses(BAD_SCAN)
        noImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()

        assertThat(vm.state().currentFingerState()).isEqualTo(CaptureState.Collected(TWO_FINGERS_IDS[0], ScanResult(BAD_QUALITY, TEMPLATE, null, 60), 1))
        vm.vibrate.assertEventReceived()
        verify { sessionEventsManager.addEventInBackground(any()) }
    }

    @Test
    fun scanPressed_withImageTransfer_badScan_doesNotTransferImage_updatesStatesCorrectlyAndCreatesEvent() {
        mockScannerSetUiIdle()
        captureFingerprintResponses(BAD_SCAN)
        withImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()

        assertThat(vm.state().currentFingerState()).isEqualTo(CaptureState.Collected(TWO_FINGERS_IDS[0], ScanResult(BAD_QUALITY, TEMPLATE, null, 60), 1))
        vm.vibrate.assertEventReceived()
        verify { sessionEventsManager.addEventInBackground(any()) }
    }

    @Test
    fun scanPressed_noFingerDetected_updatesStatesCorrectlyAndCreatesEvent() {
        mockScannerSetUiIdle()
        captureFingerprintResponses(NO_FINGER_DETECTED)
        withImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()

        assertThat(vm.state().currentFingerState()).isEqualTo(CaptureState.NotDetected(TWO_FINGERS_IDS[0]))
        vm.vibrate.assertEventReceived()
        verify { sessionEventsManager.addEventInBackground(any()) }
    }

    @Test
    fun scanPressed_scannerDisconnectedDuringScan_updatesStateCorrectlyAndReconnects() {
        mockScannerSetUiIdle()
        captureFingerprintResponses(DISCONNECTED)
        withImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()

        assertThat(vm.state().currentFingerState()).isEqualTo(CaptureState.NotCollected)
        vm.launchReconnect.assertEventReceived()
    }

    @Test
    fun scanPressed_scannerDisconnectedDuringTransfer_updatesStateCorrectlyAndReconnects() {
        mockScannerSetUiIdle()
        captureFingerprintResponses(GOOD_SCAN)
        acquireImageResponses(MockAcquireImageResult.DISCONNECTED)
        withImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()

        assertThat(vm.state().currentFingerState()).isEqualTo(CaptureState.NotCollected)
        vm.launchReconnect.assertEventReceived()
    }

    @Test
    fun badScanLastAttempt_withImageTransfer_transfersImageAndUpdatesStatesCorrectly() {
        mockScannerSetUiIdle()
        captureFingerprintResponses(BAD_SCAN)
        acquireImageResponses(OK)
        withImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()
        assertThat(vm.state().currentFingerState()).isEqualTo(CaptureState.Collected(TWO_FINGERS_IDS[0], ScanResult(BAD_QUALITY, TEMPLATE, null, 60), 1))
        vm.handleScanButtonPressed()
        assertThat(vm.state().currentFingerState()).isEqualTo(CaptureState.Collected(TWO_FINGERS_IDS[0], ScanResult(BAD_QUALITY, TEMPLATE, null, 60), 2))
        vm.handleScanButtonPressed()
        assertThat(vm.state().currentFingerState()).isEqualTo(CaptureState.Collected(TWO_FINGERS_IDS[0], ScanResult(BAD_QUALITY, TEMPLATE, IMAGE, 60), 3))

        assertThat(vm.state().isShowingSplashScreen).isTrue()
        mockTimer.executeNextTask()
        assertThat(vm.state().fingerStates.size).isEqualTo(3)
        mockTimer.executeNextTask()
        assertThat(vm.state().isShowingSplashScreen).isFalse()
        assertThat(vm.state().currentFingerIndex).isEqualTo(1)

        verify(exactly = 1) { scanner.acquireImage(any()) }
        verify(exactly = 3) { sessionEventsManager.addEventInBackground(any()) }
    }

    @Test
    fun receivesOnlyBadScansThenConfirm_performsImageTransferEventually_resultsInCorrectStateAndSavesFingersCorrectly() {
        mockScannerSetUiIdle()
        captureFingerprintResponses(BAD_SCAN)
        acquireImageResponses(OK)
        withImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        repeat(12) { // 3 times for each of the 4 fingers (2 original + 2 auto-added)
            vm.handleScanButtonPressed()
            mockTimer.executeAllTasks()
        }

        assertThat(vm.state()).isEqualTo(CollectFingerprintsState(
            fingerStates = FOUR_FINGERS_IDS.map {
                CaptureState.Collected(it, ScanResult(BAD_QUALITY, TEMPLATE, IMAGE, 60), numberOfBadScans = 3)
            },
            currentFingerIndex = 3,
            isAskingRescan = false,
            isShowingConfirmDialog = true,
            isShowingSplashScreen = false
        ))
        verify(exactly = 12) { sessionEventsManager.addEventInBackground(any()) }

        vm.handleConfirmFingerprintsAndContinue()
        coVerify(exactly = 4) { imageManager.save(any(), any(), any()) }

        vm.finishWithFingerprints.assertEventReceivedWithContentAssertions { actualFingerprints ->
            assertThat(actualFingerprints).hasSize(FOUR_FINGERS_IDS.size)
            assertThat(actualFingerprints.map { it.fingerId }).containsExactlyElementsIn(FOUR_FINGERS_IDS)
            actualFingerprints.forEach {
                assertThat(it.templateBytes).isEqualTo(TEMPLATE)
                assertThat(it.imageRef).isNotNull()
            }
        }
    }

    @Test
    fun receivesOnlyGoodScansThenConfirm_withImageTransfer_resultsInCorrectStateAndSavesFingersCorrectly() {
        mockScannerSetUiIdle()
        captureFingerprintResponses(GOOD_SCAN)
        acquireImageResponses(OK)
        withImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        repeat(2) {
            vm.handleScanButtonPressed()
            mockTimer.executeAllTasks()
        }

        assertThat(vm.state()).isEqualTo(CollectFingerprintsState(
            fingerStates = TWO_FINGERS_IDS.map { CaptureState.Collected(it, ScanResult(GOOD_QUALITY, TEMPLATE, IMAGE, 60)) },
            currentFingerIndex = 1,
            isAskingRescan = false,
            isShowingConfirmDialog = true,
            isShowingSplashScreen = false
        ))
        verify(exactly = 2) { sessionEventsManager.addEventInBackground(any()) }

        vm.handleConfirmFingerprintsAndContinue()
        coVerify(exactly = 2) { imageManager.save(any(), any(), any()) }

        vm.finishWithFingerprints.assertEventReceivedWithContentAssertions { actualFingerprints ->
            assertThat(actualFingerprints).hasSize(TWO_FINGERS_IDS.size)
            assertThat(actualFingerprints.map { it.fingerId }).containsExactlyElementsIn(TWO_FINGERS_IDS)
            actualFingerprints.forEach {
                assertThat(it.templateBytes).isEqualTo(TEMPLATE)
                assertThat(it.imageRef).isNotNull()
            }
        }
    }

    @Test
    fun receivesOnlyGoodScansThenConfirm_noImageTransfer_resultsInCorrectStateAndReturnsFingersCorrectly() {
        mockScannerSetUiIdle()
        captureFingerprintResponses(GOOD_SCAN)
        noImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        repeat(2) {
            vm.handleScanButtonPressed()
            mockTimer.executeAllTasks()
        }

        assertThat(vm.state()).isEqualTo(CollectFingerprintsState(
            fingerStates = TWO_FINGERS_IDS.map { CaptureState.Collected(it, ScanResult(GOOD_QUALITY, TEMPLATE, null, 60)) },
            currentFingerIndex = 1,
            isAskingRescan = false,
            isShowingConfirmDialog = true,
            isShowingSplashScreen = false
        ))
        verify(exactly = 2) { sessionEventsManager.addEventInBackground(any()) }

        vm.handleConfirmFingerprintsAndContinue()
        coVerify(exactly = 0) { imageManager.save(any(), any(), any()) }

        vm.finishWithFingerprints.assertEventReceivedWithContentAssertions { actualFingerprints ->
            assertThat(actualFingerprints).hasSize(TWO_FINGERS_IDS.size)
            assertThat(actualFingerprints.map { it.fingerId }).containsExactlyElementsIn(TWO_FINGERS_IDS)
            actualFingerprints.forEach {
                assertThat(it.templateBytes).isEqualTo(TEMPLATE)
                assertThat(it.imageRef).isNull()
            }
        }
    }

    @Test
    fun goodScan_swipeBackThenRescan_updatesStateCorrectly() {
        mockScannerSetUiIdle()
        captureFingerprintResponses(GOOD_SCAN, DIFFERENT_GOOD_SCAN)
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
        assertThat(vm.state().currentFingerState()).isEqualTo(CaptureState.Collected(TWO_FINGERS_IDS[0], ScanResult(DIFFERENT_GOOD_QUALITY, DIFFERENT_TEMPLATE, null, 60)))

        verify(exactly = 2) { sessionEventsManager.addEventInBackground(any()) }
    }

    @Test
    fun missingFinger_updatesStateCorrectly() {
        mockScannerSetUiIdle()

        vm.start(TWO_FINGERS_IDS)
        vm.handleMissingFingerButtonPressed()
        assertThat(vm.state().currentFingerState()).isEqualTo(CaptureState.Skipped)
        assertThat(vm.state().isShowingSplashScreen).isTrue()
        mockTimer.executeNextTask()
        assertThat(vm.state().fingerStates.size).isEqualTo(3)
        mockTimer.executeNextTask()
        assertThat(vm.state().isShowingSplashScreen).isFalse()
        assertThat(vm.state().currentFingerIndex).isEqualTo(1)

        verify { sessionEventsManager.addEventInBackground(any()) }
    }

    @Test
    fun receivesOnlyMissingFingersThenConfirm_showsToastAndResetsState() {
        mockScannerSetUiIdle()

        vm.start(TWO_FINGERS_IDS)
        repeat(4) { // 2 original + 2 auto-added
            vm.handleMissingFingerButtonPressed()
            mockTimer.executeAllTasks()
        }

        assertThat(vm.state()).isEqualTo(CollectFingerprintsState(
            fingerStates = FOUR_FINGERS_IDS.map { CaptureState.Skipped },
            currentFingerIndex = 3,
            isAskingRescan = false,
            isShowingConfirmDialog = true,
            isShowingSplashScreen = false
        ))
        verify(exactly = 4) { sessionEventsManager.addEventInBackground(any()) }

        vm.handleConfirmFingerprintsAndContinue()

        vm.noFingersScannedToast.assertEventReceived()
        assertThat(vm.state()).isEqualTo(CollectFingerprintsState(
            fingerStates = TWO_FINGERS_IDS.map { CaptureState.NotCollected },
            currentFingerIndex = 0,
            isAskingRescan = false,
            isShowingConfirmDialog = false,
            isShowingSplashScreen = false
        ))
    }

    @Test
    fun receivesMixOfScanResults_withImageTransfer_updatesStateCorrectlyAndReturnsFingersCorrectly() {
        mockScannerSetUiIdle()
        captureFingerprintResponses(
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

        assertThat(vm.state()).isEqualTo(CollectFingerprintsState(
            fingerStates = listOf(
                CaptureState.Collected(FOUR_FINGERS_IDS[0], ScanResult(GOOD_QUALITY, TEMPLATE, IMAGE, 60), numberOfBadScans = 2),
                CaptureState.Skipped,
                CaptureState.Collected(FOUR_FINGERS_IDS[2], ScanResult(BAD_QUALITY, TEMPLATE, IMAGE, 60), numberOfBadScans = 3),
                CaptureState.Collected(FOUR_FINGERS_IDS[3], ScanResult(GOOD_QUALITY, TEMPLATE, IMAGE, 60), numberOfBadScans = 0)
            ),
            currentFingerIndex = 3,
            isAskingRescan = false,
            isShowingConfirmDialog = true,
            isShowingSplashScreen = false
        ))
        verify(exactly = 14) { sessionEventsManager.addEventInBackground(any()) }

        vm.handleConfirmFingerprintsAndContinue()
        coVerify(exactly = 3) { imageManager.save(any(), any(), any()) }

        vm.finishWithFingerprints.assertEventReceivedWithContentAssertions { actualFingerprints ->
            assertThat(actualFingerprints).hasSize(3)
            assertThat(actualFingerprints.map { it.fingerId }).containsExactly(FingerIdentifier.LEFT_THUMB, FingerIdentifier.RIGHT_THUMB, FingerIdentifier.RIGHT_INDEX_FINGER)
            actualFingerprints.forEach {
                assertThat(it.templateBytes).isEqualTo(TEMPLATE)
                assertThat(it.imageRef).isNotNull()
            }
        }
    }

    @Test
    fun pressingCancel_duringScan_cancelsProperly() {
        mockScannerSetUiIdle()
        captureFingerprintResponses(NEVER_RETURNS)
        withImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()
        assertThat(vm.state().currentFingerState()).isEqualTo(CaptureState.Scanning(TWO_FINGERS_IDS[0]))
        vm.handleScanButtonPressed()
        assertThat(vm.state().currentFingerState()).isEqualTo(CaptureState.NotCollected)
    }

    @Test
    fun pressingCancel_duringImageTransfer_doesNothing() {
        mockScannerSetUiIdle()
        captureFingerprintResponses(GOOD_SCAN)
        acquireImageResponses(MockAcquireImageResult.NEVER_RETURNS)
        withImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()
        assertThat(vm.state().currentFingerState()).isEqualTo(CaptureState.TransferringImage(TWO_FINGERS_IDS[0], ScanResult(GOOD_QUALITY, TEMPLATE, null, 60)))
        vm.handleScanButtonPressed()
        assertThat(vm.state().currentFingerState()).isEqualTo(CaptureState.TransferringImage(TWO_FINGERS_IDS[0], ScanResult(GOOD_QUALITY, TEMPLATE, null, 60)))
    }

    @Test
    fun restartAfterScans_resetsStateCorrectly() {
        mockScannerSetUiIdle()
        captureFingerprintResponses(GOOD_SCAN)
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
                fingerStates = TWO_FINGERS_IDS.map { CaptureState.NotCollected },
                currentFingerIndex = 0,
                isAskingRescan = false,
                isShowingConfirmDialog = false,
                isShowingSplashScreen = false
            )
        )
    }

    @Test
    fun backPressed_whileScanning_cancelsScanning() {
        mockScannerSetUiIdle()
        captureFingerprintResponses(NEVER_RETURNS)
        withImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()
        assertThat(vm.state().currentFingerState()).isEqualTo(CaptureState.Scanning(TWO_FINGERS_IDS[0]))
        vm.handleOnBackPressed()
        assertThat(vm.state().currentFingerState()).isEqualTo(CaptureState.NotCollected)
    }

    @Test
    fun backPressed_whileTransferringImage_cancelsTransfer() {
        mockScannerSetUiIdle()
        captureFingerprintResponses(GOOD_SCAN)
        acquireImageResponses(MockAcquireImageResult.NEVER_RETURNS)
        withImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()
        assertThat(vm.state().currentFingerState()).isEqualTo(CaptureState.TransferringImage(TWO_FINGERS_IDS[0], ScanResult(GOOD_QUALITY, TEMPLATE, null, 60)))
        vm.handleOnBackPressed()
        assertThat(vm.state().currentFingerState()).isEqualTo(CaptureState.NotCollected)
    }

    @Test
    fun backPressed_whileIdle_doesNothing() {
        mockScannerSetUiIdle()
        captureFingerprintResponses(GOOD_SCAN)
        noImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()
        vm.handleOnBackPressed()
        assertThat(vm.state().currentFingerState()).isEqualTo(CaptureState.Collected(TWO_FINGERS_IDS[0], ScanResult(GOOD_QUALITY, TEMPLATE, null, 60)))
    }

    @Test
    fun unexpectedErrorWhileScanning_launchesAlertAndReportsCrash() {
        mockScannerSetUiIdle()
        captureFingerprintResponses(UNKNOWN_ERROR)
        noImageTransfer()

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed()
        assertThat(vm.state().currentFingerState()).isEqualTo(CaptureState.NotCollected)

        verify { crashReportManager.logExceptionOrSafeException(any()) }
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

    private fun noImageTransfer() {
        every { preferencesManager.saveFingerprintImagesStrategy } returns SaveFingerprintImagesStrategy.NEVER
    }

    private fun withImageTransfer() {
        every { preferencesManager.saveFingerprintImagesStrategy } returns SaveFingerprintImagesStrategy.WSQ_15
        coEvery { imageManager.save(any(), any(), any()) } returns mockk()
    }

    private fun mockScannerSetUiIdle() {
        every { scanner.setUiIdle() } returns Completable.complete()
    }

    private fun captureFingerprintResponses(vararg responses: MockCaptureFingerprintResponse) {
        every { scanner.captureFingerprint(any(), any(), any()) } returnsMany responses.map {
            when (it) {
                GOOD_SCAN -> Single.just(CaptureFingerprintResponse(TEMPLATE, GOOD_QUALITY))
                DIFFERENT_GOOD_SCAN -> Single.just(CaptureFingerprintResponse(DIFFERENT_TEMPLATE, DIFFERENT_GOOD_QUALITY))
                BAD_SCAN -> Single.just(CaptureFingerprintResponse(TEMPLATE, BAD_QUALITY))
                NO_FINGER_DETECTED -> Single.error(NoFingerDetectedException())
                DISCONNECTED -> Single.error(ScannerDisconnectedException())
                UNKNOWN_ERROR -> Single.error(Error("Oops!"))
                NEVER_RETURNS -> Single.never()
            }
        }
    }

    private fun acquireImageResponses(vararg responses: MockAcquireImageResult) {
        every { scanner.acquireImage(any()) } returnsMany responses.map {
            when (it) {
                OK -> Single.just(AcquireImageResponse(IMAGE))
                MockAcquireImageResult.DISCONNECTED -> Single.error(ScannerDisconnectedException())
                MockAcquireImageResult.NEVER_RETURNS -> Single.never()
            }
        }
    }

    private enum class MockCaptureFingerprintResponse {
        GOOD_SCAN, DIFFERENT_GOOD_SCAN, BAD_SCAN, NO_FINGER_DETECTED, DISCONNECTED, UNKNOWN_ERROR, NEVER_RETURNS
    }

    private enum class MockAcquireImageResult {
        OK, DISCONNECTED, NEVER_RETURNS
    }

    companion object {
        val TWO_FINGERS_IDS = listOf(FingerIdentifier.LEFT_THUMB, FingerIdentifier.LEFT_INDEX_FINGER)
        val FOUR_FINGERS_IDS = listOf(FingerIdentifier.LEFT_THUMB, FingerIdentifier.LEFT_INDEX_FINGER, FingerIdentifier.RIGHT_THUMB, FingerIdentifier.RIGHT_INDEX_FINGER)

        const val GOOD_QUALITY = 80
        const val DIFFERENT_GOOD_QUALITY = 80
        const val BAD_QUALITY = 20

        val TEMPLATE = FingerprintGenerator.generateRandomFingerprint().templateBytes
        val DIFFERENT_TEMPLATE = FingerprintGenerator.generateRandomFingerprint().templateBytes

        val IMAGE = byteArrayOf(0x05, 0x06, 0x07, 0x08)
    }
}
