package com.simprints.infra.facenetwrapper.detection


import android.graphics.Bitmap
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.infra.facebiosdk.detection.Face
import com.simprints.infra.facebiosdk.detection.FaceDetector
import com.simprints.infra.facenetwrapper.initialization.FaceNetInitializer
import com.simprints.infra.mlkitwrapper.tools.toBytes
import javax.inject.Inject
import com.google.mlkit.vision.face.Face as MLFace

@ExcludedFromGeneratedTestCoverageReports(
    reason = "This class uses roc class that has native functions and can't be mocked"
)
class FaceNetDetector @Inject constructor() : FaceDetector {
    companion object {
        const val FACE_NET_TEMPLATE_FORMAT = "FACE_NET_TEMPLATE_FORMAT"
    }

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .enableTracking()
        .build()
    private val detector = FaceDetection.getClient(options)


    override fun analyze(bitmap: Bitmap): Face? {
        // Detect faces using MLKIT
        val image = InputImage.fromBitmap(bitmap, 0)
        val faces = Tasks.await(detector.process(image))
        return if (faces.isEmpty()) {
            null
        } else {
            val mlFace = faces[0]
            //Extract template using FaceNet
            extractTemplate(mlFace, bitmap)
        }
    }

    private fun extractTemplate(mlFace: MLFace, bitmap: Bitmap): Face {
        val template = FaceNetInitializer.faceNetModel.getFaceEmbedding(bitmap).toBytes()
        return Face(
            sourceWidth = mlFace.boundingBox.width(),
            sourceHeight = mlFace.boundingBox.height(),
            absoluteBoundingBox = mlFace.boundingBox,
            yaw = mlFace.headEulerAngleX,
            roll = mlFace.headEulerAngleZ,
            quality = 100f,
            template = template,
            format = FACE_NET_TEMPLATE_FORMAT
        )
    }
}
