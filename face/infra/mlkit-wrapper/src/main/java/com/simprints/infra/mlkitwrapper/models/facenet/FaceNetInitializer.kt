package com.simprints.infra.mlkitwrapper.models.facenet

import android.app.Activity
import com.simprints.face.infra.basebiosdk.initialization.FaceBioSdkInitializer
import com.simprints.infra.mlkitwrapper.MlKitModelContainer
import javax.inject.Inject

class FaceNetInitializer @Inject constructor(
    val container: MlKitModelContainer,
) : FaceBioSdkInitializer {

    override fun tryInitWithLicense(activity: Activity, license: String): Boolean {
        container.matcher = "FACE_NET"
        container.templateFormat = "MLKIT_FACENET_TEMPLATE_FORMAT"
        container.mlKitModel = FaceNetModel(activity)
        return true
    }
}
