package com.simprints.feature.externalcredential.screens.scanocr

import android.graphics.Bitmap
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import com.simprints.feature.externalcredential.screens.scanocr.model.DetectedOcrBlock
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrCropConfig
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType
import com.simprints.feature.externalcredential.screens.scanocr.usecase.CropDocumentFromPreviewUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.GetCredentialCoordinatesUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.KeepOnlyBestDetectedBlockUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.NormalizeBitmapToPreviewUseCase
import com.simprints.infra.resources.R
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class ExternalCredentialScanOcrViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var normalizeBitmapToPreviewUseCase: NormalizeBitmapToPreviewUseCase

    @MockK
    private lateinit var cropDocumentFromPreviewUseCase: CropDocumentFromPreviewUseCase

    @MockK
    private lateinit var getCredentialCoordinatesUseCase: GetCredentialCoordinatesUseCase

    @MockK
    private lateinit var keepOnlyBestDetectedBlockUseCase: KeepOnlyBestDetectedBlockUseCase

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
    }

    private fun initViewModel(documentType: OcrDocumentType) = ExternalCredentialScanOcrViewModel(
        ocrDocumentType = documentType,
        normalizeBitmapToPreviewUseCase = normalizeBitmapToPreviewUseCase,
        cropDocumentFromPreviewUseCase = cropDocumentFromPreviewUseCase,
        getCredentialCoordinatesUseCase = getCredentialCoordinatesUseCase,
        keepOnlyBestDetectedBlockUseCase = keepOnlyBestDetectedBlockUseCase,
        bgDispatcher = testCoroutineRule.testCoroutineDispatcher
    )

    @Test
    fun `ocrStarted updates state to ScanningInProgress`() {
        val observer = viewModel.stateLiveData.test()
        viewModel.ocrStarted()

        val state = observer.value() as ScanOcrState.ScanningInProgress
        assertThat(state.ocrDocumentType).isEqualTo(documentType)
        assertThat(state.successfulCaptures).isEqualTo(0)
        assertThat(state.scansRequired).isEqualTo(5)
    }

    @Test
    fun `runOcrOnFrame updates detected blocks and state when successful`() = runTest {
        val mockDetectedBlock = mockk<DetectedOcrBlock>()
        val mockNormalizedBitmap = mockk<Bitmap>()
        val mockCroppedBitmap = mockk<Bitmap>()
        coEvery { normalizeBitmapToPreviewUseCase(bitmap, cropConfig) } returns mockNormalizedBitmap
        coEvery { cropDocumentFromPreviewUseCase(mockNormalizedBitmap, any()) } returns mockCroppedBitmap
        coEvery { getCredentialCoordinatesUseCase(mockCroppedBitmap, documentType) } returns mockDetectedBlock

        val observer = viewModel.stateLiveData.test()
        viewModel.ocrOnFrameStarted()
        viewModel.runOcrOnFrame(bitmap, cropConfig)

        val state = observer.value() as ScanOcrState.ScanningInProgress
        assertThat(state.successfulCaptures).isEqualTo(1)
        assertThat(viewModel.isRunningOcrOnFrame).isFalse()
        assertThat(viewModel.isOcrActive).isTrue()
    }

    @Test
    fun `processOcrResultsAndFinish sends finish event and resets state`() = runTest {
        val mockBestBlock = mockk<DetectedOcrBlock>()
        val finishObserver = viewModel.finishOcrEvent.test()
        val stateObserver = viewModel.stateLiveData.test()
        coEvery { keepOnlyBestDetectedBlockUseCase(any()) } returns mockBestBlock
        viewModel.processOcrResultsAndFinish()

        assertThat(finishObserver.value()?.peekContent()).isEqualTo(mockBestBlock)
        assertThat(stateObserver.value()).isEqualTo(ScanOcrState.NotScanning)
        assertThat(viewModel.isOcrActive).isFalse()
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
