package com.simprints.feature.externalcredential.screens.scanocr

import android.graphics.Bitmap
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.*
import com.jraska.livedata.test
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrScanResult
import com.simprints.feature.externalcredential.screens.scanocr.model.ReadoutSource
import com.simprints.feature.externalcredential.screens.scanocr.model.ScannedMfidDocument
import com.simprints.feature.externalcredential.screens.scanocr.usecase.BuildScannedCredentialResultUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.GetLightingConditionsAssessmentConfigUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.ScanMfidDocumentUseCase
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredentialResult
import com.simprints.infra.camera.Frame
import com.simprints.infra.camera.lighting.GetLightingConditionsAssessmentUseCase
import com.simprints.infra.camera.lighting.LightingConditionsAssessment
import com.simprints.infra.camera.postprocess.FrameCropToTargetUseCase
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.MFID_LIGHTING_CONDITIONS_ASSESSMENT_ENABLED
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.OCR_READ_FAYDA_BARCODE
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
    fun `startScanning emits ScanningInProgress with max scans required`() {
        val observer = viewModel.scanOcrStateLiveData.test()
        viewModel.startScanning()

        assertThat(observer.value()).isEqualTo(ScanOcrState.ScanningInProgress(maxScansRequired = 3))
    }

    @Test
    fun `processImage keeps ScanningInProgress before captures complete`() = runTest {
        val mockScannedDocument = mockk<ScannedMfidDocument> { every { source } returns ReadoutSource.TEXT_OCR }
        val mockCroppedBitmap = mockk<Bitmap>()
        coEvery { frameCropToTargetUseCase(frame) } returns mockCroppedBitmap
        coEvery { scanMfidDocumentUseCase(mockCroppedBitmap, documentType, any()) } returns mockScannedDocument

        val observer = viewModel.scanOcrStateLiveData.test()
        viewModel.startScanning()
        viewModel.processImage(frame)

        assertThat(observer.value()).isEqualTo(ScanOcrState.ScanningInProgress(maxScansRequired = 3))
        assertThat(viewModel.isProcessingImage.value).isFalse()
    }

    @Test
    fun `processImage passes Fayda barcode flag from config to scan use case`() = runTest {
        val mockCroppedBitmap = mockk<Bitmap>()
        coEvery { frameCropToTargetUseCase(frame) } returns mockCroppedBitmap
        viewModel = initViewModel(
            documentType = OcrDocumentType.FaydaCard,
            customConfig = mapOf(OCR_READ_FAYDA_BARCODE to JsonPrimitive(true)),
        )

        viewModel.startScanning()
        viewModel.processImage(frame)

        coVerify { scanMfidDocumentUseCase(mockCroppedBitmap, OcrDocumentType.FaydaCard, any(), readFaydaBarcode = true) }
    }

    @Test
    fun `processImage does not read Fayda barcode by default`() = runTest {
        val mockCroppedBitmap = mockk<Bitmap>()
        coEvery { frameCropToTargetUseCase(frame) } returns mockCroppedBitmap
        viewModel = initViewModel(documentType = OcrDocumentType.FaydaCard)

        viewModel.startScanning()
        viewModel.processImage(frame)

        coVerify { scanMfidDocumentUseCase(mockCroppedBitmap, OcrDocumentType.FaydaCard, any(), readFaydaBarcode = false) }
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
        coVerify(exactly = 0) { scanMfidDocumentUseCase.invoke(any(), any(), any(), any()) }
        coVerify(exactly = 0) { getLightingConditionsAssessment.invoke(any(), any()) }
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
        coVerify(exactly = 0) { scanMfidDocumentUseCase.invoke(any(), any(), any(), any()) }
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

    private fun stubOcrDocument(): ScannedMfidDocument {
        val document = ScannedMfidDocument(
            imagePath = "path",
            ocrScanResult = OcrScanResult.FaydaCard(credential = mockk()),
            source = ReadoutSource.TEXT_OCR,
        )
        val mockCroppedBitmap = mockk<Bitmap>()
        coEvery { frameCropToTargetUseCase(frame) } returns mockCroppedBitmap
        coEvery { scanMfidDocumentUseCase(mockCroppedBitmap, documentType, any(), any()) } returns document
        return document
    }

    @Test
    fun `startScanning emits ScanningInProgress once regardless of captures`() = runTest {
        stubOcrDocument()

        val observer = viewModel.scanOcrStateLiveData.test()
        viewModel.startScanning()
        repeat(2) { viewModel.processImage(frame) }

        val inProgress = observer.valueHistory().filterIsInstance<ScanOcrState.ScanningInProgress>()
        assertThat(inProgress).hasSize(1)
    }

    @Test
    fun `processImage emits ScansProcessing then ScansReady when captures complete`() = runTest {
        stubOcrDocument()
        val result = mockk<ScannedCredentialResult>()
        coEvery { buildScannedCredentialResultUseCase(any(), documentType, any()) } returns result

        val observer = viewModel.scanOcrStateLiveData.test()
        viewModel.startScanning()
        repeat(3) { viewModel.processImage(frame) }

        assertThat(observer.valueHistory().takeLast(2)).containsExactly(
            ScanOcrState.ScansProcessing,
            ScanOcrState.ScansReady(result),
        ).inOrder()
    }

    @Test
    fun `ScansReady carries result built from accumulated documents`() = runTest {
        val document = stubOcrDocument()
        val result = mockk<ScannedCredentialResult>()
        coEvery { buildScannedCredentialResultUseCase(any(), documentType, any()) } returns result

        val observer = viewModel.scanOcrStateLiveData.test()
        viewModel.startScanning()
        repeat(3) { viewModel.processImage(frame) }

        coVerify { buildScannedCredentialResultUseCase(listOf(document, document, document), documentType, any()) }
        assertThat((observer.value() as ScanOcrState.ScansReady).result).isEqualTo(result)
    }

    @Test
    fun `processImage completes capture after a single Fayda barcode readout`() = runTest {
        val barcodeDocument = ScannedMfidDocument(
            imagePath = "path",
            ocrScanResult = OcrScanResult.FaydaCard(credential = mockk()),
            source = ReadoutSource.BARCODE,
        )
        val mockCroppedBitmap = mockk<Bitmap>()
        coEvery { frameCropToTargetUseCase(frame) } returns mockCroppedBitmap
        viewModel = initViewModel(
            documentType = OcrDocumentType.FaydaCard,
            customConfig = mapOf(OCR_READ_FAYDA_BARCODE to JsonPrimitive(true)),
        )
        coEvery { scanMfidDocumentUseCase(mockCroppedBitmap, OcrDocumentType.FaydaCard, any(), any()) } returns barcodeDocument

        val observer = viewModel.scanOcrStateLiveData.test()
        viewModel.startScanning()
        assertThat(observer.value()).isEqualTo(ScanOcrState.ScanningInProgress(maxScansRequired = 3))

        viewModel.processImage(frame)

        assertThat(observer.valueHistory()).contains(ScanOcrState.ScansProcessing)
    }

    @Test
    fun `processImage ignores documents after captures are complete`() = runTest {
        stubOcrDocument()

        val observer = viewModel.scanOcrStateLiveData.test()
        viewModel.startScanning()
        repeat(5) { viewModel.processImage(frame) }

        assertThat(observer.valueHistory().count { it == ScanOcrState.ScansProcessing }).isEqualTo(1)
        coVerify(exactly = 1) { buildScannedCredentialResultUseCase(any(), any(), any()) }
    }

    @Test
    fun `processImage drops frames that arrive while a frame is being processed`() = runTest {
        val document = stubOcrDocument()
        val bitmap = mockk<Bitmap>()
        coEvery { frameCropToTargetUseCase(frame) } returns bitmap
        val gate = CompletableDeferred<Unit>()
        coEvery { scanMfidDocumentUseCase(bitmap, documentType, any(), any()) } coAnswers {
            gate.await()
            document
        }

        viewModel.startScanning()
        viewModel.processImage(frame)
        assertThat(viewModel.isProcessingImage.value).isTrue()
        repeat(2) { viewModel.processImage(frame) }
        gate.complete(Unit)
        runCurrent()

        coVerify(exactly = 1) { scanMfidDocumentUseCase(any(), any(), any(), any()) }
        assertThat(viewModel.isProcessingImage.value).isFalse()
    }

    @Test
    fun `processImage runs lighting assessment but not OCR after ScansProcessing`() = runTest {
        viewModel = initViewModel(
            documentType = documentType,
            customConfig = mapOf(MFID_LIGHTING_CONDITIONS_ASSESSMENT_ENABLED to JsonPrimitive(true)),
        )
        runCurrent()
        stubOcrDocument()

        viewModel.startScanning()
        repeat(3) { viewModel.processImage(frame) }
        clearMocks(scanMfidDocumentUseCase, getLightingConditionsAssessment, answers = false)

        viewModel.processImage(frame)

        coVerify(exactly = 1) { getLightingConditionsAssessment(any(), any()) }
        coVerify(exactly = 0) { scanMfidDocumentUseCase.invoke(any(), any(), any(), any()) }
    }

    @Test
    fun `startScanning again drops previous documents`() = runTest {
        val document = stubOcrDocument()

        viewModel.startScanning()
        repeat(2) { viewModel.processImage(frame) }
        viewModel.startScanning()
        repeat(3) { viewModel.processImage(frame) }

        coVerify(exactly = 1) { buildScannedCredentialResultUseCase(listOf(document, document, document), documentType, any()) }
    }
}
