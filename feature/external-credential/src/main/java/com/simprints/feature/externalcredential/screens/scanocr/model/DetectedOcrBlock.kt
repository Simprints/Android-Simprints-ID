package com.simprints.feature.externalcredential.screens.scanocr.model

import com.google.mlkit.vision.text.Text
import com.simprints.feature.externalcredential.model.BoundingBox

/**
 * Result of the OCR credential detection of image. [Text.TextBlock] contains a [Text.Line] that was contains the detected credential.
 * [readoutValue] is a normalized string that was read from the [Text.Line] (no extra space, trimmed).
 *
 * To save memory, the image is not stored directly in the data class. Rather this data class keeps a file path reference to the image
 * in [imagePath].
 *
 * @param imagePath path to bitmap that was used for OCR
 * @param documentType type of a supported document
 * @param blockBoundingBox bounding box of block in which [lineBoundingBox] was detected
 * @param lineBoundingBox bounding box of line that contained [Text.Element] objects that were concatenated and normalized to produce a [readoutValue]
 * @param readoutValue normalized readout value from all [Text.Element] objects in [lineBoundingBox]
 */
internal data class DetectedOcrBlock(
    val imagePath: String,
    val documentType: OcrDocumentType,
    val blockBoundingBox: BoundingBox,
    val lineBoundingBox: BoundingBox,
    val readoutValue: String,
)
