package com.simprints.document.infra.basedocumentsdk.detection

import android.graphics.Rect
import android.graphics.RectF

/**
 * Document that represents a detected target after
 *
 * @property sourceWidth bounding rectangle width
 * @property sourceHeight bounding rectangle height
 * @property absoluteBoundingBox Rect for the absolute coordinates of the detected document
 * @property yaw rotation around the y access
 * @property roll rotation around the x access
 * @property quality image quality
 * @property template
 * @property format
 *
 */
data class Document(
    private val sourceWidth: Int,
    private val sourceHeight: Int,
    private val absoluteBoundingBox: Rect,
    val yaw: Float,
    var roll: Float,
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
