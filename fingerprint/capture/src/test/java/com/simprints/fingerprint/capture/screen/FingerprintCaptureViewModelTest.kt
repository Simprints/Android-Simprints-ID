package com.simprints.fingerprint.capture.screen

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.core.tools.time.TimeHelper
import com.simprints.fingerprint.capture.screen.FingerprintCaptureViewModelTest.MockAcquireImageResult.OK
import com.simprints.fingerprint.capture.screen.FingerprintCaptureViewModelTest.MockCaptureFingerprintResponse.BAD_SCAN
import com.simprints.fingerprint.capture.screen.FingerprintCaptureViewModelTest.MockCaptureFingerprintResponse.DIFFERENT_GOOD_SCAN
import com.simprints.fingerprint.capture.screen.FingerprintCaptureViewModelTest.MockCaptureFingerprintResponse.GOOD_SCAN
import com.simprints.fingerprint.capture.screen.FingerprintCaptureViewModelTest.MockCaptureFingerprintResponse.NEVER_RETURNS
import com.simprints.fingerprint.capture.screen.FingerprintCaptureViewModelTest.MockCaptureFingerprintResponse.NO_FINGER_DETECTED
import com.simprints.fingerprint.capture.screen.FingerprintCaptureViewModelTest.MockCaptureFingerprintResponse.UNKNOWN_ERROR
import com.simprints.fingerprint.capture.state.CaptureState
import com.simprints.fingerprint.capture.state.CollectFingerprintsState
import com.simprints.fingerprint.capture.state.FingerState
import com.simprints.fingerprint.capture.state.LiveFeedbackState
import com.simprints.fingerprint.capture.state.ScanResult
import com.simprints.fingerprint.capture.usecase.AddCaptureEventsUseCase
import com.simprints.fingerprint.capture.usecase.GetNextFingerToAddUseCase
import com.simprints.fingerprint.capture.usecase.GetStartStateUseCase
import com.simprints.fingerprint.capture.usecase.IsNoFingerDetectedLimitReachedUseCase
import com.simprints.fingerprint.capture.usecase.SaveImageUseCase
import com.simprints.fingerprint.infra.basebiosdk.exceptions.BioSdkException
import com.simprints.fingerprint.infra.biosdk.BioSdkWrapper
import com.simprints.fingerprint.infra.biosdk.ResolveBioSdkWrapperUseCase
import com.simprints.fingerprint.infra.scanner.ScannerManager
import com.simprints.fingerprint.infra.scanner.domain.ScannerGeneration
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireFingerprintImageResponse
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireFingerprintTemplateResponse
import com.simprints.fingerprint.infra.scanner.exceptions.safe.NoFingerDetectedException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.infra.scanner.wrapper.ScannerWrapper
import com.simprints.fingerprint.testtools.FingerprintGenerator
import com.simprints.infra.config.store.models.FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER
import com.simprints.infra.config.store.models.Vero1Configuration
import com.simprints.infra.config.store.models.Vero2Configuration
import com.simprints.infra.config.store.models.Vero2Configuration.ImageSavingStrategy
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.images.model.Path
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.assertEventNotReceived
import com.simprints.testtools.common.livedata.assertEventReceived
import com.simprints.testtools.common.livedata.assertEventReceivedWithContentAssertions
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class FingerprintCaptureViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()


    @MockK
    private lateinit var vero2Configuration: Vero2Configuration

    @MockK
    private lateinit var configManager: ConfigManager

    @MockK
    private lateinit var scanner: ScannerWrapper

    @MockK
    private lateinit var scannerManager: ScannerManager

    @MockK
    private lateinit var bioSdkWrapper: BioSdkWrapper

    @MockK
    private lateinit var resolveBioSdkWrapperUseCase: ResolveBioSdkWrapperUseCase

    @MockK
    private lateinit var timeHelper: TimeHelper

    @MockK
    private lateinit var saveImageUseCase: SaveImageUseCase

    @MockK
    private lateinit var addCaptureEventsUseCase: AddCaptureEventsUseCase

    @MockK
    private lateinit var isNoFingerDetectedLimitReachedUseCase: IsNoFingerDetectedLimitReachedUseCase

    private val getStartStateUseCase = GetStartStateUseCase()
    private val getNextFingerToAddUseCase = GetNextFingerToAddUseCase()


    private lateinit var vm: FingerprintCaptureViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { resolveBioSdkWrapperUseCase(any()) } returns bioSdkWrapper
        every { vero2Configuration.qualityThreshold } returns 60
        every { vero2Configuration.displayLiveFeedback } returns false
        every { vero2Configuration.captureStrategy } returns Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1000_DPI
        every { vero2Configuration.imageSavingStrategy } returns ImageSavingStrategy.NEVER
        coEvery { configManager.getProjectConfiguration().fingerprint?.getSdkConfiguration(any()) } returns mockk {
            every { vero1 } returns Vero1Configuration(60)
            every { vero2 } returns vero2Configuration
        }

        coEvery { addCaptureEventsUseCase.invoke(any(), any(), any(), any()) } returns "payloadId"
        coEvery { isNoFingerDetectedLimitReachedUseCase.invoke(any(), any()) } returns false

        every { scanner.isLiveFeedbackAvailable() } returns false
        every { scanner.isImageTransferSupported() } returns true
        every { scanner.versionInformation().generation } returns ScannerGeneration.VERO_1
        every { scannerManager.scanner } returns scanner
        every { scannerManager.isScannerConnected } returns true

        coJustRun { bioSdkWrapper.initialize() }
        every { bioSdkWrapper.scanningTimeoutMs } returns 1000
        every { bioSdkWrapper.imageTransferTimeoutMs } returns 1000

        vm = FingerprintCaptureViewModel(
            scannerManager = scannerManager,
            configManager = configManager,
            timeHelper = timeHelper,
            resolveBioSdkWrapperUseCase = resolveBioSdkWrapperUseCase,
            saveImage = saveImageUseCase,
            getNextFingerToAdd = getNextFingerToAddUseCase,
            getStartState = getStartStateUseCase,
            addCaptureEvents = addCaptureEventsUseCase,
            isNoFingerDetectedLimitReachedUseCase = isNoFingerDetectedLimitReachedUseCase,
            externalScope = CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
        )
    }

    @Test
    fun viewModel_start_beginsWithCorrectState() = runTest {
        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)

        assertThat(vm.stateLiveData.value).isEqualTo(
            CollectFingerprintsState(
                fingerStates = TWO_FINGERS_IDS.map { FingerState(it, listOf(CaptureState.NotCollected)) },
                currentFingerIndex = 0,
                isAskingRescan = false,
                isShowingConfirmDialog = false,
                isShowingSplashScreen = false,
                isShowingConnectionScreen = false,
            )
        )
    }

    @Test
    @ExperimentalTime
    fun shouldNot_launchAlertScreen_whenOngoingFingerScan_isCancelled() = runTest {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(NEVER_RETURNS)

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)

        // start and cancel finger scan
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()

        vm.launchAlert.assertEventNotReceived()
    }

    @Test
    @ExperimentalTime
    fun scanPressed_noImageTransfer_updatesStateToScanningDuringScan() = runTest {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(NEVER_RETURNS)
        noImageTransfer()

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        vm.handleScanButtonPressed()

        assertThat(vm.stateLiveData.value?.currentCaptureState()).isEqualTo(CaptureState.ScanProcess.Scanning(0, 0))
    }

    @Test
    fun `test scanner supports image transfer then isImageTransferRequired should be equal to scanningTimeoutMs + imageTransferTimeoutMs`() = runTest {
        withImageTransfer()
        every { scanner.isImageTransferSupported() } returns true
        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        assertThat(vm.progressBarTimeout()).isEqualTo(bioSdkWrapper.scanningTimeoutMs + bioSdkWrapper.imageTransferTimeoutMs)
    }

    @Test
    fun `test scanner doesn't support imageTransfer then progressBarTimeout should be equal to scanningTimeoutMs`() = runTest {
        withImageTransfer()
        every { scanner.isImageTransferSupported() } returns false
        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        assertThat(vm.progressBarTimeout()).isEqualTo(bioSdkWrapper.scanningTimeoutMs)
    }

    @Test
    @ExperimentalTime
    fun scanPressed_withImageTransfer_updatesStateToTransferringImageAfterScan() = runTest {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(GOOD_SCAN)
        acquireImageResponses(MockAcquireImageResult.NEVER_RETURNS)
        withImageTransfer()

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        vm.handleScanButtonPressed()

        assertThat(vm.stateLiveData.value?.currentCaptureState()).isEqualTo(
            CaptureState.ScanProcess.TransferringImage(
                numberOfBadScans = 0,
                numberOfNoFingerDetectedScans = 0,
                scanResult = ScanResult(
                    GOOD_QUALITY, TEMPLATE, TEMPLATE_FORMAT, null, 60
                )
            )
        )
    }

    @Test
    @ExperimentalTime
    fun scanPressed_noImageTransfer_goodScan_updatesStatesCorrectlyAndCreatesEvent() = runTest {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(GOOD_SCAN)
        noImageTransfer()

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        vm.handleScanButtonPressed()

        assertThat(vm.stateLiveData.value?.currentCaptureState()).isEqualTo(
            CaptureState.ScanProcess.Collected(
                numberOfBadScans = 0,
                numberOfNoFingerDetectedScans = 0,
                scanResult = ScanResult(
                    GOOD_QUALITY, TEMPLATE, TEMPLATE_FORMAT, null, 60
                )
            )
        )
        vm.vibrate.assertEventReceived()

        advanceTimeBy(TIME_SKIP_MS)
        assertThat(vm.stateLiveData.value?.currentFingerIndex).isEqualTo(1)

        coVerify { addCaptureEventsUseCase.invoke(any(), any(), any(), any()) }
    }

    @Test
    @ExperimentalTime
    fun scanPressed_withImageTransfer_goodScan_updatesStatesCorrectlyAndCreatesEvent() = runTest {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(GOOD_SCAN)
        acquireImageResponses(OK)
        withImageTransfer()

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        vm.handleScanButtonPressed()

        assertThat(vm.stateLiveData.value?.currentCaptureState()).isEqualTo(
            CaptureState.ScanProcess.Collected(
                numberOfBadScans = 0,
                numberOfNoFingerDetectedScans = 0,
                scanResult = ScanResult(GOOD_QUALITY, TEMPLATE, TEMPLATE_FORMAT, IMAGE, 60)
            )
        )
        vm.vibrate.assertEventReceived()
        coVerify { addCaptureEventsUseCase.invoke(any(), any(), any(), any()) }

        advanceTimeBy(TIME_SKIP_MS)
        assertThat(vm.stateLiveData.value?.currentFingerIndex).isEqualTo(1)
    }

    @Test
    @ExperimentalTime
    fun scanPressed_noImageTransfer_badScan_updatesStatesCorrectlyAndCreatesEvent() = runTest {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(BAD_SCAN)
        noImageTransfer()

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        vm.handleScanButtonPressed()

        assertThat(vm.stateLiveData.value?.currentCaptureState()).isEqualTo(
            CaptureState.ScanProcess.Collected(
                numberOfBadScans = 1,
                numberOfNoFingerDetectedScans = 0,
                scanResult = ScanResult(
                    BAD_QUALITY, TEMPLATE, TEMPLATE_FORMAT, null, 60
                )
            )
        )
        vm.vibrate.assertEventReceived()
        coVerify { addCaptureEventsUseCase.invoke(any(), any(), any(), any()) }
    }

    @Test
    @ExperimentalTime
    fun scanPressed_withImageTransfer_badScan_doesNotTransferImage_updatesStatesCorrectlyAndCreatesEvent() = runTest {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(BAD_SCAN)
        withImageTransfer()

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        vm.handleScanButtonPressed()

        assertThat(vm.stateLiveData.value?.currentCaptureState()).isEqualTo(
            CaptureState.ScanProcess.Collected(
                numberOfBadScans = 1,
                numberOfNoFingerDetectedScans = 0,
                scanResult = ScanResult(
                    BAD_QUALITY, TEMPLATE, TEMPLATE_FORMAT, null, 60
                )
            )
        )
        vm.vibrate.assertEventReceived()
        coVerify { addCaptureEventsUseCase.invoke(any(), any(), any(), any()) }
    }

    @Test
    @ExperimentalTime
    fun scanPressed_noFingerDetected_updatesStatesCorrectlyAndCreatesEvent() = runTest {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(NO_FINGER_DETECTED)
        withImageTransfer()

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        vm.handleScanButtonPressed()

        assertThat(vm.stateLiveData.value?.currentCaptureState()).isEqualTo(
            CaptureState.ScanProcess.NotDetected(
                numberOfBadScans = 0,
                numberOfNoFingerDetectedScans = 1,
            )
        )
        vm.vibrate.assertEventReceived()
        coVerify { addCaptureEventsUseCase.invoke(any(), any(), any(), any()) }
    }

    @Test
    fun scanPressed_scannerDisconnectedDuringScan_updatesStateCorrectlyAndReconnects() = runTest {
        mockScannerSetUiIdle()
        coEvery {
            bioSdkWrapper.acquireFingerprintTemplate(any(), any(), any(),any())
        } throws ScannerDisconnectedException()
        withImageTransfer()

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        vm.handleScanButtonPressed()

        assertThat(vm.stateLiveData.value?.currentCaptureState()).isEqualTo(CaptureState.NotCollected)
        vm.launchReconnect.assertEventReceived()
    }

    @Test
    @ExperimentalTime
    fun scanPressed_scannerDisconnectedDuringTransfer_updatesStateCorrectlyAndReconnects() = runTest {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(GOOD_SCAN)
        acquireImageResponses(MockAcquireImageResult.DISCONNECTED)
        withImageTransfer()

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        vm.handleScanButtonPressed()

        assertThat(vm.stateLiveData.value?.currentCaptureState()).isEqualTo(CaptureState.NotCollected)
        vm.launchReconnect.assertEventReceived()
    }

    @Test
    @ExperimentalTime
    fun badScanLastAttempt_withImageTransfer_transfersImageAndUpdatesStatesCorrectly() = runTest {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(BAD_SCAN)
        acquireImageResponses(OK)
        withImageTransfer()

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        vm.handleScanButtonPressed()
        assertThat(vm.stateLiveData.value?.currentCaptureState()).isEqualTo(
            CaptureState.ScanProcess.Collected(
                numberOfBadScans = 1,
                numberOfNoFingerDetectedScans = 0,
                scanResult = ScanResult(
                    BAD_QUALITY, TEMPLATE, TEMPLATE_FORMAT, null, 60
                )
            )
        )
        vm.handleScanButtonPressed()
        assertThat(vm.stateLiveData.value?.currentCaptureState()).isEqualTo(
            CaptureState.ScanProcess.Collected(
                numberOfBadScans = 2,
                numberOfNoFingerDetectedScans = 0,
                scanResult = ScanResult(
                    BAD_QUALITY, TEMPLATE, TEMPLATE_FORMAT, null, 60
                )
            )
        )
        vm.handleScanButtonPressed()
        assertThat(vm.stateLiveData.value?.currentCaptureState()).isEqualTo(
            CaptureState.ScanProcess.Collected(
                numberOfBadScans = 3,
                numberOfNoFingerDetectedScans = 0,
                scanResult = ScanResult(
                    BAD_QUALITY, TEMPLATE, TEMPLATE_FORMAT, IMAGE, 60
                )
            )
        )

        assertThat(vm.stateLiveData.value?.isShowingSplashScreen).isTrue()
        advanceTimeBy(TIME_SKIP_MS)
        assertThat(vm.stateLiveData.value?.fingerStates?.size).isEqualTo(3)
        advanceTimeBy(TIME_SKIP_MS)
        advanceTimeBy(TIME_SKIP_MS)
        assertThat(vm.stateLiveData.value?.isShowingSplashScreen).isFalse()
        assertThat(vm.stateLiveData.value?.currentFingerIndex).isEqualTo(1)

        coVerify(exactly = 2) { bioSdkWrapper.acquireFingerprintTemplate(any(), any(), any(), false) }
        coVerify(exactly = 1) { bioSdkWrapper.acquireFingerprintTemplate(any(), any(), any(), true) }
        coVerify(exactly = 1) { bioSdkWrapper.acquireFingerprintImage() }
        coVerify(exactly = 3) { addCaptureEventsUseCase.invoke(any(), any(), any(), any()) }
    }

    @Test
    @ExperimentalTime
    fun receivesOnlyBadScansThenConfirm_performsImageTransferEventually_resultsInCorrectStateAndSavesFingersCorrectly() = runTest {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(BAD_SCAN)
        acquireImageResponses(OK)
        withImageTransfer()

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        repeat(12) { // 3 times for each of the 4 fingers (2 original + 2 auto-added)
            vm.handleScanButtonPressed()
            advanceTimeBy(TIME_SKIP_MS)
        }
        assertThat(vm.stateLiveData.value).isEqualTo(
            CollectFingerprintsState(
                fingerStates = FOUR_FINGERS_IDS.map {
                    FingerState(
                        it, listOf(
                            CaptureState.ScanProcess.Collected(
                                numberOfBadScans = 3,
                                numberOfNoFingerDetectedScans = 0,
                                ScanResult(BAD_QUALITY, TEMPLATE, TEMPLATE_FORMAT, IMAGE, 60),
                            )
                        )
                    )
                },
                currentFingerIndex = 3,
                isAskingRescan = false,
                isShowingConfirmDialog = true,
                isShowingSplashScreen = false,
                isShowingConnectionScreen = false,
            )
        )
        coVerify(exactly = 12) { addCaptureEventsUseCase.invoke(any(), any(), any(), any()) }
        vm.handleConfirmFingerprintsAndContinue()
        coVerify(exactly = 4) { saveImageUseCase.invoke(any(), any(), any(), any()) }

        vm.finishWithFingerprints.assertEventReceivedWithContentAssertions { actualFingerprints ->
            assertThat(actualFingerprints?.results).hasSize(FOUR_FINGERS_IDS.size)
            assertThat(actualFingerprints?.results?.map { it.identifier }).containsExactlyElementsIn(
                FOUR_FINGERS_IDS
            )
            actualFingerprints?.results?.forEach {
                assertThat(it.sample?.template).isEqualTo(TEMPLATE)
                assertThat(it.sample?.imageRef).isNotNull()
            }
        }
    }

    @Test
    @ExperimentalTime
    fun receivesOnlyGoodScansThenConfirm_withImageTransfer_resultsInCorrectStateAndSavesFingersCorrectly() = runTest {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(GOOD_SCAN)
        acquireImageResponses(OK)
        withImageTransfer()

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        repeat(2) {
            vm.handleScanButtonPressed()
            advanceTimeBy(TIME_SKIP_MS)
        }

        assertThat(vm.stateLiveData.value).isEqualTo(
            CollectFingerprintsState(
                fingerStates = TWO_FINGERS_IDS.map {
                    FingerState(
                        it, listOf(
                            CaptureState.ScanProcess.Collected(
                                numberOfBadScans = 0,
                                numberOfNoFingerDetectedScans = 0,
                                scanResult = ScanResult(
                                    GOOD_QUALITY, TEMPLATE, TEMPLATE_FORMAT, IMAGE, 60
                                )
                            )
                        )
                    )
                },
                currentFingerIndex = 1,
                isAskingRescan = false,
                isShowingConfirmDialog = true,
                isShowingSplashScreen = false,
                isShowingConnectionScreen = false,
            )
        )
        coVerify(exactly = 2) { addCaptureEventsUseCase.invoke(any(), any(), any(), any()) }

        vm.handleConfirmFingerprintsAndContinue()
        coVerify(exactly = 2) { saveImageUseCase.invoke(any(), any(), any(), any()) }

        vm.finishWithFingerprints.assertEventReceivedWithContentAssertions { actualFingerprints ->
            assertThat(actualFingerprints?.results).hasSize(TWO_FINGERS_IDS.size)
            assertThat(actualFingerprints?.results?.map { it.identifier }).containsExactlyElementsIn(
                TWO_FINGERS_IDS
            )
            actualFingerprints?.results?.forEach {
                assertThat(it.sample?.template).isEqualTo(TEMPLATE)
                assertThat(it.sample?.imageRef).isNotNull()
            }
        }
    }

    @Test
    @ExperimentalTime
    fun receivesOnlyGoodScansThenConfirm_noImageTransfer_resultsInCorrectStateAndReturnsFingersCorrectly() = runTest {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(GOOD_SCAN)
        noImageTransfer()

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        repeat(2) {
            vm.handleScanButtonPressed()
            advanceTimeBy(TIME_SKIP_MS)
        }

        assertThat(vm.stateLiveData.value).isEqualTo(
            CollectFingerprintsState(
                fingerStates = TWO_FINGERS_IDS.map {
                    FingerState(
                        it, listOf(
                            CaptureState.ScanProcess.Collected(
                                numberOfBadScans = 0,
                                numberOfNoFingerDetectedScans = 0,
                                scanResult = ScanResult(
                                    GOOD_QUALITY, TEMPLATE, TEMPLATE_FORMAT, null, 60
                                )
                            )
                        )
                    )
                },
                currentFingerIndex = 1,
                isAskingRescan = false,
                isShowingConfirmDialog = true,
                isShowingSplashScreen = false,
                isShowingConnectionScreen = false,
            )
        )
        coVerify(exactly = 2) { addCaptureEventsUseCase.invoke(any(), any(), any(), any()) }
        vm.handleConfirmFingerprintsAndContinue()
        coVerify(exactly = 0) { saveImageUseCase.invoke(any(), any(), any(), any()) }

        vm.finishWithFingerprints.assertEventReceivedWithContentAssertions { actualFingerprints ->
            assertThat(actualFingerprints?.results).hasSize(TWO_FINGERS_IDS.size)
            assertThat(actualFingerprints?.results?.map { it.identifier }).containsExactlyElementsIn(
                TWO_FINGERS_IDS
            )
            actualFingerprints?.results?.forEach {
                assertThat(it.sample?.template).isEqualTo(TEMPLATE)
                assertThat(it.sample?.imageRef).isNull()
            }
        }
    }

    @Test
    @ExperimentalTime
    fun goodScan_swipeBackThenRescan_updatesStateCorrectly() = runTest {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(GOOD_SCAN, DIFFERENT_GOOD_SCAN)
        noImageTransfer()

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        vm.handleScanButtonPressed()
        advanceTimeBy(TIME_SKIP_MS)
        assertThat(vm.stateLiveData.value?.currentFingerIndex).isEqualTo(1)

        vm.updateSelectedFinger(0)
        assertThat(vm.stateLiveData.value?.currentFingerIndex).isEqualTo(0)

        vm.handleScanButtonPressed()
        assertThat(vm.stateLiveData.value?.isAskingRescan).isTrue()

        vm.handleScanButtonPressed()
        assertThat(vm.stateLiveData.value?.isAskingRescan).isFalse()
        assertThat(vm.stateLiveData.value?.currentCaptureState()).isEqualTo(
            CaptureState.ScanProcess.Collected(
                numberOfBadScans = 0,
                numberOfNoFingerDetectedScans = 0,
                scanResult = ScanResult(
                    DIFFERENT_GOOD_QUALITY, DIFFERENT_TEMPLATE, TEMPLATE_FORMAT, null, 60
                )
            )
        )

        coVerify(exactly = 2) { addCaptureEventsUseCase.invoke(any(), any(), any(), any()) }
    }

    @Test
    fun missingFinger_updatesStateCorrectly() = runTest {
        mockScannerSetUiIdle()

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        vm.handleMissingFingerButtonPressed()
        assertThat(vm.stateLiveData.value?.currentCaptureState()).isEqualTo(CaptureState.Skipped)
        assertThat(vm.stateLiveData.value?.isShowingSplashScreen).isTrue()
        advanceTimeBy(TIME_SKIP_MS)
        assertThat(vm.stateLiveData.value?.fingerStates?.size).isEqualTo(3)
        advanceTimeBy(TIME_SKIP_MS)
        assertThat(vm.stateLiveData.value?.isShowingSplashScreen).isFalse()
        assertThat(vm.stateLiveData.value?.currentFingerIndex).isEqualTo(1)

        coVerify { addCaptureEventsUseCase.invoke(any(), any(), any(), any()) }
    }

    @Test
    fun receivesOnlyMissingFingersThenConfirm_showsToastAndResetsState() = runTest {
        mockScannerSetUiIdle()

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        repeat(4) { // 2 original + 2 auto-added
            vm.handleMissingFingerButtonPressed()
            advanceTimeBy(TIME_SKIP_MS)
        }

        assertThat(vm.stateLiveData.value).isEqualTo(
            CollectFingerprintsState(
                fingerStates = FOUR_FINGERS_IDS.map {
                    FingerState(
                        it, listOf(CaptureState.Skipped)
                    )
                },
                currentFingerIndex = 3,
                isAskingRescan = false,
                isShowingConfirmDialog = true,
                isShowingSplashScreen = false,
                isShowingConnectionScreen = false,
            )
        )
        coVerify(exactly = 4) { addCaptureEventsUseCase.invoke(any(), any(), any(), any()) }

        vm.handleConfirmFingerprintsAndContinue()

        vm.noFingersScannedToast.assertEventReceived()
        assertThat(vm.stateLiveData.value).isEqualTo(
            CollectFingerprintsState(
                fingerStates = TWO_FINGERS_IDS.map {
                    FingerState(
                        it, listOf(CaptureState.NotCollected)
                    )
                },
                currentFingerIndex = 0,
                isAskingRescan = false,
                isShowingConfirmDialog = false,
                isShowingSplashScreen = false,
                isShowingConnectionScreen = false,
            )
        )
    }

    @Test
    @ExperimentalTime
    fun receivesMixOfScanResults_withImageTransfer_updatesStateCorrectlyAndReturnsFingersCorrectly() = runTest {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(
            BAD_SCAN,
            NO_FINGER_DETECTED,
            BAD_SCAN,
            GOOD_SCAN,
            BAD_SCAN,
            NO_FINGER_DETECTED,
            NO_FINGER_DETECTED, // skipped
            NO_FINGER_DETECTED,
            BAD_SCAN,
            BAD_SCAN,
            BAD_SCAN,
            NO_FINGER_DETECTED,
            GOOD_SCAN
        )
        acquireImageResponses(OK)
        withImageTransfer()

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)

        // Finger 1
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        advanceTimeBy(TIME_SKIP_MS)

        // Finger 2
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        vm.handleMissingFingerButtonPressed()
        advanceTimeBy(TIME_SKIP_MS)

        // Finger 3
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        advanceTimeBy(TIME_SKIP_MS)

        // Finger 4
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()

        assertThat(vm.stateLiveData.value).isEqualTo(
            CollectFingerprintsState(
                fingerStates = listOf(
                    FingerState(
                        FOUR_FINGERS_IDS[0],
                        listOf(
                            CaptureState.ScanProcess.Collected(
                                numberOfBadScans = 2,
                                numberOfNoFingerDetectedScans = 1,
                                scanResult = ScanResult(GOOD_QUALITY, TEMPLATE, TEMPLATE_FORMAT, IMAGE, 60),
                            )
                        )
                    ),
                    FingerState(FOUR_FINGERS_IDS[1], listOf(CaptureState.Skipped)),
                    FingerState(
                        FOUR_FINGERS_IDS[2],
                        listOf(
                            CaptureState.ScanProcess.Collected(
                                numberOfBadScans = 3,
                                numberOfNoFingerDetectedScans = 1,
                                scanResult = ScanResult(BAD_QUALITY, TEMPLATE, TEMPLATE_FORMAT, IMAGE, 60),
                            )
                        )
                    ),
                    FingerState(
                        FOUR_FINGERS_IDS[3],
                        listOf(
                            CaptureState.ScanProcess.Collected(
                                numberOfBadScans = 0,
                                numberOfNoFingerDetectedScans = 1,
                                scanResult = ScanResult(GOOD_QUALITY, TEMPLATE, TEMPLATE_FORMAT, IMAGE, 60),
                            )
                        )
                    )
                ),
                currentFingerIndex = 3,
                isAskingRescan = false,
                isShowingConfirmDialog = true,
                isShowingSplashScreen = false,
                isShowingConnectionScreen = false,
            )
        )
        coVerify(exactly = 14) { addCaptureEventsUseCase.invoke(any(), any(), any(), any()) }

        vm.handleConfirmFingerprintsAndContinue()
        coVerify(exactly = 3) { saveImageUseCase.invoke(any(), any(), any(), any()) }

        vm.finishWithFingerprints.assertEventReceivedWithContentAssertions { actualFingerprints ->
            assertThat(actualFingerprints?.results).hasSize(3)
            assertThat(actualFingerprints?.results?.map { it.identifier }).containsExactly(
                IFingerIdentifier.LEFT_THUMB,
                IFingerIdentifier.RIGHT_THUMB,
                IFingerIdentifier.RIGHT_INDEX_FINGER
            )
            actualFingerprints?.results?.forEach {
                assertThat(it.sample?.template).isEqualTo(TEMPLATE)
                assertThat(it.sample?.imageRef).isNotNull()
            }
        }
    }

    @Test
    @ExperimentalTime
    fun receivesMixOfScanResults_withEagerImageTransfer_updatesStateCorrectlyAndReturnsFingersCorrectly() = runTest {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(
            BAD_SCAN,
            NO_FINGER_DETECTED,
            BAD_SCAN,
            GOOD_SCAN,
            BAD_SCAN,
            NO_FINGER_DETECTED,
            NO_FINGER_DETECTED, // skipped
            NO_FINGER_DETECTED,
            BAD_SCAN,
            BAD_SCAN,
            BAD_SCAN,
            NO_FINGER_DETECTED,
            GOOD_SCAN
        )
        acquireImageResponses(OK)
        withImageTransfer(strategy = ImageSavingStrategy.EAGER)

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)

        // Finger 1
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        advanceTimeBy(TIME_SKIP_MS)

        // Finger 2
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        vm.handleMissingFingerButtonPressed()
        advanceTimeBy(TIME_SKIP_MS)

        // Finger 3
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        advanceTimeBy(TIME_SKIP_MS)

        // Finger 4
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        advanceTimeBy(TIME_SKIP_MS)

        assertThat(vm.stateLiveData.value).isEqualTo(
            CollectFingerprintsState(
                fingerStates = listOf(
                    FingerState(
                        FOUR_FINGERS_IDS[0],
                        listOf(
                            CaptureState.ScanProcess.Collected(
                                numberOfBadScans = 2,
                                numberOfNoFingerDetectedScans = 1,
                                scanResult = ScanResult(GOOD_QUALITY, TEMPLATE, TEMPLATE_FORMAT, IMAGE, 60),
                            )
                        )
                    ),
                    FingerState(FOUR_FINGERS_IDS[1], listOf(CaptureState.Skipped)),
                    FingerState(
                        FOUR_FINGERS_IDS[2],
                        listOf(
                            CaptureState.ScanProcess.Collected(
                                numberOfBadScans = 3,
                                numberOfNoFingerDetectedScans = 1,
                                scanResult = ScanResult(BAD_QUALITY, TEMPLATE, TEMPLATE_FORMAT, IMAGE, 60),
                            )
                        )
                    ),
                    FingerState(
                        FOUR_FINGERS_IDS[3],
                        listOf(
                            CaptureState.ScanProcess.Collected(
                                numberOfBadScans = 0,
                                numberOfNoFingerDetectedScans = 1,
                                scanResult = ScanResult(GOOD_QUALITY, TEMPLATE, TEMPLATE_FORMAT, IMAGE, 60),
                            )
                        )
                    )
                ),
                currentFingerIndex = 3,
                isAskingRescan = false,
                isShowingConfirmDialog = true,
                isShowingSplashScreen = false,
                isShowingConnectionScreen = false,
            )
        )

        coVerify(exactly = 14) { addCaptureEventsUseCase.invoke(any(), any(), any(), any()) }
        // If eager, expect that images were saved before confirm was pressed, including bad scans
        coVerify(exactly = 8) { saveImageUseCase.invoke(any(), any(), any(), any()) }

        vm.handleConfirmFingerprintsAndContinue()

        vm.finishWithFingerprints.assertEventReceivedWithContentAssertions { actualFingerprints ->
            assertThat(actualFingerprints?.results).hasSize(3)
            assertThat(actualFingerprints?.results?.map { it.identifier }).containsExactly(
                IFingerIdentifier.LEFT_THUMB,
                IFingerIdentifier.RIGHT_THUMB,
                IFingerIdentifier.RIGHT_INDEX_FINGER
            )
            actualFingerprints?.results?.forEach {
                assertThat(it.sample?.template).isEqualTo(TEMPLATE)
                assertThat(it.sample?.imageRef).isNotNull()
            }
        }
    }


    @Test
    @ExperimentalTime
    fun receivesMixOfScanResults_withGoodScansOnlyTransfer_updatesStateCorrectlyAndReturnsFingersCorrectly() = runTest {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(
            BAD_SCAN,
            BAD_SCAN,
            BAD_SCAN,

            BAD_SCAN,
            GOOD_SCAN,

            GOOD_SCAN,
        )
        acquireImageResponses(OK)
        withImageTransfer(strategy = ImageSavingStrategy.ONLY_GOOD_SCAN)

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)

        // Finger 1
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
       // vm.handleScanButtonPressed()
        advanceTimeBy(TIME_SKIP_MS)

        // Finger 3
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        advanceTimeBy(TIME_SKIP_MS)

        // Finger 4
       // vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        advanceTimeBy(TIME_SKIP_MS)

        assertThat(vm.stateLiveData.value).isEqualTo(
            CollectFingerprintsState(
                fingerStates = listOf(
                    FingerState(
                        FOUR_FINGERS_IDS[0],
                        listOf(
                            CaptureState.ScanProcess.Collected(
                                numberOfBadScans = 3,
                                numberOfNoFingerDetectedScans = 0,
                                ScanResult(BAD_QUALITY, TEMPLATE, TEMPLATE_FORMAT, null, 60),
                            )
                        )
                    ),
                    FingerState(
                        FOUR_FINGERS_IDS[1],
                        listOf(
                            CaptureState.ScanProcess.Collected(
                                numberOfBadScans = 1,
                                numberOfNoFingerDetectedScans = 0,
                                ScanResult(GOOD_QUALITY, TEMPLATE, TEMPLATE_FORMAT, IMAGE, 60),
                            )
                        )
                    ),
                    FingerState(
                        FOUR_FINGERS_IDS[2],
                        listOf(
                            CaptureState.ScanProcess.Collected(
                                numberOfBadScans = 0,
                                numberOfNoFingerDetectedScans = 0,
                                ScanResult(GOOD_QUALITY, TEMPLATE, TEMPLATE_FORMAT, IMAGE, 60),
                            )
                        )
                    )
                ),
                currentFingerIndex = 2,
                isAskingRescan = false,
                isShowingConfirmDialog = true,
                isShowingSplashScreen = false,
                isShowingConnectionScreen = false,
            )
        )
        coVerify(exactly = 6) { addCaptureEventsUseCase.invoke(any(), any(), any(), any()) }

        vm.handleConfirmFingerprintsAndContinue()
        // Save image is called even if scanResult.image == null
        coVerify(exactly = 3) { saveImageUseCase.invoke(any(), any(), any(),any()) }

        vm.finishWithFingerprints.assertEventReceivedWithContentAssertions { actualFingerprints ->
            assertThat(actualFingerprints?.results).hasSize(3)
            assertThat(actualFingerprints?.results?.map { it.identifier }).containsExactly(
                IFingerIdentifier.LEFT_THUMB,
                IFingerIdentifier.LEFT_INDEX_FINGER,
                IFingerIdentifier.RIGHT_THUMB
            )
            actualFingerprints?.results?.forEach {
                assertThat(it.sample?.template).isEqualTo(TEMPLATE)
                assertThat(it.sample?.imageRef).isNotNull()
            }
        }
    }

    @Test
    @ExperimentalTime
    fun pressingCancel_duringScan_cancelsProperly() = runTest {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(NEVER_RETURNS)
        withImageTransfer()

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        vm.handleScanButtonPressed()
        assertThat(vm.stateLiveData.value?.currentCaptureState()).isEqualTo(CaptureState.ScanProcess.Scanning(numberOfBadScans = 0,
            numberOfNoFingerDetectedScans = 0,))
        vm.handleScanButtonPressed()
        assertThat(vm.stateLiveData.value?.currentCaptureState()).isEqualTo(CaptureState.NotCollected)
    }

    @Test
    @ExperimentalTime
    fun pressingCancel_duringImageTransfer_doesNothing() = runTest {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(GOOD_SCAN)
        acquireImageResponses(MockAcquireImageResult.NEVER_RETURNS)
        withImageTransfer()

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        vm.handleScanButtonPressed()
        assertThat(vm.stateLiveData.value?.currentCaptureState()).isEqualTo(
            CaptureState.ScanProcess.TransferringImage(
                numberOfBadScans = 0,
                numberOfNoFingerDetectedScans = 0,
                scanResult = ScanResult(
                    qualityScore = GOOD_QUALITY,
                    template = TEMPLATE,
                    templateFormat = TEMPLATE_FORMAT,
                    image = null,
                    qualityThreshold = 60
                )
            )
        )
        vm.handleScanButtonPressed()
        assertThat(vm.stateLiveData.value?.currentCaptureState()).isEqualTo(
            CaptureState.ScanProcess.TransferringImage(
                numberOfBadScans = 0,
                numberOfNoFingerDetectedScans = 0,
                ScanResult(
                    GOOD_QUALITY, TEMPLATE, TEMPLATE_FORMAT, null, 60
                )
            )
        )
    }

    @Test
    @ExperimentalTime
    fun restartAfterScans_resetsStateCorrectly() = runTest {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(GOOD_SCAN)
        acquireImageResponses(OK)
        withImageTransfer()

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        vm.handleScanButtonPressed()
        advanceTimeBy(TIME_SKIP_MS)
        vm.handleMissingFingerButtonPressed()
        advanceTimeBy(TIME_SKIP_MS)
        vm.handleScanButtonPressed()
        advanceTimeBy(TIME_SKIP_MS)

        assertThat(vm.stateLiveData.value?.isShowingConfirmDialog).isTrue()

        vm.handleRestart()

        assertThat(vm.stateLiveData.value).isEqualTo(
            CollectFingerprintsState(
                fingerStates = TWO_FINGERS_IDS.map { FingerState(it, listOf(CaptureState.NotCollected)) },
                currentFingerIndex = 0,
                isAskingRescan = false,
                isShowingConfirmDialog = false,
                isShowingSplashScreen = false,
                isShowingConnectionScreen = false,
            )
        )
    }

    @Test
    @ExperimentalTime
    fun backPressed_whileScanning_cancelsScanning() = runTest {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(NEVER_RETURNS)
        withImageTransfer()

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        vm.handleScanButtonPressed()
        assertThat(vm.stateLiveData.value?.currentCaptureState()).isEqualTo(CaptureState.ScanProcess.Scanning(0, 0))
        vm.handleOnBackPressed()
        assertThat(vm.stateLiveData.value?.currentCaptureState()).isEqualTo(CaptureState.NotCollected)
    }

    @Test
    fun shouldLaunch_reconnectActivity_whenScanner_isNotAvailable() = runTest {
        every { scannerManager.isScannerConnected } returns false

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)

        assertThat(vm.stateLiveData.value?.currentCaptureState()).isEqualTo(CaptureState.NotCollected)
        vm.launchReconnect.assertEventReceived()
    }


    @Test
    @ExperimentalTime
    fun backPressed_whileTransferringImage_cancelsTransfer() = runTest {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(GOOD_SCAN)
        acquireImageResponses(MockAcquireImageResult.NEVER_RETURNS)
        withImageTransfer()

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        vm.handleScanButtonPressed()
        assertThat(vm.stateLiveData.value?.currentCaptureState()).isEqualTo(
            CaptureState.ScanProcess.TransferringImage(
                numberOfBadScans = 0,
                numberOfNoFingerDetectedScans = 0,
                ScanResult(
                    GOOD_QUALITY, TEMPLATE, TEMPLATE_FORMAT, null, 60
                )
            )
        )
        vm.handleOnBackPressed()
        assertThat(vm.stateLiveData.value?.currentCaptureState()).isEqualTo(CaptureState.NotCollected)
    }

    @Test
    @ExperimentalTime
    fun backPressed_whileIdle_doesNothing() = runTest {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(GOOD_SCAN)
        noImageTransfer()

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        vm.handleScanButtonPressed()
        vm.handleOnBackPressed()
        assertThat(vm.stateLiveData.value?.currentCaptureState()).isEqualTo(
            CaptureState.ScanProcess.Collected(
                numberOfBadScans = 0,
                numberOfNoFingerDetectedScans = 0,
                scanResult = ScanResult(
                    qualityScore = GOOD_QUALITY,
                    template = TEMPLATE,
                    templateFormat = TEMPLATE_FORMAT,
                    image = null,
                    qualityThreshold = 60
                )
            )
        )
    }

    @Test
    @ExperimentalTime
    fun unexpectedErrorWhileScanning_launchesAlert() = runTest {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(UNKNOWN_ERROR)
        noImageTransfer()

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        vm.handleScanButtonPressed()
        assertThat(vm.stateLiveData.value?.currentCaptureState()).isEqualTo(CaptureState.NotCollected)

        vm.launchAlert.assertEventReceived()
    }

    @Test
    fun onResumeCalled_registersScannerTrigger() = runTest {
        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        vm.handleOnResume()

        verify { scanner.registerTriggerListener(any()) }
    }

    @Test
    fun onPauseCalled_unregistersScannerTrigger() = runTest {
        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        vm.handleOnPause()

        verify { scanner.unregisterTriggerListener(any()) }
    }

    @Test
    fun whenStart_AndLiveFeedbackIsEnabled_liveFeedbackIsStarted() = runTest {
        setupLiveFeedbackOn()

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)

        coVerify { scanner.startLiveFeedback() }
        assertThat(vm.liveFeedbackState).isEqualTo(LiveFeedbackState.START)
    }

    @Test
    fun whenStart_AndLiveFeedbackIsNotEnabled_liveFeedbackIsNotStarted() = runTest {
        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)

        coVerify(exactly = 0) { scanner.startLiveFeedback() }
    }

    @Test
    fun `test init bioSDK success should show invalid license dialog if init fail`() = runTest {
        coEvery {
            bioSdkWrapper.initialize()
        } throws  BioSdkException.BioSdkInitializationException(Exception())
        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)

        vm.invalidLicense.assertEventReceived()
    }

    @Test
    @ExperimentalTime
    fun whenScanButtonPressed_AndLiveFeedbackIsEnabled_liveFeedbackIsPaused() = runTest {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(NEVER_RETURNS)
        setupLiveFeedbackOn()

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        vm.handleScanButtonPressed()

        assertThat(vm.liveFeedbackState).isEqualTo(LiveFeedbackState.PAUSE)
    }

    @Test
    @ExperimentalTime
    fun whenGoodScan_AndLiveFeedbackIsEnabled_liveFeedbackIsStarted() = runTest {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(GOOD_SCAN)
        setupLiveFeedbackOn()

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        vm.handleScanButtonPressed()

        coVerify { scanner.startLiveFeedback() }
        assertThat(vm.liveFeedbackState).isEqualTo(LiveFeedbackState.START)
    }

    @Test
    @ExperimentalTime
    fun whenBadScan_AndLiveFeedbackIsEnabled_liveFeedbackIsStarted() = runTest {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(BAD_SCAN)
        setupLiveFeedbackOn()

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        vm.handleScanButtonPressed()

        coVerify { scanner.startLiveFeedback() }
        assertThat(vm.liveFeedbackState).isEqualTo(LiveFeedbackState.START)
    }

    @Test
    @ExperimentalTime
    fun whenSecondScan_AndLiveFeedbackIsEnabled_liveFeedbackIsPaused() = runTest {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(GOOD_SCAN, NEVER_RETURNS)
        setupLiveFeedbackOn()

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        vm.handleScanButtonPressed()
        advanceTimeBy(TIME_SKIP_MS)
        vm.handleScanButtonPressed()

        assertThat(vm.liveFeedbackState).isEqualTo(LiveFeedbackState.PAUSE)
    }

    @Test
    @ExperimentalTime
    fun whenEndOfWorkflow_AndLiveFeedbackIsEnabled_liveFeedbackIsStopped() = runTest {
        mockScannerSetUiIdle()
        setupLiveFeedbackOn()

        getToEndOfWorkflow()

        coVerify { scanner.stopLiveFeedback() }
        assertThat(vm.liveFeedbackState).isEqualTo(LiveFeedbackState.STOP)
    }

    @Test
    fun whenRestart_AndLiveFeedbackIsEnabled_liveFeedbackIsStarted() = runTest {
        mockScannerSetUiIdle()
        setupLiveFeedbackOn()

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        vm.handleRestart()

        coVerify { scanner.startLiveFeedback() }
        assertThat(vm.liveFeedbackState).isEqualTo(LiveFeedbackState.START)
    }

    @Test
    fun whenPause_AndLiveFeedbackIsEnabled_liveFeedbackIsStopped() = runTest {
        setupLiveFeedbackOn()

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        vm.handleOnPause()

        coVerify { scanner.stopLiveFeedback() }
        assertThat(vm.liveFeedbackState).isEqualTo(LiveFeedbackState.STOP)
    }

    @Test
    fun whenResume_AndLiveFeedbackWasStarted_liveFeedbackIsStarted() = runTest {
        setupLiveFeedbackOn()

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        vm.handleOnPause()
        vm.handleOnResume()

        coVerify { scanner.startLiveFeedback() }
        assertThat(vm.liveFeedbackState).isEqualTo(LiveFeedbackState.START)
    }

    @Test
    @ExperimentalTime
    fun whenResume_AndLiveFeedbackWasStopped_liveFeedbackIsStopped() = runTest {
        mockScannerSetUiIdle()
        setupLiveFeedbackOn()

        getToEndOfWorkflow()
        vm.handleOnPause()
        vm.handleOnResume()

        coVerify { scanner.stopLiveFeedback() }
        assertThat(vm.liveFeedbackState).isEqualTo(LiveFeedbackState.STOP)
    }

    @Test
    fun whenScannerDisconnects_AndUserSelectsDifferentFinger_updatesStateCorrectlyAndReconnects() = runTest {
        mockScannerSetUiIdle()
        coEvery { scanner.setUiIdle() } throws ScannerDisconnectedException()

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        vm.updateSelectedFinger(1)

        assertThat(vm.stateLiveData.value?.currentCaptureState()).isEqualTo(CaptureState.NotCollected)
        vm.launchReconnect.assertEventReceived()
    }

    @Test
    @ExperimentalTime
    fun given3BadScans_whenNoFingerDetected_doesNotAddFingerprintCaptureBiometricsEvent() = runTest {
        mockScannerSetUiIdle()
        setupCaptureFingerprintResponses(BAD_SCAN, BAD_SCAN, BAD_SCAN, NO_FINGER_DETECTED)
        noImageTransfer()

        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()
        vm.handleScanButtonPressed()

        assertThat(vm.stateLiveData.value?.isShowingSplashScreen).isTrue()
        advanceTimeBy(TIME_SKIP_MS)
        assertThat(vm.stateLiveData.value?.fingerStates?.size).isEqualTo(3)
        advanceTimeBy(TIME_SKIP_MS)
        assertThat(vm.stateLiveData.value?.isShowingSplashScreen).isFalse()
        assertThat(vm.stateLiveData.value?.currentFingerIndex).isEqualTo(1)

        vm.updateSelectedFinger(0)
        assertThat(vm.stateLiveData.value?.currentFingerIndex).isEqualTo(0)

        vm.handleScanButtonPressed()

        assertThat(vm.stateLiveData.value?.currentCaptureState()).isEqualTo(
            /* expected = */ CaptureState.ScanProcess.NotDetected(
                numberOfBadScans = 3,
                numberOfNoFingerDetectedScans = 1
            )
        )

        coVerify(exactly = 4) { addCaptureEventsUseCase.invoke(any(), any(), any(), any()) }

    }

    @ExperimentalTime
    private fun getToEndOfWorkflow() = runTest {
        setupCaptureFingerprintResponses(GOOD_SCAN)
        vm.handleOnViewCreated(TWO_FINGERS_IDS, SECUGEN_SIM_MATCHER)
        repeat(2) {
            vm.handleScanButtonPressed()
            advanceTimeBy(TIME_SKIP_MS)
        }
    }

    private fun noImageTransfer() {
        every { vero2Configuration.imageSavingStrategy } returns ImageSavingStrategy.NEVER
    }

    private fun withImageTransfer(strategy: ImageSavingStrategy = ImageSavingStrategy.ONLY_USED_IN_REFERENCE) {
        every { vero2Configuration.imageSavingStrategy } returns strategy
        coEvery { saveImageUseCase.invoke(any(), any(), any(), any()) } returns mockk {
            every { relativePath } returns Path(emptyArray())
        }
    }

    private fun mockScannerSetUiIdle() {
        coJustRun { scanner.setUiIdle() }
    }

    @ExperimentalTime
    private fun setupCaptureFingerprintResponses(vararg mockResponses: MockCaptureFingerprintResponse) {
        val initialMock = coEvery { bioSdkWrapper.acquireFingerprintTemplate(any(), any(), any(),any()) }
        val fingerprintResponses = mockResponses.map { it.toCaptureFingerprintResponse() }

        // capture the first response in the list
        val firstResponse = fingerprintResponses.first()
        val subsequentMock = when {
            mockResponses[0] == NEVER_RETURNS -> initialMock.coAnswers { neverReturnResponse() }
            firstResponse is Throwable -> initialMock.throws(firstResponse)
            else -> initialMock.returns(firstResponse as AcquireFingerprintTemplateResponse)
        }

        // capture subsequent responses except the first.
        fingerprintResponses.forEachIndexed { index, response ->
            // skip the first response
            if (index != 0) {
                when {
                    mockResponses[index] == NEVER_RETURNS -> subsequentMock.coAndThen { neverReturnResponse() }
                    response is Throwable -> subsequentMock.andThenThrows(response)
                    else -> subsequentMock.andThen(response as AcquireFingerprintTemplateResponse)
                }
            }
        }
    }

    @ExperimentalTime
    private fun acquireImageResponses(response: MockAcquireImageResult) {
        val mock = coEvery { bioSdkWrapper.acquireFingerprintImage() }
        when (response) {
            OK -> mock.returns(AcquireFingerprintImageResponse(IMAGE))
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
            GOOD_SCAN -> AcquireFingerprintTemplateResponse(TEMPLATE, TEMPLATE_FORMAT, GOOD_QUALITY)
            DIFFERENT_GOOD_SCAN -> AcquireFingerprintTemplateResponse(
                DIFFERENT_TEMPLATE, TEMPLATE_FORMAT, DIFFERENT_GOOD_QUALITY
            )

            BAD_SCAN -> AcquireFingerprintTemplateResponse(TEMPLATE, TEMPLATE_FORMAT, BAD_QUALITY)
            NO_FINGER_DETECTED -> NoFingerDetectedException("No finger detected")
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
        val TWO_FINGERS_IDS = listOf(
            IFingerIdentifier.LEFT_THUMB,
            IFingerIdentifier.LEFT_INDEX_FINGER
        )
        val FOUR_FINGERS_IDS = listOf(
            IFingerIdentifier.LEFT_THUMB,
            IFingerIdentifier.LEFT_INDEX_FINGER,
            IFingerIdentifier.RIGHT_THUMB,
            IFingerIdentifier.RIGHT_INDEX_FINGER
        )

        const val GOOD_QUALITY = 80
        const val TEMPLATE_FORMAT = "ISO_19794_2"
        const val DIFFERENT_GOOD_QUALITY = 80
        const val BAD_QUALITY = 20

        val TEMPLATE = FingerprintGenerator.generateRandomFingerprint().template
        val DIFFERENT_TEMPLATE = FingerprintGenerator.generateRandomFingerprint().template

        val IMAGE = byteArrayOf(0x05, 0x06, 0x07, 0x08)

        private const val TIME_SKIP_MS = 3000L
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}
