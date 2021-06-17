package com.simprints.face.utils

import com.simprints.face.models.FaceDetection
import com.simprints.uicomponents.models.PreviewFrame

val mockFaceDetectionList = listOf(
    FaceDetection(
        frame = PreviewFrame(
            width = 100,
            height = 100,
            format = 0,
            mirrored = false,
            bytes = byteArrayOf()
        ),
        face = null,
        status = FaceDetection.Status.VALID,
        securedImageRef = null,
        detectionStartTime = 0,
        isFallback = false,
        id = "",
        detectionEndTime = 0
    )
)
