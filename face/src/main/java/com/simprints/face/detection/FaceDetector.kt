package com.simprints.face.detection

import android.graphics.Bitmap
import com.simprints.uicomponents.models.PreviewFrame

interface FaceDetector {

    fun analyze(previewFrame: PreviewFrame): Face?

    suspend fun analyze(bitmap: Bitmap): Face?
}
