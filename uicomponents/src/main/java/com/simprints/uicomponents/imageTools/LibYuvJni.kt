package com.simprints.uicomponents.imageTools

import android.graphics.Rect
import com.simprints.uicomponents.models.Size

class LibYuvJni {

    companion object {
        init {
            System.loadLibrary("yuvjni")
        }
    }

    fun cropRotateYuvNV21(
        srcSize: Size, srcBytes: ByteArray,
        rect: Rect,
        rotation: Int
    ): Pair<Size, ByteArray> {
        // NV21 works by blocks of 2x2 pixels, so cropping and rotating regions
        // with even coordinates is faster
        val evenRect = with(rect) {
            Rect(2 * (left / 2), 2 * (top / 2), 2 * (right / 2), 2 * (bottom / 2))
        }
        val evenSize = with(evenRect) {
            Size(
                width(),
                height()
            )
        }
        val ySize = evenSize.rotate(rotation)

        val dstBytes = cropRotateYuvNV21(
            srcSize.width, srcSize.height, srcBytes,
            evenRect.left, evenRect.top, evenRect.right, evenRect.bottom, rotation
        )

        return Pair(ySize, dstBytes)
    }

    private external fun cropRotateYuvNV21(
        srcWidth: Int, srcHeight: Int, srcByteArray: ByteArray,
        left: Int, top: Int, right: Int, bottom: Int,
        rotation: Int
    ): ByteArray

    private fun Size.rotate(rotation: Int) =
        when (rotation) {
            in arrayOf(90, 270) -> Size(height, width)
            in arrayOf(0, 180, 360) -> Size(
                width,
                height
            )
            else -> throw IllegalArgumentException("Unsupported rotation: $rotation")
        }
}
