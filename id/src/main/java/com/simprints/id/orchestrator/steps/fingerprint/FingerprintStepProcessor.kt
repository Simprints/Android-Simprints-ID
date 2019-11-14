package com.simprints.id.orchestrator.steps.fingerprint

import android.content.Intent
import com.simprints.id.data.db.person.domain.FingerprintSample
import com.simprints.id.data.db.person.local.PersonLocalDataSource.Query
import com.simprints.id.orchestrator.steps.Step
/**
 * It creates a Step to launch (used to launch a specific Activity) to execute
 * a particular task in the FingerprintModule
 */
interface FingerprintStepProcessor {


    fun buildStepToCapture(): Step

    fun buildStepToMatch(probeSamples: List<FingerprintSample>, query: Query): Step

    fun processResult(requestCode: Int,
                      resultCode: Int,
                      data: Intent?): Step.Result?
}
