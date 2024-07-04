package com.simprints.infra.mlkitwrapper.initialization

import android.app.Activity
import com.simprints.face.infra.basebiosdk.initialization.FaceBioSdkInitializer
import com.simprints.infra.mlkitwrapper.MlKitModelContainer
import com.simprints.infra.mlkitwrapper.model.MlKitModel
import com.simprints.infra.mlkitwrapper.model.ModelInfo
import javax.inject.Inject

class FaceNetInitializer @Inject constructor(
    val container: MlKitModelContainer,
) : FaceBioSdkInitializer {

    override fun tryInitWithLicense(activity: Activity, license: String): Boolean {
        val model = ModelInfo(
            name = "FaceNet",
            assetsFilename = "facenet.tflite",
            inputDims = 160,
            outputDims = 128,
            useGpu = true,
        )

        container.matcher = "FACE_NET"
        container.templateFormat = "MLKIT_FACENET_TEMPLATE_FORMAT"
        container.mlKitModel = MlKitModel(activity, model, useXNNPack = true)
        return true
    }
}
