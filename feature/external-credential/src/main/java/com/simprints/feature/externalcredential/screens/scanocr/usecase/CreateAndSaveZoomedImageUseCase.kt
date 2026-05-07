package com.simprints.feature.externalcredential.screens.scanocr.usecase

import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrLine
import com.simprints.infra.credential.store.CredentialImageRepository
import com.simprints.infra.credential.store.model.CredentialScanImageType.ZoomedInCredential
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MULTI_FACTOR_ID
import com.simprints.infra.logging.Simber
import javax.inject.Inject

internal class CreateAndSaveZoomedImageUseCase @Inject constructor(
    private val zoomOntoCredentialUseCase: ZoomOntoCredentialUseCase,
    private val credentialImageRepository: CredentialImageRepository,
) {
    suspend operator fun invoke(
        ocrLine: OcrLine,
        fullSizeImagePath: String,
    ) = try {
        credentialImageRepository.saveCredentialScan(
            bitmap = zoomOntoCredentialUseCase(fullSizeImagePath, ocrLine.blockBoundingBox),
            imageType = ZoomedInCredential,
        )
    } catch (e: Exception) {
        Simber.e(
            "Unable to zoom into bounding box [${ocrLine.blockBoundingBox}] image $fullSizeImagePath",
            e,
            MULTI_FACTOR_ID,
        )
        null
    }
}
