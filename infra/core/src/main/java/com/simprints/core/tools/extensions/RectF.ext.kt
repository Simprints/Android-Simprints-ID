package com.simprints.core.tools.extensions

import android.graphics.Rect
import android.graphics.RectF
import kotlin.math.abs

fun RectF.area() = abs(height() * width())

/**
 * Expands a bounding box expressed as fractions of the source image into source pixels.
 */
fun RectF.scaledTo(
    width: Int,
    height: Int,
) = RectF(left * width, top * height, right * width, bottom * height)

/**
 * Inverse of [scaledTo]: expresses a pixel rect as fractions of the source image.
 */
fun Rect.normalisedIn(
    width: Int,
    height: Int,
) = RectF(
    left.toFloat() / width,
    top.toFloat() / height,
    right.toFloat() / width,
    bottom.toFloat() / height,
)
