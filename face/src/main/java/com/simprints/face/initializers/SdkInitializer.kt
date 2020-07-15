package com.simprints.face.initializers

import android.app.Activity

interface SdkInitializer {
    fun tryInitWithLicense(activity: Activity, license: String): Boolean
}
