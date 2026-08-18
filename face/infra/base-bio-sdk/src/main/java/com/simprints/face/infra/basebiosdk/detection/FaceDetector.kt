package com.simprints.face.infra.basebiosdk.detection

import android.graphics.Bitmap
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports

@ExcludedFromGeneratedTestCoverageReports("No need to test the interface")
interface FaceDetector {
    /**
     * Analyze an ARGB_8888 bitmap and return the detected face data
     *
     * @param bitmap ARGB_8888 formatted
     * @param estimateAgeAndGender whether to also request age/gender estimation. This is extra native
     * processing on top of face detection/template extraction, so it should only be requested for
     * the final selected capture, not on every live-preview frame.
     * @return Face object or null if no face is detected
     */
    fun analyze(
        bitmap: Bitmap,
        estimateAgeAndGender: Boolean = false,
    ): Face?

    /**
     * Perform a spoof check on an ARGB_8888 bitmap
     *
     * @param bitmap original captured image (ARGB_8888)
     * @return Either a spoof score (lower is better) or a reason why the check was skipped
     */
    fun spoofCheck(
        bitmap: Bitmap,
        configuredMaxSize: Int,
    ): SpoofCheckResult
}
