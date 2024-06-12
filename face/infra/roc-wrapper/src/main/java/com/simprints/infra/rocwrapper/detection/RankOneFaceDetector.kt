package com.simprints.infra.rocwrapper.detection

import android.graphics.Bitmap
import android.graphics.Rect
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.infra.facebiosdk.detection.Face
import com.simprints.infra.facebiosdk.detection.FaceDetector


import java.nio.ByteBuffer
import javax.inject.Inject


@ExcludedFromGeneratedTestCoverageReports(
    reason = "This class uses roc class that has native functions and can't be mocked"
)
class RankOneFaceDetector @Inject constructor() : FaceDetector {
    companion object {
        const val RANK_ONE_TEMPLATE_FORMAT_1_23 = "RANK_ONE_1_23"
    }

    private val maxFaces = 1
    private val falseDetectionRate = 0.1f
    private val relativeMinSize = 0.2f
    private val absoluteMinSize = 36L

    // Ignore this class from test coverage calculations
    // because it uses jni native code which is hard to test
    @ExcludedFromGeneratedTestCoverageReports(
        reason = "This class uses roc class that has native functions and can't be mocked"
    )


    override fun analyze(bitmap: Bitmap): Face? {

        val byteBuffer: ByteBuffer = ByteBuffer.allocate(bitmap.rowBytes * bitmap.height)
        bitmap.copyPixelsToBuffer(byteBuffer)

        return null
    }

    /**
     * @param rocImage is a grayscale roc_image
     */




}
