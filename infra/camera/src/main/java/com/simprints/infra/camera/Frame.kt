package com.simprints.infra.camera

import android.graphics.Bitmap
import android.graphics.Rect

data class Frame(
    val bitmap: Bitmap,
    val rotation: Int,
    val targetBounds: Rect,
    val previewBounds: Rect,
)
