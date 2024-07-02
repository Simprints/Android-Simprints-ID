package com.simprints.face.infra.basebiosdk.detection

import android.graphics.Bitmap

fun interface FaceDetector {
    /**
     * Analyze an ARGB_8888 bitmap and return the detected face data
     *
     * @param bitmap ARGB_8888 formatted
     * @return Face object or null if no face is detected
     */
    fun analyze(bitmap: Bitmap): Face?
}
