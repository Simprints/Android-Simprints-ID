package com.simprints.feature.externalcredential.screens.scanocr.usecase

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.simprints.feature.externalcredential.model.BoundingBox
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

internal class ZoomOntoCredentialUseCase @Inject constructor() {

    companion object {
        private const val TARGET_ASPECT_RATIO = 16f / 10f
        private const val BOX_INCREASE_PERCENTAGE = 15
    }

    /**
     * Zooms into given image. Zoom area defined by the [boundingBox]
     *
     * @param imagePath path to image containing the document to zoom into
     * @param boundingBox bounding box that defines the zoom area
     * @return zoomed-in bitmap
     */
    operator fun invoke(imagePath: String, boundingBox: BoundingBox): Bitmap {
        val bitmap = BitmapFactory.decodeFile(imagePath)
        val expandedBox = expandBoundingBox(boundingBox, BOX_INCREASE_PERCENTAGE)

        val left = expandedBox.left.coerceIn(0, bitmap.width)
        val top = expandedBox.top.coerceIn(0, bitmap.height)
        val right = expandedBox.right.coerceIn(left, bitmap.width)
        val bottom = expandedBox.bottom.coerceIn(top, bitmap.height)
        val boxWidth = right - left
        val boxHeight = bottom - top

        if (boxWidth <= 0 || boxHeight <= 0) {
            return bitmap
        }

        val boxAspectRatio = boxWidth.toFloat() / boxHeight.toFloat()
        val finalWidth: Int
        val finalHeight: Int
        // Bounding box is taller/wider than 16:10, adding padding to left/right or top/bottom
        if (boxAspectRatio > TARGET_ASPECT_RATIO) {
            finalWidth = boxWidth
            finalHeight = (boxWidth / TARGET_ASPECT_RATIO).toInt()
        } else {
            finalHeight = boxHeight
            finalWidth = (boxHeight * TARGET_ASPECT_RATIO).toInt()
        }

        val extraWidth = finalWidth - boxWidth
        val extraHeight = finalHeight - boxHeight
        val cropLeft = max(0, left - extraWidth / 2)
        val cropTop = max(0, top - extraHeight / 2)
        val cropRight = min(bitmap.width, cropLeft + finalWidth)
        val cropBottom = min(bitmap.height, cropTop + finalHeight)

        // Adjust if we hit image boundaries
        val adjustedLeft = max(0, cropRight - finalWidth)
        val adjustedTop = max(0, cropBottom - finalHeight)
        val actualWidth = cropRight - adjustedLeft
        val actualHeight = cropBottom - adjustedTop
        return Bitmap.createBitmap(bitmap, adjustedLeft, adjustedTop, actualWidth, actualHeight)
    }

    /**
     * Expands a bounding box by a percentage on all sides.
     *
     * @param box Original bounding box
     * @return Expanded bounding box that may exceed image bounds as it is addressed later
     */
    private fun expandBoundingBox(box: BoundingBox, percentage: Int): BoundingBox {
        val boxWidth = box.right - box.left
        val boxHeight = box.bottom - box.top
        val horizontalExpansion = (boxWidth * percentage / 100f).toInt()
        val verticalExpansion = (boxHeight * percentage / 100f).toInt()
        return BoundingBox(
            left = box.left - horizontalExpansion,
            top = box.top - verticalExpansion,
            right = box.right + horizontalExpansion,
            bottom = box.bottom + verticalExpansion
        )
    }
}
