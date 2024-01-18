package com.simprints.face.capture.usecases

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PixelFormat
import android.graphics.Rect
import androidx.camera.core.ImageProxy
import javax.inject.Inject

/**
 * Convert ImageProxy image To bitmap then crop and rotate it.
 * Image format should be RGBA_8888
 */
internal class ImageProxyToBitmapUseCase @Inject constructor() {

    operator fun invoke(imageProxy: ImageProxy, cropRect: Rect): Bitmap {
        require(imageProxy.format == PixelFormat.RGBA_8888) {
            "${imageProxy.format} is not supported. RGBA_8888 is the only supported image format"
        }
        val buffer = imageProxy.planes[0].buffer
        val pixelStride = imageProxy.planes[0].pixelStride
        val rowStride = imageProxy.planes[0].rowStride
        val rowPadding = rowStride - pixelStride * imageProxy.width
        val bitmap = Bitmap.createBitmap(
            imageProxy.width + rowPadding / pixelStride, imageProxy.height, Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)
        val rotationMatrix = Matrix()
        rotationMatrix.postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())

        // If cropRect.left or cropRect.right is less than 0, then Bitmap.createBitmap throws an exception.
        // cropRect.left is < 0 on some devices when in landscape mode
        // TODO Revert these changes and review the most appropriate way to solve the issue.
        var left = cropRect.left.takeIf { it >= 0 } ?: 0
        var right = cropRect.right.takeIf { it >= 0 } ?: 0

        if(right + cropRect.height() > bitmap.height) {
            val currentSum = right + cropRect.height()
            right = right - ((right + cropRect.height()) - bitmap.height)
        }
        val croppedRotatedBitmap = Bitmap.createBitmap(
            bitmap,
            left,
            right,
            cropRect.width(),
            cropRect.height(),
            rotationMatrix,
            true
        )
        bitmap.recycle()
        return croppedRotatedBitmap
    }

}
