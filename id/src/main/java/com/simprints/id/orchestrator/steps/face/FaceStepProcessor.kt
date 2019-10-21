package com.simprints.id.orchestrator.steps.face

import android.content.Intent
import com.simprints.id.data.db.person.domain.FaceSample
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.orchestrator.steps.Step

/**
 * It creates a Step to launch (used to launch a specific Activity) to execute
 * a particular task in the FingerprintModule
 */
interface FaceStepProcessor {

    fun buildCaptureStep(): Step

    fun buildStepMatch(probeFaceSample: List<FaceSample>, query: PersonLocalDataSource.Query): Step

    fun processResult(requestCode: Int,
                      resultCode: Int,
                      data: Intent?): Step.Result?
}
