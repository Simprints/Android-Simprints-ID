package com.simprints.ear.infra.basebiosdk.detection

import android.graphics.Bitmap

fun interface EarDetector {
    /**
     * Analyze an ARGB_8888 bitmap and return the detected ear data
     *
     * @param bitmap ARGB_8888 formatted
     * @return Ear object or null if no ear is detected
     */
    fun analyze(bitmap: Bitmap): Ear?
}
