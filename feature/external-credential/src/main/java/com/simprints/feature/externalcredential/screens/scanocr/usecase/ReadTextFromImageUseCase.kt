package com.simprints.feature.externalcredential.screens.scanocr.usecase

import android.graphics.Bitmap
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.feature.externalcredential.model.toBoundingBox
import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrBlock
import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrLine
import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrText
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ExcludedFromGeneratedTestCoverageReports("Unable to mock Google ML Kit")
internal class ReadTextFromImageUseCase @Inject constructor() {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    operator fun invoke(bitmap: Bitmap): OcrText? {
        val image = InputImage.fromBitmap(bitmap, 0)
        val result = Tasks.await(recognizer.process(image)) ?: return null
        return build(result)
    }

    private fun build(mlKitText: Text): OcrText {
        var nextLineId = 0

        val blocks = mlKitText.textBlocks.map { block ->
            val lines = block.lines.map { line ->
                OcrLine(
                    id = nextLineId++,
                    text = line.text.trim(),
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
