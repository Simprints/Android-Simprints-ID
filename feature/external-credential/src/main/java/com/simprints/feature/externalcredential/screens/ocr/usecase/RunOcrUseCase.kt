package com.simprints.feature.externalcredential.screens.ocr.usecase

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import kotlin.coroutines.resumeWithException

internal class RunOcrUseCase @Inject constructor() {

    suspend operator fun invoke(image: Bitmap, timeoutMs: Long): Text = withTimeout(timeoutMs) {
        suspendCancellableCoroutine { cont ->
            val inputImage = InputImage.fromBitmap(image, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    cont.resume(visionText) { _, _, _ -> }
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }
        }
    }
}
