package com.simprints.feature.externalcredential.screens.scanocr.usecase

import com.simprints.feature.externalcredential.screens.scanocr.model.DetectedOcrBlock
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MULTI_FACTOR_ID
import com.simprints.infra.logging.Simber
import javax.inject.Inject

internal class GetExternalCredentialBasedOnConfidenceUseCase @Inject constructor() {

    /**
     * Constructs the most likely credential string by selecting the most frequent character at each position across all detected OCR
     * blocks. This is necessary because during the OCR readouts, detection mechanisms might confuse characters (think, 'l' versus 'I').
     *
     * To account for that, this method puts the most frequent character at each position across all readings.
     *
     * If strings have different lengths, all are truncated to the shortest length.
     *
     * Example: ["ABC", "ACD", "CCD"] -> "ACD"
     * - Position 0: 'A' appears 2 times, 'C' appears 1 time -> 'A' wins
     * - Position 1: 'B' appears 1 time, 'C' appears 2 times -> 'C' wins
     * - Position 2: 'C' appears 1 time, 'D' appears 2 times -> 'D' wins
     * Result: 'ACD'
     *
     * @param detectedBlocks list of OCR detection results containing readout values
     * @return most likely credential string based on character frequency voting
     * @throws [IllegalArgumentException] if [detectedBlocks] is empty
     */
    operator fun invoke(detectedBlocks: List<DetectedOcrBlock>): String {
        val detectedValues: List<String> = detectedBlocks.map { it.readoutValue }
        if (detectedValues.isEmpty()) {
            throw IllegalArgumentException("OCR block list is empty, cannot extract external credential from it")
        }

        // Find shortest string length and truncate all strings to that length
        val minLength = detectedValues.minOf { it.length }
        val maxLength = detectedValues.maxOf { it.length }

        if (maxLength > minLength) {
            val documentType = detectedBlocks.first().documentType
            Simber.i(
                "OCR: max length of $documentType OCR readouts is $maxLength, truncating to min length of $minLength.",
                tag = MULTI_FACTOR_ID
            )
        }
        val truncatedValues = detectedValues.map { it.take(minLength) }

        // Grouping characters at each position across all strings and picking the most frequent one
        return (0 until minLength).map { position ->
            truncatedValues
                .map { it[position] }
                .groupingBy { it }
                .eachCount()
                .maxBy { it.value }
                .key
        }.joinToString(separator = "")
    }
}
