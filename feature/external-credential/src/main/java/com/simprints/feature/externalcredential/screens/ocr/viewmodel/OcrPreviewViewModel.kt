package com.simprints.feature.externalcredential.screens.ocr.viewmodel

import android.graphics.Bitmap
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.feature.externalcredential.screens.ocr.model.OcrDocument
import com.simprints.feature.externalcredential.screens.ocr.model.OcrPreprocessData
import com.simprints.feature.externalcredential.screens.ocr.model.OcrPreviewState
import com.simprints.feature.externalcredential.screens.ocr.usecase.PreprocessOcrImageUseCase
import com.simprints.infra.logging.Simber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class OcrPreviewViewModel @Inject constructor(
    private val preprocessOcrImageUseCase: PreprocessOcrImageUseCase,
) : ViewModel() {
    private var ocrJob: Job? = null
    var imageFileName: String = ""

    val stateLiveData: LiveData<LiveDataEventWithContent<OcrPreviewState>>
        get() = _stateLiveData
    private val _stateLiveData = MutableLiveData<LiveDataEventWithContent<OcrPreviewState>>()

    fun setLoadingState(isLoading: Boolean) = when (isLoading) {
        true -> _stateLiveData.send(OcrPreviewState.Loading)
        false -> _stateLiveData.send(OcrPreviewState.Initial)
    }

    fun preprocessOcrImage(image: Bitmap, exif: ExifInterface, ocrPreprocessData: OcrPreprocessData, ocrDocument: OcrDocument) {
        Simber.d("Starting image preprocessing for OCR. Scanning: [$ocrDocument]")
        ocrJob?.cancel(CancellationException("New image starts preprocessing"))
        ocrJob = viewModelScope.launch {
            try {
                updateState(OcrPreviewState.Loading)
                val preprocessedImage = preprocessOcrImageUseCase(
                    image = image,
                    exif = exif,
                    ocrPreprocessData = ocrPreprocessData,
                )
                updateState(OcrPreviewState.Success(preprocessedImage))
            } catch (e: Exception) {
                updateState(OcrPreviewState.Error(e.message ?: "Error preprocessing image"))
            }
        }
    }

    override fun onCleared() {
        ocrJob?.cancel(CancellationException("ViewModel cleared"))
        super.onCleared()
    }

    private fun updateState(state: OcrPreviewState) = _stateLiveData.postValue(LiveDataEventWithContent(state))
}
