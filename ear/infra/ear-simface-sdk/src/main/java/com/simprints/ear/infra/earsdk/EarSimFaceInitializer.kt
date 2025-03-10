package com.simprints.ear.infra.earsdk

import android.app.Activity
import com.simprints.ear.infra.basebiosdk.initialization.EarBioSdkInitializer
import javax.inject.Inject

class EarSimFaceInitializer @Inject constructor() : EarBioSdkInitializer {

    override fun tryInitWithLicense(activity: Activity, license: String): Boolean {
        return true
    }
}
