package com.simprints.fingerprint.activities.orchestrator

import android.content.Context
import android.content.Intent

interface OrchestratorCallback {

    val context: Context

    fun tryAgain()
    fun onActivityResultReceived()
    fun resultNotHandleByOrchestrator(resultCode: Int?, data: Intent?)
    fun setResultDataAndFinish(resultCode: Int?, data: Intent?)
}
