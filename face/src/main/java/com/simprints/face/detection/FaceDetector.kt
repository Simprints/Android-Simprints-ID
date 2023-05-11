package com.simprints.face.detection

import android.graphics.Bitmap

interface FaceDetector {

    fun analyze(bitmap: Bitmap): Face?
}
