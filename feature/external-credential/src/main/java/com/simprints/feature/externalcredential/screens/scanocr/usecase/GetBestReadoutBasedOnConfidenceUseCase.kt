package com.simprints.feature.externalcredential.screens.scanocr.usecase

import com.simprints.infra.logging.Simber
import javax.inject.Inject

internal class GetBestReadoutBasedOnConfidenceUseCase @Inject constructor() {
    /**
     * Constructs the most likely string by selecting the most frequent character at each position across all detected OCR
     * blocks. This is necessary because during the OCR readouts, detection mechanisms might confuse characters (think, 'l' versus 'I').
     *
     * To account for that, this method puts the most frequent character at each position across all readings.
     *
     * Example: ["ABC", "ACD", "CCD"], targetLength=3 -> "ACD"
     * - Position 0: 'A' appears 2 times, 'C' appears 1 time -> 'A' wins
     * - Position 1: 'B' appears 1 time, 'C' appears 2 times -> 'C' wins
     * - Position 2: 'C' appears 1 time, 'D' appears 2 times -> 'D' wins
     * Result: 'ACD'
     *
     * @param readoutValues list of OCR detection results containing readout values
     * @param targetLength expected length of the field. When provided, only readouts matching this length participate in voting.
     * When null, the most common length among readouts is used as the target.
     * @return most likely string based on character frequency voting
     * @throws [IllegalArgumentException] if [readoutValues] is empty or no readouts match the target length
     */
    operator fun invoke(
        readoutValues: List<String>,
        targetLength: Int? = null,
    ): String {
        // Either target length or a maximum out of all readouts
        val length = targetLength ?: readoutValues
            .groupingBy { it.length }
            .eachCount()
            .maxBy { it.value }
            .key

        val detectedValues = readoutValues.filter { it.length == length }
        if (detectedValues.isEmpty()) {
            Simber.d("OCR: no values of length [$length] is detected in readout values $readoutValues")
            throw IllegalArgumentException("OCR block list is empty, cannot extract external credential from it")
        }

        return (0 until length)
            .map { position ->
                detectedValues
                    .map { it[position] }
                    .groupingBy { it }
                    .eachCount()
                    .maxBy { it.value }
                    .key
            }.joinToString(separator = "")
    }
}
