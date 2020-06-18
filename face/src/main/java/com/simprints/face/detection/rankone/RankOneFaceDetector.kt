package com.simprints.face.detection.rankone

import android.graphics.Bitmap
import android.graphics.Rect
import com.simprints.face.detection.Face
import com.simprints.face.detection.FaceDetector
import com.simprints.uicomponents.models.PreviewFrame
import io.rankone.rocsdk.embedded.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import kotlin.experimental.and

class RankOneFaceDetector : FaceDetector {
    private val maxFaces = 1
    private val falseDetectionRate = 0.1f

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

    override suspend fun analyze(previewFrame: PreviewFrame): Face? = withContext(Dispatchers.IO) {
        val bytes = yuv420ToY888(previewFrame.bytes, previewFrame.width, previewFrame.height)
        val rocImage = getRocImage(bytes, previewFrame.width, previewFrame.height)
        return@withContext analyze(rocImage, previewFrame.width, previewFrame.height)
    }

    override suspend fun analyze(bitmap: Bitmap): Face? = withContext(Dispatchers.IO) {
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

        return@withContext analyze(rocGrayImage, bitmap.width, bitmap.height)
    }

    /**
     * @param rocImage is a grayscale roc_image
     */
    private fun analyze(rocImage: roc_image, imageWidth: Int, imageHeight: Int): Face? {
        val rocFace = ROCFace(
            roc_detection(),
            roc.new_uint8_t_array(roc.ROC_FR_FAST_FV_SIZE.toInt()),
            roc.new_float(),
            roc.new_float()
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
                (rocFace.face.y + rocFace.face.height / 2).toInt()
            ),
            yawValue,
            rocFace.face.rotation,
            qualityValue,
            roc.cdata(roc.roc_cast(rocFace.template), roc.ROC_FR_FAST_FV_SIZE.toInt())
        )

        // Free all resources after getting the face
        roc.roc_free_image(rocImage)
        rocFace.cleanup()

        return face
    }

    private fun getRocImage(bytes: ByteArray, imageWidth: Int, imageHeight: Int): roc_image {
        val rocImage = roc_image()

        with(rocImage) {
            color_space = roc_color_space.ROC_GRAY8
            step = imageWidth.toLong()
            width = imageWidth.toLong()
            height = imageHeight.toLong()
            data = roc.new_uint8_t_array(bytes.size)
            roc.memmove(roc.roc_cast(rocImage.data), bytes)
        }

        return rocImage
    }

    /**
     * Converts YUV420 NV21 to Y888 (RGB8888). The grayscale image still holds 3 bytes on the pixel.
     *
     * @param pixels output array with the converted array o grayscale pixels
     * @param data byte array on YUV420 NV21 format.
     * @param width pixels width
     * @param height pixels height
     */
    private fun yuv420ToY888(data: ByteArray, width: Int, height: Int): ByteArray {
        val size = width * height
        val pixels = ByteArray(size)
        var p: Int

        for (i in 0 until size) {
            p = (data[i] and 0xFF.toByte()).toInt()
            pixels[i] = (-0x1000000 or (p shl 16) or (p shl 8) or p).toByte()
        }

        return pixels
    }

    private fun getRocTemplateFromImage(image: roc_image, rocFace: ROCFace): Boolean {
        val adaptiveMinimumSize = roc.new_size_t()
        roc.roc_ensure(
            roc.roc_adaptive_minimum_size(image, 0.2f, 36, adaptiveMinimumSize)
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
                    rocFace.face
                )
            )
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
                    rocFace.yaw
                )
            )
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
                    null
                )
            )
        )

        // Cleanup
        roc.delete_size_t(adaptiveMinimumSize)
        roc.delete_size_t(n)
        roc.delete_roc_embedded_landmark_array(landmarks)

        return true
    }

}
