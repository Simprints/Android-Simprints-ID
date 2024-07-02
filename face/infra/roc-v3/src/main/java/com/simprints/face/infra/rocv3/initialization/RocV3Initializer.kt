package com.simprints.face.infra.rocv3.initialization


import ai.roc.rocsdk.embedded.roc
import ai.roc.rocsdk.embedded.roc_embedded_error
import ai.roc.rocsdk.embedded.roc_log_level
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.face.infra.basebiosdk.initialization.FaceBioSdkInitializer
import com.simprints.infra.logging.Simber
import java.io.File
import java.io.FileOutputStream
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
        reason = "This function uses roc class that has native functions and can't be mocked"
    )
    override fun tryInitWithLicense(activity: Activity, license: String): Boolean {
        // As both V1 and V3 are using the same library name and we can't rename the library name
        // We moved the v3 lib files into the assets folder and we will load them from there
        System.loadLibrary("roc_embedded_3_1")
        System.loadLibrary("_roc_embedded_3_1")

        roc.roc_set_log_level(roc_log_level.ROC_LOG_LEVEL_VERBOSE)
        roc.roc_preinitialize_android(activity)
        val initResult = roc.roc_embedded_initialize(license)

        Simber.i(
            "ROC initialized with result: $initResult which is ${
                roc.roc_embedded_error_to_string(
                    initResult
                )
            }"
        )
        return initResult == roc_embedded_error.ROC_SUCCESS
    }

    companion object {
        private const val OLD_LIB_WRAPPER_NAME = "lib_rankone_v3_embedded.so"
        private const val LIB_WRAPPER_NAME = "lib_roc_embedded.so"
        private const val OLD_LIB_NAME = "librankone_v3_embedded.so"
        private const val LIB_NAME = "libroc_embedded.so"
        private const val SUB_FOLDER = "roc-v3" // Change this to load different libraries
    }
}
