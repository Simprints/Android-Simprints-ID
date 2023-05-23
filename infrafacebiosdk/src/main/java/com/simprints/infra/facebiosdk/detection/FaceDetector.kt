package com.simprints.infra.facebiosdk.detection

import android.graphics.Bitmap

fun interface FaceDetector {
    /**
     * Analyze captured bitmap and returns the detected face data
     *
     * @param bitmap
     * @return Face object or null if no face is detected
     */
    fun analyze(bitmap: Bitmap): Face?
}
