package com.simprints.feature.externalcredential.screens.scanocr.usecase

import com.simprints.feature.externalcredential.screens.scanocr.model.DetectedOcrBlock
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType
import javax.inject.Inject

internal class KeepOnlyBestDetectedBlockUseCase @Inject constructor(
    private val getExternalCredentialBasedOnConfidenceUseCase: GetExternalCredentialBasedOnConfidenceUseCase,
    private val findBestTextBlockForCredentialUseCase: FindBestTextBlockForCredentialUseCase,
    private val deleteScannedImageUseCase: DeleteScannedImageUseCase,
) {

    suspend operator fun invoke(allDetectedBlock: List<DetectedOcrBlock>, documentType: OcrDocumentType): DetectedOcrBlock {
        val credentialLength = when(documentType){
            OcrDocumentType.NhisCard -> 8 // NHIS membership number contains 8 digits: 12345678
            OcrDocumentType.GhanaIdCard -> 15 // Ghana ID field contains 15 chars: GHA-123456789-0
        }
        val externalCredential = getExternalCredentialBasedOnConfidenceUseCase(allDetectedBlock, credentialLength)
        val detectedBlock = findBestTextBlockForCredentialUseCase(credential = externalCredential, detectedBlocks = allDetectedBlock)

        // Deleting cached scan images for all remaining blocks
        allDetectedBlock
            .map(DetectedOcrBlock::imagePath)
            .filterNot { imagePath -> imagePath == detectedBlock.imagePath }
            .onEach { imagePath -> deleteScannedImageUseCase(imagePath) }
        return detectedBlock
    }
}
