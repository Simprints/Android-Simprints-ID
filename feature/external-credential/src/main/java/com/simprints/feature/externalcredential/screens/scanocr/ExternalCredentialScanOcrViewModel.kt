package com.simprints.feature.externalcredential.screens.scanocr

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.DispatcherBG
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.feature.externalcredential.screens.scanocr.model.DetectedOcrBlock
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrCropConfig
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType
import com.simprints.feature.externalcredential.screens.scanocr.usecase.CropDocumentFromPreviewUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.GetCredentialCoordinatesUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.KeepOnlyBestDetectedBlockUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.NormalizeBitmapToPreviewUseCase
import com.simprints.infra.logging.Simber
import com.simprints.infra.resources.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch

internal class ExternalCredentialScanOcrViewModel @AssistedInject constructor(
    @Assisted val ocrDocumentType: OcrDocumentType,
    private val normalizeBitmapToPreviewUseCase: NormalizeBitmapToPreviewUseCase,
    private val cropDocumentFromPreviewUseCase: CropDocumentFromPreviewUseCase,
    private val getCredentialCoordinatesUseCase: GetCredentialCoordinatesUseCase,
    private val keepOnlyBestDetectedBlockUseCase: KeepOnlyBestDetectedBlockUseCase,
    @DispatcherBG private val bgDispatcher: CoroutineDispatcher
) : ViewModel() {
    @AssistedFactory
    interface Factory {
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
    val finishOcrEvent: LiveData<LiveDataEventWithContent<DetectedOcrBlock>>
        get() = _finishOcrEvent
    private val _finishOcrEvent = MutableLiveData<LiveDataEventWithContent<DetectedOcrBlock>>()

    private fun updateState(state: (ScanOcrState) -> ScanOcrState) {
        this.state = state(this.state)
    }

    fun getDocumentTypeRes(): Int = when (ocrDocumentType) {
        OcrDocumentType.NhisCard -> R.string.mfid_type_nhis_card
        OcrDocumentType.GhanaIdCard -> R.string.mfid_type_ghana_id_card
    }

    fun ocrStarted() {
        updateState {
            ScanOcrState.ScanningInProgress(
                ocrDocumentType = ocrDocumentType,
                successfulCaptures = 0,
                scansRequired = SUCCESSFUL_SCANS_REQUIRED
            )
        }
    }

    fun runOcrOnFrame(frame: Bitmap, cropConfig: OcrCropConfig) {
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
                        scansRequired = SUCCESSFUL_SCANS_REQUIRED
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
            val detectedBlock = keepOnlyBestDetectedBlockUseCase(detectedBlocks, ocrDocumentType)
            _finishOcrEvent.send(detectedBlock)
            detectedBlocks = emptyList()
        }
    }

    fun ocrOnFrameStarted() {
        isRunningOcrOnFrame = true
    }

    companion object {
        private const val SUCCESSFUL_SCANS_REQUIRED = 5
    }
}
