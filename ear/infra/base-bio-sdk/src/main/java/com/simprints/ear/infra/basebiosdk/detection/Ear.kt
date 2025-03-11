package com.simprints.ear.infra.basebiosdk.detection

import android.graphics.Rect
import android.graphics.RectF

/**
 * Face that represents a detected biometric target after
 *
 * @property sourceWidth bounding rectangle width
 * @property sourceHeight bounding rectangle height
 * @property quality image quality
 * @property template
 * @property format
 */
data class Ear(
    private val sourceWidth: Int,
    private val sourceHeight: Int,
    private val absoluteBoundingBox: Rect,
    val quality: Float,
    val template: ByteArray,
    val format: String,
) {
    // Relative = coordinates are fractions of the source image dimensions
    val relativeBoundingBox
        get() = RectF(
            absoluteBoundingBox.left.toFloat() / sourceWidth,
            absoluteBoundingBox.top.toFloat() / sourceHeight,
            absoluteBoundingBox.right.toFloat() / sourceWidth,
            absoluteBoundingBox.bottom.toFloat() / sourceHeight,
        )
}
