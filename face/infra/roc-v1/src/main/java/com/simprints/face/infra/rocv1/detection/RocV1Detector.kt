package com.simprints.face.infra.rocv1.detection

import android.graphics.Bitmap
import android.graphics.Rect
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.face.infra.basebiosdk.detection.Face
import com.simprints.face.infra.basebiosdk.detection.FaceDetector
import com.simprints.face.infra.basebiosdk.detection.FaceSelector
import com.simprints.face.infra.basebiosdk.detection.SpoofCheckResult
import io.rankone.rocsdk.embedded.SWIGTYPE_p_float
import io.rankone.rocsdk.embedded.SWIGTYPE_p_unsigned_char
import io.rankone.rocsdk.embedded.roc
import io.rankone.rocsdk.embedded.roc_detection
import io.rankone.rocsdk.embedded.roc_embedded_gender
import io.rankone.rocsdk.embedded.roc_embedded_landmark
import io.rankone.rocsdk.embedded.roc_image
import java.nio.ByteBuffer
import javax.inject.Inject

@ExcludedFromGeneratedTestCoverageReports(
    reason = "This class uses roc class that has native functions and can't be mocked",
)
class RocV1Detector @Inject constructor() : FaceDetector {
    private val falseDetectionRate = 0.1f
    private val relativeMinSize = 0.2f
    private val absoluteMinSize = 36L

    // Ignore this class from test coverage calculations
    // because it uses jni native code which is hard to test
    @ExcludedFromGeneratedTestCoverageReports(
        reason = "This class uses roc class that has native functions and can't be mocked",
    )
    data class ROCFace(
        var face: roc_detection?,
        var template: SWIGTYPE_p_unsigned_char,
        var yaw: SWIGTYPE_p_float,
        var quality: SWIGTYPE_p_float,
        var age: SWIGTYPE_p_float?,
        var gender: roc_embedded_gender?,
    ) {
        fun cleanup() {
            face?.delete()
            roc.delete_uint8_t_array(template)
            roc.delete_float(yaw)
            roc.delete_float(quality)
            age?.let { roc.delete_float(it) }
            gender?.delete()
        }
    }

    override fun spoofCheck(
        bitmap: Bitmap,
        configuredMaxSize: Int,
        selectFace: FaceSelector?,
    ) = SpoofCheckResult(0f, SpoofCheckResult.SkipReason.NOT_AVAILABLE)

    override fun analyze(
        bitmap: Bitmap,
        estimateAgeAndGender: Boolean,
        selectFace: FaceSelector?,
    ): Face? {
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

        return analyze(rocGrayImage, bitmap.width, bitmap.height, estimateAgeAndGender, selectFace)
    }

    /**
     * @param rocImage is a grayscale roc_image
     */
    private fun analyze(
        rocImage: roc_image,
        imageWidth: Int,
        imageHeight: Int,
        estimateAgeAndGender: Boolean,
        selectFace: FaceSelector?,
    ): Face? {
        val rocFace = ROCFace(
            null,
            roc.new_uint8_t_array(roc.ROC_FAST_FV_SIZE.toInt()),
            roc.new_float(),
            roc.new_float(),
            if (estimateAgeAndGender) roc.new_float() else null,
            if (estimateAgeAndGender) roc_embedded_gender() else null,
        )

        getRocTemplateFromImage(rocImage, rocFace, selectFace)
        val detection = rocFace.face

        if (detection == null) {
            roc.roc_free_image(rocImage)
            rocFace.cleanup()
            return null
        }

        val yawValue = roc.float_value(rocFace.yaw)

        val qualityValue = roc.float_value(rocFace.quality)

        val ageValue = rocFace.age?.let { roc.float_value(it) }

        val face = Face(
            imageWidth,
            imageHeight,
            detection.boundingRect(),
            yawValue,
            detection.rotation,
            qualityValue,
            roc.cdata(roc.roc_cast(rocFace.template), roc.ROC_FAST_FV_SIZE.toInt()),
            RANK_ONE_TEMPLATE_FORMAT_1_23,
            age = ageValue,
            gender = rocFace.gender?.let {
                Face.Gender(
                    maleProbability = it.male,
                    femaleProbability = it.female,
                )
            },
        )

        // Free all resources after getting the face
        roc.roc_free_image(rocImage)
        rocFace.cleanup()

        return face
    }

    private fun getRocTemplateFromImage(
        image: roc_image,
        rocFace: ROCFace,
        selectFace: FaceSelector?,
    ) {
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

        val maxFaces = maxFacesFor(selectFace)
        val n = roc.new_size_t()
        val detections = roc.new_roc_detection_array(maxFaces)

        roc.roc_ensure(
            roc.roc_embedded_error_to_string(
                roc.roc_embedded_detect_faces(
                    image,
                    roc.size_t_value(adaptiveMinimumSize),
                    maxFaces,
                    falseDetectionRate,
                    n,
                    detections,
                ),
            ),
        )

        val numFaces = roc.size_t_value(n).toInt().coerceIn(0, maxFaces)
        val candidates = (0 until numFaces).map { roc.roc_detection_array_getitem(detections, it) }
        val selectedIndex = when {
            candidates.isEmpty() -> null
            selectFace == null -> 0
            else -> selectFace(candidates.map { it.boundingRect() })
        }

        val detection = selectedIndex?.let { candidates.getOrNull(it) }
        candidates.forEach { if (it !== detection) it.delete() }
        roc.delete_roc_detection_array(detections)

        if (detection == null) {
            roc.delete_size_t(adaptiveMinimumSize)
            roc.delete_size_t(n)
            return
        }
        rocFace.face = detection

        val landmarks = roc.new_roc_embedded_landmark_array(68)
        val rightEye = roc_embedded_landmark()
        val leftEye = roc_embedded_landmark()
        val chin = roc_embedded_landmark()
        roc.roc_ensure(
            roc.roc_embedded_error_to_string(
                roc.roc_embedded_landmark_face(
                    image,
                    detection,
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
                    detection,
                    rightEye,
                    leftEye,
                    chin,
                    rocFace.template,
                    rocFace.quality,
                    rocFace.age,
                    null,
                    rocFace.gender,
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
    }

    private fun roc_detection.boundingRect() = Rect(
        (x - width / 2).toInt(),
        (y - height / 2).toInt(),
        (x + width / 2).toInt(),
        (y + height / 2).toInt(),
    )

    // Detecting several faces is only useful when the caller can choose between them.
    private fun maxFacesFor(selectFace: FaceSelector?) = if (selectFace == null) 1 else MAX_FACES

    companion object {
        const val RANK_ONE_TEMPLATE_FORMAT_1_23 = "RANK_ONE_1_23"
        const val MAX_FACES = 3
    }
}
