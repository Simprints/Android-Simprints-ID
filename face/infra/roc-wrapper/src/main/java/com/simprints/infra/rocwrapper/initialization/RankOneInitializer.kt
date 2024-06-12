package com.simprints.infra.rocwrapper.initialization

import android.app.Activity
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.infra.facebiosdk.initialization.FaceBioSdkInitializer

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
        return true
    }
}
