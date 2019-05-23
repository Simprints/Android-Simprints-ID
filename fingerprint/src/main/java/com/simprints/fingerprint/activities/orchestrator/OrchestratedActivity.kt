package com.simprints.fingerprint.activities.orchestrator

import android.content.Context
import android.content.Intent

interface OrchestratedActivity {

    val activity: ActivityName
    val context: Context

    fun tryAgain()
    fun handleResult(resultCode: Int?, data: Intent?)
    fun setResultDataAndFinish(resultCode: Int?, data: Intent?)

    enum class ActivityName {
        COLLECT,
        LAUNCH,
        MATCHING
    }
}
