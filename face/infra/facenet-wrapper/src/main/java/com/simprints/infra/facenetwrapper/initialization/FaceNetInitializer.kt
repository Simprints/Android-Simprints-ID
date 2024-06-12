package com.simprints.infra.facenetwrapper.initialization

import android.app.Activity
import android.content.Context
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.infra.facebiosdk.initialization.FaceBioSdkInitializer
import com.simprints.infra.facenetwrapper.model.FaceNetModel
import com.simprints.infra.facenetwrapper.model.Models
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class FaceNetInitializer @Inject constructor(
    @ApplicationContext private val context: Context
) : FaceBioSdkInitializer {

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
        faceNetModel = FaceNetModel(activity, Models.FACENET, useGpu = true, useXNNPack = true)

        return true
    }

    companion object {
        lateinit var faceNetModel: FaceNetModel
    }

}
