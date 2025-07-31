package com.simprints.feature.consent.screens.consent.tempocr

import android.graphics.Rect
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OcrViewModel @Inject constructor() : ViewModel() {

    private val totalReadoutsRequired = 20

    val ocrState: LiveData<OcrState?>
        get() = _ocrState
    private val _ocrState = MutableLiveData<OcrState>()
    val uiModelState: LiveData<OcrUIModel>
        get() = _uiModelState

    private val _uiModelState = MutableLiveData<OcrUIModel>(OcrUIModel.NotScanning)

    fun startOcr() {
        _uiModelState.value = OcrUIModel.ScanningInProgress(
            readoutValue = "test",
            rawBox = Rect(0, 0, 0, 0),
            totalReadoutsRequired = totalReadoutsRequired,
            successfulReadouts = 1,
        )
        _ocrState.value = OcrState.Running(everyNthFrame = 5, totalReadoutsRequired = totalReadoutsRequired) // TODO change values
    }

    fun stopOcr() {
        _ocrState.value = OcrState.Stopped
        _uiModelState.value = OcrUIModel.NotScanning
    }

    fun addOcrReadout(text: String, boundingBox: Rect) {
        val currentProgress = _uiModelState.value as? OcrUIModel.ScanningInProgress ?: return
        val updatedProgress = currentProgress.copy(
            readoutValue = text,
            rawBox = boundingBox,
            totalReadoutsRequired = currentProgress.totalReadoutsRequired,
            successfulReadouts = currentProgress.successfulReadouts + 1
        )
        val newState = if (updatedProgress.successfulReadouts >= updatedProgress.totalReadoutsRequired) {
            OcrUIModel.Success(
                readoutValue = updatedProgress.readoutValue,
                rawBox = updatedProgress.rawBox,
                totalFramesProcessed = updatedProgress.successfulReadouts
            )
        } else {
            updatedProgress
        }
        _uiModelState.value = newState
    }

}

sealed class OcrState {
    object Stopped : OcrState()
    data class Running(
        val everyNthFrame: Int,
        val totalReadoutsRequired: Int,
    ) : OcrState()
}

sealed class OcrUIModel {
    data object NotScanning : OcrUIModel()
    data class ScanningInProgress(
        val readoutValue: String,
        val rawBox: Rect,
        val totalReadoutsRequired: Int,
        val successfulReadouts: Int,
    ) : OcrUIModel()

    data class Success(
        val readoutValue: String,
        val rawBox: Rect,
        val totalFramesProcessed: Int,
    ) : OcrUIModel()
}
