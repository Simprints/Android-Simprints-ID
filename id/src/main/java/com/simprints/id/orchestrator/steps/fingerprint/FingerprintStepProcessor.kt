package com.simprints.id.orchestrator.steps.fingerprint

import android.content.Intent
import com.simprints.id.data.db.person.local.PersonLocalDataSource.Query
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureSample
import com.simprints.id.orchestrator.steps.Step
/**
 * It creates a Step to launch (used to launch a specific Activity) to execute
 * a particular task in the FingerprintModule
 */
interface FingerprintStepProcessor {


    fun buildStepToCapture(projectId: String,
                           userId: String,
                           moduleId: String,
                           metadata: String): Step

    fun buildStepToMatch(probeSamples: List<FingerprintCaptureSample>, query: Query): Step

    fun processResult(requestCode: Int,
                      resultCode: Int,
                      data: Intent?): Step.Result?
}
