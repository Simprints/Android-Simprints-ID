package com.simprints.feature.externalcredential.screens.scanocr

import android.graphics.Bitmap
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.*
import com.jraska.livedata.test
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.externalcredential.model.BoundingBox
import com.simprints.feature.externalcredential.screens.scanocr.model.DetectedOcrBlock
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrCropConfig
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType
import com.simprints.feature.externalcredential.screens.scanocr.usecase.CropDocumentFromPreviewUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.GetCredentialCoordinatesUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.KeepOnlyBestDetectedBlockUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.NormalizeBitmapToPreviewUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.ZoomOntoCredentialUseCase
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.credential.store.CredentialImageRepository
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
    private lateinit var timeHelper: TimeHelper

    @MockK
    private lateinit var normalizeBitmapToPreviewUseCase: NormalizeBitmapToPreviewUseCase

    @MockK
    private lateinit var cropDocumentFromPreviewUseCase: CropDocumentFromPreviewUseCase

    @MockK
    private lateinit var getCredentialCoordinatesUseCase: GetCredentialCoordinatesUseCase

    @MockK
    private lateinit var keepOnlyBestDetectedBlockUseCase: KeepOnlyBestDetectedBlockUseCase

    @MockK
    private lateinit var zoomOntoCredentialUseCase: ZoomOntoCredentialUseCase

    @MockK
    private lateinit var credentialImageRepository: CredentialImageRepository

    @MockK
    private lateinit var tokenizationProcessor: TokenizationProcessor

    @MockK
    private lateinit var authStore: AuthStore

    @MockK
    private lateinit var configManager: ConfigManager

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

    private fun initViewModel(documentType: OcrDocumentType) = ExternalCredentialScanOcrViewModel(
        ocrDocumentType = documentType,
        timeHelper = timeHelper,
        normalizeBitmapToPreviewUseCase = normalizeBitmapToPreviewUseCase,
        cropDocumentFromPreviewUseCase = cropDocumentFromPreviewUseCase,
        getCredentialCoordinatesUseCase = getCredentialCoordinatesUseCase,
        keepOnlyBestDetectedBlockUseCase = keepOnlyBestDetectedBlockUseCase,
        zoomOntoCredentialUseCase = zoomOntoCredentialUseCase,
        credentialImageRepository = credentialImageRepository,
        bgDispatcher = testCoroutineRule.testCoroutineDispatcher,
        tokenizationProcessor = tokenizationProcessor,
        configManager = configManager,
        authStore = authStore,
    )

    @Test
    fun `ocrStarted updates state to ScanningInProgress`() {
        val observer = viewModel.scanOcrStateLiveData.test()
        viewModel.ocrStarted()

        val state = observer.value() as ScanOcrState.ScanningInProgress
        assertThat(state.ocrDocumentType).isEqualTo(documentType)
        assertThat(state.successfulCaptures).isEqualTo(0)
        assertThat(state.scansRequired).isEqualTo(3)
    }

    @Test
    fun `runOcrOnFrame updates detected blocks and state when successful`() = runTest {
        val mockDetectedBlock = mockk<DetectedOcrBlock>()
        val mockNormalizedBitmap = mockk<Bitmap>()
        val mockCroppedBitmap = mockk<Bitmap>()
        coEvery { normalizeBitmapToPreviewUseCase(bitmap, cropConfig) } returns mockNormalizedBitmap
        coEvery { cropDocumentFromPreviewUseCase(mockNormalizedBitmap, any()) } returns mockCroppedBitmap
        coEvery { getCredentialCoordinatesUseCase(mockCroppedBitmap, documentType) } returns mockDetectedBlock

        val observer = viewModel.scanOcrStateLiveData.test()
        viewModel.ocrOnFrameStarted()
        viewModel.runOcrOnFrame(bitmap, cropConfig)

        val state = observer.value() as ScanOcrState.ScanningInProgress
        assertThat(state.successfulCaptures).isEqualTo(1)
        assertThat(viewModel.isRunningOcrOnFrame).isFalse()
        assertThat(viewModel.isOcrActive).isTrue()
    }

    @Test
    fun `processOcrResultsAndFinish sends finish event with scanned credential`() = runTest {
        val detectedBlockImagePath = "detectedBlockImagePath"
        val readoutValue = "readoutValue"
        val zoomedImagePath = "zoomedImagePath"
        val projectId = "projectId"
        val mockBoundingBox = mockk<BoundingBox>()
        val mockBitmap = mockk<Bitmap>()
        val mockProject = mockk<Project>()
        val mockTokenizedCredential = mockk<TokenizableString.Tokenized>()

        val mockBestBlock = mockk<DetectedOcrBlock> {
            every { documentType } returns OcrDocumentType.NhisCard
            every { blockBoundingBox } returns mockBoundingBox
            every { imagePath } returns detectedBlockImagePath
            every { this@mockk.readoutValue } returns readoutValue
        }

        coEvery { authStore.signedInProjectId } returns projectId
        coEvery { configManager.getProject(projectId) } returns mockProject
        coEvery { keepOnlyBestDetectedBlockUseCase(any(), documentType) } returns mockBestBlock
        coEvery { tokenizationProcessor.encrypt(any(), TokenKeyType.ExternalCredential, mockProject) } returns mockTokenizedCredential
        coEvery { zoomOntoCredentialUseCase(detectedBlockImagePath, mockBoundingBox) } returns mockBitmap
        coEvery { credentialImageRepository.saveCredentialScan(mockBitmap, any()) } returns zoomedImagePath

        val finishObserver = viewModel.finishOcrEvent.test()
        val stateObserver = viewModel.scanOcrStateLiveData.test()

        viewModel.ocrStarted() // Initialises capture timing
        viewModel.processOcrResultsAndFinish()

        val scannedCredential = finishObserver.value()?.peekContent()
        assertThat(scannedCredential?.credential).isEqualTo(mockTokenizedCredential)
        assertThat(scannedCredential?.documentImagePath).isEqualTo(detectedBlockImagePath)
        assertThat(scannedCredential?.zoomedCredentialImagePath).isEqualTo(zoomedImagePath)
        assertThat(scannedCredential?.credentialBoundingBox).isEqualTo(mockBoundingBox)
        assertThat(stateObserver.value()).isEqualTo(ScanOcrState.Complete)
        assertThat(viewModel.isOcrActive).isFalse()
    }

    @Test
    fun `processOcrResultsAndFinish sets null zoomed image path when zoom fails`() = runTest {
        val detectedBlockImagePath = "detectedBlockImagePath"
        val readoutValue = "readoutValue"
        val projectId = "projectId"
        val mockBoundingBox = mockk<BoundingBox>()
        val mockProject = mockk<Project>()
        val mockTokenizedCredential = mockk<TokenizableString.Tokenized>()

        val mockBestBlock = mockk<DetectedOcrBlock> {
            every { documentType } returns OcrDocumentType.NhisCard
            every { blockBoundingBox } returns mockBoundingBox
            every { imagePath } returns detectedBlockImagePath
            every { this@mockk.readoutValue } returns readoutValue
        }

        coEvery { authStore.signedInProjectId } returns projectId
        coEvery { configManager.getProject(projectId) } returns mockProject
        coEvery { keepOnlyBestDetectedBlockUseCase(any(), documentType) } returns mockBestBlock
        coEvery { tokenizationProcessor.encrypt(any(), TokenKeyType.ExternalCredential, mockProject) } returns mockTokenizedCredential
        coEvery { zoomOntoCredentialUseCase(detectedBlockImagePath, mockBoundingBox) } throws Exception("Zoom failed")

        val finishObserver = viewModel.finishOcrEvent.test()

        viewModel.ocrStarted() // Initialises capture timing
        viewModel.processOcrResultsAndFinish()

        val scannedCredential = finishObserver.value()?.peekContent()
        assertThat(scannedCredential?.zoomedCredentialImagePath).isNull()
        assertThat(scannedCredential?.credential).isEqualTo(mockTokenizedCredential)
        assertThat(scannedCredential?.documentImagePath).isEqualTo(detectedBlockImagePath)
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
