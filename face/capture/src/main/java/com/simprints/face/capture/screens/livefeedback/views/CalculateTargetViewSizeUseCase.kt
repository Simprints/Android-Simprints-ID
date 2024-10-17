package com.simprints.face.capture.screens.livefeedback.views

import android.content.Context
import com.simprints.core.tools.extentions.dpToPx
import com.simprints.infra.logging.Simber
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class CalculateTargetViewSizeUseCase @Inject constructor(@ApplicationContext private val context: Context) {
    companion object{
        const val BASE_TARGET_SIZE_DP = 240f // Target size in dp
        const val DEFAULT_MULTIPLIER = 0.5f    // Default multiplier
    }
    /**
     * This function calculates the appropriate width for the target face capture view based on
     * the device's screen size. The target width is set to half of the smallest dimension
     * (either the screen width or height), but it will never be smaller than the minimum target
     * width of 240dp.
     *
     * Purpose:
     * - Ensures that the face capture area adapts to different screen sizes
     * - Prevents the target view from being too small on large screens by enforcing a minimum
     *   size, allowing health workers to easily capture a user's face without needing to adjust
     *   too much based on the device size.
     */

    operator fun invoke(): Float {
        // Get the screen width and height
        val displayMetrics = context.resources.displayMetrics
        val screenWidthPx = displayMetrics.widthPixels
        val screenHeightPx = displayMetrics.heightPixels
        log("screenWidthPx: $screenWidthPx, screenHeightPx: $screenHeightPx")

        // Calculate the smaller dimension in pixels
        val smallerDimensionPx = minOf(screenWidthPx, screenHeightPx).toFloat()

        // Convert the base target size from dp to pixels
        val baseTargetSizePx = BASE_TARGET_SIZE_DP.dpToPx(context)

        // Calculate the scaled target size based on the multiplier
        val scaledTargetSizePx = smallerDimensionPx * DEFAULT_MULTIPLIER
        log("scaledTargetSizePx: $scaledTargetSizePx")

        // Ensure the target size is at least 240dp
        return maxOf(scaledTargetSizePx, baseTargetSizePx)
    }
}
fun log(message: String) {
    Simber.tag("calculateTargetWidth").i(message)
}
