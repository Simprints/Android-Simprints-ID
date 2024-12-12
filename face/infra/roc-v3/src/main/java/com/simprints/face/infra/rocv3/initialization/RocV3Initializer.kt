package com.simprints.face.infra.rocv3.initialization

import ai.roc.rocsdk.embedded.roc
import ai.roc.rocsdk.embedded.roc_embedded_error
import ai.roc.rocsdk.embedded.roc_log_level
import android.app.Activity
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.face.infra.basebiosdk.initialization.FaceBioSdkInitializer
import com.simprints.infra.logging.Simber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RocV3Initializer @Inject constructor() : FaceBioSdkInitializer {
    /**
     * This will try to load ROC library from jniLibs and then initialize using the [license].
     *
     * @param activity Needs to be an Activity instead of Context because ROC ask so
     * @param license The license file as a String
     *
     * @return true if initializing was successful, false otherwise
     */
    @ExcludedFromGeneratedTestCoverageReports(
        reason = "This function uses roc class that has native functions and can't be mocked",
    )
    override fun tryInitWithLicense(
        activity: Activity,
        license: String,
    ): Boolean {
        // As both V1 and V3 are using the same library name and we can't rename the library name
        // the version number is used to differentiate between the two
        System.loadLibrary("roc_embedded_3_1")
        System.loadLibrary("_roc_embedded_3_1")
        Simber.d("ROC V3 library loaded")
        roc.roc_set_log_level(roc_log_level.ROC_LOG_LEVEL_VERBOSE)
        roc.roc_preinitialize_android(activity)
        val initResult = roc.roc_embedded_initialize(license)

        return initResult == roc_embedded_error.ROC_SUCCESS
    }
}
