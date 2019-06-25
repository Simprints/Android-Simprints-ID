package com.simprints.fingerprint.activities.orchestrator

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OrchestratorViewModel : ViewModel() {

    val finishedResult = MutableLiveData<FinishedResult>()

    data class FinishedResult(val resultCode: Int, val resultData: Intent?)
}
