package com.simprints.feature.externalcredential.screens.scanocr.usecase

import com.simprints.feature.externalcredential.screens.scanocr.model.DetectedOcrBlock
import javax.inject.Inject

internal class FindBestTextBlockForCredentialUseCase @Inject constructor(
    private val calculateLevenshteinDistanceUseCase: CalculateLevenshteinDistanceUseCase,
) {
    /**
     * Finds the detected OCR block that contains the readout value most similar to the given credential string.
     *
     * This method first attempts to find an exact match by comparing the readout value of each block with the credential. If no exact
     * match is found, it uses Levenshtein distance to find the block with the smallest edit distance to the credential.
     *
     * @param credential the target credential string to match
     * @param detectedBlocks list of detected OCR blocks to search through. Must not be empty
     * @return the detected OCR block with the best matching readout value
     * @throws IllegalArgumentException if no blocks provided
     */
    operator fun invoke(
        credential: String,
        detectedBlocks: List<DetectedOcrBlock>,
    ): DetectedOcrBlock {
        if (detectedBlocks.isEmpty()) {
            throw IllegalArgumentException("OCR: cannot find match for credential, provided detected block list is empty")
        }

        // Searching from the end of detected blocks to maximize chances of getting the image closest to what the user have seen on the
        // camera preview. This allows for natural look when transitioning to the next screen, as the best fitting text block will be as
        // close to the last frame the user sees as possible.
        for (block in detectedBlocks.asReversed()) {
            if (block.readoutValue == credential) {
                return block
            }
        }

        // If no exact match, finding the closest one using Levenshtein distance. Updating its credential value to the given for consistency
        return detectedBlocks
            .minBy { block ->
                calculateLevenshteinDistanceUseCase(credential, block.readoutValue)
            }.copy(readoutValue = credential)
    }
}
