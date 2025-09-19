package com.simprints.infra.uibase.camera.qrscan.usecase

import android.graphics.Rect
import javax.inject.Inject

class MapCropRectToImageSpaceUseCase @Inject constructor() {
    operator fun invoke(
        cropRectInRoot: Rect,
        rootWidth: Int,
        rootHeight: Int,
        imageWidth: Int,
        imageHeight: Int,
    ): Rect {
        val scaleX = imageWidth.toFloat() / rootWidth
        val scaleY = imageHeight.toFloat() / rootHeight

        return Rect(
            (cropRectInRoot.left * scaleX).toInt(),
            (cropRectInRoot.top * scaleY).toInt(),
            (cropRectInRoot.right * scaleX).toInt(),
            (cropRectInRoot.bottom * scaleY).toInt()
        )
    }
}
