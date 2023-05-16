package com.simprints.face.detection

import android.graphics.Bitmap

fun interface FaceDetector {

    fun analyze(bitmap: Bitmap): Face?
}
