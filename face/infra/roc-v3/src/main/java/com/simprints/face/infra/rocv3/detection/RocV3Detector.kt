package com.simprints.face.infra.rocv3.detection

import ai.roc.rocsdk.embedded.SWIGTYPE_p_float
import ai.roc.rocsdk.embedded.SWIGTYPE_p_unsigned_char
import ai.roc.rocsdk.embedded.roc
import ai.roc.rocsdk.embedded.roc_detection
import ai.roc.rocsdk.embedded.roc_image
import ai.roc.rocsdk.embedded.roc_landmark
import android.graphics.Bitmap
import android.graphics.Rect
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.face.infra.basebiosdk.detection.Face
import com.simprints.face.infra.basebiosdk.detection.FaceDetector
import com.simprints.infra.logging.Simber
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton


@ExcludedFromGeneratedTestCoverageReports(
    reason = "This class uses roc class that has native functions and can't be mocked"
)
@Singleton
class RocV3Detector @Inject constructor() : FaceDetector {
    companion object {
        const val RANK_ONE_TEMPLATE_FORMAT_3_1 = "RANK_ONE_EMBEDDED_3_1"
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
    data class ROCFace(
        var face: roc_detection,
        var template: SWIGTYPE_p_unsigned_char,
        var yaw: SWIGTYPE_p_float,
        var quality: SWIGTYPE_p_float
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
            rocColorImage
        )

        roc.roc_bgr2gray(rocColorImage, rocGrayImage)

        roc.roc_free_image(rocColorImage)

        return analyze(rocColorImage, rocGrayImage, bitmap.width, bitmap.height)
    }

    /**
     * @param rocGrayImage is a grayscale roc_image
     */
    private fun analyze(
        rocColorImage: roc_image,
        rocGrayImage: roc_image,
        imageWidth: Int,
        imageHeight: Int
    ): Face? {
        val rocFace = ROCFace(
            roc_detection(),
            roc.new_uint8_t_array(roc.ROC_FACE_FAST_FV_SIZE.toInt()),
            roc.new_float(),
            roc.new_float()
        )

        val faceDetected = getRocTemplateFromImage(rocColorImage, rocGrayImage, rocFace)

        if (!faceDetected) {
            roc.roc_free_image(rocGrayImage)
            rocFace.cleanup()
            return null
        }
        val yawValue = roc.float_value(rocFace.yaw)

        val qualityValue = roc.float_value(rocFace.quality)
        log("Yaw: $yawValue, Quality: $qualityValue")
        val face = Face(
            imageWidth,
            imageHeight,
            Rect(
                (rocFace.face.x - rocFace.face.width / 2).toInt(),
                (rocFace.face.y - rocFace.face.height / 2).toInt(),
                (rocFace.face.x + rocFace.face.width / 2).toInt(),
                (rocFace.face.y + rocFace.face.height / 2).toInt()
            ),
            yawValue,
            rocFace.face.rotation,
            qualityValue,
            roc.cdata(roc.roc_cast(rocFace.template), roc.ROC_FACE_FAST_FV_SIZE.toInt()),
            RANK_ONE_TEMPLATE_FORMAT_3_1
        )

        // Free all resources after getting the face
        roc.roc_free_image(rocGrayImage)
        rocFace.cleanup()
        log("Face detected: ${face.template.size}")
        return face
    }


    private fun getRocTemplateFromImage(
        colorImage: roc_image,
        grayImage: roc_image,
        rocFace: ROCFace
    ): Boolean {
        if (noFaceDetected(grayImage, rocFace)) {
            return false
        }

        val landmarks =
            roc.new_roc_landmark_array(roc.roc_num_landmarks_for_pose(rocFace.face.pose))
        val rightEye = roc_landmark()
        val leftEye = roc_landmark()
        val chin = roc_landmark()
        roc.roc_embedded_landmark_face(
            grayImage,
            rocFace.face,
            landmarks,
            rightEye,
            leftEye,
            chin,
            null,
            rocFace.yaw
        )
        roc.delete_roc_landmark_array(landmarks)

        roc.roc_embedded_represent_face(
            colorImage,
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
            null
        )

        return true
    }


    private fun noFaceDetected(
        image: roc_image,
        rocFace: ROCFace,
    ): Boolean {

        val adaptiveMinimumSize = roc.new_size_t()

        roc.roc_adaptive_minimum_size(
            image.width,
            image.height,
            relativeMinSize,
            absoluteMinSize,
            adaptiveMinimumSize
        )
        val n = roc.new_size_t()

        roc.roc_embedded_detect_faces(
            image,
            roc.size_t_value(adaptiveMinimumSize),
            maxFaces,
            falseDetectionRate,
            n,
            rocFace.face
        )
        val numFaces = roc.size_t_value(n)
        roc.delete_size_t(n)
        roc.delete_size_t(adaptiveMinimumSize)

        return numFaces != 1L
    }


}

fun log(message: String) {
    Simber.tag("RankOne").i(message)
}
