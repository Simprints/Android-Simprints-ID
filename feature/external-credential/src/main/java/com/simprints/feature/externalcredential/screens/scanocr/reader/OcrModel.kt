package com.simprints.feature.externalcredential.screens.scanocr.reader

import com.simprints.feature.externalcredential.model.BoundingBox

/**
 * Wrapper for all scanned text after the OCR
 *
 * @param blocks blocks of text, each containing multiple [OcrLine]
 * @param allLines all lines from blocks sorted by top-right bounding box coordinates ascending
 */
data class OcrText(
    val blocks: List<OcrBlock>,
    val allLines: List<OcrLine>,
)

/**
 * Representation of a single block detected by the OCR kit. A block can contain multiple lines that he OCR kit labeled as belonging to the
 * same paragraph of text.
 *
 * @param boundingBox coordinates of the block
 * @param lines nested lines of the block
 */
data class OcrBlock(
    val boundingBox: BoundingBox,
    val lines: List<OcrLine>,
)

/**
 * A single line of text detected by the OCR kit.
 *
 * @param id unique id of the line in a single OCR scan
 * @param text normalized text (extra spaces removed)
 * @param boundingBox coordinates of the line
 * @param blockBoundingBox parent coordinates
 * @param confidence overall confidence of the text value based on the average confidence for each character (aka element) in [text]
 */
data class OcrLine(
    val id: Int,
    val text: String,
    val boundingBox: BoundingBox,
    val blockBoundingBox: BoundingBox,
    val confidence: Float,
)
