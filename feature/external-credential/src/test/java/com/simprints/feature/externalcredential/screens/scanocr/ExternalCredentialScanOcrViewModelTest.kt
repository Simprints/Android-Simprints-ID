package com.simprints.feature.externalcredential.screens.scanocr

import android.graphics.Bitmap
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.*
import com.jraska.livedata.test
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.externalcredential.screens.scanocr.model.LightingConditionsAssessment
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrCropConfig
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType
import com.simprints.feature.externalcredential.screens.scanocr.model.ScannedMfidDocument
import com.simprints.feature.externalcredential.screens.scanocr.usecase.BuildScannedCredentialResultUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.CropDocumentFromPreviewUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.GetLightingConditionsAssessmentConfigUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.GetLightingConditionsAssessmentUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.NormalizeBitmapToPreviewUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.ScanMfidDocumentUseCase
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredentialResult
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

@OptIn(ExperimentalCoroutinesApi::class)
internal class ExternalCredentialScanOcrViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var timeHelper: TimeHelper

    @MockK
    private lateinit var normalizeBitmapToPreviewUseCase: NormalizeBitmapToPreviewUseCase

    @MockK
    private lateinit var cropDocumentFromPreviewUseCase: CropDocumentFromPreviewUseCase

    @MockK
    private lateinit var scanMfidDocumentUseCase: ScanMfidDocumentUseCase

    @MockK
    private lateinit var buildScannedCredentialResultUseCase: BuildScannedCredentialResultUseCase

    @MockK
    private lateinit var getLightingConditionsAssessment: GetLightingConditionsAssessmentUseCase

    @MockK
    private lateinit var configRepository: ConfigRepository

    @MockK
    private lateinit var bitmap: Bitmap

    @MockK
    private lateinit var cropConfig: OcrCropConfig

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
            normalizeBitmapToPreviewUseCase = normalizeBitmapToPreviewUseCase,
            cropDocumentFromPreviewUseCase = cropDocumentFromPreviewUseCase,
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
    fun `imageProcessingStopped resets the flag for image processing`() {
        viewModel.imageProcessingStarted()
        assertThat(viewModel.isProcessingImage.get()).isTrue()

        viewModel.imageProcessingStopped()
        assertThat(viewModel.isProcessingImage.get()).isFalse()
    }

    @Test
    fun `processImage updates detected blocks and state when OCR successful`() = runTest {
        val mockScannedDocument = mockk<ScannedMfidDocument>()
        val mockNormalizedBitmap = mockk<Bitmap>()
        val mockCroppedBitmap = mockk<Bitmap>()
        coEvery { normalizeBitmapToPreviewUseCase(bitmap, cropConfig) } returns mockNormalizedBitmap
        coEvery { cropDocumentFromPreviewUseCase(mockNormalizedBitmap, any()) } returns mockCroppedBitmap
        coEvery { scanMfidDocumentUseCase(mockCroppedBitmap, documentType, any()) } returns mockScannedDocument

        val observer = viewModel.scanOcrStateLiveData.test()
        viewModel.imageProcessingStarted()
        viewModel.startScanning()
        viewModel.processImage(bitmap, cropConfig)

        val state = observer.value() as ScanOcrState.ScanningInProgress
        assertThat(state.successfulCaptures).isEqualTo(1)
        assertThat(viewModel.isProcessingImage.get()).isFalse()
        assertThat(viewModel.isOcrActive).isTrue()
    }

    @Test
    fun `processImage updates lighting conditions after debounce`() = runTest {
        val mockNormalizedBitmap = mockk<Bitmap>()
        val mockCroppedBitmap = mockk<Bitmap>()
        val lightingConditionsAssessment = LightingConditionsAssessment.TOO_DIM
        viewModel = initViewModel(
            documentType = documentType,
            customConfig = mapOf(
                MFID_LIGHTING_CONDITIONS_ASSESSMENT_ENABLED to JsonPrimitive(true),
            ),
        )
        runCurrent()
        coEvery { normalizeBitmapToPreviewUseCase(bitmap, cropConfig) } returns mockNormalizedBitmap
        coEvery { cropDocumentFromPreviewUseCase(mockNormalizedBitmap, any()) } returns mockCroppedBitmap
        coEvery { getLightingConditionsAssessment(mockCroppedBitmap, any()) } returns lightingConditionsAssessment

        val observer = viewModel.lightingConditionsAssessment.test()

        viewModel.processImage(bitmap, cropConfig)
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
        viewModel.processImage(bitmap, cropConfig)

        assertThat(observer.value()).isInstanceOf(ScanOcrState.NotScanning::class.java)
        coVerify(exactly = 0) { normalizeBitmapToPreviewUseCase.invoke(any(), any()) }
        coVerify(exactly = 0) { cropDocumentFromPreviewUseCase.invoke(any(), any()) }
        coVerify(exactly = 0) { scanMfidDocumentUseCase.invoke(any(), any(), any()) }
        coVerify(exactly = 0) { getLightingConditionsAssessment.invoke(any(), any()) }
        assertThat(viewModel.isOcrActive).isFalse()
    }

    @Test
    fun `processImage updates lighting conditions when scanning not in progress`() = runTest {
        val mockNormalizedBitmap = mockk<Bitmap>()
        val mockCroppedBitmap = mockk<Bitmap>()
        val lightingConditionsAssessment = LightingConditionsAssessment.TOO_BRIGHT
        viewModel = initViewModel(
            documentType = documentType,
            customConfig = mapOf(
                MFID_LIGHTING_CONDITIONS_ASSESSMENT_ENABLED to JsonPrimitive(true),
            ),
        )
        runCurrent()
        coEvery { normalizeBitmapToPreviewUseCase(bitmap, cropConfig) } returns mockNormalizedBitmap
        coEvery { cropDocumentFromPreviewUseCase(mockNormalizedBitmap, any()) } returns mockCroppedBitmap
        coEvery { getLightingConditionsAssessment(mockCroppedBitmap, any()) } returns lightingConditionsAssessment

        val observer = viewModel.lightingConditionsAssessment.test()

        viewModel.processImage(bitmap, cropConfig)
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

        viewModel.processImage(bitmap, cropConfig)
        runCurrent()

        advanceTimeBy(500L) // debounce delay
        runCurrent()

        coVerify(exactly = 0) { normalizeBitmapToPreviewUseCase.invoke(any(), any()) }
        coVerify(exactly = 0) { cropDocumentFromPreviewUseCase.invoke(any(), any()) }
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
            normalizeBitmapToPreviewUseCase = normalizeBitmapToPreviewUseCase,
            cropDocumentFromPreviewUseCase = cropDocumentFromPreviewUseCase,
            scanMfidDocumentUseCase = scanMfidDocumentUseCase,
            buildScannedCredentialResultUseCase = buildScannedCredentialResultUseCase,
            getLightingConditionsAssessmentConfig = GetLightingConditionsAssessmentConfigUseCase(configRepository),
            getLightingConditionsAssessment = getLightingConditionsAssessment,
            configRepository = configRepository,
            bgDispatcher = testCoroutineRule.testCoroutineDispatcher,
        )

        viewModel.processImage(bitmap, cropConfig)
        runCurrent()

        advanceTimeBy(500L) // debounce delay
        runCurrent()

        coVerify(exactly = 0) { normalizeBitmapToPreviewUseCase.invoke(any(), any()) }
        coVerify(exactly = 0) { cropDocumentFromPreviewUseCase.invoke(any(), any()) }
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
        val mockNormalizedBitmap = mockk<Bitmap>()
        val mockCroppedBitmap = mockk<Bitmap>()

        coEvery { normalizeBitmapToPreviewUseCase(bitmap, cropConfig) } returns mockNormalizedBitmap
        coEvery { cropDocumentFromPreviewUseCase(mockNormalizedBitmap, any()) } returns mockCroppedBitmap
        coEvery { scanMfidDocumentUseCase(mockCroppedBitmap, documentType, any()) } returns mockScannedDocument
        coEvery { buildScannedCredentialResultUseCase(any(), documentType, any()) } returns mockScannedCredentialResult

        viewModel.startScanning()
        viewModel.processImage(bitmap, cropConfig)
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
}
