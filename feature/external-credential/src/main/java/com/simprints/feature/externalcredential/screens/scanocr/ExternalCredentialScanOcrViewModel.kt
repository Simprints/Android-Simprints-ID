package com.simprints.feature.externalcredential.screens.scanocr

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.simprints.core.DispatcherBG
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.externalcredential.screens.scanocr.model.LightingConditionsAssessment
import com.simprints.feature.externalcredential.screens.scanocr.model.LightingConditionsAssessmentConfig
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrConfig
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
import com.simprints.infra.config.store.models.experimental
import com.simprints.infra.logging.Simber
import com.simprints.infra.resources.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

internal class ExternalCredentialScanOcrViewModel @AssistedInject constructor(
    @Assisted val ocrDocumentType: OcrDocumentType,
    private val timeHelper: TimeHelper,
    private val normalizeBitmapToPreviewUseCase: NormalizeBitmapToPreviewUseCase,
    private val cropDocumentFromPreviewUseCase: CropDocumentFromPreviewUseCase,
    private val scanMfidDocumentUseCase: ScanMfidDocumentUseCase,
    private val buildScannedCredentialResultUseCase: BuildScannedCredentialResultUseCase,
    private val getLightingConditionsAssessmentConfig: GetLightingConditionsAssessmentConfigUseCase,
    private val getLightingConditionsAssessment: GetLightingConditionsAssessmentUseCase,
    private val configRepository: ConfigRepository,
    @param:DispatcherBG private val bgDispatcher: CoroutineDispatcher,
) : ViewModel() {
    @AssistedFactory
    fun interface Factory {
        fun create(ocrDocumentType: OcrDocumentType): ExternalCredentialScanOcrViewModel
    }

    private var scannedMfidDocuments: List<ScannedMfidDocument> = emptyList()
    val isProcessingImage = AtomicBoolean(false)

    val isOcrActive: Boolean
        get() = scannedMfidDocuments.isNotEmpty()
    private var ocrState: ScanOcrState = ScanOcrState.EMPTY
        set(value) {
            field = value
            _scanOcrStateLiveData.postValue(value)
        }
    private val _scanOcrStateLiveData = MutableLiveData(ocrState)
    val scanOcrStateLiveData: LiveData<ScanOcrState> = _scanOcrStateLiveData
    val finishOcrEvent: LiveData<LiveDataEventWithContent<ScannedCredentialResult>>
        get() = _finishOcrEvent
    private val _finishOcrEvent = MutableLiveData<LiveDataEventWithContent<ScannedCredentialResult>>()

    private val lightingConditionsAssessmentFlow = MutableStateFlow<LightingConditionsAssessment?>(null)
    val lightingConditionsAssessment: LiveData<LightingConditionsAssessment> =
        lightingConditionsAssessmentFlow
            .filterNotNull()
            .debounce(LIGHTING_CONDITIONS_ASSESSMENT_DEBOUNCE_MILLIS)
            .asLiveData(viewModelScope.coroutineContext)

    private lateinit var startTime: Timestamp
    lateinit var ocrConfig: OcrConfig
        private set
    private var lightingConditionsAssessmentConfig: LightingConditionsAssessmentConfig? = null

    init {
        viewModelScope.launch {
            with(configRepository.getProjectConfiguration().experimental()) {
                ocrConfig = OcrConfig(
                    useHighRes = ocrUseHighRes,
                    capturesRequired = ocrCaptures.coerceIn(OCR_CAPTURE_MIN, OCR_CAPTURE_MAX),
                )
            }
            lightingConditionsAssessmentConfig = getLightingConditionsAssessmentConfig()
        }
    }

    private fun updateState(state: (ScanOcrState) -> ScanOcrState) {
        this.ocrState = state(this.ocrState)
    }

    fun getDocumentTypeRes(): Int = when (ocrDocumentType) {
        OcrDocumentType.NhisCard -> R.string.mfid_type_nhis_card
        OcrDocumentType.GhanaIdCard -> R.string.mfid_type_ghana_id_card
    }

    fun startScanning() {
        startTime = timeHelper.now()
        updateState {
            ScanOcrState.ScanningInProgress(
                ocrDocumentType = ocrDocumentType,
                successfulCaptures = 0,
                scansRequired = ocrConfig.capturesRequired,
            )
        }
    }

    val isScanningInProgress: Boolean
        get() = ocrState is ScanOcrState.ScanningInProgress

    fun imageProcessingStopped() {
        isProcessingImage.set(false)
    }

    fun processImage(
        bitmap: Bitmap,
        cropConfig: OcrCropConfig,
    ) {
        viewModelScope.launch(bgDispatcher) {
            try {
                val isOcrAllowed = isScanningInProgress
                val isLightningAssessmentEnabled = lightingConditionsAssessmentConfig != null

                if (!isOcrAllowed && !isLightningAssessmentEnabled) return@launch // no-op
                Simber.d("started image processing; with OCR: $isOcrAllowed, lighting assessment: $isLightningAssessmentEnabled")
                val normalizedBitmap = normalizeBitmapToPreviewUseCase(bitmap, cropConfig)
                val cropped = cropDocumentFromPreviewUseCase(bitmap = normalizedBitmap, cutoutRect = cropConfig.cutoutRect)
                lightingConditionsAssessmentConfig?.run {
                    lightingConditionsAssessmentFlow.value = getLightingConditionsAssessment(
                        bitmap = cropped,
                        lightingConditionsAssessmentConfig = this,
                    )
                }

                if (!isOcrAllowed) return@launch
                val mfidConfig = configRepository.getProjectConfiguration().multifactorId ?: return@launch
                val scannedMfidDocument =
                    scanMfidDocumentUseCase(bitmap = cropped, documentType = ocrDocumentType, config = mfidConfig) ?: return@launch
                Simber.d("Detected OCR")
                scannedMfidDocuments += scannedMfidDocument
                updateState {
                    ScanOcrState.ScanningInProgress(
                        ocrDocumentType = ocrDocumentType,
                        successfulCaptures = scannedMfidDocuments.size,
                        scansRequired = ocrConfig.capturesRequired,
                    )
                }
            } finally {
                isProcessingImage.set(false)
            }
        }
    }

    fun processOcrResultsAndFinish() {
        updateState { ScanOcrState.Complete }
        viewModelScope.launch {
            val scannedCredentialResult = buildScannedCredentialResultUseCase(scannedMfidDocuments, ocrDocumentType, startTime)
            _finishOcrEvent.send(scannedCredentialResult)
            scannedMfidDocuments = emptyList()
        }
    }

    fun imageProcessingStarted() {
        isProcessingImage.set(true)
    }

    companion object {
        private const val OCR_CAPTURE_MIN = 1
        private const val OCR_CAPTURE_MAX = 10
        private const val LIGHTING_CONDITIONS_ASSESSMENT_DEBOUNCE_MILLIS = 500L
    }
}
