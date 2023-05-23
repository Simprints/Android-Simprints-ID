package com.simprints.infra.facebiosdk.initialization

import android.app.Activity

fun interface FaceBioSdkInitializer {
    fun tryInitWithLicense(activity: Activity, license: String): Boolean
}
