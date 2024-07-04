package com.simprints.infra.mlkitwrapper.initialization

import android.app.Activity
import com.simprints.face.infra.basebiosdk.initialization.FaceBioSdkInitializer
import com.simprints.infra.mlkitwrapper.MlKitModelContainer
import com.simprints.infra.mlkitwrapper.model.MlKitModel
import com.simprints.infra.mlkitwrapper.model.ModelInfo
import javax.inject.Inject

class EdgeFaceInitializer @Inject constructor(
    val container: MlKitModelContainer,
) : FaceBioSdkInitializer {

    override fun tryInitWithLicense(activity: Activity, license: String): Boolean {
        val model = ModelInfo(
            name = "EdgeFace",
            assetsFilename = "edgefacexs.tflite",
            inputDims = 112,
            outputDims = 512,
            useGpu = false,
        )

        container.matcher = "EDGE_FACE"
        container.templateFormat = "MLKIT_EDGEFACE_TEMPLATE_FORMAT"
        container.mlKitModel = MlKitModel(activity, model, useXNNPack = true)
        return true
    }
}
