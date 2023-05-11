package com.simprints.face.capture.livefeedback.tools

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import androidx.camera.core.ImageProxy
import androidx.core.graphics.toRect
import com.simprints.core.tools.extentions.toBitmap
import javax.inject.Inject

class FrameProcessor @Inject constructor() {
    private lateinit var cropRect: RectF

    fun init(cropRect: RectF) {
        this.cropRect = cropRect

    }

    //Todo adjust cropped image bounds
    fun cropRotateFrame(image: ImageProxy): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(image.imageInfo.rotationDegrees.toFloat())

        return with(cropRect.toRect()) {
            Bitmap.createBitmap(
                image.toBitmap(),
                top,
                left,
                width(),
                height(),
                matrix,
                true
            )
        }
    }
}
