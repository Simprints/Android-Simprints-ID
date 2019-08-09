package com.simprints.id.orchestrator.steps.fingerprint

import android.content.Intent
import com.simprints.id.orchestrator.steps.Step

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

    fun processResult(requestCode: Int, resultCode: Int, data: Intent?): Step.Result?
}
