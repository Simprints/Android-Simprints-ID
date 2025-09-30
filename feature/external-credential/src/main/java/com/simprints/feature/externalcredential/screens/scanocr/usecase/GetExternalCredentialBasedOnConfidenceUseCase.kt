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
     * Example: ["ABC", "ACD", "CCD"], credentialLength=3 -> "ACD"
     * - Position 0: 'A' appears 2 times, 'C' appears 1 time -> 'A' wins
     * - Position 1: 'B' appears 1 time, 'C' appears 2 times -> 'C' wins
     * - Position 2: 'C' appears 1 time, 'D' appears 2 times -> 'D' wins
     * Result: 'ACD'
     *
     * Example: ["ABC", "AC"], credentialLength=3 -> "ACD"
     *
     * @param detectedBlocks list of OCR detection results containing readout values
     * @param credentialLength target length of the external credential
     * @return most likely credential string based on character frequency voting
     * @throws [IllegalArgumentException] if [detectedBlocks] is empty
     */
    operator fun invoke(detectedBlocks: List<DetectedOcrBlock>, credentialLength: Int): String {
        val detectedValues: List<String> = detectedBlocks
            .map(DetectedOcrBlock::readoutValue)
            .filter { it.length == credentialLength }
        if (detectedValues.isEmpty()) {
            throw IllegalArgumentException("OCR block list is empty, cannot extract external credential from it")
        }

        // Grouping characters at each position across all strings and picking the most frequent one
        return (0 until credentialLength).map { position ->
            detectedValues
                .map { it[position] }
                .groupingBy { it }
                .eachCount()
                .maxBy { it.value }
                .key
        }.joinToString(separator = "")
    }
}
