package com.simprints.face.infra.simface.initialization

import android.app.Activity
import android.content.Context
import com.simprints.face.infra.basebiosdk.initialization.FaceBioSdkInitializer
import com.simprints.simface.core.SimFace
import com.simprints.simface.core.SimFaceConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SimFaceInitializer @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val simFace: SimFace,
) : FaceBioSdkInitializer {
    override fun tryInitWithLicense(
        activity: Activity,
        license: String,
    ): Boolean {
        simFace.initialize(SimFaceConfig(applicationContext))
        return true
    }
}
