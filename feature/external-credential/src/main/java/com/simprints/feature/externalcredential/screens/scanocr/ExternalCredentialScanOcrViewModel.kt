package com.simprints.feature.externalcredential.screens.scanocr

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.DispatcherIO
import com.simprints.core.domain.permission.PermissionStatus
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.feature.externalcredential.screens.scanocr.model.DetectedOcrBlock
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrCropConfig
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType
import com.simprints.feature.externalcredential.screens.scanocr.usecase.CropDocumentFromPreviewUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.FindBestTextBlockForCredentialUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.GetCredentialCoordinatesUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.GetExternalCredentialBasedOnConfidenceUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.NormalizeBitmapToPreviewUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.SaveScannedImageUseCase
import com.simprints.infra.resources.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch

internal class ExternalCredentialScanOcrViewModel @AssistedInject constructor(
    @Assisted private val ocrDocumentType: OcrDocumentType,
    private val normalizeBitmapToPreviewUseCase: NormalizeBitmapToPreviewUseCase,
    private val cropDocumentFromPreviewUseCase: CropDocumentFromPreviewUseCase,
    private val getCredentialCoordinatesUseCase: GetCredentialCoordinatesUseCase,
    private val getExternalCredentialBasedOnConfidenceUseCase: GetExternalCredentialBasedOnConfidenceUseCase,
    private val findBestTextBlockForCredentialUseCase: FindBestTextBlockForCredentialUseCase,
    private val saveScannedImageUseCase: SaveScannedImageUseCase,
    @DispatcherIO private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(ocrDocumentType: OcrDocumentType): ExternalCredentialScanOcrViewModel
    }

    private var detectedBlocks: List<DetectedOcrBlock> = emptyList()
    private var state: ScanOcrState = ScanOcrState.ReadyToScan(ocrDocumentType)
        set(value) {
            field = value
            _stateLiveData.postValue(value)
        }
    private val _stateLiveData = MutableLiveData<ScanOcrState>(ScanOcrState.ReadyToScan(ocrDocumentType))
    val stateLiveData: LiveData<ScanOcrState> = _stateLiveData
    val finishOcrEvent: LiveData<LiveDataEventWithContent<DetectedOcrBlock>>
        get() = _finishOcrEvent
    private val _finishOcrEvent = MutableLiveData<LiveDataEventWithContent<DetectedOcrBlock>>()

    private fun updateState(state: (ScanOcrState) -> ScanOcrState) {
        this.state = state(this.state)
    }

    fun updateCameraPermissionStatus(permissionStatus: PermissionStatus) {
        val newState = when (permissionStatus) {
            PermissionStatus.Granted -> ScanOcrState.ReadyToScan(ocrDocumentType)
            PermissionStatus.Denied -> ScanOcrState.NoCameraPermission(
                shouldOpenPhoneSettings = false,
                ocrDocumentType = ocrDocumentType
            )

            PermissionStatus.DeniedNeverAskAgain -> ScanOcrState.NoCameraPermission(
                shouldOpenPhoneSettings = true,
                ocrDocumentType = ocrDocumentType
            )
        }
        updateState { newState }
    }

    fun getDocumentTypeRes(ocrDocumentType: OcrDocumentType): Int = when (ocrDocumentType) {
        OcrDocumentType.NhisCard -> R.string.mfid_type_nhis_card
        OcrDocumentType.GhanaIdCard -> R.string.mfid_type_ghana_id_card
    }

    fun startOcr() {
        detectedBlocks = emptyList()
        updateState {
            ScanOcrState.InProgress(
                ocrDocumentType = ocrDocumentType,
                successfulCaptures = 0
            )
        }
    }

    fun runOcrOnFrame(frame: Bitmap, cropConfig: OcrCropConfig) {
        viewModelScope.launch(ioDispatcher) {
            val normalizedBitmap = normalizeBitmapToPreviewUseCase(frame, cropConfig)
            val cropped = cropDocumentFromPreviewUseCase(bitmap = normalizedBitmap, cutoutRect = cropConfig.cutoutRect)
            val detectedBlock = getCredentialCoordinatesUseCase(bitmap = cropped, documentType = ocrDocumentType) ?: return@launch
            detectedBlocks += detectedBlock
            updateState { currentState ->
                ScanOcrState.InProgress(
                    ocrDocumentType = ocrDocumentType,
                    successfulCaptures = detectedBlocks.size
                )
            }
        }
    }

    fun processOcrResultsAndFinish() {
        val externalCredential = getExternalCredentialBasedOnConfidenceUseCase(detectedBlocks)
        val detectedBlock = findBestTextBlockForCredentialUseCase(credential = externalCredential, detectedBlocks = detectedBlocks)
        _finishOcrEvent.send(detectedBlock)
        updateState { ScanOcrState.ReadyToScan(ocrDocumentType) }
    }
}
