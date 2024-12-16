package com.simprints.face.capture.screens.livefeedback

import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.simprints.face.capture.screens.livefeedback.views.CameraTargetOverlay
import kotlin.math.max
import kotlin.math.min

internal class CropToTargetOverlayAnalyzer(
    private val targetOverlay: CameraTargetOverlay,
    private val onImageCropped: (Bitmap) -> Unit,
) : ImageAnalysis.Analyzer {
    override fun analyze(image: ImageProxy) {
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
