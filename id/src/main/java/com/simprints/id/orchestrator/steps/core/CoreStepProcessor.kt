package com.simprints.id.orchestrator.steps.core

import android.content.Intent
import com.simprints.id.orchestrator.steps.Step

interface CoreStepProcessor {

    fun buildStepConsent(projectId: String, userId: String,
                         moduleId: String, metadata: String): Step

    fun buildStepVerify(projectId: String, userId: String,
                        moduleId: String, metadata: String): Step

    fun processResult(resultCode: Int, data: Intent?): Step.Result?
}
