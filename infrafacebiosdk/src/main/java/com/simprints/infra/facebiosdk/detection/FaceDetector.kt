package com.simprints.infra.facebiosdk.detection

import android.graphics.Bitmap

fun interface FaceDetector {
    fun analyze(bitmap: Bitmap): Face?
}
