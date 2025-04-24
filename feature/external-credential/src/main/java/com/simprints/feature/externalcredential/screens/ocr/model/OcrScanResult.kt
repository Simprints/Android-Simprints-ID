package com.simprints.feature.externalcredential.screens.ocr.model

import com.google.mlkit.vision.text.Text

data class OcrScanResult(
    val ocrAllText: Text,
    val requestedFields: Map<OcrId, String?>
)
