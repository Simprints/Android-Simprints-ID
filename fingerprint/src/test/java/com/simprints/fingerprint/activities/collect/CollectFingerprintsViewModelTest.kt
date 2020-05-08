package com.simprints.fingerprint.activities.collect

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.EncodingUtils
import com.simprints.fingerprint.activities.collect.domain.Finger
import com.simprints.fingerprint.activities.collect.domain.FingerConfig
import com.simprints.fingerprint.activities.collect.state.CollectFingerprintsState
import com.simprints.fingerprint.activities.collect.state.FingerCollectionState
import com.simprints.fingerprint.activities.collect.state.ScanResult
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
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.mock.declareModule
import java.lang.Thread.sleep

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
    private val preferencesManager: FingerprintPreferencesManager = mockk(relaxed = true)
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
            fingerStates = TWO_FINGERS_IDS.toFingers()
                .associateWith { FingerCollectionState.NotCollected }.toMutableMap(),
            currentFingerIndex = 0,
            isAskingRescan = false,
            isShowingConfirmDialog = false,
            isShowingSplashScreen = false
        ))
    }

    @Test
    fun scanPressed_noImageTransfer_updatesStateToScanningDuringScan() {
        every { scanner.setUiIdle() } returns Completable.complete()
        every { scanner.captureFingerprint(any(), any(), any()) } returns Single.never()
        every { preferencesManager.saveFingerprintImagesStrategy } returns SaveFingerprintImagesStrategy.NEVER

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed(false)

        assertThat(vm.state().currentFingerState()).isEqualTo(FingerCollectionState.Scanning())
    }

    @Test
    fun scanPressed_withImageTransfer_updatesStateToTransferringImageAfterScan() {
        val qualityScore = 80
        val template = byteArrayOf(0x01, 0x02, 0x03)
        every { scanner.setUiIdle() } returns Completable.complete()
        every { scanner.captureFingerprint(any(), any(), any()) } returns Single.just(CaptureFingerprintResponse(template, qualityScore))
        every { scanner.acquireImage(any()) } returns Single.never()
        every { preferencesManager.saveFingerprintImagesStrategy } returns SaveFingerprintImagesStrategy.WSQ_15

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed(false)

        assertThat(vm.state().currentFingerState()).isEqualTo(FingerCollectionState.TransferringImage(ScanResult(qualityScore, template, null)))
    }

    @Test
    fun scanPressed_noImageTransfer_goodScan_updatesStatesCorrectlyAndCreatesEvent() {
        val qualityScore = 80
        val template = byteArrayOf(0x01, 0x02, 0x03)
        every { scanner.setUiIdle() } returns Completable.complete()
        every { scanner.captureFingerprint(any(), any(), any()) } returns Single.just(CaptureFingerprintResponse(template, qualityScore))
        every { preferencesManager.saveFingerprintImagesStrategy } returns SaveFingerprintImagesStrategy.NEVER

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed(false)

        assertThat(vm.state().currentFingerState()).isEqualTo(FingerCollectionState.Collected(ScanResult(qualityScore, template, null)))
        assertThat(vm.vibrate.value).isNotNull()
        verify { sessionEventsManager.addEventInBackground(any()) }
    }

    @Test
    fun scanPressed_withImageTransfer_goodScan_updatesStatesCorrectlyAndCreatesEvent() {
        val qualityScore = 80
        val template = byteArrayOf(0x01, 0x02, 0x03)
        val image = byteArrayOf(0x05, 0x06, 0x07, 0x08)
        every { scanner.setUiIdle() } returns Completable.complete()
        every { scanner.captureFingerprint(any(), any(), any()) } returns Single.just(CaptureFingerprintResponse(template, qualityScore))
        every { scanner.acquireImage(any()) } returns Single.just(AcquireImageResponse(image))
        every { preferencesManager.saveFingerprintImagesStrategy } returns SaveFingerprintImagesStrategy.WSQ_15

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed(false)

        assertThat(vm.state().currentFingerState()).isEqualTo(FingerCollectionState.Collected(ScanResult(qualityScore, template, image)))
        assertThat(vm.vibrate.value).isNotNull()
        verify { sessionEventsManager.addEventInBackground(any()) }
    }

    @Test
    fun scanPressed_noImageTransfer_badScan_updatesStatesCorrectlyAndCreatesEvent() {
        val qualityScore = 20
        val template = byteArrayOf(0x01, 0x02, 0x03)
        every { scanner.setUiIdle() } returns Completable.complete()
        every { scanner.captureFingerprint(any(), any(), any()) } returns Single.just(CaptureFingerprintResponse(template, qualityScore))
        every { preferencesManager.saveFingerprintImagesStrategy } returns SaveFingerprintImagesStrategy.NEVER

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed(false)

        assertThat(vm.state().currentFingerState()).isEqualTo(FingerCollectionState.Collected(ScanResult(qualityScore, template, null), 1))
        assertThat(vm.vibrate.value).isNotNull()
        verify { sessionEventsManager.addEventInBackground(any()) }
    }

    @Test
    fun scanPressed_withImageTransfer_badScan_doesNotTransferImage_updatesStatesCorrectlyAndCreatesEvent() {
        val qualityScore = 20
        val template = byteArrayOf(0x01, 0x02, 0x03)
        every { scanner.setUiIdle() } returns Completable.complete()
        every { scanner.captureFingerprint(any(), any(), any()) } returns Single.just(CaptureFingerprintResponse(template, qualityScore))
        every { preferencesManager.saveFingerprintImagesStrategy } returns SaveFingerprintImagesStrategy.WSQ_15

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed(false)

        assertThat(vm.state().currentFingerState()).isEqualTo(FingerCollectionState.Collected(ScanResult(qualityScore, template, null), 1))
        assertThat(vm.vibrate.value).isNotNull()
        verify { sessionEventsManager.addEventInBackground(any()) }
    }

    @Test
    fun scanPressed_noFingerDetected_updatesStatesCorrectlyAndCreatesEvent() {
        every { scanner.setUiIdle() } returns Completable.complete()
        every { scanner.captureFingerprint(any(), any(), any()) } returns Single.error(NoFingerDetectedException())
        every { preferencesManager.saveFingerprintImagesStrategy } returns SaveFingerprintImagesStrategy.WSQ_15

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed(false)

        assertThat(vm.state().currentFingerState()).isEqualTo(FingerCollectionState.NotDetected())
        assertThat(vm.vibrate.value).isNotNull()
        verify { sessionEventsManager.addEventInBackground(any()) }
    }

    @Test
    fun scanPressed_scannerDisconnectedDuringScan_updatesStateCorrectlyAndReconnects() {
        every { scanner.setUiIdle() } returns Completable.complete()
        every { scanner.captureFingerprint(any(), any(), any()) } returns Single.error(ScannerDisconnectedException())
        every { preferencesManager.saveFingerprintImagesStrategy } returns SaveFingerprintImagesStrategy.WSQ_15

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed(false)

        assertThat(vm.state().currentFingerState()).isEqualTo(FingerCollectionState.NotCollected)
        assertThat(vm.launchReconnect.value).isNotNull()
    }

    @Test
    fun scanPressed_scannerDisconnectedDuringTransfer_updatesStateCorrectlyAndReconnects() {
        val qualityScore = 80
        val template = byteArrayOf(0x01, 0x02, 0x03)
        every { scanner.setUiIdle() } returns Completable.complete()
        every { scanner.captureFingerprint(any(), any(), any()) } returns Single.just(CaptureFingerprintResponse(template, qualityScore))
        every { scanner.acquireImage(any()) } returns Single.error(ScannerDisconnectedException())
        every { preferencesManager.saveFingerprintImagesStrategy } returns SaveFingerprintImagesStrategy.WSQ_15

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed(false)

        assertThat(vm.state().currentFingerState()).isEqualTo(FingerCollectionState.NotCollected)
        assertThat(vm.launchReconnect.value).isNotNull()
    }

    @Test
    fun scanPressed_withImageTransfer_badScanLastAttempt_transfersImage_updatesStatesCorrectlyAndCreatesEvent() {
        val qualityScore = 20
        val template = byteArrayOf(0x01, 0x02, 0x03)
        val image = byteArrayOf(0x05, 0x06, 0x07, 0x08)
        every { scanner.setUiIdle() } returns Completable.complete()
        every { scanner.captureFingerprint(any(), any(), any()) } returns Single.just(CaptureFingerprintResponse(template, qualityScore))
        every { scanner.acquireImage(any()) } returns Single.just(AcquireImageResponse(image))
        every { preferencesManager.saveFingerprintImagesStrategy } returns SaveFingerprintImagesStrategy.WSQ_15

        vm.start(TWO_FINGERS_IDS)
        vm.handleScanButtonPressed(false)
        assertThat(vm.state().currentFingerState()).isEqualTo(FingerCollectionState.Collected(ScanResult(qualityScore, template, null), 1))
        vm.handleScanButtonPressed(false)
        assertThat(vm.state().currentFingerState()).isEqualTo(FingerCollectionState.Collected(ScanResult(qualityScore, template, null), 2))
        vm.handleScanButtonPressed(false)
        assertThat(vm.state().currentFingerState()).isEqualTo(FingerCollectionState.Collected(ScanResult(qualityScore, template, image), 3))

        assertThat(vm.state().isShowingSplashScreen)
        mockTimer.executeNextTask()
        assertThat(vm.state().fingerStates.size).isEqualTo(3)
        mockTimer.executeNextTask()
        assertThat(vm.state().currentFingerIndex).isEqualTo(1)

        verify(exactly = 1) { scanner.acquireImage(any()) }
        verify(exactly = 3) { sessionEventsManager.addEventInBackground(any()) }
    }

    @Test
    fun scanPressed_receivesOnlyBadScans_performsImageTransferEventually_correctlyAddsFingersAndResultsInCorrectState() {
        val qualityScore = 20
        val template = byteArrayOf(0x01, 0x02, 0x03)
        val image = byteArrayOf(0x05, 0x06, 0x07, 0x08)
        every { scanner.setUiIdle() } returns Completable.complete()
        every { scanner.captureFingerprint(any(), any(), any()) } returns Single.just(CaptureFingerprintResponse(template, qualityScore))
        every { scanner.acquireImage(any()) } returns Single.just(AcquireImageResponse(image))
        every { preferencesManager.saveFingerprintImagesStrategy } returns SaveFingerprintImagesStrategy.WSQ_15

        vm.start(TWO_FINGERS_IDS)
        repeat(12) { // 3 times for each of the 4 fingers (2 original + 2 auto-added)
            vm.handleScanButtonPressed(false)
            mockTimer.executeAllTasks()
        }

        assertThat(vm.state()).isEqualTo(CollectFingerprintsState(
            fingerStates = FOUR_FINGERS_IDS.toFingers()
                .associateWith { FingerCollectionState.Collected(ScanResult(qualityScore, template, image), numberOfBadScans = 3) }.toMutableMap(),
            currentFingerIndex = 3,
            isAskingRescan = false,
            isShowingConfirmDialog = true,
            isShowingSplashScreen = false
        ))
        verify(exactly = 12) { sessionEventsManager.addEventInBackground(any()) }
    }

    private fun List<FingerIdentifier>.toFingers(fingerConfig: FingerConfig = FingerConfig.DEFAULT) =
        map { Finger(it, fingerConfig.getPriority(it), fingerConfig.getOrder(it)) }

    companion object {
        val TWO_FINGERS_IDS = listOf(FingerIdentifier.LEFT_THUMB, FingerIdentifier.LEFT_INDEX_FINGER)
        val FOUR_FINGERS_IDS = listOf(FingerIdentifier.LEFT_THUMB, FingerIdentifier.LEFT_INDEX_FINGER, FingerIdentifier.RIGHT_THUMB, FingerIdentifier.RIGHT_INDEX_FINGER)
    }
}
