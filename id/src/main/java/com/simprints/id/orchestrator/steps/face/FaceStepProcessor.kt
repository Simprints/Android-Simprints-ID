package com.simprints.id.orchestrator.steps.face

import android.content.Intent
import com.simprints.id.data.db.subject.local.SubjectQuery
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureSample
import com.simprints.id.orchestrator.steps.Step

/**
 * It creates a Step to launch (used to launch a specific Activity) to execute
 * a particular task in the FingerprintModule
 */
interface FaceStepProcessor {

    fun buildCaptureStep(): Step

    fun buildStepMatch(probeFaceSample: List<FaceCaptureSample>, query: SubjectQuery): Step

    fun processResult(requestCode: Int,
                      resultCode: Int,
                      data: Intent?): Step.Result?

    fun buildConfigurationStep(projectId: String, deviceId: String): Step

}
