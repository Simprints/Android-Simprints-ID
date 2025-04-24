package com.simprints.feature.externalcredential.screens.ocr.usecase

import android.graphics.Bitmap
import com.simprints.feature.externalcredential.screens.ocr.model.OcrDocument
import com.simprints.feature.externalcredential.screens.ocr.model.OcrId
import com.simprints.feature.externalcredential.screens.ocr.model.OcrScanResult
import javax.inject.Inject

internal class ProcessOcrUseCase @Inject constructor(
    private val runOcrUseCase: RunOcrUseCase,
    private val ocrFuzzySearchUseCase: OcrFuzzySearchUseCase,
    private val ocrPatternSearchUseCase: OcrPatternSearchUseCase,
    private val ocrGhanaIdCardUseCase: OcrGhanaIdCardUseCase,
    private val ocrGhanaNHISCardUseCase: OcrGhanaNHISCardUseCase,
) {

    suspend operator fun invoke(
        image: Bitmap,
        ocrDocument: OcrDocument
    ): OcrScanResult {
        val ocrResult = runOcrUseCase(image = image, timeoutMs = 5000)
        val requestedFields = when (ocrDocument) {
            is OcrDocument.Custom -> {
                val fieldIds = ocrDocument.fieldIds
                val mappedFuzzySearchFields = ocrFuzzySearchUseCase(ocrResult, fieldIds.filterIsInstance<OcrId.FuzzySearch>())
                val mappedPatternSearchFields = ocrPatternSearchUseCase(ocrResult, fieldIds.filterIsInstance<OcrId.Pattern>())
                mappedFuzzySearchFields + mappedPatternSearchFields
            }

            OcrDocument.GhanaIdCard -> ocrGhanaIdCardUseCase(ocrResult)
            OcrDocument.GhanaNHISCard -> ocrGhanaNHISCardUseCase(ocrResult)
        }
        return OcrScanResult(ocrResult, requestedFields = requestedFields)
    }
}
