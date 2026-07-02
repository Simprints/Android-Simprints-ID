package com.simprints.face.infra.basebiosdk.detection

import android.graphics.Bitmap

interface FaceDetector {
    /**
     * Analyze an ARGB_8888 bitmap and return the detected face data
     *
     * @param bitmap ARGB_8888 formatted
     * @return Face object or null if no face is detected
     */
    fun analyze(bitmap: Bitmap): Face?

    /**
     * Perform a spoof check on an ARGB_8888 bitmap
     *
     * @param bitmap original captured image (ARGB_8888)
     * @return Either a spoof score (lower is better) or a reason why the check was skipped
     */
    fun spoofCheck(bitmap: Bitmap): SpoofCheckResult
}
