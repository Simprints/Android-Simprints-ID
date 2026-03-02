package com.simprints.face.infra.simface.initialization

import android.app.Activity
import android.content.Context
import com.simprints.biometrics.simpalm.SimPalm
import com.simprints.biometrics.simpalm.SimPalmConfig
import com.simprints.face.infra.basebiosdk.initialization.FaceBioSdkInitializer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SimFaceInitializer @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val simPalm: SimPalm,
) : FaceBioSdkInitializer {
    override fun tryInitWithLicense(
        activity: Activity,
        license: String,
    ): Boolean {
        simPalm.initialize(SimPalmConfig(applicationContext))
        return true
    }
}
