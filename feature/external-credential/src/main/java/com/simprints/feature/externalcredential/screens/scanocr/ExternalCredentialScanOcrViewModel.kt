package com.simprints.feature.externalcredential.screens.scanocr

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.DispatcherBG
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.externalcredential.screens.scanocr.model.DetectedOcrBlock
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrCropConfig
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType
import com.simprints.feature.externalcredential.screens.scanocr.model.asExternalCredentialType
import com.simprints.feature.externalcredential.screens.scanocr.usecase.CropDocumentFromPreviewUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.GetCredentialCoordinatesUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.KeepOnlyBestDetectedBlockUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.NormalizeBitmapToPreviewUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.ZoomOntoCredentialUseCase
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.credential.store.CredentialImageRepository
import com.simprints.infra.credential.store.model.CredentialScanImageType.ZoomedInCredential
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MULTI_FACTOR_ID
import com.simprints.infra.logging.Simber
import com.simprints.infra.resources.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch

internal class ExternalCredentialScanOcrViewModel @AssistedInject constructor(
    @Assisted val ocrDocumentType: OcrDocumentType,
    private val timeHelper: TimeHelper,
    private val normalizeBitmapToPreviewUseCase: NormalizeBitmapToPreviewUseCase,
    private val cropDocumentFromPreviewUseCase: CropDocumentFromPreviewUseCase,
    private val getCredentialCoordinatesUseCase: GetCredentialCoordinatesUseCase,
    private val keepOnlyBestDetectedBlockUseCase: KeepOnlyBestDetectedBlockUseCase,
    private val credentialImageRepository: CredentialImageRepository,
    private val zoomOntoCredentialUseCase: ZoomOntoCredentialUseCase,
    private val tokenizationProcessor: TokenizationProcessor,
    private val authStore: AuthStore,
    private val configManager: ConfigManager,
    @DispatcherBG private val bgDispatcher: CoroutineDispatcher,
) : ViewModel() {
    @AssistedFactory
    fun interface Factory {
        fun create(ocrDocumentType: OcrDocumentType): ExternalCredentialScanOcrViewModel
    }

    private var detectedBlocks: List<DetectedOcrBlock> = emptyList()
    var isRunningOcrOnFrame: Boolean = false
        private set
    val isOcrActive: Boolean
        get() = detectedBlocks.isNotEmpty()
    private var state: ScanOcrState = ScanOcrState.EMPTY
        set(value) {
            field = value
            _stateLiveData.postValue(value)
        }
    private val _stateLiveData = MutableLiveData<ScanOcrState>()
    val stateLiveData: LiveData<ScanOcrState> = _stateLiveData
    val finishOcrEvent: LiveData<LiveDataEventWithContent<ScannedCredential>>
        get() = _finishOcrEvent
    private val _finishOcrEvent = MutableLiveData<LiveDataEventWithContent<ScannedCredential>>()

    private lateinit var startTime: Timestamp

    private fun updateState(state: (ScanOcrState) -> ScanOcrState) {
        this.state = state(this.state)
    }

    fun getDocumentTypeRes(): Int = when (ocrDocumentType) {
        OcrDocumentType.NhisCard -> R.string.mfid_type_nhis_card
        OcrDocumentType.GhanaIdCard -> R.string.mfid_type_ghana_id_card
    }

    fun ocrStarted() {
        startTime = timeHelper.now()
        updateState {
            ScanOcrState.ScanningInProgress(
                ocrDocumentType = ocrDocumentType,
                successfulCaptures = 0,
                scansRequired = SUCCESSFUL_SCANS_REQUIRED,
            )
        }
    }

    fun runOcrOnFrame(
        frame: Bitmap,
        cropConfig: OcrCropConfig,
    ) {
        viewModelScope.launch(bgDispatcher) {
            try {
                Simber.d("started OCR")
                val normalizedBitmap = normalizeBitmapToPreviewUseCase(frame, cropConfig)
                val cropped = cropDocumentFromPreviewUseCase(bitmap = normalizedBitmap, cutoutRect = cropConfig.cutoutRect)
                val detectedBlock = getCredentialCoordinatesUseCase(bitmap = cropped, documentType = ocrDocumentType) ?: return@launch
                Simber.d("Detected OCR")
                detectedBlocks += detectedBlock
                updateState {
                    ScanOcrState.ScanningInProgress(
                        ocrDocumentType = ocrDocumentType,
                        successfulCaptures = detectedBlocks.size,
                        scansRequired = SUCCESSFUL_SCANS_REQUIRED,
                    )
                }
            } finally {
                isRunningOcrOnFrame = false
            }
        }
    }

    fun processOcrResultsAndFinish() {
        updateState { ScanOcrState.Complete }
        viewModelScope.launch {
            val project = configManager.getProject(authStore.signedInProjectId)
            val detectedBlock = keepOnlyBestDetectedBlockUseCase(detectedBlocks, ocrDocumentType)
            val credentialType = detectedBlock.documentType.asExternalCredentialType()
            val blockBoundingBox = detectedBlock.blockBoundingBox
            val zoomedCredentialImagePath = buildZoomedImagePath(detectedBlock)
            val detectedValueRaw = detectedBlock.readoutValue.asTokenizableRaw()
            val credential = tokenizationProcessor.encrypt(
                decrypted = detectedValueRaw,
                tokenKeyType = TokenKeyType.ExternalCredential,
                project = project,
            ) as TokenizableString.Tokenized

            val scannedCredential = ScannedCredential(
                credential = credential,
                credentialType = credentialType,
                documentImagePath = detectedBlock.imagePath,
                zoomedCredentialImagePath = zoomedCredentialImagePath,
                credentialBoundingBox = blockBoundingBox,
                scanStartTime = startTime,
                scanEndTime = timeHelper.now(),
                scannedValue = detectedValueRaw,
            )
            _finishOcrEvent.send(scannedCredential)
            detectedBlocks = emptyList()
        }
    }

    private suspend fun buildZoomedImagePath(detectedBlock: DetectedOcrBlock): String? = try {
        credentialImageRepository.saveCredentialScan(
            bitmap = zoomOntoCredentialUseCase(detectedBlock.imagePath, detectedBlock.blockBoundingBox),
            imageType = ZoomedInCredential,
        )
    } catch (e: Exception) {
        Simber.e(
            "Unable to zoom into bounding box [${detectedBlock.blockBoundingBox}] of ${detectedBlock.documentType} image ${detectedBlock.imagePath}",
            e,
            MULTI_FACTOR_ID,
        )
        null
    }

    fun ocrOnFrameStarted() {
        isRunningOcrOnFrame = true
    }

    companion object {
        private const val SUCCESSFUL_SCANS_REQUIRED = 3
    }
}
