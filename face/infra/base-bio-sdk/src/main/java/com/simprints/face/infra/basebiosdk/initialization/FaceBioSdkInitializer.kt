package com.simprints.face.infra.basebiosdk.initialization

import android.app.Activity

fun interface FaceBioSdkInitializer {
    /**
     * Try to initalize the biometric sdk with the license
     *
     * @param activity some bio SDKs needs the activity to do the initialization, make sure to not capture the activity reference while implementing this method to avoid memory leaks.
     * @param license the license string
     * @return true if the sdk is initialized successfully
     */
    fun tryInitWithLicense(
        activity: Activity,
        license: String,
    ): Boolean
}
