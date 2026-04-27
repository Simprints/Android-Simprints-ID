package com.simprints.feature.externalcredential.screens.scanocr.reader

import com.google.mlkit.vision.text.Text
import com.simprints.feature.externalcredential.model.toBoundingBox

/**
 * Converts a ML Kit [Text] result into custom [OcrText].
 *
 * Each lines is assigned an ID in the order they are encountered in blocks
 * The resulting [OcrText.allLines] are sorted by bounding box top (ascending).
 */
internal object OcrModelBuilder {
    fun build(mlKitText: Text): OcrText {
        var nextLineId = 0

        val blocks = mlKitText.textBlocks.map { block ->
            val lines = block.lines.map { line ->
                OcrLine(
                    id = nextLineId++,
                    text = line.text.trim().replace(" ", ""),
                    boundingBox = line.boundingBox.toBoundingBox(),
                    blockBoundingBox = block.boundingBox.toBoundingBox(),
                    confidence = line.confidence,
                )
            }
            OcrBlock(
                boundingBox = block.boundingBox.toBoundingBox(),
                lines = lines,
            )
        }

        val allLinesSorted = blocks
            .flatMap { it.lines }
            .sortedBy { it.boundingBox.top }

        return OcrText(
            blocks = blocks,
            allLines = allLinesSorted,
        )
    }
}
