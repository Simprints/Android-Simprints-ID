package com.simprints.fingerprint.activities.orchestrator

import android.content.Context
import android.content.Intent

@Deprecated("Old orchestrator")
interface OrchestratorCallback {

    val context: Context

    @Deprecated("Old orchestrator")
    fun tryAgain()
    @Deprecated("Old orchestrator")
    fun onActivityResultReceived()
    @Deprecated("Old orchestrator")
    fun resultNotHandleByOrchestrator(resultCode: Int?, data: Intent?)
    @Deprecated("Old orchestrator")
    fun setResultDataAndFinish(resultCode: Int?, data: Intent?)
}
