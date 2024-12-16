package com.simprints.face.infra.rocv3.detection

import ai.roc.rocsdk.embedded.SWIGTYPE_p_float
import ai.roc.rocsdk.embedded.SWIGTYPE_p_unsigned_char
import ai.roc.rocsdk.embedded.roc_detection
import ai.roc.rocsdk.embedded.roc_image
import ai.roc.rocsdk.embedded.roc_landmark
import android.graphics.Bitmap
import android.graphics.Rect
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.face.infra.basebiosdk.detection.Face
import com.simprints.face.infra.basebiosdk.detection.FaceDetector
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton
import ai.roc.rocsdk.embedded.roc as roc3

@ExcludedFromGeneratedTestCoverageReports(
    reason = "This class uses roc 3 class that has native functions and can't be mocked",
)
@Singleton
class RocV3Detector @Inject constructor() : FaceDetector {
    override fun analyze(bitmap: Bitmap): Face? {
        val rocColorImage = roc_image()
        val rocGrayImage = roc_image()
        val byteBuffer = bitmap.toByteBuffer()
        roc3.roc_from_rgba(
            byteBuffer.array(),
            bitmap.width.toLong(),
            bitmap.height.toLong(),
            bitmap.rowBytes.toLong(),
            rocColorImage,
        )
        roc3.roc_bgr2gray(rocColorImage, rocGrayImage)
        return detectFace(rocColorImage, rocGrayImage, bitmap.width, bitmap.height)
    }

    /*
    To detect a face from image
    1- use the gray image to detect the face
    2- if the face is detected, then use the gray image to generate the landmarks
    3- use the landmarks and the color image to generate the face template
     */
    private fun detectFace(
        coloredImage: roc_image,
        grayImage: roc_image,
        width: Int,
        height: Int,
    ): Face? {
        val detection = roc_detection()
        val template = roc3.new_uint8_t_array(roc3.ROC_FACE_FAST_FV_SIZE.toInt())
        val yaw = roc3.new_float()
        val quality = roc3.new_float()
        val face = if (isFaceDetected(coloredImage, detection)) {
            generateFaceTemplateFromImage(
                coloredImage,
                grayImage,
                detection,
                yaw,
                template,
                quality,
            )
            val yawValue = roc3.float_value(yaw)
            val qualityValue = roc3.float_value(quality)
            Face(
                width,
                height,
                detection.boundingRect(),
                yawValue,
                detection.rotation,
                qualityValue,
                roc3.cdata(roc3.roc_cast(template), roc3.ROC_FACE_FAST_FV_SIZE.toInt()),
                RANK_ONE_TEMPLATE_FORMAT_3_1,
            )
        } else {
            null
        }
        // Free all resources
        roc3.roc_free_image(grayImage)
        roc3.roc_free_image(coloredImage)
        roc3.delete_float(yaw)
        roc3.delete_float(quality)
        roc3.delete_uint8_t_array(template)
        detection.delete()
        return face
    }

    private fun generateFaceTemplateFromImage(
        colorImage: roc_image,
        grayImage: roc_image,
        detection: roc_detection,
        yaw: SWIGTYPE_p_float,
        template: SWIGTYPE_p_unsigned_char,
        quality: SWIGTYPE_p_float,
    ) {
        val landmarks = roc3.new_roc_landmark_array(roc3.roc_num_landmarks_for_pose(detection.pose))
        val rightEye = roc_landmark()
        val leftEye = roc_landmark()
        val chin = roc_landmark()
        roc3.roc_embedded_landmark_face(
            grayImage,
            detection,
            landmarks,
            rightEye,
            leftEye,
            chin,
            null,
            yaw,
        )
        roc3.delete_roc_landmark_array(landmarks)

        roc3.roc_embedded_represent_face(
            colorImage,
            detection,
            rightEye,
            leftEye,
            chin,
            template,
            quality,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
        )
    }

    private fun isFaceDetected(
        image: roc_image,
        detection: roc_detection,
    ): Boolean {
        val adaptiveMinimumSize = roc3.new_size_t()

        roc3.roc_adaptive_minimum_size(
            image.width,
            image.height,
            RELATIVE_MIN_SIZE,
            ABSOLUTE_MIN_SIZE,
            adaptiveMinimumSize,
        )
        val n = roc3.new_size_t()

        roc3.roc_embedded_detect_faces_accurate(
            image,
            roc3.size_t_value(adaptiveMinimumSize),
            MAX_FACE_DETECTION,
            FALSE_DETECTION_RATE,
            n,
            detection,
        )
        val numFaces = roc3.size_t_value(n)
        roc3.delete_size_t(n)
        roc3.delete_size_t(adaptiveMinimumSize)

        return numFaces == 1L
    }

    private fun roc_detection.boundingRect() = Rect(
        (x - width / 2).toInt(),
        (y - height / 2).toInt(),
        (x + width / 2).toInt(),
        (y + height / 2).toInt(),
    )

    private fun Bitmap.toByteBuffer(): ByteBuffer {
        val byteBuffer = ByteBuffer.allocate(rowBytes * height)
        copyPixelsToBuffer(byteBuffer)
        return byteBuffer
    }

    companion object {
        const val RANK_ONE_TEMPLATE_FORMAT_3_1 = "RANK_ONE_3_1"
        const val MAX_FACE_DETECTION = 1
        const val FALSE_DETECTION_RATE = 0.1f
        const val RELATIVE_MIN_SIZE = 0.2f
        const val ABSOLUTE_MIN_SIZE = 36L
    }
}
