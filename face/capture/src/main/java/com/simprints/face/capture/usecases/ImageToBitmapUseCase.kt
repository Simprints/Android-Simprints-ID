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
internal class ImageToBitmapUseCase @Inject constructor() {

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
        val croppedRotatedBitmap = Bitmap.createBitmap(
            bitmap,
            cropRect.left,
            cropRect.top,
            cropRect.width(),
            cropRect.height(),
            rotationMatrix,
            true
        )
        bitmap.recycle()
        return croppedRotatedBitmap
    }

}
