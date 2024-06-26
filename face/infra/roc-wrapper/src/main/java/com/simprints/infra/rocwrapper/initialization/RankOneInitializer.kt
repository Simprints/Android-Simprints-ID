package com.simprints.infra.rocwrapper.initialization

import android.app.Activity
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.infra.facebiosdk.initialization.FaceBioSdkInitializer
import ai.roc.rocsdk.embedded.roc
import ai.roc.rocsdk.embedded.roc_embedded_error
import ai.roc.rocsdk.embedded.roc_log_level
import com.simprints.infra.logging.Simber
import javax.inject.Inject

class RankOneInitializer @Inject constructor() : FaceBioSdkInitializer {

    /**
     * This will try to load ROC library from jniLibs and then initialize using the [license].
     *
     * @param activity Needs to be an Activity instead of Context because ROC ask so
     * @param license The license file as a String
     *
     * @return true if initializing was successful, false otherwise
     */
    @ExcludedFromGeneratedTestCoverageReports(
        reason = "This function uses roc class that has native functions and can't be mocked"
    )
    override fun tryInitWithLicense(activity: Activity, license: String): Boolean {
        System.loadLibrary("roc_embedded")
        System.loadLibrary("_roc_embedded")
        roc.roc_set_log_level(roc_log_level.ROC_LOG_LEVEL_VERBOSE )
        roc.roc_preinitialize_android(activity)
        val initResult = roc.roc_embedded_initialize(license)

        Simber.i("ROC initialized with result: $initResult which is ${roc.roc_embedded_error_to_string(initResult)}")
        return initResult == roc_embedded_error.ROC_SUCCESS
    }
}
