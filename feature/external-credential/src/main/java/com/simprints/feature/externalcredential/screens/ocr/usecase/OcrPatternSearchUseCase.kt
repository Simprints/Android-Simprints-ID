package com.simprints.feature.externalcredential.screens.ocr.usecase

import com.google.mlkit.vision.text.Text
import com.simprints.feature.externalcredential.screens.ocr.model.OcrId
import javax.inject.Inject


internal class OcrPatternSearchUseCase @Inject constructor() {

    operator fun invoke(ocrResult: Text, patterns: List<OcrId.Pattern>): Map<OcrId, String> {
        val result: MutableMap<OcrId, String> = patterns.associateWith { "" }.toMutableMap()
        val fullText = ocrResult.text

        patterns.forEach { pattern ->
            val regex = Regex(pattern.regex)
            val match = regex.find(fullText)
            if (match != null) {
                result[pattern] = match.value
            }
        }

        return result
    }

}
