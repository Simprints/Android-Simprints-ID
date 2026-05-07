package com.simprints.feature.externalcredential.screens.scanocr.usecase

import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType
import com.simprints.feature.externalcredential.screens.scanocr.model.ScannedMfidDocument
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredentialResult
import javax.inject.Inject

internal class BuildScannedCredentialResultUseCase @Inject constructor(
    private val buildMfidDocumentUseCase: BuildMfidDocumentUseCase,
    private val createAndSaveZoomedImageUseCase: CreateAndSaveZoomedImageUseCase,
    private val timeHelper: TimeHelper,
) {
    suspend operator fun invoke(
        scannedDocuments: List<ScannedMfidDocument>,
        documentType: OcrDocumentType,
        startTime: Timestamp,
    ): ScannedCredentialResult {
        val document = buildMfidDocumentUseCase(scannedDocuments, documentType)

        // Saving the image and its OCR data closest to what the user have seen on the camera preview. This allows for natural look when
        // transitioning to the next screen, as the image looks closest to the last frame the user saw
        val (latestImagePath, latestCredentialOcrBlock) = scannedDocuments.last().imagePath to
            scannedDocuments.last().ocrScanResult.credential
        val zoomedCredentialImagePath = createAndSaveZoomedImageUseCase(latestCredentialOcrBlock, latestImagePath)

        return ScannedCredentialResult(
            document = document,
            documentImagePath = latestImagePath,
            zoomedCredentialImagePath = zoomedCredentialImagePath,
            credentialBoundingBox = latestCredentialOcrBlock.blockBoundingBox,
            scanStartTime = startTime,
            scanEndTime = timeHelper.now(),
        )
    }
}
