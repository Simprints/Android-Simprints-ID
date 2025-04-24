package com.simprints.feature.externalcredential.screens.ocr.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.feature.externalcredential.screens.ocr.model.OcrDocument
import com.simprints.feature.externalcredential.screens.ocr.model.OcrScanState
import com.simprints.feature.externalcredential.screens.ocr.usecase.ProcessOcrUseCase
import com.simprints.infra.logging.Simber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class OcrScanViewModel @Inject constructor(
    private val ocrUseCase: ProcessOcrUseCase
) : ViewModel() {

    private var ocrJob: Job? = null
    private var isOcrProcessed = false

    val stateLiveData: LiveData<OcrScanState>
        get() = _stateLiveData
    private val _stateLiveData = MutableLiveData<OcrScanState>()

    fun startOcr(image: Bitmap, ocrDocument: OcrDocument) {
        if (isOcrProcessed) {
            Simber.d("OCR is already processed, no launching again")
            return
        }
        ocrJob?.cancel(CancellationException("New image starts processing"))
        ocrJob = viewModelScope.launch {
            _stateLiveData.value = OcrScanState.OcrInProgress
            val state = try {
                val ocrScanResult = ocrUseCase(image, ocrDocument)
                val requestedFields = ocrScanResult.requestedFields
                val externalCredentialEntry = requestedFields.entries.first { it.key.isExternalCredentialId }
                val externalCredentialField = externalCredentialEntry.key.fieldOnTheDocument to (externalCredentialEntry.value ?: "")
                isOcrProcessed = true
                OcrScanState.Finished(
                    ocrAllText = ocrScanResult.ocrAllText,
                    externalCredentialField = externalCredentialField,
                    fieldIds = requestedFields.mapKeys { it.key.fieldOnTheDocument }
                )
            } catch (e: Exception) {
                Simber.e(e.message ?: "", e)
                OcrScanState.Error(e.toString())
            }
            _stateLiveData.value = state
        }
    }

    override fun onCleared() {
        ocrJob?.cancel(CancellationException("ViewModel cleared"))
        super.onCleared()
    }
}
