package com.simprints.feature.externalcredential.screens.ocr.usecase

import com.google.mlkit.vision.text.Text
import com.simprints.feature.externalcredential.screens.ocr.model.OcrId
import com.simprints.feature.externalcredential.screens.ocr.tools.replaceCharactersWithDigits
import javax.inject.Inject

internal class OcrGhanaIdCardUseCase @Inject constructor(
    private val ocrFuzzySearchUseCase: OcrFuzzySearchUseCase,
) {
    operator fun invoke(ocrResult: Text): Map<OcrId, String?> {
        val fieldIds = listOf(
            OcrId.FuzzySearch(fieldOnTheDocument = "Surname", name = "sur"), // Looking for 'Surname' value
            OcrId.FuzzySearch(fieldOnTheDocument = "Firstnames", name = "first"),  // Looking for 'First name' value
        )
        val fuzzySearchResult = ocrFuzzySearchUseCase(ocrResult, fieldIds)
        val personalIdOcr = OcrId.FuzzySearch(fieldOnTheDocument = "Personal ID", name = "Personal ID", isExternalCredentialId = true)
        val ghanaId = personalIdOcr to extractGhanaId(ocrResult.text)
        return fuzzySearchResult + ghanaId
    }



    /**
     * This function extracts 10 digits after 'GHA-' prefix and returns string in 'GHA-123456789-0' format
     *
     * Reasoning: the Personal ID on Ghana card starts with 'GHA-' and is followed by 9 digits, '-' symbol, and another digit.
     * Example: GHA-123456789-0
     * However, the Personal ID is placed right next to the Height column on the ID card, and sometimes OCR recognizes height (i.e. '1.8')
     * as a part of the Personal ID row:
     *
     * +-----------------+--------+
     * |   Personal ID   | Height |
     * +-----------------+--------+
     * | GHA-123456789-0 |    1.8 |
     * +-----------------+--------+
     *
     * And the ID becomes 'GHA-123456789-01.8' once read by the OCR. This function tries to address this issue.
     *
     * @return String in 'GHA-123456789-0' format if Personal ID is found in any line, and contains 10 digits. Null otherwise
     */
    private fun extractGhanaId(input: String): String? {
        if (input.length <= 14) return null
        val personalIdPrefix = "A-"
        val startIndex = input.indexOf(personalIdPrefix)
        if (startIndex == -1) return null
        val substring = input.substring(startIndex + personalIdPrefix.length)

        // Trying to prevent the common readout  errors where digits are considered characters. Replacing them.
        val normalized = substring.replaceCharactersWithDigits()
        val digits = normalized.filter { it.isDigit() }.take(10)

        return if (digits.length == 10) {
            "GHA-${digits.take(9)}-${digits.last()}"
        } else null
    }
}
