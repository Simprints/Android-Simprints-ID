package com.simprints.fingerprint.activities.orchestrator

import android.content.Intent

interface Orchestrator {
    fun onActivityResult(receiver: OrchestratorCallback, requestCode: Int, resultCode: Int?, data: Intent?)
}
