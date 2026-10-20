package com.simprints.feature.externalcredential.screens.scanocr

import android.graphics.Bitmap
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.*
import com.jraska.livedata.test
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.externalcredential.screens.scanocr.model.LightingConditionsAssessment
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType
import com.simprints.feature.externalcredential.screens.scanocr.model.ScannedMfidDocument
import com.simprints.feature.externalcredential.screens.scanocr.usecase.BuildScannedCredentialResultUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.GetLightingConditionsAssessmentConfigUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.GetLightingConditionsAssessmentUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.ScanMfidDocumentUseCase
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredentialResult
import com.simprints.infra.camera.Frame
import com.simprints.infra.camera.postprocess.FrameCropToTargetUseCase
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.MFID_LIGHTING_CONDITIONS_ASSESSMENT_ENABLED
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.resources.R
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
internal class ExternalCredentialScanOcrViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var timeHelper: TimeHelper

    @MockK
    private lateinit var frameCropToTargetUseCase: FrameCropToTargetUseCase

    @MockK
    private lateinit var scanMfidDocumentUseCase: ScanMfidDocumentUseCase

    @MockK
    private lateinit var buildScannedCredentialResultUseCase: BuildScannedCredentialResultUseCase

    @MockK
    private lateinit var getLightingConditionsAssessment: GetLightingConditionsAssessmentUseCase

    @MockK
    private lateinit var configRepository: ConfigRepository

    @MockK
    private lateinit var frame: Frame

    private lateinit var viewModel: ExternalCredentialScanOcrViewModel

    private val documentType = OcrDocumentType.NhisCard

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = initViewModel(documentType)

        every { timeHelper.now() } returns Timestamp(1L)
    }

    private fun initViewModel(
        documentType: OcrDocumentType,
        customConfig: Map<String, JsonPrimitive> = emptyMap(),
    ): ExternalCredentialScanOcrViewModel {
        val projectConfiguration = mockk<ProjectConfiguration>(relaxed = true) {
            every { custom } returns customConfig
        }
        coEvery { configRepository.getProjectConfiguration() } returns projectConfiguration

        return ExternalCredentialScanOcrViewModel(
            ocrDocumentType = documentType,
            timeHelper = timeHelper,
            frameCropToTargetUseCase = frameCropToTargetUseCase,
            scanMfidDocumentUseCase = scanMfidDocumentUseCase,
            buildScannedCredentialResultUseCase = buildScannedCredentialResultUseCase,
            getLightingConditionsAssessmentConfig = GetLightingConditionsAssessmentConfigUseCase(configRepository),
            getLightingConditionsAssessment = getLightingConditionsAssessment,
            configRepository = configRepository,
            bgDispatcher = testCoroutineRule.testCoroutineDispatcher,
        )
    }

    @Test
    fun `startScanning updates state to ScanningInProgress`() {
        val observer = viewModel.scanOcrStateLiveData.test()
        viewModel.startScanning()

        val state = observer.value() as ScanOcrState.ScanningInProgress
        assertThat(state.ocrDocumentType).isEqualTo(documentType)
        assertThat(state.successfulCaptures).isEqualTo(0)
        assertThat(state.scansRequired).isEqualTo(3)
    }

    @Test
    fun `imageProcessingStopped resets the flag for image processing`() = runTest {
        viewModel.imageProcessingStarted()
        assertThat(viewModel.isProcessingImage.value).isTrue()

        viewModel.imageProcessingStopped()
        assertThat(viewModel.isProcessingImage.value).isFalse()
    }

    @Test
    fun `processImage updates detected blocks and state when OCR successful`() = runTest {
        val mockScannedDocument = mockk<ScannedMfidDocument>()
        val mockCroppedBitmap = mockk<Bitmap>()
        coEvery { frameCropToTargetUseCase(frame) } returns mockCroppedBitmap
        coEvery { scanMfidDocumentUseCase(mockCroppedBitmap, documentType, any()) } returns mockScannedDocument

        val observer = viewModel.scanOcrStateLiveData.test()
        viewModel.imageProcessingStarted()
        viewModel.startScanning()
        viewModel.processImage(frame)

        val state = observer.value() as ScanOcrState.ScanningInProgress
        assertThat(state.successfulCaptures).isEqualTo(1)
        assertThat(viewModel.isProcessingImage.value).isFalse()
        assertThat(viewModel.isOcrActive).isTrue()
    }

    @Test
    fun `processImage updates lighting conditions after debounce`() = runTest {
        val mockCroppedBitmap = mockk<Bitmap>()
        val lightingConditionsAssessment = LightingConditionsAssessment.TOO_DIM
        viewModel = initViewModel(
            documentType = documentType,
            customConfig = mapOf(
                MFID_LIGHTING_CONDITIONS_ASSESSMENT_ENABLED to JsonPrimitive(true),
            ),
        )
        runCurrent()
        coEvery { frameCropToTargetUseCase(frame) } returns mockCroppedBitmap
        coEvery { getLightingConditionsAssessment(mockCroppedBitmap, any()) } returns lightingConditionsAssessment

        val observer = viewModel.lightingConditionsAssessment.test()

        viewModel.processImage(frame)
        runCurrent()
        observer.assertNoValue()

        advanceTimeBy(499L)
        runCurrent()
        observer.assertNoValue()

        advanceTimeBy(1L) // full debounce delay now
        runCurrent()
        assertThat(observer.value()).isEqualTo(lightingConditionsAssessment)
        coVerify(exactly = 1) { getLightingConditionsAssessment(mockCroppedBitmap, any()) }
    }

    @Test
    fun `processImage skips normalization and OCR when scanning not in progress and lighting is disabled`() = runTest {
        val observer = viewModel.scanOcrStateLiveData.test()
        viewModel.processImage(frame)

        assertThat(observer.value()).isInstanceOf(ScanOcrState.NotScanning::class.java)
        coVerify(exactly = 0) { frameCropToTargetUseCase(frame) }
        coVerify(exactly = 0) { scanMfidDocumentUseCase.invoke(any(), any(), any()) }
        coVerify(exactly = 0) { getLightingConditionsAssessment.invoke(any(), any()) }
        assertThat(viewModel.isOcrActive).isFalse()
    }

    @Test
    fun `processImage updates lighting conditions when scanning not in progress`() = runTest {
        val mockCroppedBitmap = mockk<Bitmap>()
        val lightingConditionsAssessment = LightingConditionsAssessment.TOO_BRIGHT
        viewModel = initViewModel(
            documentType = documentType,
            customConfig = mapOf(
                MFID_LIGHTING_CONDITIONS_ASSESSMENT_ENABLED to JsonPrimitive(true),
            ),
        )
        runCurrent()
        coEvery { frameCropToTargetUseCase(any()) } returns mockCroppedBitmap
        coEvery { getLightingConditionsAssessment(mockCroppedBitmap, any()) } returns lightingConditionsAssessment

        val observer = viewModel.lightingConditionsAssessment.test()

        viewModel.processImage(frame)
        runCurrent()

        advanceTimeBy(500L) // debounce delay
        runCurrent()

        assertThat(observer.value()).isEqualTo(lightingConditionsAssessment)
        coVerify(exactly = 1) { getLightingConditionsAssessment(mockCroppedBitmap, any()) }
        coVerify(exactly = 0) { scanMfidDocumentUseCase.invoke(any(), any(), any()) }
    }

    @Test
    fun `processImage does not update lighting conditions when disabled in custom config`() = runTest {
        viewModel = initViewModel(
            documentType = documentType,
            customConfig = mapOf(
                MFID_LIGHTING_CONDITIONS_ASSESSMENT_ENABLED to JsonPrimitive(false),
            ),
        )
        runCurrent()

        viewModel.processImage(frame)
        runCurrent()

        advanceTimeBy(500L) // debounce delay
        runCurrent()

        coVerify(exactly = 0) { frameCropToTargetUseCase(frame) }
        coVerify(exactly = 0) { getLightingConditionsAssessment(any(), any()) }
    }

    @Test
    fun `processImage skips image processing when config is not yet initialized and scanning is not in progress`() = runTest {
        val configLoadingDeferred = CompletableDeferred<ProjectConfiguration>()
        val projectConfiguration = mockk<ProjectConfiguration>(relaxed = true) {
            every { custom } returns mapOf(
                MFID_LIGHTING_CONDITIONS_ASSESSMENT_ENABLED to JsonPrimitive(true),
            )
        }
        coEvery { configRepository.getProjectConfiguration() } coAnswers {
            configLoadingDeferred.await()
        }
        viewModel = ExternalCredentialScanOcrViewModel(
            ocrDocumentType = documentType,
            timeHelper = timeHelper,
            frameCropToTargetUseCase = frameCropToTargetUseCase,
            scanMfidDocumentUseCase = scanMfidDocumentUseCase,
            buildScannedCredentialResultUseCase = buildScannedCredentialResultUseCase,
            getLightingConditionsAssessmentConfig = GetLightingConditionsAssessmentConfigUseCase(configRepository),
            getLightingConditionsAssessment = getLightingConditionsAssessment,
            configRepository = configRepository,
            bgDispatcher = testCoroutineRule.testCoroutineDispatcher,
        )

        viewModel.processImage(frame)
        runCurrent()

        advanceTimeBy(500L) // debounce delay
        runCurrent()

        coVerify(exactly = 0) { frameCropToTargetUseCase(frame) }
        coVerify(exactly = 0) { getLightingConditionsAssessment(any(), any()) }

        configLoadingDeferred.complete(projectConfiguration)
    }

    @Test
    fun `processOcrResultsAndFinish sends finish event with scanned credential`() = runTest {
        val mockScannedCredentialResult = mockk<ScannedCredentialResult>()
        coEvery { buildScannedCredentialResultUseCase(any(), documentType, any()) } returns mockScannedCredentialResult

        val finishObserver = viewModel.finishOcrEvent.test()
        val stateObserver = viewModel.scanOcrStateLiveData.test()

        viewModel.startScanning()
        viewModel.processOcrResultsAndFinish()

        assertThat(finishObserver.value()?.peekContent()).isEqualTo(mockScannedCredentialResult)
        assertThat(stateObserver.value()).isEqualTo(ScanOcrState.Complete)
        assertThat(viewModel.isOcrActive).isFalse()
    }

    @Test
    fun `processOcrResultsAndFinish passes accumulated documents to build use case`() = runTest {
        val mockScannedDocument = mockk<ScannedMfidDocument>()
        val mockScannedCredentialResult = mockk<ScannedCredentialResult>()
        val mockCroppedBitmap = mockk<Bitmap>()

        coEvery { frameCropToTargetUseCase(frame) } returns mockCroppedBitmap
        coEvery { scanMfidDocumentUseCase(mockCroppedBitmap, documentType, any()) } returns mockScannedDocument
        coEvery { buildScannedCredentialResultUseCase(any(), documentType, any()) } returns mockScannedCredentialResult

        viewModel.startScanning()
        viewModel.processImage(frame)
        viewModel.processOcrResultsAndFinish()

        coVerify { buildScannedCredentialResultUseCase(listOf(mockScannedDocument), documentType, any()) }
    }

    @Test
    fun `getDocumentTypeRes returns correct resource for NHIS card`() {
        viewModel = initViewModel(OcrDocumentType.NhisCard)
        val result = viewModel.getDocumentTypeRes()
        assertThat(result).isEqualTo(R.string.mfid_type_nhis_card)
    }

    @Test
    fun `getDocumentTypeRes returns correct resource for Ghana ID card`() {
        viewModel = initViewModel(OcrDocumentType.GhanaIdCard)
        val result = viewModel.getDocumentTypeRes()
        assertThat(result).isEqualTo(R.string.mfid_type_ghana_id_card)
    }

    @Test
    fun `getDocumentTypeRes returns correct resource for Fayda card`() {
        viewModel = initViewModel(OcrDocumentType.FaydaCard)
        val result = viewModel.getDocumentTypeRes()
        assertThat(result).isEqualTo(R.string.mfid_type_fayda_card)
    }

    @Test
    fun `processImage does not add documents or update state once required captures are reached`() = runTest {
        val mockScannedDocument = mockk<ScannedMfidDocument>()
        val mockCroppedBitmap = mockk<Bitmap>()

        coEvery { frameCropToTargetUseCase(frame) } returns mockCroppedBitmap
        coEvery { scanMfidDocumentUseCase(mockCroppedBitmap, documentType, any()) } returns mockScannedDocument

        val observer = viewModel.scanOcrStateLiveData.test()
        viewModel.startScanning()

        val capturesRequired = (observer.value() as ScanOcrState.ScanningInProgress).scansRequired
        repeat(capturesRequired) { viewModel.processImage(frame) }

        val stateAtCapacity = observer.value() as ScanOcrState.ScanningInProgress
        assertThat(stateAtCapacity.successfulCaptures).isEqualTo(capturesRequired)

        viewModel.processImage(frame)

        assertThat(observer.value()).isEqualTo(stateAtCapacity)
    }

    @Test
    fun `processImage emits ScanningInProgress with enough captures exactly once`() = runTest {
        val mockScannedDocument = mockk<ScannedMfidDocument>()
        val mockCroppedBitmap = mockk<Bitmap>()

        coEvery { frameCropToTargetUseCase(frame) } returns mockCroppedBitmap
        coEvery { scanMfidDocumentUseCase(mockCroppedBitmap, documentType, any()) } returns mockScannedDocument

        val observer = viewModel.scanOcrStateLiveData.test()
        viewModel.startScanning()

        val capturesRequired = (observer.value() as ScanOcrState.ScanningInProgress).scansRequired
        repeat(capturesRequired + 2) { viewModel.processImage(frame) }

        val completionTriggers = observer
            .valueHistory()
            .filterIsInstance<ScanOcrState.ScanningInProgress>()
            .count { it.successfulCaptures >= it.scansRequired }
        assertThat(completionTriggers).isEqualTo(1)
    }

    @Test
    fun `processImage does not append documents or update state after scanning is complete`() = runTest {
        val mockScannedDocument = mockk<ScannedMfidDocument>()
        val mockScannedCredentialResult = mockk<ScannedCredentialResult>()
        val mockCroppedBitmap = mockk<Bitmap>()

        coEvery { frameCropToTargetUseCase(frame) } returns mockCroppedBitmap
        coEvery { scanMfidDocumentUseCase(mockCroppedBitmap, documentType, any()) } returns mockScannedDocument
        coEvery { buildScannedCredentialResultUseCase(any(), documentType, any()) } returns mockScannedCredentialResult

        val stateObserver = viewModel.scanOcrStateLiveData.test()

        viewModel.startScanning()
        // transitioning to Complete
        viewModel.processOcrResultsAndFinish()

        // Frame from camera arrives after Complete
        viewModel.imageProcessingStarted()
        viewModel.processImage(frame)

        assertThat(stateObserver.value()).isEqualTo(ScanOcrState.Complete)
        assertThat(viewModel.isOcrActive).isFalse()
        coVerify(exactly = 0) { scanMfidDocumentUseCase.invoke(any(), any(), any()) }
    }

    @Test
    fun `processImage finishes scan with partial captures after default cutoff elapses`() = runTest {
        val mockScannedDocument = mockk<ScannedMfidDocument>()
        val mockCroppedBitmap = mockk<Bitmap>()
        val mockScannedCredentialResult = mockk<ScannedCredentialResult>()

        coEvery { frameCropToTargetUseCase(frame) } returns mockCroppedBitmap
        coEvery { scanMfidDocumentUseCase(mockCroppedBitmap, documentType, any()) } returns mockScannedDocument
        coEvery { buildScannedCredentialResultUseCase(any(), documentType, any()) } returns mockScannedCredentialResult

        val stateObserver = viewModel.scanOcrStateLiveData.test()
        viewModel.startScanning()
        viewModel.processImage(frame)
        runCurrent()

        assertThat((stateObserver.value() as ScanOcrState.ScanningInProgress).successfulCaptures).isEqualTo(1)

        advanceTimeBy(4_999L.milliseconds)
        runCurrent()
        assertThat(stateObserver.value()).isInstanceOf(ScanOcrState.ScanningInProgress::class.java)

        advanceTimeBy(1L.milliseconds) // 5s default cutoff now elapsed
        runCurrent()

        assertThat(stateObserver.value()).isEqualTo(ScanOcrState.Complete)
        coVerify { buildScannedCredentialResultUseCase(listOf(mockScannedDocument), documentType, any()) }
    }

    @Test
    fun `processImage uses ocrScanTimeCutoff from custom config to schedule early finish`() = runTest {
        val mockScannedDocument = mockk<ScannedMfidDocument>()
        val mockCroppedBitmap = mockk<Bitmap>()
        val mockScannedCredentialResult = mockk<ScannedCredentialResult>()
        viewModel = initViewModel(
            documentType = documentType,
            customConfig = mapOf("ocrScanTimeCutoff" to JsonPrimitive(1)),
        )
        runCurrent()

        coEvery { frameCropToTargetUseCase(frame) } returns mockCroppedBitmap
        coEvery { scanMfidDocumentUseCase(mockCroppedBitmap, documentType, any()) } returns mockScannedDocument
        coEvery { buildScannedCredentialResultUseCase(any(), documentType, any()) } returns mockScannedCredentialResult

        val stateObserver = viewModel.scanOcrStateLiveData.test()
        viewModel.startScanning()
        viewModel.processImage(frame)
        runCurrent()

        advanceTimeBy(1_000L) // 1s custom cutoff now elapsed
        runCurrent()

        assertThat(stateObserver.value()).isEqualTo(ScanOcrState.Complete)
    }

    @Test
    fun `processImage does not re-trigger finish from a stale timeout once scan already completed`() = runTest {
        val mockScannedDocument = mockk<ScannedMfidDocument>()
        val mockCroppedBitmap = mockk<Bitmap>()

        coEvery { frameCropToTargetUseCase(frame) } returns mockCroppedBitmap
        coEvery { scanMfidDocumentUseCase(mockCroppedBitmap, documentType, any()) } returns mockScannedDocument
        coEvery { buildScannedCredentialResultUseCase(any(), documentType, any()) } returns mockk()

        viewModel.startScanning()
        repeat(3) { viewModel.processImage(frame) } // reaches the 3 required captures
        runCurrent()
        viewModel.processOcrResultsAndFinish() // as triggered by the fragment once captures are complete

        advanceTimeBy(5_000L) // let the pending timeout job from the first capture elapse
        runCurrent()

        coVerify(exactly = 1) { buildScannedCredentialResultUseCase(any(), documentType, any()) }
    }

    @Test
    fun `startScanning cancels a pending timeout job left over from a previous scan`() = runTest {
        val mockScannedDocument = mockk<ScannedMfidDocument>()
        val mockCroppedBitmap = mockk<Bitmap>()

        coEvery { frameCropToTargetUseCase(frame) } returns mockCroppedBitmap
        coEvery { scanMfidDocumentUseCase(mockCroppedBitmap, documentType, any()) } returns mockScannedDocument
        coEvery { buildScannedCredentialResultUseCase(any(), documentType, any()) } returns mockk()

        val stateObserver = viewModel.scanOcrStateLiveData.test()

        viewModel.startScanning()
        viewModel.processImage(frame) // schedules the 5s default timeout
        runCurrent()

        advanceTimeBy(2_000L)
        runCurrent()

        viewModel.startScanning() // restart before the stale timeout elapses

        advanceTimeBy(5_000L)
        runCurrent()

        val state = stateObserver.value() as ScanOcrState.ScanningInProgress
        assertThat(state.successfulCaptures).isEqualTo(0)
    }
}
