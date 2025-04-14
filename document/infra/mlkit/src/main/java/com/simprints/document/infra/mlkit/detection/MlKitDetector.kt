package com.simprints.document.infra.mlkit.detection

import android.graphics.Bitmap
import android.graphics.Rect
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.document.infra.basedocumentsdk.detection.Document
import com.simprints.document.infra.basedocumentsdk.detection.DocumentDetector
import javax.inject.Inject

@ExcludedFromGeneratedTestCoverageReports(
    reason = "TODO evaluate for MLkit",
)
class MlKitDetector @Inject constructor() : DocumentDetector {

    override fun analyze(bitmap: Bitmap): Document? {
        // todo
    }

}
