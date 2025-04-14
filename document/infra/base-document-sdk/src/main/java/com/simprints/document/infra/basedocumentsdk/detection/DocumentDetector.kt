package com.simprints.document.infra.basedocumentsdk.detection

import android.graphics.Bitmap

fun interface DocumentDetector {
    /**
     * Analyze an ARGB_8888 bitmap and return the detected document data
     *
     * @param bitmap ARGB_8888 formatted
     * @return Document object or null if no document is detected
     */
    fun analyze(bitmap: Bitmap): Document?
}
