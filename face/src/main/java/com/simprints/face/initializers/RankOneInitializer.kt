package com.simprints.face.initializers

import android.app.Activity
import android.content.Context
import com.google.android.play.core.splitinstall.SplitInstallHelper
import io.rankone.rocsdk.embedded.roc
import io.rankone.rocsdk.embedded.roc_embedded_error
import timber.log.Timber

class RankOneInitializer : SdkInitializer {
    /**
     * This will try to load ROC library from jniLibs and then initialize using the [license].
     *
     * @param activity Needs to be an Activity instead of Context because ROC ask so
     * @param license The license file as a String
     *
     * @return true if initializing was successful, false otherwise
     */
    override fun tryInitWithLicense(activity: Activity, license: String): Boolean {
        try {
            loadNdkLibraries(activity)
        } catch (t: Throwable) {
            Timber.e("Error initializing license $t")
            return false
        }
        roc.roc_preinitialize_android(activity)
        val initResult = roc.roc_embedded_initialize(license)
        return initResult == roc_embedded_error.ROC_SUCCESS
    }

    private fun loadNdkLibraries(ctx: Context) {
        SplitInstallHelper.loadLibrary(ctx, "yuv")
        SplitInstallHelper.loadLibrary(ctx, "yuvjni")
        SplitInstallHelper.loadLibrary(ctx, "roc_embedded")
        SplitInstallHelper.loadLibrary(ctx, "_roc_embedded")
    }
}
