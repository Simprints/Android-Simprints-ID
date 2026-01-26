package com.simprints.feature.externalcredential.model

import android.graphics.Rect
import androidx.annotation.Keep
import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

/**
 * A serializable substitute for Android's Rect class, which is not serializable.
 * Used for passing rectangular bounds as navigation arguments or in any other scenarios where serialization is required
 */
@Keep
@Serializable
data class BoundingBox(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
) : JavaSerializable

fun Rect.toBoundingBox(): BoundingBox = BoundingBox(left, top, right, bottom)

fun BoundingBox.toRect(): Rect = Rect(left, top, right, bottom)
