package com.simprints.feature.externalcredential.screens.search.usecase

import android.graphics.Bitmap
import com.simprints.feature.externalcredential.model.BoundingBox
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

internal class ZoomOntoCredentialUseCase @Inject constructor() {

    companion object {
        // All currently scanned documents are in 16:10 format
        private const val TARGET_ASPECT_RATIO = 16f / 10f
        // Increase bounding box by this percentage on each side to provide breathing room
        private const val BOX_INCREASE_PERCENTAGE = 15
    }

    /**
     * Zooms into given image. Zoom area defined by the [boundingBox]
     *
     * @param bitmap bitmap containing the document to zoom into
     * @param boundingBox bounding box that defines the zoom area
     * @return zoomed-in bitmap
     */
    operator fun invoke(bitmap: Bitmap, boundingBox: BoundingBox): Bitmap {
        // Expand the bounding box before processing
        val expandedBox = expandBoundingBox(boundingBox, bitmap.width, bitmap.height)

        // Ensuring bounding box is within image bounds
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
        if (boxAspectRatio > TARGET_ASPECT_RATIO) {
            // Bounding box is wider than 16:10, adding padding to top/bottom
            finalWidth = boxWidth
            finalHeight = (boxWidth / TARGET_ASPECT_RATIO).toInt()
        } else {
            // Bounding box is higher than 16:10, adding padding to left/right
            finalHeight = boxHeight
            finalWidth = (boxHeight * TARGET_ASPECT_RATIO).toInt()
        }

        // Center the bounding box within the final dimensions
        val extraWidth = finalWidth - boxWidth
        val extraHeight = finalHeight - boxHeight

        // Calculate crop region, trying to keep bounding box centered
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
     * @param imageWidth Width of the source image
     * @param imageHeight Height of the source image
     * @return Expanded bounding box (may exceed image bounds, will be clamped later)
     */
    private fun expandBoundingBox(
        box: BoundingBox,
        imageWidth: Int,
        imageHeight: Int
    ): BoundingBox {
        val boxWidth = box.right - box.left
        val boxHeight = box.bottom - box.top

        // Calculate the expansion amount for each dimension
        val horizontalExpansion = (boxWidth * BOX_INCREASE_PERCENTAGE / 100f).toInt()
        val verticalExpansion = (boxHeight * BOX_INCREASE_PERCENTAGE / 100f).toInt()

        return BoundingBox(
            left = box.left - horizontalExpansion,
            top = box.top - verticalExpansion,
            right = box.right + horizontalExpansion,
            bottom = box.bottom + verticalExpansion
        )
    }
}
