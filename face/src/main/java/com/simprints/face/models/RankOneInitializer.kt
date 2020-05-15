package com.simprints.face.models

import android.app.Activity
import io.rankone.rocsdk.embedded.roc
import io.rankone.rocsdk.embedded.roc_embedded_error

object RankOneInitializer {
    /**
     * This will try to load ROC library from jniLibs and then initialize using the [license].
     *
     * @param activity Needs to be an Activity instead of Context because ROC ask so
     * @param license The license file as a String
     *
     * @return true if initializing was successful, false otherwise
     */
    fun tryInitWithLicense(activity: Activity, license: String): Boolean {
        try {
            System.loadLibrary("_roc_embedded")
        } catch (t: Throwable) {
            // TODO: log the exception on Crashlytics
            return false
        }
        roc.roc_preinitialize_android(activity)
        val initResult = roc.roc_embedded_initialize(license)
        return initResult == roc_embedded_error.ROC_SUCCESS
    }
}
