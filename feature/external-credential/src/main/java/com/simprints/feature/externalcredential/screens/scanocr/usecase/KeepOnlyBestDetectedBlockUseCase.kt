package com.simprints.feature.externalcredential.screens.scanocr.usecase

import com.simprints.feature.externalcredential.screens.scanocr.model.DetectedOcrBlock
import javax.inject.Inject

internal class KeepOnlyBestDetectedBlockUseCase @Inject constructor(
    private val getExternalCredentialBasedOnConfidenceUseCase: GetExternalCredentialBasedOnConfidenceUseCase,
    private val findBestTextBlockForCredentialUseCase: FindBestTextBlockForCredentialUseCase,
    private val deleteScannedImageUseCase: DeleteScannedImageUseCase,
) {

    suspend operator fun invoke(allDetectedBlock: List<DetectedOcrBlock>): DetectedOcrBlock {
        val externalCredential = getExternalCredentialBasedOnConfidenceUseCase(allDetectedBlock)
        val detectedBlock = findBestTextBlockForCredentialUseCase(credential = externalCredential, detectedBlocks = allDetectedBlock)

        // Deleting cached scan images for all remaining blocks
        allDetectedBlock
            .map(DetectedOcrBlock::imagePath)
            .filterNot { imagePath -> imagePath == detectedBlock.imagePath }
            .onEach { imagePath -> deleteScannedImageUseCase(imagePath) }
        return detectedBlock
    }
}
