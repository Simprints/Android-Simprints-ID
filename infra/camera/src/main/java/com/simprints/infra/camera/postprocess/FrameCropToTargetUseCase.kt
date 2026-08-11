package com.simprints.infra.camera.postprocess

import android.graphics.Bitmap
import com.simprints.infra.camera.Frame
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class FrameCropToTargetUseCase @Inject constructor() {
    operator fun invoke(frame: Frame): Bitmap {
        if (frame.targetBounds.isEmpty) {
            return frame.bitmap
        }

        val imageWidth = frame.bitmap.width
        val imageHeight = frame.bitmap.height
        val previewWidth = frame.previewBounds.width()
        val previewHeight = frame.previewBounds.height()

        // Adjust preview size to be fit-center with the image size
        val widthRatio = imageWidth / previewWidth.toFloat()
        val heightRatio = imageHeight / previewHeight.toFloat()

        val scale = min(widthRatio, heightRatio)
        val scaledWidth = (previewWidth * scale).toInt()
        val scaledHeight = (previewHeight * scale).toInt()

        // Find the offsets caused by fit-center scaling
        val offsetX = (max(imageWidth, scaledWidth) - min(imageWidth, scaledWidth)) / 2
        val offsetY = (max(imageHeight, scaledHeight) - min(imageHeight, scaledHeight)) / 2

        // Clamp the target bounds to the preview size
        val left = frame.targetBounds.left.coerceIn(0, previewWidth)
        val right = frame.targetBounds.right.coerceIn(left, previewWidth)
        val top = frame.targetBounds.top.coerceIn(0, previewHeight)
        val bottom = frame.targetBounds.bottom.coerceIn(top, previewHeight)

        // Scale the preview target to the new scale and offset
        val cropLeft = offsetX + (left * scale).toInt()
        val cropWidth = ((right - left) * scale).toInt()
        val cropTop = offsetY + (top * scale).toInt()
        val cropHeight = ((bottom - top) * scale).toInt()

        // Cannot crop if target is empty
        if (cropWidth <= 0 || cropHeight <= 0) {
            return frame.bitmap
        }

        return Bitmap.createBitmap(
            frame.bitmap,
            cropLeft,
            cropTop,
            cropWidth,
            cropHeight,
        )
    }
}
