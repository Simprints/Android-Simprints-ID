package com.simprints.face.infra.rocv3.detection

import ai.roc.rocsdk.embedded.SWIGTYPE_p_float
import ai.roc.rocsdk.embedded.SWIGTYPE_p_unsigned_char
import ai.roc.rocsdk.embedded.roc_detection
import ai.roc.rocsdk.embedded.roc_embedded_gender
import ai.roc.rocsdk.embedded.roc_image
import ai.roc.rocsdk.embedded.roc_landmark
import android.graphics.Bitmap
import android.graphics.Rect
import androidx.core.graphics.scale
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.face.infra.basebiosdk.detection.Face
import com.simprints.face.infra.basebiosdk.detection.FaceDetector
import com.simprints.face.infra.basebiosdk.detection.FaceSelector
import com.simprints.face.infra.basebiosdk.detection.SpoofCheckResult
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.roundToInt
import ai.roc.rocsdk.embedded.roc as roc3

@ExcludedFromGeneratedTestCoverageReports(
    reason = "This class uses roc 3 class that has native functions and can't be mocked",
)
@Singleton
class RocV3Detector @Inject constructor() : FaceDetector {
    override fun analyze(
        bitmap: Bitmap,
        estimateAgeAndGender: Boolean,
        selectFace: FaceSelector?,
    ): Face? {
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
        return detectFace(rocColorImage, rocGrayImage, bitmap.width, bitmap.height, estimateAgeAndGender, selectFace)
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
        estimateAgeAndGender: Boolean,
        selectFace: FaceSelector?,
    ): Face? {
        val maxFaces = maxFacesFor(selectFace)
        val detections = roc3.new_roc_detection_array(maxFaces)
        val template = roc3.new_uint8_t_array(roc3.ROC_FACE_FAST_FV_SIZE.toInt())
        val yaw = roc3.new_float()
        val quality = roc3.new_float()
        val age = if (estimateAgeAndGender) roc3.new_float() else null
        val gender = if (estimateAgeAndGender) roc_embedded_gender() else null

        val numFaces = detectFaces(coloredImage, detections, maxFaces).toInt().coerceIn(0, maxFaces)
        // SWIG copies each element out of the array, so all of these are owned by Java and are
        // deleted below whether they end up being used or not.
        val candidates = (0 until numFaces).map { roc3.roc_detection_array_getitem(detections, it) }
        val selectedIndex = when {
            candidates.isEmpty() -> null
            selectFace == null -> 0
            else -> selectFace(candidates.map { it.boundingRect() })
        }
        val detection = selectedIndex?.let { candidates.getOrNull(it) }

        val face = if (detection != null) {
            generateFaceTemplateFromImage(
                coloredImage,
                grayImage,
                detection,
                yaw,
                template,
                quality,
                age,
                gender,
            )
            val yawValue = roc3.float_value(yaw)
            val qualityValue = roc3.float_value(quality)
            val ageValue = age?.let { roc3.float_value(it) }
            Face(
                sourceWidth = width,
                sourceHeight = height,
                absoluteBoundingBox = detection.boundingRect(),
                yaw = yawValue,
                roll = detection.rotation,
                quality = qualityValue,
                template = roc3.cdata(roc3.roc_cast(template), roc3.ROC_FACE_FAST_FV_SIZE.toInt()),
                format = RANK_ONE_TEMPLATE_FORMAT_3_1,
                age = ageValue,
                gender = gender?.let {
                    Face.Gender(
                        maleProbability = it.male,
                        femaleProbability = it.female,
                    )
                },
            )
        } else {
            null
        }
        // Free all resources
        roc3.roc_free_image(grayImage)
        roc3.roc_free_image(coloredImage)
        roc3.delete_float(yaw)
        roc3.delete_float(quality)
        age?.let { roc3.delete_float(it) }
        gender?.delete()
        roc3.delete_uint8_t_array(template)
        candidates.forEach { it.delete() }
        roc3.delete_roc_detection_array(detections)
        return face
    }

