package com.simprints.infra.mlkitwrapper.models.edgeface

import android.app.Activity
import com.simprints.face.infra.basebiosdk.initialization.FaceBioSdkInitializer
import com.simprints.infra.mlkitwrapper.MlKitModelContainer
import com.simprints.infra.mlkitwrapper.model.ModelInfo
import javax.inject.Inject

class EdgeFaceInitializer @Inject constructor(
    val container: MlKitModelContainer,
) : FaceBioSdkInitializer {

    override fun tryInitWithLicense(activity: Activity, license: String): Boolean {
        container.matcher = "EDGE_FACE"
        container.templateFormat = "MLKIT_EDGEFACE_TEMPLATE_FORMAT"
        container.mlKitModel = EdgeFaceModel(activity)
        return true
    }
}
