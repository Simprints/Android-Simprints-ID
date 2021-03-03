package com.simprints.face.detection

import android.graphics.Rect
import android.graphics.RectF
import com.simprints.face.models.FaceDetection

// All values are after rotation of the source image
open class Face(
    val sourceWidth: Int,
    val sourceHeight: Int,
    val absoluteBoundingBox: Rect,
    val yaw: Float,
    var roll: Float,
    val quality: Float,
    val template: ByteArray,
    val format: FaceDetection.TemplateFormat
) {

    // Relative = coordinates are fractions of the source image dimensions
    val relativeBoundingBox: RectF
        get() = RectF(
            absoluteBoundingBox.left.toFloat() / sourceWidth,
            absoluteBoundingBox.top.toFloat() / sourceHeight,
            absoluteBoundingBox.right.toFloat() / sourceWidth,
            absoluteBoundingBox.bottom.toFloat() / sourceHeight
        )

    fun mirror(): Face = Face(
        sourceWidth = sourceWidth,
        sourceHeight = sourceHeight,
        absoluteBoundingBox = with(absoluteBoundingBox) {
            Rect(sourceWidth - right, top, sourceWidth - left, bottom)
        },
        yaw = -yaw,
        roll = -roll,
        quality = quality,
        template = template,
        format = format
    )

    fun scale(scale: Float): Face =
        if (scale == 1F) {
            this
        } else {
            Face(
                sourceWidth = (sourceWidth * scale).toInt(),
                sourceHeight = (sourceHeight * scale).toInt(),
                absoluteBoundingBox = with(absoluteBoundingBox) {
                    Rect(
                        (left * scale).toInt(),
                        (top * scale).toInt(),
                        (right * scale).toInt(),
                        (bottom * scale).toInt()
                    )
                },
                yaw = yaw,
                roll = roll,
                quality = quality,
                template = template,
                format = format
            )
        }

}
