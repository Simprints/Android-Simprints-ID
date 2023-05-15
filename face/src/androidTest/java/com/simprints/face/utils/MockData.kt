package com.simprints.face.utils

import android.graphics.Bitmap
import com.simprints.face.models.FaceDetection

val mockFaceDetectionList = listOf(
    FaceDetection(
        Bitmap.createBitmap(100,100,Bitmap.Config.ARGB_8888),
        face = null,
        status = FaceDetection.Status.VALID,
        securedImageRef = null,
        detectionStartTime = 0,
        isFallback = false,
        id = "",
        detectionEndTime = 0
    )
)
