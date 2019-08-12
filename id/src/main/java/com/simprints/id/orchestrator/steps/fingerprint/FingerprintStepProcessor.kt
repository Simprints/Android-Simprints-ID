package com.simprints.id.orchestrator.steps.fingerprint

import android.content.Intent
import com.simprints.id.orchestrator.steps.Step
/**
 * It creates a Step to launch (used to launch a specific Activity) to execute
 * a particular task in the FingerprintModule
 */
interface FingerprintStepProcessor {


    fun buildStepEnrol(projectId: String,
                       userId: String,
                       moduleId: String,
                       metadata: String): Step

    fun buildStepIdentify(projectId: String,
                          userId: String,
                          moduleId: String,
                          metadata: String): Step

    fun buildStepVerify(projectId: String,
                        userId: String,
                        moduleId: String,
                        metadata: String,
                        verifyGuid: String): Step

    fun processResult(requestCode: Int,
                      resultCode: Int,
                      data: Intent?): Step.Result?
}
