package com.simprints.feature.consent.screens.consent.tempocr

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.simprints.infra.logging.Simber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerformOcrUseCase @Inject constructor() {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    data class OcrResult(val text: String, val boundingBox: Rect)

    suspend operator fun invoke(bitmap: Bitmap, selector: (String) -> Boolean): OcrResult? {
        val image = InputImage.fromBitmap(bitmap, 0)
        return try {
            val result = Tasks.await(recognizer.process(image))?: return null
            val allTexts = result.textBlocks.flatMap { it.lines }.flatMap { it.elements }.map { it.text }
            Simber.e(allTexts.toString(), NullPointerException())
            result.textBlocks
                .flatMap { it.lines }
                .firstOrNull { selector(it.text) }
                ?.let { OcrResult(it.text, it.boundingBox ?: Rect()) }
        } catch (e: Exception) {
            Simber.e("OCR failed", e)
            null
        }
    }
}
