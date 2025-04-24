package com.simprints.feature.externalcredential.screens.ocr.model

import androidx.annotation.Keep
import com.google.mlkit.vision.text.Text

@Keep
internal sealed class OcrScanState {
    data object OcrInProgress : OcrScanState()
    data class Error(val message: String) : OcrScanState()
    data class Finished(
        val ocrAllText: Text,
        val externalCredentialField: Pair<String, String>,
        val fieldIds: Map<String, String?>
    ) : OcrScanState()
}
