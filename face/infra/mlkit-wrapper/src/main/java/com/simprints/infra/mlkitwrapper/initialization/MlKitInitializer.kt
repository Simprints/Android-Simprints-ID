package com.simprints.infra.mlkitwrapper.initialization

import android.app.Activity
import android.content.Context
import com.simprints.infra.facebiosdk.initialization.FaceBioSdkInitializer
import com.simprints.infra.mlkitwrapper.model.MlKitModel
import com.simprints.infra.mlkitwrapper.model.Models
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MlKitInitializer @Inject constructor(
    @ApplicationContext private val context: Context
) : FaceBioSdkInitializer {

    override fun tryInitWithLicense(activity: Activity, license: String): Boolean {

        mlKitModel = MlKitModel(
            activity,
            // TODO can switch between models here by commenting/uncommenting one of them
            Models.EDGEFACE,
            // Models.FACENET
            useXNNPack = true
        )

        return true
    }

    companion object {
        lateinit var mlKitModel: MlKitModel
    }

}
