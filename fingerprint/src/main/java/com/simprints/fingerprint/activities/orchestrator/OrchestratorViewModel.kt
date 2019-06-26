package com.simprints.fingerprint.activities.orchestrator

import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.fingerprint.activities.launch.LaunchActivity
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest

class OrchestratorViewModel : ViewModel() {

    lateinit var fingerprintRequest: FingerprintRequest

    val nextActivity = MutableLiveData<ActivityRequest>()
    val finishedResult = MutableLiveData<FinishedResult>()

    fun start() {
        nextActivity.postValue(ActivityRequest.Launch(fingerprintRequest))
    }
}

sealed class ActivityRequest(
    private val fingerprintRequest: FingerprintRequest,
    private val targetClass: Class<*>,
    val resultCode: Int
) {

    class Launch(fingerprintRequest: FingerprintRequest) :
        ActivityRequest(fingerprintRequest, LaunchActivity::class.java, 0)

    fun toIntent(packageContext: Context) =
        Intent(packageContext, targetClass).apply {
            putExtra(FingerprintRequest.BUNDLE_KEY, fingerprintRequest)
        }
}

data class FinishedResult(val resultCode: Int, val resultData: Intent?)
