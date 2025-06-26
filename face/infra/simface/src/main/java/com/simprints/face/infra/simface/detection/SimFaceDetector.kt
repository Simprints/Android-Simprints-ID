package com.simprints.face.infra.simface.detection

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import com.simprints.biometrics.simface.SimFace
import com.simprints.biometrics.simface.data.FaceDetection
import com.simprints.biometrics.simface.data.FacialLandmarks
import com.simprints.biometrics.simface.data.Point2D
import com.simprints.face.infra.basebiosdk.detection.Face
import com.simprints.face.infra.basebiosdk.detection.FaceDetector
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import com.google.mlkit.vision.face.FaceDetection as MlKitFaceDetection

class SimFaceDetector @Inject constructor(
    private val simFace: SimFace,
) : FaceDetector {
    val faceDetector = FaceDetectorOptions
        .Builder()
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setMinFaceSize(0.20f)
        .build()
        .let { MlKitFaceDetection.getClient(it) }

    override fun analyze(bitmap: Bitmap): Face? = runBlocking {
        Simber.d("vvvvvvvvvvvvvvvvvvv", tag = "FACE_DETECTOR")

        // Load a bitmap image for processing
        val faces = simFace.detectFaceBlocking(bitmap)
        Simber.d("Faces found: ${faces.size}", tag = "FACE_DETECTOR")

        val faces2 = detectFaceBlocking(bitmap)
        Simber.d("Faces2 found: ${faces2.size}", tag = "FACE_DETECTOR")

        val face = faces.getOrNull(0) ?: faces2.getOrNull(0) ?: return@runBlocking null
        // Skip the obviously bad images, but leave the rest to be determined by the caller

        Simber.d("Quality: ${face.quality} Box: ${face.absoluteBoundingBox}", tag = "FACE_DETECTOR")
        if (face.quality < BAD_FACE_THRESHOLD) return@runBlocking null

        val alignedBitmap = face.alignedFaceImage(bitmap)
        val template = simFace.getEmbedding(alignedBitmap)

        Simber.d("Yaw: ${face.yaw} Roll: ${face.roll}", tag = "FACE_DETECTOR")
        Simber.d("Original size: ${bitmap.width}x${bitmap.height}", tag = "FACE_DETECTOR")
        Simber.d("Aligned size: ${alignedBitmap.width}x${alignedBitmap.height}", tag = "FACE_DETECTOR")
        Simber.d("^^^^^^^^^^^^^^^^^^^^", tag = "FACE_DETECTOR")

        Face(
            sourceWidth = bitmap.width,
            sourceHeight = bitmap.height,
            absoluteBoundingBox = face.absoluteBoundingBox,
            yaw = face.yaw,
            roll = face.roll,
            quality = face.quality,
            template = template,
            format = simFace.getTemplateVersion(),
        )
    }

    private suspend fun detectFaceBlocking(image: Bitmap): List<FaceDetection> {
        val inputImage = InputImage.fromBitmap(image, 0)

        return suspendCoroutine { continuation ->
            faceDetector
                .process(inputImage)
                .addOnSuccessListener { faces ->
                    val faceDetections = mutableListOf<FaceDetection>()
                    faces?.forEach { face ->
                        val faceDetection = FaceDetection(
                            sourceWidth = image.width,
                            sourceHeight = image.height,
                            absoluteBoundingBox = face.boundingBox.clampToBounds(
                                image.width,
                                image.height,
                            ),
                            yaw = face.headEulerAngleY,
                            roll = face.headEulerAngleZ,
                            quality = 0.5f,
                            landmarks = buildLandmarks(face),
                        )
                        faceDetections.add(faceDetection)
                    }
                    continuation.resume(faceDetections)
                }.addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }

    private fun Rect.clampToBounds(
        width: Int,
        height: Int,
    ): Rect = Rect(
        left.coerceAtLeast(0),
        top.coerceAtLeast(0),
        right.coerceAtMost(width),
        bottom.coerceAtMost(height),
    )

    private fun buildLandmarks(face: com.google.mlkit.vision.face.Face): FacialLandmarks? {
        val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)?.position
            ?: face.getContour(FaceContour.LEFT_EYE)?.points?.getOrNull(4)
            ?: return null

        val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)?.position
            ?: face.getContour(FaceContour.RIGHT_EYE)?.points?.getOrNull(4)
            ?: return null

        val nose = face.getLandmark(FaceLandmark.NOSE_BASE)?.position
            ?: face.getContour(FaceContour.NOSE_BRIDGE)?.points?.lastOrNull()
            ?: return null

        val mouthLeft = face.getLandmark(FaceLandmark.MOUTH_LEFT)?.position
            ?: face.getContour(FaceContour.LOWER_LIP_BOTTOM)?.points?.lastOrNull()
            ?: return null

        val mouthRight = face.getLandmark(FaceLandmark.MOUTH_RIGHT)?.position
            ?: face.getContour(FaceContour.LOWER_LIP_BOTTOM)?.points?.firstOrNull()
            ?: return null

        return FacialLandmarks(
            Point2D(leftEye.x, leftEye.y),
            Point2D(rightEye.x, rightEye.y),
            Point2D(nose.x, nose.y),
            Point2D(mouthLeft.x, mouthLeft.y),
            Point2D(mouthRight.x, mouthRight.y),
        )
    }

    companion object {
        private const val BAD_FACE_THRESHOLD = 0.1
    }
}
