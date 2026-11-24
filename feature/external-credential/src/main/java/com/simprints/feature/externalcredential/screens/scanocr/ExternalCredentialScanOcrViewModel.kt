package com.simprints.feature.externalcredential.screens.scanocr

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.simprints.core.DispatcherBG
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.externalcredential.screens.scanocr.model.MfidCaptureState
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrConfig
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType
import com.simprints.feature.externalcredential.screens.scanocr.usecase.BuildScannedCredentialResultUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.GetLightingConditionsAssessmentConfigUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.ScanMfidDocumentUseCase
import com.simprints.infra.camera.Frame
import com.simprints.infra.camera.lighting.GetLightingConditionsAssessmentUseCase
import com.simprints.infra.camera.lighting.LightingConditionsAssessment
import com.simprints.infra.camera.lighting.LightingConditionsAssessmentConfig
import com.simprints.infra.camera.postprocess.FrameCropToTargetUseCase
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.experimental
import com.simprints.infra.logging.Simber
import com.simprints.infra.resources.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

internal class ExternalCredentialScanOcrViewModel @AssistedInject constructor(
    @Assisted val ocrDocumentType: OcrDocumentType,
    private val timeHelper: TimeHelper,
    private val frameCropToTargetUseCase: FrameCropToTargetUseCase,
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

    private lateinit var captureState: MfidCaptureState

    val isProcessingImage: StateFlow<Boolean>
        field = MutableStateFlow(false)

    private var ocrState: ScanOcrState = ScanOcrState.NotScanning
        set(value) {
            field = value
            _scanOcrStateLiveData.postValue(value)
        }
    private val _scanOcrStateLiveData = MutableLiveData(ocrState)
    val scanOcrStateLiveData: LiveData<ScanOcrState> = _scanOcrStateLiveData
    private val lightingConditionsAssessmentFlow = MutableStateFlow<LightingConditionsAssessment?>(null)
    val lightingConditionsAssessment: LiveData<LightingConditionsAssessment> =
        lightingConditionsAssessmentFlow
            .filterNotNull()
            .debounce(LIGHTING_CONDITIONS_ASSESSMENT_DEBOUNCE_MILLIS.milliseconds)
            .asLiveData(viewModelScope.coroutineContext)

    private lateinit var startTime: Timestamp
    private val ocrConfigFlow = MutableStateFlow<OcrConfig?>(null)

    private var lightingConditionsAssessmentConfig: LightingConditionsAssessmentConfig? = null

    init {
        viewModelScope.launch {
            with(configRepository.getProjectConfiguration().experimental()) {
                ocrConfigFlow.value = OcrConfig(
                    useHighRes = ocrUseHighRes,
                    keepHighResDetail = ocrKeepHighResDetail,
                    capturesRequired = ocrCaptures.coerceIn(OCR_CAPTURE_MIN, OCR_CAPTURE_MAX),
                    readFaydaBarcode = ocrReadFaydaBarcode,
                )
            }
            lightingConditionsAssessmentConfig = getLightingConditionsAssessmentConfig()
        }
    }

    suspend fun awaitOcrConfig(): OcrConfig = ocrConfigFlow.filterNotNull().first()

    fun getDocumentTypeRes(): Int = when (ocrDocumentType) {
        OcrDocumentType.NhisCard -> R.string.mfid_type_nhis_card
        OcrDocumentType.GhanaIdCard -> R.string.mfid_type_ghana_id_card
        OcrDocumentType.FaydaCard -> R.string.mfid_type_fayda_card
    }

    fun startScanning() {
        val captureConfig = ocrConfigFlow.value ?: return
        captureState = MfidCaptureState(ocrDocumentType, captureConfig)
        startTime = timeHelper.now()
        ocrState = ScanOcrState.ScanningInProgress(captureState.maxScansRequired)
    }

    fun processImage(frame: Frame) {
        if (isProcessingImage.value) return
        imageProcessingStarted()
        viewModelScope.launch(bgDispatcher) {
            try {
                val isOcrAllowed = ocrState is ScanOcrState.ScanningInProgress
                val isLightningAssessmentEnabled = lightingConditionsAssessmentConfig != null

                if (!isOcrAllowed && !isLightningAssessmentEnabled) return@launch // no-op
                Simber.d("started image processing; with OCR: $isOcrAllowed, lighting assessment: $isLightningAssessmentEnabled")
                val cropped = frameCropToTargetUseCase(frame)
                lightingConditionsAssessmentConfig?.run {
                    lightingConditionsAssessmentFlow.value = getLightingConditionsAssessment(
                        bitmap = cropped,
                        lightingConditionsAssessmentConfig = this,
                    )
                }

                if (!isOcrAllowed) return@launch
                val mfidConfig = configRepository.getProjectConfiguration().multifactorId ?: return@launch
                val captureConfig = ocrConfigFlow.value ?: return@launch
                val scannedMfidDocument = scanMfidDocumentUseCase(
                    bitmap = cropped,
                    documentType = ocrDocumentType,
                    config = mfidConfig,
                    readFaydaBarcode = captureConfig.readFaydaBarcode,
                ) ?: return@launch
                Simber.d("Detected OCR")
                if (ocrState !is ScanOcrState.ScanningInProgress) return@launch
                captureState.add(scannedMfidDocument)
                if (captureState.isComplete) onScansComplete()
            } finally {
                imageProcessingStopped()
            }
        }
    }

    private fun onScansComplete() {
        ocrState = ScanOcrState.ScansProcessing
        viewModelScope.launch {
            val scanResults = buildScannedCredentialResultUseCase(captureState.documents, ocrDocumentType, startTime)
            ocrState = ScanOcrState.ScansReady(scanResults)
        }
    }

    private fun imageProcessingStarted() {
        isProcessingImage.value = true
    }

    private fun imageProcessingStopped() {
        isProcessingImage.value = false
    }

    companion object {
        private const val OCR_CAPTURE_MIN = 1
        private const val OCR_CAPTURE_MAX = 10
        private const val LIGHTING_CONDITIONS_ASSESSMENT_DEBOUNCE_MILLIS = 500L
    }
}