    private fun generateFaceTemplateFromImage(
        colorImage: roc_image,
        grayImage: roc_image,
        detection: roc_detection,
        yaw: SWIGTYPE_p_float,
        template: SWIGTYPE_p_unsigned_char,
        quality: SWIGTYPE_p_float,
        age: SWIGTYPE_p_float?,
        gender: roc_embedded_gender?,
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
            age,
            null,
            gender,
            null,
            null,
            null,
            null,
            null,
            null,
        )
    }

    /**
     * Fills [detections] with up to [maxFaces] faces and returns how many were found.
     *
     * [detections] must be an array of at least [maxFaces] elements - the native call writes
     * straight into it.
     */
    private fun detectFaces(
        image: roc_image,
        detections: roc_detection,
        maxFaces: Int,
    ): Long {
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
            maxFaces,
            FALSE_DETECTION_RATE,
            n,
            detections,
        )
        val numFaces = roc3.size_t_value(n)
        roc3.delete_size_t(n)
        roc3.delete_size_t(adaptiveMinimumSize)

        return numFaces
    }

    private fun Rect.scaledBy(factor: Float) = Rect(
        (left * factor).roundToInt(),
        (top * factor).roundToInt(),
        (right * factor).roundToInt(),
        (bottom * factor).roundToInt(),
    )

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

    override fun spoofCheck(
        bitmap: Bitmap,
        configuredMaxSize: Int,
        selectFace: FaceSelector?,
    ): SpoofCheckResult {
        if (minOf(bitmap.width, bitmap.height) < SPOOF_MIN_SIZE) {
            // As per documentation - smallest dimension must be at least 720px
            return SpoofCheckResult(0f, SpoofCheckResult.SkipReason.IMAGE_TOO_SMALL)
        }

        // Scaling image down to lower the chance that IOD is outside of requirements. The check also runs faster on smaller images.
        val scale = if (maxOf(bitmap.width, bitmap.height) > configuredMaxSize) {
            configuredMaxSize.toFloat() / maxOf(bitmap.width, bitmap.height).toFloat()
        } else {
            1f
        }
        val scaledBitmap = if (scale < 1f) {
            bitmap.scale((bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), false)
        } else {
            bitmap
        }

        val rocColorImage = roc_image()
        val rocGrayImage = roc_image()
        val byteBuffer = scaledBitmap.toByteBuffer()
        roc3.roc_from_rgba(
            byteBuffer.array(),
            scaledBitmap.width.toLong(),
            scaledBitmap.height.toLong(),
            scaledBitmap.rowBytes.toLong(),
            rocColorImage,
        )
        roc3.roc_bgr2gray(rocColorImage, rocGrayImage)

        val maxFaces = maxFacesFor(selectFace)
        val detections = roc3.new_roc_detection_array(maxFaces)
        val numFaces = detectFaces(rocColorImage, detections, maxFaces).toInt().coerceIn(0, maxFaces)

        // The score has to describe the same person the template was taken from, so the caller's
        // selector picks the face to measure here too.
        // SWIG copies each element out of the array, so all of these are owned by Java and are
        // deleted below whether they end up being used or not.
        val candidates = (0 until numFaces).map { roc3.roc_detection_array_getitem(detections, it) }
        val selectedIndex = when {
            candidates.isEmpty() -> null
            selectFace == null -> 0
            else -> selectFace(candidates.map { it.boundingRect().scaledBy(1f / scale) })
        }
        val detection = selectedIndex?.let { candidates.getOrNull(it) }

        val faceDetected = detection != null

        var iod = 0f
        var finalScore = 0f

        if (detection != null) {
            val rightEye = roc_landmark()
            val leftEye = roc_landmark()
            val chin = roc_landmark()

            val landmarks = roc3.new_roc_landmark_array(roc3.roc_num_landmarks_for_pose(detection.pose))
            roc3.roc_embedded_landmark_face(
                rocGrayImage,
                detection,
                landmarks,
                rightEye,
                leftEye,
                chin,
                null,
                null,
            )
            roc3.delete_roc_landmark_array(landmarks)

            iod = abs(leftEye.x - rightEye.x)

            // As per documentation - Inter-ocular distance must be in 100-320px range
            if (iod in SPOOF_MIN_IOD..SPOOF_MAX_IOD) {
                val embeddingSpoof = roc3.new_float()
                val livenessSpoof = roc3.new_float()

                roc3.roc_embedded_represent_face(
                    rocColorImage,
                    detection,
                    rightEye,
                    leftEye,
                    chin,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    embeddingSpoof,
                )
                val embeddingScore = roc3.float_value(embeddingSpoof)
                roc3.delete_float(embeddingSpoof)

                roc3.roc_embedded_liveness(
                    rocGrayImage,
                    rightEye,
                    leftEye,
                    chin,
                    livenessSpoof,
                )
                val livenessScore = roc3.float_value(livenessSpoof)
                roc3.delete_float(livenessSpoof)

                // As per documentation - weighted average of 2 call scores with 75% weight for direct liveness result
                finalScore = ((3 * livenessScore) + embeddingScore) / 4f
            }
            leftEye.delete()
            rightEye.delete()
            chin.delete()
        }
        roc3.roc_free_image(rocGrayImage)
        roc3.roc_free_image(rocColorImage)
        candidates.forEach { it.delete() }
        roc3.delete_roc_detection_array(detections)
        byteBuffer.clear()
        // The scaled copy is this method's own; the bitmap the caller passed in is not
        if (scaledBitmap !== bitmap) scaledBitmap.recycle()

        return when {
            !faceDetected -> SpoofCheckResult(finalScore, SpoofCheckResult.SkipReason.NOT_AVAILABLE)
            iod < SPOOF_MIN_IOD -> SpoofCheckResult(finalScore, SpoofCheckResult.SkipReason.IOD_TOO_SMALL)
            iod > SPOOF_MAX_IOD -> SpoofCheckResult(finalScore, SpoofCheckResult.SkipReason.IOD_TOO_LARGE)
            else -> SpoofCheckResult(finalScore)
        }
    }

    // Detecting several faces is only useful when the caller can choose between them.
    private fun maxFacesFor(selectFace: FaceSelector?) = if (selectFace == null) 1 else MAX_FACE_DETECTION

    companion object {
        const val RANK_ONE_TEMPLATE_FORMAT_3_1 = "RANK_ONE_3_1"
        const val MAX_FACE_DETECTION = 3
        const val FALSE_DETECTION_RATE = 0.1f
        const val RELATIVE_MIN_SIZE = 0.2f
        const val ABSOLUTE_MIN_SIZE = 36L

        private const val SPOOF_MIN_SIZE = 720
        private const val SPOOF_MIN_IOD = 100f
        private const val SPOOF_MAX_IOD = 320f
    }
}
