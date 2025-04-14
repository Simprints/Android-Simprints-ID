package com.simprints.document.infra.mlkit.initialization

import android.app.Activity
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.document.infra.basedocumentsdk.initialization.DocumentSdkInitializer
import javax.inject.Inject

class MlKitInitializer @Inject constructor() : DocumentSdkInitializer {
    @ExcludedFromGeneratedTestCoverageReports(
        reason = "TODO evaluate for MLkit",
    )
    override fun init(
        activity: Activity,
    ): Boolean {
        // todo
    }
}
