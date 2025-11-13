package com.simprints.infra.uibase.camera.qrscan.usecase

import android.graphics.Rect
import javax.inject.Inject

class MapCropRectToImageSpaceUseCase @Inject constructor() {
    /**
     * Maps a crop rectangle defined in the root view's coordinate space into the corresponding rectangle in the image's coordinate space.
     * This is required when the crop area is selected on a view (UI) with different dimensions than the underlying image being processed.
     *
     * @param cropRectInRoot the crop rectangle coordinates in the root view space
     * @param rootWidth the width of the root view
     * @param rootHeight the height of the root view
     * @param imageWidth the width of the image
     * @param imageHeight the height of the image
     * @return a [Rect] representing the crop area in the image's coordinate space
     */
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
