package com.simprints.fingerprint.activities.orchestrator

import android.content.Intent

@Deprecated("Old orchestrator")
interface Orchestrator {

    @Deprecated("Old orchestrator")
    fun onActivityResult(receiver: OrchestratorCallback, requestCode: Int, resultCode: Int?, data: Intent?)
}
