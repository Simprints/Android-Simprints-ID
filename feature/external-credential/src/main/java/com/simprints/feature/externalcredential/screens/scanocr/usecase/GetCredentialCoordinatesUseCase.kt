package com.simprints.feature.externalcredential.screens.scanocr.usecase

import android.graphics.Bitmap
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.feature.externalcredential.model.toBoundingBox
import com.simprints.feature.externalcredential.screens.scanocr.model.DetectedOcrBlock
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType
import com.simprints.infra.credential.store.CredentialImageRepository
import com.simprints.infra.credential.store.model.CredentialScanImageType.FullDocument
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MULTI_FACTOR_ID
import com.simprints.infra.logging.Simber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ExcludedFromGeneratedTestCoverageReports("Unable to mock Google ML Kit")
internal class GetCredentialCoordinatesUseCase @Inject constructor(
    private val ghanaNhisCardOcrSelectorUseCase: GhanaNhisCardOcrSelectorUseCase,
    private val ghanaIdCardOcrSelectorUseCase: GhanaIdCardOcrSelectorUseCase,
    private val faydaCardOcrSelectorUseCase: FaydaCardOcrSelectorUseCase,
    private val credentialImageRepository: CredentialImageRepository,
) {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * OCR uses Google ML kit. It has a following hierarchy:
     *      - Block. A contiguous set of text lines, such as a paragraph or column,
     *      - Line. A contiguous set of words on the same axis. There can be multiple Lines in the Block
     *      - Element. A contiguous set of alphanumeric characters ("word") on the same axis. There can be Elements in one Line
     *      - Symbol. A single alphanumeric character in an Element.
     *
     * This method returns a [DetectedOcrBlock] class if the OCR managed to find a line that satisfies the given [documentType] pattern.
     * If such Line is found, then it is returned in a [DetectedOcrBlock] alongside its parent block, and a normalized value.
     *
     * Lines are used instead of Elements because the OCR might mistakenly read an extra white space in a Line, resulting in multiple
     * Elements. Since Lines are geometrically in one plane, we just take the concatenation of all underlying child Elements, and analyze
     * them it as a single string.
     *
     * @param bitmap bitmap to run OCR on
     * @param documentType type of the document
     *
     * @return [DetectedOcrBlock] if any Line satisfies the [documentType] pattern, or null if none.
     */
    suspend operator fun invoke(
        bitmap: Bitmap,
        documentType: OcrDocumentType,
    ): DetectedOcrBlock? {
        val image = InputImage.fromBitmap(bitmap, 0)
        return try {
            val result = Tasks.await(recognizer.process(image)) ?: return null
            return result.textBlocks.firstNotNullOfOrNull { textBlock ->
                textBlock.lines.firstNotNullOfOrNull { textLine ->
                    // Getting text from the entire line readout, and normalizing to avoid any extra spaces
                    val lineReadout = textLine.text.trim().replace(" ", "")
                    val isValid = when (documentType) {
                        OcrDocumentType.NhisCard -> ghanaNhisCardOcrSelectorUseCase(lineReadout)
                        OcrDocumentType.GhanaIdCard -> ghanaIdCardOcrSelectorUseCase(lineReadout)
                        OcrDocumentType.FaydaCard -> faydaCardOcrSelectorUseCase(lineReadout)
                    }
                    if (isValid) {
                        val blockBoundingRect = textBlock.boundingBox ?: return@firstNotNullOfOrNull null
                        val lineBoundingRect = textLine.boundingBox ?: return@firstNotNullOfOrNull null
                        val savedImagePath = credentialImageRepository.saveCredentialScan(bitmap, imageType = FullDocument)
                        return@firstNotNullOfOrNull DetectedOcrBlock(
                            imagePath = savedImagePath,
                            documentType = documentType,
                            blockBoundingBox = blockBoundingRect.toBoundingBox(),
                            lineBoundingBox = lineBoundingRect.toBoundingBox(),
                            readoutValue = lineReadout,
                        )
                    } else {
                        return@firstNotNullOfOrNull null
                    }
                }
            }
        } catch (e: Exception) {
            Simber.e("OCR failed for $documentType", e, tag = MULTI_FACTOR_ID)
            null
        }
    }
}
