package com.simprints.face.capture.screens.livefeedback

import android.graphics.Bitmap
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.simprints.face.capture.facelandmarks.FaceOverlayView
import com.simprints.face.capture.screens.livefeedback.views.CameraTargetOverlay
import kotlin.math.max
import kotlin.math.min
import com.google.mlkit.vision.face.FaceDetection as MlkitDetector

internal class CropToTargetOverlayAnalyzer(
    private val targetOverlay: CameraTargetOverlay,
    private val faceOverlayView: FaceOverlayView,
    private val onImageCropped: (Bitmap) -> Unit,
) : ImageAnalysis.Analyzer {
    private val mlDetector = MlkitDetector.getClient(
        FaceDetectorOptions
            .Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .build(),
    )

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        val mediaImage = image.image ?: run {
            image.close()
            return
        }
        val previewRect = targetOverlay.circleRect
        if (previewRect.isEmpty) return

        // Adjust overlay size to be fit-center with the image size
        val scale = getSmallerRatio(
            image.width,
            image.height,
            targetOverlay.width,
            targetOverlay.height,
        )
        val scaledWidth = (targetOverlay.width * scale).toInt()
        val scaledHeight = (targetOverlay.height * scale).toInt()

        val inputImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)
        mlDetector
            .process(inputImage)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    val face = faces[0]
                    faceOverlayView.update(
                        face,
                        inputImage.width,
                        inputImage.height,
                        targetOverlay.circleRect,
                    )
                } else {
                    faceOverlayView.reset()
                }
            }

        // Find the offsets caused by fit-center scaling
        val offsetX = (max(image.width, scaledWidth) - min(image.width, scaledWidth)) / 2
        val offsetY = (max(image.height, scaledHeight) - min(image.height, scaledHeight)) / 2

        // Scale the preview target to the new scale and offset
        val cropLeft = offsetX + (previewRect.left * scale).toInt()
        val cropWidth = (previewRect.width() * scale).toInt()
        val cropTop = offsetY + (previewRect.top * scale).toInt()
        val cropHeight = (previewRect.height() * scale).toInt()
        onImageCropped(
            image.use {
                Bitmap.createBitmap(
                    it.toBitmap(),
                    cropLeft,
                    cropTop,
                    cropWidth,
                    cropHeight,
                )
            },
        )
    }

    private fun getSmallerRatio(
        cameraWidth: Int,
        cameraHeight: Int,
        screenWidth: Int,
        screenHeight: Int,
    ): Float {
        val widthRatio = cameraWidth / screenWidth.toFloat()
        val heightRatio = cameraHeight / screenHeight.toFloat()
        return min(widthRatio, heightRatio)
    }
}
