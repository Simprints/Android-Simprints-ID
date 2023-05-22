package com.simprints.infra.facebiosdk.detection

import android.graphics.Rect
import android.graphics.RectF

// All values are after rotation of the source image
open class Face(
    val sourceWidth: Int,
    val sourceHeight: Int,
    val absoluteBoundingBox: Rect,
    val yaw: Float,
    var roll: Float,
    val quality: Float,
    val template: ByteArray,
    val format: String
) {

    // Relative = coordinates are fractions of the source image dimensions
    val relativeBoundingBox: RectF
        get() = RectF(
            absoluteBoundingBox.left.toFloat() / sourceWidth,
            absoluteBoundingBox.top.toFloat() / sourceHeight,
            absoluteBoundingBox.right.toFloat() / sourceWidth,
            absoluteBoundingBox.bottom.toFloat() / sourceHeight
        )

}
