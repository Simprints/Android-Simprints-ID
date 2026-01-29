package com.simprints.face.capture.models

import android.graphics.Bitmap

/**
 * Diagnostic information for face detection debugging.
 * This class holds all the raw detection values and thresholds to help
 * diagnose why faces may not be detected correctly during testing.
 */
internal data class FaceDiagnostics(
    val croppedBitmap: Bitmap?,
    val faceDetected: Boolean,
    val areaOccupied: Float?,
    val areaRangeMin: Float,
    val areaRangeMax: Float,
    val yaw: Float?,
    val yawThreshold: Float,
    val roll: Float?,
    val rollThreshold: Float,
    val quality: Float?,
    val qualityThreshold: Float,
    val status: FaceDetection.Status,
) {
    fun toDisplayString(): String = buildString {
        appendLine("Status: $status")
        appendLine("Face detected: $faceDetected")
        appendLine("─────────────────")
        appendLine("Bitmap: ${croppedBitmap?.width ?: 0}x${croppedBitmap?.height ?: 0}")
        appendLine("─────────────────")
        appendLine("Area: ${areaOccupied?.format(3) ?: "N/A"}")
        appendLine("  Range: [${areaRangeMin.format(2)} - ${areaRangeMax.format(2)}]")
        appendLine("─────────────────")
        appendLine("Yaw: ${yaw?.format(1) ?: "N/A"}°")
        appendLine("  Threshold: ±${yawThreshold.format(1)}°")
        appendLine("─────────────────")
        appendLine("Roll: ${roll?.format(1) ?: "N/A"}°")
        appendLine("  Threshold: ±${rollThreshold.format(1)}°")
        appendLine("─────────────────")
        appendLine("Quality: ${quality?.format(3) ?: "N/A"}")
        appendLine("  Threshold: ${qualityThreshold.format(3)}")
    }

    private fun Float.format(decimals: Int): String = "%.${decimals}f".format(this)
}
