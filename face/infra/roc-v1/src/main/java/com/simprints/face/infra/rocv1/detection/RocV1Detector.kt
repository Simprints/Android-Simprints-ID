package com.simprints.face.infra.rocv1.detection

import android.graphics.Bitmap
import android.graphics.Rect
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.face.infra.basebiosdk.detection.Face
import com.simprints.face.infra.basebiosdk.detection.FaceDetector
import io.rankone.rocsdk.embedded.SWIGTYPE_p_float
import io.rankone.rocsdk.embedded.SWIGTYPE_p_unsigned_char
import io.rankone.rocsdk.embedded.roc
import io.rankone.rocsdk.embedded.roc_detection
import io.rankone.rocsdk.embedded.roc_embedded_landmark
import io.rankone.rocsdk.embedded.roc_image
import java.nio.ByteBuffer
import javax.inject.Inject

@ExcludedFromGeneratedTestCoverageReports(
    reason = "This class uses roc class that has native functions and can't be mocked",
)
class RocV1Detector @Inject constructor() : FaceDetector {
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
        reason = "This class uses roc class that has native functions and can't be mocked",
    )
    data class ROCFace(
        var face: roc_detection,
        var template: SWIGTYPE_p_unsigned_char,
        var yaw: SWIGTYPE_p_float,
        var quality: SWIGTYPE_p_float,
    ) {
        fun cleanup() {
            face.delete()
            roc.delete_uint8_t_array(template)
            roc.delete_float(yaw)
            roc.delete_float(quality)
        }
    }

    override fun analyze(bitmap: Bitmap): Face? {
        val rocColorImage = roc_image()
        val rocGrayImage = roc_image()

        val byteBuffer: ByteBuffer = ByteBuffer.allocate(bitmap.rowBytes * bitmap.height)
        bitmap.copyPixelsToBuffer(byteBuffer)
        roc.roc_from_rgba(
            byteBuffer.array(),
            bitmap.width.toLong(),
            bitmap.height.toLong(),
            bitmap.rowBytes.toLong(),
            rocColorImage,
        )

        roc.roc_bgr2gray(rocColorImage, rocGrayImage)

        roc.roc_free_image(rocColorImage)

        return analyze(rocGrayImage, bitmap.width, bitmap.height)
    }

    /**
     * @param rocImage is a grayscale roc_image
     */
    private fun analyze(
        rocImage: roc_image,
        imageWidth: Int,
        imageHeight: Int,
    ): Face? {
        val rocFace = ROCFace(
            roc_detection(),
            roc.new_uint8_t_array(roc.ROC_FAST_FV_SIZE.toInt()),
            roc.new_float(),
            roc.new_float(),
        )

        val faceDetected = getRocTemplateFromImage(rocImage, rocFace)

        if (!faceDetected) {
            roc.roc_free_image(rocImage)
            rocFace.cleanup()
            return null
        }

        val yawValue = roc.float_value(rocFace.yaw)

        val qualityValue = roc.float_value(rocFace.quality)

        val face = Face(
            imageWidth,
            imageHeight,
            Rect(
                (rocFace.face.x - rocFace.face.width / 2).toInt(),
                (rocFace.face.y - rocFace.face.height / 2).toInt(),
                (rocFace.face.x + rocFace.face.width / 2).toInt(),
                (rocFace.face.y + rocFace.face.height / 2).toInt(),
            ),
            yawValue,
            rocFace.face.rotation,
            qualityValue,
            floatArrayOf(),
            RANK_ONE_TEMPLATE_FORMAT_1_23,
        )

        // Free all resources after getting the face
        roc.roc_free_image(rocImage)
        rocFace.cleanup()

        return face
    }

    private fun getRocTemplateFromImage(
        image: roc_image,
        rocFace: ROCFace,
    ): Boolean {
        val adaptiveMinimumSize = roc.new_size_t()
        roc.roc_ensure(
            roc.roc_adaptive_minimum_size(
                image.width,
                image.height,
                relativeMinSize,
                absoluteMinSize,
                adaptiveMinimumSize,
            ),
        )

        val n = roc.new_size_t()

        roc.roc_ensure(
            roc.roc_embedded_error_to_string(
                roc.roc_embedded_detect_faces(
                    image,
                    roc.size_t_value(adaptiveMinimumSize),
                    maxFaces,
                    falseDetectionRate,
                    n,
                    rocFace.face,
                ),
            ),
        )

        if (roc.size_t_value(n) != 1L) {
            roc.delete_size_t(adaptiveMinimumSize)
            roc.delete_size_t(n)
            return false
        }

        val landmarks = roc.new_roc_embedded_landmark_array(68)
        val rightEye = roc_embedded_landmark()
        val leftEye = roc_embedded_landmark()
        val chin = roc_embedded_landmark()
        roc.roc_ensure(
            roc.roc_embedded_error_to_string(
                roc.roc_embedded_landmark_face(
                    image,
                    rocFace.face,
                    landmarks,
                    rightEye,
                    leftEye,
                    chin,
                    null,
                    rocFace.yaw,
                ),
            ),
        )

        roc.roc_ensure(
            roc.roc_embedded_error_to_string(
                roc.roc_embedded_represent_face(
                    image,
                    rocFace.face,
                    rightEye,
                    leftEye,
                    chin,
                    rocFace.template,
                    rocFace.quality,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                ),
            ),
        )

        // Cleanup
        roc.delete_size_t(adaptiveMinimumSize)
        roc.delete_size_t(n)
        roc.delete_roc_embedded_landmark_array(landmarks)

        return true
    }
}
