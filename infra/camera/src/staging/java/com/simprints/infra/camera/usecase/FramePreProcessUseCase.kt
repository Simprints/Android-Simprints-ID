package com.simprints.infra.camera.usecase

import android.graphics.Bitmap
import android.graphics.Rect
import com.simprints.infra.camera.Frame
import javax.inject.Inject

internal class FramePreProcessUseCase @Inject constructor() {
    /**
     * Wraps the provided arguments into a [Frame] object.
     */
    operator fun invoke(
        bitmap: Bitmap,
        rotation: Int,
        previewRect: Rect,
        targetRect: Rect,
    ): Frame = Frame(
        bitmap = bitmap,
        rotation = rotation,
        previewBounds = previewRect,
        targetBounds = targetRect,
    )
}
